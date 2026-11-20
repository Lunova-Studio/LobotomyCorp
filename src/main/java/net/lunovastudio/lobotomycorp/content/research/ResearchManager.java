package net.lunovastudio.lobotomycorp.content.research;

import net.lunovastudio.lobotomycorp.LCIds;
import net.lunovastudio.lobotomycorp.api.capability.PlayerCareer;
import net.lunovastudio.lobotomycorp.api.capability.PlayerCareerAccess;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearch;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearchAccess;
import net.lunovastudio.lobotomycorp.api.capability.ResearchFlag;
import net.lunovastudio.lobotomycorp.api.recipe.ResearchGate;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.lunovastudio.lobotomycorp.api.research.IResearchEntry;
import net.lunovastudio.lobotomycorp.api.research.IResearchStage;
import net.lunovastudio.lobotomycorp.api.research.KnowledgeReward;
import net.lunovastudio.lobotomycorp.api.research.ResearchEntryMeta;
import net.lunovastudio.lobotomycorp.api.research.ResearchEvent;
import net.lunovastudio.lobotomycorp.api.research.ResearchRequirement;
import net.lunovastudio.lobotomycorp.api.research.ResearchStageRequirements;
import net.lunovastudio.lobotomycorp.api.research.ResearchUnlockConditions;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeAccess;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeType;
import net.lunovastudio.lobotomycorp.network.ClientboundKnowledgeGainPayload;
import net.lunovastudio.lobotomycorp.registry.LCAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

/**
 * 研究系统的服务端内核：解锁、推进阶段、完成、知识点与配方/阶段门槛判定。
 *
 * <p>所有会改动 {@link PlayerKnowledge} 的入口都走这里，以便统一触发事件、
 * 统一扣费并统一安排同步。</p>
 */
public final class ResearchManager {
    private static final int STAGE_EXPERIENCE = 5;
    private static final String CRAFTED_SEGMENT = "crafted";
    private static final int FIRST_STAGE = 0;

    private ResearchManager() {
    }

    // ------------------------------------------------------------------
    // 条目与分类查询
    // ------------------------------------------------------------------

    /**
     * 已合成物品的“记忆键”。合成过的物品会被记进研究记录，之后再次需要时无需重复持有。
     */
    public static ResourceLocation craftedKey(ResourceLocation item) {
        return LCIds.rl(CRAFTED_SEGMENT + "/" + item.getNamespace() + "/" + item.getPath());
    }

