package net.lunovastudio.lobotomycorp.content.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearch;
import net.lunovastudio.lobotomycorp.api.capability.ResearchFlag;
import net.lunovastudio.lobotomycorp.api.capability.ResearchStatus;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.lunovastudio.lobotomycorp.api.research.IResearchEntry;
import net.lunovastudio.lobotomycorp.api.research.ResearchEntryMeta;
import net.lunovastudio.lobotomycorp.api.research.ResearchUnlockConditions;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 玩家研究进度的权威实现，作为玩家附件保存。
 *
 * <p>只记录“差异”：学会的条目、完成状态、阶段、界面标记与按分类累计的知识点。
 * 静态定义（分类/条目/阶段）永远不复制进来，随时从数据包注册表读取。</p>
 */
public final class PlayerKnowledge implements PlayerResearch {
    public static final MapCodec<PlayerKnowledge> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResearchRecord.CODEC
                            .listOf()
                            .optionalFieldOf("research", List.of())
                            .forGetter(PlayerKnowledge::researchEntries),
                    KnowledgeRecord.CODEC
                            .listOf()
                            .optionalFieldOf("knowledge", List.of())
                            .forGetter(PlayerKnowledge::knowledgeEntries))
            .apply(instance, PlayerKnowledge::fromRecords));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerKnowledge> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    private final Set<ResourceLocation> research = new HashSet<>();
    private final Set<ResourceLocation> completed = new HashSet<>();
    private final Map<ResourceLocation, Integer> stages = new HashMap<>();
    private final Map<ResourceLocation, EnumSet<ResearchFlag>> flags = new HashMap<>();
    private final Map<KnowledgeKey, Integer> knowledge = new HashMap<>();
    private boolean syncPending;

    public PlayerKnowledge() {
    }

    private static PlayerKnowledge fromRecords(
            List<ResearchRecord> researchRecords, List<KnowledgeRecord> knowledgeRecords) {
        PlayerKnowledge result = new PlayerKnowledge();
        for (ResearchRecord record : researchRecords) {
            result.research.add(record.key());
            if (record.complete()) {
                result.completed.add(record.key());
            }
            record.stage().ifPresent(stage -> result.stages.put(record.key(), stage));
            if (!record.flags().isEmpty()) {
                EnumSet<ResearchFlag> flagSet = EnumSet.noneOf(ResearchFlag.class);
                flagSet.addAll(record.flags());
                result.flags.put(record.key(), flagSet);
            }
        }
        for (KnowledgeRecord record : knowledgeRecords) {
            if (record.amount() <= 0) {
                continue;
            }
            result.knowledge.put(
                    new KnowledgeKey(record.type(), record.category().map(ResourceKey::location).orElse(null)),
                    record.amount());
        }
        return result;
    }

    private List<ResearchRecord> researchEntries() {
        return research.stream()
                .sorted()
                .map(key -> new ResearchRecord(
                        key,
                        Optional.ofNullable(stages.get(key)),
                        flagsAsList(key),
                        completed.contains(key)))
                .toList();
    }

    private List<ResearchFlag> flagsAsList(ResourceLocation key) {
        EnumSet<ResearchFlag> set = flags.get(key);
        return set == null ? List.of() : List.copyOf(set);
    }

    private List<KnowledgeRecord> knowledgeEntries() {
        return knowledge.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> new KnowledgeRecord(
                        entry.getKey().type(),
                        Optional.ofNullable(entry.getKey().category())
                                .map(path -> ResourceKey.create(IResearchCategory.REGISTRY_KEY, path)),
                        entry.getValue()))
                .toList();
    }

    // ------------------------------------------------------------------
    // 查询
    // ------------------------------------------------------------------

    @Override
    public ResearchStatus status(ResourceLocation entry) {
        if (!research.contains(entry)) {
            return ResearchStatus.UNKNOWN;
        }
        return completed.contains(entry) ? ResearchStatus.COMPLETE : ResearchStatus.IN_PROGRESS;
    }

    @Override
    public boolean knows(ResourceLocation entry) {
        return research.contains(entry);
    }

    @Override
    public boolean hasCompleted(ResourceLocation entry) {
        return completed.contains(entry);
    }

    @Override
    public boolean hasReachedStage(ResourceLocation entry, int stage) {
        return research.contains(entry) && (completed.contains(entry) || stageOf(entry) >= stage);
    }

    @Override
    public int stageOf(ResourceLocation entry) {
        if (!research.contains(entry)) {
            return -1;
        }
        return stages.getOrDefault(entry, 0);
    }

    @Override
    public Set<ResourceLocation> knownEntries() {
        return Collections.unmodifiableSet(research);
    }

    @Override
    public boolean hasFlag(ResourceLocation entry, ResearchFlag flag) {
        EnumSet<ResearchFlag> set = flags.get(entry);
        return set != null && set.contains(flag);
    }

    // ------------------------------------------------------------------
    // 状态变更
    // ------------------------------------------------------------------

    @Override
    public boolean learn(ResourceLocation entry) {
        return entry != null && research.add(entry);
    }

    /**
     * 把条目标记为完成，并把阶段推进到末尾。
     *
     * @return 之前未完成时为 true
     */
    public boolean completeEntry(ResourceLocation entry) {
        if (entry == null || !research.contains(entry)) {
            return false;
        }
        return completed.add(entry);
    }

    @Override
    public boolean setStage(ResourceLocation entry, int stage) {
        if (entry == null || stage <= 0 || !research.contains(entry)) {
            return false;
        }
        Integer previous = stages.get(entry);
        if (previous != null && previous == stage) {
            return false;
        }
        stages.put(entry, stage);
        return true;
    }

    /**
     * 直接写入阶段，不做任何校验，供管理器内部使用。
     */
    void setStageRaw(ResourceLocation entry, int stage) {
        stages.put(entry, stage);
    }

    @Override
    public boolean forget(ResourceLocation entry) {
        if (entry == null) {
            return false;
        }
        boolean known = research.remove(entry);
        completed.remove(entry);
        stages.remove(entry);
        flags.remove(entry);
        return known;
    }

    @Override
    public boolean addFlag(ResourceLocation entry, ResearchFlag flag) {
        if (entry == null || flag == null) {
            return false;
        }
        return flags.computeIfAbsent(entry, key -> EnumSet.noneOf(ResearchFlag.class)).add(flag);
    }

    @Override
    public boolean removeFlag(ResourceLocation entry, ResearchFlag flag) {
        if (entry == null || flag == null) {
            return false;
        }
        EnumSet<ResearchFlag> set = flags.get(entry);
        if (set == null) {
            return false;
        }
        boolean removed = set.remove(flag);
        if (set.isEmpty()) {
            flags.remove(entry);
        }
        return removed;
    }

    @Override
    public int knowledgeLevel(KnowledgeType type, ResourceKey<IResearchCategory> category) {
        return knowledgePoints(type, category) / type.progression();
    }

    @Override
    public int knowledgePoints(KnowledgeType type, ResourceKey<IResearchCategory> category) {
        if (type == null) {
            return 0;
        }
        KnowledgeKey key = new KnowledgeKey(type, category == null ? null : category.location());
        return knowledge.getOrDefault(key, 0);
    }

    @Override
    public boolean addKnowledgePoints(KnowledgeType type, ResourceKey<IResearchCategory> category, int amount) {
        if (type == null || amount == 0) {
            return false;
        }
        KnowledgeKey key = new KnowledgeKey(type, category == null ? null : category.location());
        int next = Math.max(0, knowledge.getOrDefault(key, 0) + amount);
        if (next == knowledge.getOrDefault(key, 0)) {
            return false;
        }
        if (next == 0) {
            knowledge.remove(key);
        } else {
            knowledge.put(key, next);
        }
        return true;
    }

    @Override
    public void wipe() {
        research.clear();
        completed.clear();
        stages.clear();
        flags.clear();
        knowledge.clear();
    }

    @Override
    public void queueSync(ServerPlayer player) {
        syncPending = true;
    }

    /**
     * 取出并清除待同步标记。
     */
    public boolean takeSyncPending() {
        boolean pending = syncPending;
        syncPending = false;
        return pending;
    }

    // ------------------------------------------------------------------
    // 批量操作
    // ------------------------------------------------------------------

    /**
     * 生成一份深拷贝，用于命令回滚或导出。
     */
    public static PlayerKnowledge snapshotOf(PlayerKnowledge source) {
        PlayerKnowledge snapshot = new PlayerKnowledge();
        snapshot.copyFrom(source);
        return snapshot;
    }

    /**
     * 把另一份进度的“已学会/已完成/阶段”并入本进度（取较新者）。
     */
    public void mergeResearchFrom(PlayerKnowledge other) {
        research.addAll(other.research);
        completed.addAll(other.completed);
        for (Map.Entry<ResourceLocation, Integer> entry : other.stages.entrySet()) {
            stages.merge(entry.getKey(), entry.getValue(), Math::max);
        }
    }

    /**
     * 用另一份进度完全替换本进度。
     */
    public void copyFrom(PlayerKnowledge other) {
        wipe();
        research.addAll(other.research);
        completed.addAll(other.completed);
        stages.putAll(other.stages);
        for (Map.Entry<ResourceLocation, EnumSet<ResearchFlag>> entry : other.flags.entrySet()) {
            flags.put(entry.getKey(), EnumSet.copyOf(entry.getValue()));
        }
        knowledge.putAll(other.knowledge);
    }

    /**
     * 把带 {@link ResearchEntryMeta#AUTOUNLOCK} 的条目自动加入记录。
     */
    public void applyAutoUnlock(ServerPlayer player) {
        player.registryAccess()
                .lookup(IResearchEntry.REGISTRY_KEY)
                .ifPresent(lookup -> lookup.listElements().forEach(holder -> {
                    if (!holder.value().hasMeta(ResearchEntryMeta.AUTOUNLOCK)) {
                        return;
                    }
                    holder.unwrapKey().map(ResourceKey::location).ifPresent(id -> {
                        if (!research.contains(id) && ResearchUnlockConditions.passes(player, this, id)) {
                            learn(id);
                        }
                    });
                }));
    }

    private record KnowledgeKey(KnowledgeType type, ResourceLocation category) {
    }

    private record ResearchRecord(
            ResourceLocation key, Optional<Integer> stage, List<ResearchFlag> flags, boolean complete) {
        static final Codec<ResearchRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        ResourceLocation.CODEC.fieldOf("id").forGetter(ResearchRecord::key),
                        Codec.INT.optionalFieldOf("stage").forGetter(ResearchRecord::stage),
                        ResearchFlag.CODEC
                                .listOf()
                                .optionalFieldOf("flags", List.of())
                                .forGetter(ResearchRecord::flags),
                        Codec.BOOL.optionalFieldOf("complete", false).forGetter(ResearchRecord::complete))
                .apply(instance, ResearchRecord::new));
    }

    private record KnowledgeRecord(KnowledgeType type, Optional<ResourceKey<IResearchCategory>> category, int amount) {
        static final Codec<KnowledgeRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        KnowledgeType.CODEC.fieldOf("type").forGetter(KnowledgeRecord::type),
                        ResourceKey.codec(IResearchCategory.REGISTRY_KEY)
                                .optionalFieldOf("category")
                                .forGetter(KnowledgeRecord::category),
                        Codec.INT.fieldOf("amount").forGetter(KnowledgeRecord::amount))
                .apply(instance, KnowledgeRecord::new));
    }
}