    @Nullable
    public static IResearchEntry entryOf(Player player, ResourceLocation id) {
        if (id == null) {
            return null;
        }
        return player.registryAccess()
                .lookup(IResearchEntry.REGISTRY_KEY)
                .flatMap(lookup -> lookup.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, id)))
                .map(Holder.Reference::value)
                .orElse(null);
    }

    /**
     * 取注册表里全部条目，未加载时返回空列表。
     */
    public static List<Holder.Reference<IResearchEntry>> allEntries(RegistryAccess access) {
        return access.lookup(IResearchEntry.REGISTRY_KEY)
                .map(lookup -> lookup.listElements().toList())
                .orElse(List.of());
    }

    public static List<Holder.Reference<IResearchEntry>> allEntries(Player player) {
        return allEntries(player.registryAccess());
    }

    /**
     * 取注册表里全部分类，未加载时返回空列表。
     */
    public static List<Holder.Reference<IResearchCategory>> allCategories(RegistryAccess access) {
        return access.lookup(IResearchCategory.REGISTRY_KEY)
                .map(lookup -> lookup.listElements().toList())
                .orElse(List.of());
    }

    public static List<Holder.Reference<IResearchCategory>> allCategories(Player player) {
        return allCategories(player.registryAccess());
    }

    /**
     * 取某个分类在记录里使用的 id。
     */
    public static Optional<ResourceLocation> idOf(Holder<IResearchCategory> category) {
        return category.unwrapKey().map(ResourceKey::location);
    }

    /**
     * 玩家对某分类的部门权限：记录里的值与“等级达标给的最低权限”取大。
     */
    public static int accessLevel(Player player, Holder<IResearchCategory> category) {
        IResearchCategory value = category.value();
        PlayerCareer career = PlayerCareerAccess.of(player);
        int stored = idOf(category).map(career::accessLevel).orElse(0);
        if (career.employeeGrade() < value.requiredEmployeeGrade()) {
            return stored;
        }
        return Math.max(stored, value.defaultAccessLevel());
    }

    /**
     * 分类是否对玩家开放：完成解锁研究、或拥有部门权限。
     */
    public static boolean categoryOpen(Player player, Holder<IResearchCategory> category) {
        IResearchCategory value = category.value();
        if (value.requiredResearch()
                .map(id -> PlayerResearchAccess.of(player).hasCompleted(id))
                .orElse(false)) {
            return true;
        }
        return accessLevel(player, category) >= 1;
    }

    /**
     * 条目是否对玩家开放（所属分类已开放）。
     */
    public static boolean entryOpen(Player player, IResearchEntry entry) {
        return categoryOpen(player, entry.category());
    }

    /**
     * 按 {@link IResearchCategory#index()} 排序的全部分类。
     */
    public static List<Holder.Reference<IResearchCategory>> categoriesSorted(Player player) {
        return allCategories(player).stream()
                .sorted(java.util.Comparator.comparingInt(holder -> holder.value().index()))
                .toList();
    }

    /**
     * 某个分类的完成情况：{已完成条目数, 条目总数}。
     */
    public static int[] progress(Player player, Holder<IResearchCategory> category) {
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        int total = 0;
        int completed = 0;
        for (Holder.Reference<IResearchEntry> entry : allEntries(player)) {
            if (entry.value().category().value() != category.value()) {
                continue;
            }
            total++;
            if (knowledge.hasCompleted(entry.key().location())) {
                completed++;
            }
        }
        return new int[] {completed, total};
    }

    /**
     * 某个分类下对玩家可见的条目：隐藏条目在未学会前不出现在网格里。
     */
    public static List<Holder.Reference<IResearchEntry>> visibleEntries(
            Player player, Holder<IResearchCategory> category) {
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        List<Holder.Reference<IResearchEntry>> visible = new ArrayList<>();
        for (Holder.Reference<IResearchEntry> entry : allEntries(player)) {
            if (entry.value().category().value() != category.value()) {
                continue;
            }
            if (entry.value().hasMeta(ResearchEntryMeta.HIDDEN)
                    && !knowledge.knows(entry.key().location())) {
                continue;
            }
            visible.add(entry);
        }
        return visible;
    }

    /**
     * 分类未开放时的原因提示。
     */
    public static List<Component> categoryLockedReasons(Player player, Holder<IResearchCategory> category) {
        List<Component> reasons = new ArrayList<>();
        IResearchCategory value = category.value();
        PlayerCareer career = PlayerCareerAccess.of(player);
        if (career.employeeGrade() < value.requiredEmployeeGrade()) {
            reasons.add(Component.translatable("research.lobotomycorp.locked.grade",
                            value.requiredEmployeeGrade(), career.employeeGrade())
                    .withStyle(ChatFormatting.RED));
        }
        ResourceLocation id = idOf(category).orElse(null);
        if (id != null && accessLevel(player, category) < 1) {
            reasons.add(Component.translatable("research.lobotomycorp.locked.access", 1, career.accessLevel(id))
                    .withStyle(ChatFormatting.RED));
        }
        return reasons;
    }

    // ------------------------------------------------------------------
    // 解锁
    // ------------------------------------------------------------------

    /**
     * 玩家在界面里请求解锁某条目。
     */
    public static boolean unlockRequested(ServerPlayer player, ResourceLocation id) {
        IResearchEntry entry = entryOf(player, id);
        return entry != null && entryOpen(player, entry) && !isHiddenRoot(entry) && unlock(player, id);
    }

    private static boolean isHiddenRoot(IResearchEntry entry) {
        return entry.parents().isEmpty() && entry.hasMeta(ResearchEntryMeta.HIDDEN);
    }

    /**
     * 解锁条目：满足父条目、附加解锁条件且事件未取消时加入记录。
     */
    public static boolean unlock(ServerPlayer player, ResourceLocation id) {
        if (id == null) {
            return false;
        }
        PlayerKnowledge knowledge = record(player);
        if (!mayUnlock(player, knowledge, id)) {
            return false;
        }
        knowledge.learn(id);
        knowledge.queueSync(player);
        return true;
    }

    private static boolean mayUnlock(ServerPlayer player, PlayerKnowledge knowledge, ResourceLocation id) {
        if (knowledge.knows(id)) {
            return false;
        }
        IResearchEntry entry = entryOf(player, id);
        boolean parentsOk = entry == null || parentsSatisfied(knowledge, entry);
        return parentsOk
                && ResearchUnlockConditions.passes(player, knowledge, id)
                && !vetoed(new ResearchEvent.Unlocked(player, id));
    }

    // ------------------------------------------------------------------
    // 阶段推进
    // ------------------------------------------------------------------

    public static boolean advanceStage(ServerPlayer player, ResourceLocation id) {
        return advanceStage(player, id, true);
    }

    /**
     * 推进一个阶段。
     *
     * @param checkRequirements false 时跳过要求检查与代价扣除（命令/调试用）
     */
    public static boolean advanceStage(ServerPlayer player, ResourceLocation id, boolean checkRequirements) {
        if (id == null) {
            return false;
        }
        IResearchEntry entry = entryOf(player, id);
        PlayerKnowledge knowledge = record(player);
        OptionalInt reachable = reachableStage(knowledge, entry, id);
        if (reachable.isEmpty()) {
            return false;
        }
        int current = reachable.getAsInt();
        if (checkRequirements && !stageRequirementsMet(player, knowledge, entry, id, current)) {
            return false;
        }
        if (vetoed(new ResearchEvent.StageAdvanced(player, id, current, current + 1))) {
            return false;
        }
        if (checkRequirements) {
            consume(player, id, entry.stages().get(current), current);
        }
        progressTo(player, knowledge, entry, id, current);
        finishAdvance(player, knowledge);
        return true;
    }

    private static OptionalInt reachableStage(
            PlayerKnowledge knowledge, @Nullable IResearchEntry entry, ResourceLocation id) {
        if (entry == null || !knowledge.knows(id) || knowledge.hasCompleted(id)) {
            return OptionalInt.empty();
        }
        int current = knowledge.stageOf(id);
        return current < entry.stages().size() ? OptionalInt.of(current) : OptionalInt.empty();
    }

    private static void finishAdvance(ServerPlayer player, PlayerKnowledge knowledge) {
        player.giveExperiencePoints(STAGE_EXPERIENCE);
        knowledge.queueSync(player);
    }

    private static void progressTo(
            ServerPlayer player, PlayerKnowledge knowledge, IResearchEntry entry, ResourceLocation id, int current) {
        List<IResearchStage> stages = entry.stages();
        int total = stages.size();
        applyStageEffects(stages.get(Math.max(current, FIRST_STAGE)), player);
        IResearchStage finalStage = stages.get(total - 1);
        boolean finishNow = current + 1 == total - 1 && !hasRequirements(finalStage);
        if (finishNow) {
            applyStageEffects(finalStage, player);
        }
        int next = finishNow ? total : current + 1;
        if (next >= total) {
            complete(player, id);
        } else {
            knowledge.setStageRaw(id, next);
        }
    }

    /**
     * 直接设置阶段，不做要求检查。
     */
    public static boolean setStage(ServerPlayer player, ResourceLocation id, int stage) {
        PlayerKnowledge knowledge = record(player);
        if (id == null || stage <= 0 || !knowledge.knows(id)) {
            return false;
        }
        int current = knowledge.stageOf(id);
        if (current == stage || vetoed(new ResearchEvent.StageAdvanced(player, id, current, stage))) {
            return false;
        }
        knowledge.setStageRaw(id, stage);
        knowledge.queueSync(player);
        return true;
    }

    /**
     * 完成条目：学会它、标记完成并补上界面标记。
     */
    public static boolean complete(ServerPlayer player, ResourceLocation id) {
        if (id == null) {
            return false;
        }
        PlayerKnowledge knowledge = record(player);
        boolean added = false;
        if (!knowledge.knows(id)) {
            added = knowledge.learn(id);
            if (!added) {
                return false;
            }
        }
        if (knowledge.hasCompleted(id)
                || vetoed(new ResearchEvent.Completed(player, id))
                || !knowledge.completeEntry(id)) {
            if (added) {
                knowledge.queueSync(player);
            }
            return false;
        }
        IResearchEntry entry = entryOf(player, id);
        if (entry != null) {
            knowledge.setStageRaw(id, entry.stages().size());
            knowledge.addFlag(id, ResearchFlag.POPUP);
            knowledge.addFlag(id, ResearchFlag.RESEARCH);
        }
        knowledge.queueSync(player);
        return true;
    }

    // ------------------------------------------------------------------
    // 知识
    // ------------------------------------------------------------------

    /**
     * 增加知识点：先触发事件，再通过 {@link KnowledgeAccess} 后端写入。
     */
    public static boolean gainKnowledge(
            ServerPlayer player, KnowledgeType type, @Nullable Holder<IResearchCategory> category, int amount) {
        if (type == null || amount == 0) {
            return false;
        }
        if (vetoed(new ResearchEvent.KnowledgeGained(player, type, category, amount))) {
            return false;
        }
        KnowledgeAccess.grant(player, type, category, amount);
        return true;
    }

    /**
     * 低层知识点写入：不触发事件，负责同步与客户端提示。供 {@link KnowledgeAccess} 后端调用。
     */
    public static boolean grantKnowledge(ServerPlayer player, KnowledgeType type,
                                         @Nullable Holder<IResearchCategory> category, int amount) {
        if (type == null || amount == 0) {
            return false;
        }
        PlayerKnowledge knowledge = record(player);
        ResourceKey<IResearchCategory> key = categoryKey(category);
        int before = knowledge.knowledgePoints(type, key);
        if (!knowledge.addKnowledgePoints(type, key, amount)) {
            return false;
        }
        knowledge.queueSync(player);
        int gained = knowledge.knowledgePoints(type, key) - before;
        if (gained > 0) {
            PacketDistributor.sendToPlayer(
                    player, new ClientboundKnowledgeGainPayload(type, Optional.ofNullable(key), gained));
        }
        return true;
    }

    /**
     * 把带自动解锁标记的条目补进记录。
     */
    public static void applyAutoUnlock(ServerPlayer player) {
        PlayerKnowledge knowledge = record(player);
        knowledge.applyAutoUnlock(player);
        knowledge.queueSync(player);
    }

    // ------------------------------------------------------------------
    // 门槛判定
    // ------------------------------------------------------------------

    /**
     * 配方门槛判定：条目完成或达到指定阶段。
     */
    public static boolean doesPassGate(Player player, @Nullable ResearchGate gate) {
        if (gate == null) {
            return true;
        }
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        boolean known = gate.stage()
                .map(stage -> knowledge.hasReachedStage(gate.entry(), stage))
                .orElseGet(() -> knowledge.hasCompleted(gate.entry()));
        return gate.negate() != known;
    }

    public static PlayerResearch of(ServerPlayer player) {
        return PlayerResearchAccess.of(player);
    }

    public static boolean parentsSatisfied(PlayerResearch knowledge, IResearchEntry entry) {
        return entry.parents().stream().allMatch(parent -> parent.isSatisfiedBy(knowledge));
    }

    /**
     * 判定一条前置研究要求。
     */
    public static boolean requirementSatisfied(Player player, PlayerResearch knowledge, ResourceLocation required) {
        return knowledge.hasCompleted(required);
    }

    /**
     * 判定某阶段是否满足推进条件。
     */
    public static boolean stageRequirementsMet(
            Player player, PlayerResearch knowledge, IResearchEntry entry, ResourceLocation id, int stageIndex) {
        if (stageIndex < 0 || stageIndex >= entry.stages().size()) {
            return false;
        }
        IResearchStage stage = entry.stages().get(stageIndex);
        boolean prerequisitesDone = stage.requiredResearch().stream()
                .allMatch(required -> requirementSatisfied(player, knowledge, required));
        boolean itemsHeld = stage.obtain().stream()
                .allMatch(requirement -> countMatching(player, requirement) >= requirement.amount());
        boolean craftsDone =
                stage.craft().stream().allMatch(requirement -> isCraftSatisfied(player, knowledge, requirement));
        if (!prerequisitesDone || !itemsHeld || !craftsDone || !knowledgeHeld(player, stage)) {
            return false;
        }
        return ResearchStageRequirements.passes(player, knowledge, id, stageIndex);
    }

    private static boolean knowledgeHeld(Player player, IResearchStage stage) {
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        for (KnowledgeReward required : stage.requiredKnowledge()) {
            if (knowledge.knowledgePoints(required.type(), categoryKey(required.category())) < required.amount()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCraftSatisfied(Player player, PlayerResearch knowledge, ResearchRequirement requirement) {
        for (Holder<Item> item : requirement.items()) {
            Optional<ResourceKey<Item>> key = item.unwrapKey();
            if (key.isPresent() && knowledge.knows(craftedKey(key.get().location()))) {
                return true;
            }
        }
        return countMatching(player, requirement) > 0;
    }

    public static int countMatching(Player player, ResearchRequirement requirement) {
        Inventory inventory = player.getInventory();
        return IntStream.range(0, inventory.getContainerSize())
                .mapToObj(inventory::getItem)
                .filter(stack -> !stack.isEmpty() && requirement.matches(stack))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    /**
     * 收集某阶段所有未满足原因的提示，界面用它画锁定说明。
     */
    public static List<Component> lockedReasons(
            Player player, IResearchEntry entry, ResourceLocation id, int stageIndex) {
        List<Component> reasons = new ArrayList<>();
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        if (stageIndex < 0 || stageIndex >= entry.stages().size()) {
            reasons.add(Component.translatable("research.lobotomycorp.locked.stage").withStyle(ChatFormatting.RED));
            return reasons;
        }
        IResearchStage stage = entry.stages().get(stageIndex);
        for (ResourceLocation required : stage.requiredResearch()) {
            if (!requirementSatisfied(player, knowledge, required)) {
                reasons.add(Component.translatable("research.lobotomycorp.locked.research", required.toString())
                        .withStyle(ChatFormatting.RED));
            }
        }
        for (ResearchRequirement requirement : stage.obtain()) {
            int held = countMatching(player, requirement);
            if (held < requirement.amount()) {
                reasons.add(Component.translatable("research.lobotomycorp.locked.items", held, requirement.amount())
                        .withStyle(ChatFormatting.RED));
            }
        }
        for (ResearchRequirement requirement : stage.craft()) {
            if (!isCraftSatisfied(player, knowledge, requirement)) {
                reasons.add(Component.translatable("research.lobotomycorp.locked.craft")
                        .withStyle(ChatFormatting.RED));
            }
        }
        for (KnowledgeReward required : stage.requiredKnowledge()) {
            int held = knowledge.knowledgePoints(required.type(), categoryKey(required.category()));
            if (held < required.amount()) {
                reasons.add(Component.translatable("research.lobotomycorp.locked.knowledge",
                                Component.translatable(required.type().translationKey()), held, required.amount())
                        .withStyle(ChatFormatting.RED));
            }
        }
        reasons.addAll(ResearchStageRequirements.lockedMessages(player, knowledge, id, stageIndex));
        return reasons;
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    public static PlayerKnowledge record(ServerPlayer player) {
        return player.getData(LCAttachments.knowledge());
    }

    public static @Nullable ResourceKey<IResearchCategory> categoryKey(@Nullable Holder<IResearchCategory> category) {
        return category == null ? null : category.unwrapKey().orElse(null);
    }

    private static boolean vetoed(ResearchEvent event) {
        return NeoForge.EVENT_BUS.post(event).isCanceled();
    }

    private static boolean hasRequirements(IResearchStage stage) {
        return !stage.obtain().isEmpty()
                || !stage.craft().isEmpty()
                || !stage.requiredKnowledge().isEmpty()
                || !stage.requiredResearch().isEmpty();
    }

    private static void applyStageEffects(IResearchStage stage, ServerPlayer player) {
        for (KnowledgeReward reward : stage.knowledge()) {
            gainKnowledge(player, reward.type(), reward.category(), reward.amount());
        }
    }

    private static void consume(ServerPlayer player, ResourceLocation id, IResearchStage stage, int stageIndex) {
        for (ResearchRequirement requirement : stage.obtain()) {
            removeMatching(player.getInventory(), requirement);
        }
        PlayerKnowledge knowledge = record(player);
        for (KnowledgeReward required : stage.requiredKnowledge()) {
            knowledge.addKnowledgePoints(required.type(), categoryKey(required.category()), -required.amount());
        }
        ResearchStageRequirements.consume(player, id, stageIndex);
    }

    private static void removeMatching(Inventory inventory, ResearchRequirement requirement) {
        int remaining = requirement.amount();
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && requirement.matches(stack)) {
                remaining -= inventory.removeItem(slot, remaining).getCount();
            }
        }
    }
}
