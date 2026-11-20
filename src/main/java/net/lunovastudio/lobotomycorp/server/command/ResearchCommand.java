package net.lunovastudio.lobotomycorp.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.lunovastudio.lobotomycorp.LCIds;
import net.lunovastudio.lobotomycorp.api.capability.PlayerCareer;
import net.lunovastudio.lobotomycorp.api.capability.PlayerCareerAccess;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearchAccess;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.lunovastudio.lobotomycorp.api.research.IResearchEntry;
import net.lunovastudio.lobotomycorp.api.research.IResearchStage;
import net.lunovastudio.lobotomycorp.api.research.ResearchParent;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeAccess;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeType;
import net.lunovastudio.lobotomycorp.content.career.CareerManager;
import net.lunovastudio.lobotomycorp.content.research.ResearchManager;
import net.lunovastudio.lobotomycorp.content.research.ResearchProgression;
import net.lunovastudio.lobotomycorp.content.research.ResearchRecipeLocks;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 研究系统的管理命令：{@code /lobotomy research ...}。
 *
 * <p>每个子命令默认作用于执行者本人，也可以在末尾追加一个玩家参数。</p>
 */
@EventBusSubscriber(modid = LCIds.MODID)
public final class ResearchCommand {
    private static final int PERMISSION_LEVEL = 2;
    private static final SimpleCommandExceptionType UNKNOWN_KNOWLEDGE_TYPE =
            new SimpleCommandExceptionType(Component.translatable("research.lobotomycorp.command.unknown_knowledge"));

    private static final SuggestionProvider<CommandSourceStack> ENTRY_SUGGESTIONS = (ctx, builder) -> {
        ServerPlayer player = firstPlayer(ctx);
        if (player == null) {
            return builder.buildFuture();
        }
        return SharedSuggestionProvider.suggest(
                ResearchManager.allEntries(player).stream()
                        .map(holder -> holder.key().location().toString())
                        .toList(),
                builder);
    };

    private static final SuggestionProvider<CommandSourceStack> CATEGORY_SUGGESTIONS = (ctx, builder) -> {
        ServerPlayer player = firstPlayer(ctx);
        if (player == null) {
            return builder.buildFuture();
        }
        return SharedSuggestionProvider.suggest(
                ResearchManager.allCategories(player).stream()
                        .map(holder -> holder.key().location().toString())
                        .toList(),
                builder);
    };

    private ResearchCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("lobotomy").then(root()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root() {
        return Commands.literal("research")
                .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                .then(Commands.literal("grant").then(withTarget(
                        argument("entry", ENTRY_SUGGESTIONS),
                        ctx -> grant(ctx, self(ctx), entryId(ctx)),
                        ctx -> grant(ctx, target(ctx), entryId(ctx)))))
                .then(Commands.literal("prepare").then(withTarget(
                        argument("entry", ENTRY_SUGGESTIONS),
                        ctx -> prepare(ctx, self(ctx), entryId(ctx)),
                        ctx -> prepare(ctx, target(ctx), entryId(ctx)))))
                .then(Commands.literal("revoke").then(withTarget(
                        argument("entry", ENTRY_SUGGESTIONS),
                        ctx -> revoke(ctx, self(ctx), entryId(ctx)),
                        ctx -> revoke(ctx, target(ctx), entryId(ctx)))))
                .then(Commands.literal("unlock").then(withTarget(
                        argument("entry", ENTRY_SUGGESTIONS),
                        ctx -> unlock(ctx, self(ctx), entryId(ctx)),
                        ctx -> unlock(ctx, target(ctx), entryId(ctx)))))
                .then(Commands.literal("advance").then(withTarget(
                        argument("entry", ENTRY_SUGGESTIONS),
                        ctx -> advance(ctx, self(ctx), entryId(ctx)),
                        ctx -> advance(ctx, target(ctx), entryId(ctx)))))
                .then(Commands.literal("set_stage").then(
                        argument("entry", ENTRY_SUGGESTIONS).then(withTarget(
                                Commands.argument("stage", IntegerArgumentType.integer(1, 64)),
                                ctx -> setStage(ctx, self(ctx), entryId(ctx), stage(ctx)),
                                ctx -> setStage(ctx, target(ctx), entryId(ctx), stage(ctx))))))
                .then(Commands.literal("complete_category").then(withTarget(
                        argument("category", CATEGORY_SUGGESTIONS),
                        ctx -> completeCategory(ctx, self(ctx), categoryId(ctx)),
                        ctx -> completeCategory(ctx, target(ctx), categoryId(ctx)))))
                .then(Commands.literal("reset")
                        .executes(ctx -> reset(ctx, self(ctx)))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> reset(ctx, target(ctx)))))
                .then(Commands.literal("knowledge").then(
                        Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(KnowledgeType.values())
                                                .map(KnowledgeType::getSerializedName)
                                                .toList(),
                                        builder))
                                .then(argument("category", CATEGORY_SUGGESTIONS).then(withTarget(
                                        Commands.argument("amount", IntegerArgumentType.integer(-9999, 9999)),
                                        ctx -> knowledge(ctx, self(ctx), knowledgeType(ctx),
                                                categoryId(ctx), amount(ctx)),
                                        ctx -> knowledge(ctx, target(ctx), knowledgeType(ctx),
                                                categoryId(ctx), amount(ctx)))))))
                .then(Commands.literal("grade").then(withTarget(
                        Commands.argument("grade", IntegerArgumentType.integer(1, PlayerCareer.MAX_GRADE)),
                        ctx -> grade(ctx, self(ctx), ctx),
                        ctx -> grade(ctx, target(ctx), ctx))))
                .then(Commands.literal("access").then(
                        argument("category", CATEGORY_SUGGESTIONS).then(withTarget(
                                Commands.argument("level", IntegerArgumentType.integer(0, 10)),
                                ctx -> access(ctx, self(ctx), categoryId(ctx), level(ctx)),
                                ctx -> access(ctx, target(ctx), categoryId(ctx), level(ctx))))))
                .then(Commands.literal("status")
                        .executes(ctx -> status(ctx, self(ctx)))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> status(ctx, target(ctx)))))
                .then(Commands.literal("validate").executes(ResearchCommand::validate));
    }

    // ------------------------------------------------------------------
    // 子命令实现
    // ------------------------------------------------------------------

    private static int grant(CommandContext<CommandSourceStack> ctx, ServerPlayer player, ResourceLocation entry) {
        int completed = ResearchProgression.grant(player, entry);
        return report(ctx, "research.lobotomycorp.command.grant", completed, entry.toString());
    }

    private static int prepare(CommandContext<CommandSourceStack> ctx, ServerPlayer player, ResourceLocation entry) {
        int completed = ResearchProgression.prepare(player, entry);
        return report(ctx, "research.lobotomycorp.command.prepare", completed, entry.toString());
    }

    private static int revoke(CommandContext<CommandSourceStack> ctx, ServerPlayer player, ResourceLocation entry) {
        int removed = ResearchProgression.revoke(player, entry);
        return report(ctx, "research.lobotomycorp.command.revoke", removed, entry.toString());
    }

    private static int unlock(CommandContext<CommandSourceStack> ctx, ServerPlayer player, ResourceLocation entry) {
        boolean unlocked = ResearchManager.unlock(player, entry);
        return report(ctx, "research.lobotomycorp.command.unlock", unlocked ? 1 : 0, entry.toString());
    }

    private static int advance(CommandContext<CommandSourceStack> ctx, ServerPlayer player, ResourceLocation entry) {
        boolean advanced = ResearchManager.advanceStage(player, entry, false);
        return report(ctx, "research.lobotomycorp.command.advance", advanced ? 1 : 0, entry.toString());
    }

    private static int setStage(CommandContext<CommandSourceStack> ctx, ServerPlayer player,
                                ResourceLocation entry, int stage) {
        boolean changed = ResearchManager.setStage(player, entry, stage);
        return report(ctx, "research.lobotomycorp.command.set_stage", changed ? stage : 0, entry.toString());
    }

    private static int completeCategory(CommandContext<CommandSourceStack> ctx, ServerPlayer player,
                                        ResourceLocation category) {
        ResourceKey<IResearchCategory> key = ResourceKey.create(IResearchCategory.REGISTRY_KEY, category);
        int completed = ResearchProgression.completeCategory(player, key);
        return report(ctx, "research.lobotomycorp.command.complete_category", completed, category.toString());
    }

    private static int reset(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        ResearchProgression.reset(player);
        return report(ctx, "research.lobotomycorp.command.reset", player.getGameProfile().getName());
    }

    private static int knowledge(CommandContext<CommandSourceStack> ctx, ServerPlayer player, KnowledgeType type,
                                 ResourceLocation category, int amount) {
        ResourceKey<IResearchCategory> key = ResourceKey.create(IResearchCategory.REGISTRY_KEY, category);
        Holder<IResearchCategory> holder = holder(player, key);
        boolean changed;
        if (amount >= 0) {
            changed = holder != null && KnowledgeAccess.grant(player, type, holder, amount);
        } else {
            changed = PlayerResearchAccess.of(player).addKnowledgePoints(type, key, amount);
            if (changed) {
                PlayerResearchAccess.of(player).queueSync(player);
            }
        }
        return report(ctx, "research.lobotomycorp.command.knowledge", amount, category.toString());
    }

    private static int grade(CommandContext<CommandSourceStack> ctx, ServerPlayer player,
                             CommandContext<CommandSourceStack> unused) {
        int grade = IntegerArgumentType.getInteger(ctx, "grade");
        CareerManager.setEmployeeGrade(player, grade);
        return report(ctx, "research.lobotomycorp.command.grade", grade, player.getGameProfile().getName());
    }

    private static int access(CommandContext<CommandSourceStack> ctx, ServerPlayer player,
                              ResourceLocation category, int level) {
        CareerManager.setAccessLevel(player, category, level);
        return report(ctx, "research.lobotomycorp.command.access", level, category.toString());
    }

    private static int status(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        CommandSourceStack source = ctx.getSource();
        var knowledge = PlayerResearchAccess.of(player);
        var career = PlayerCareerAccess.of(player);
        source.sendSuccess(() -> Component.translatable("research.lobotomycorp.command.status.header",
                player.getGameProfile().getName(), career.employeeGrade()), false);
        for (Holder.Reference<IResearchCategory> category : ResearchManager.allCategories(player)) {
            ResourceLocation id = category.key().location();
            ResourceKey<IResearchCategory> key = ResourceKey.create(IResearchCategory.REGISTRY_KEY, id);
            int total = ResearchProgression.totalInCategory(player, key);
            int completed = ResearchProgression.completedInCategory(player, key);
            int theory = knowledge.knowledgePoints(KnowledgeType.THEORY, key);
            int observation = knowledge.knowledgePoints(KnowledgeType.OBSERVATION, key);
            int access = ResearchManager.accessLevel(player, category);
            boolean open = ResearchManager.categoryOpen(player, category);
            source.sendSuccess(() -> Component.translatable("research.lobotomycorp.command.status.category",
                    id.toString(), access, open ? "open" : "closed", completed, total, theory, observation), false);
        }
        for (Holder.Reference<IResearchEntry> entry : ResearchManager.allEntries(player)) {
            ResourceLocation id = entry.key().location();
            if (!knowledge.knows(id)) {
                continue;
            }
            source.sendSuccess(() -> Component.translatable("research.lobotomycorp.command.status.entry",
                    id.toString(), knowledge.status(id).name(), knowledge.stageOf(id),
                    entry.value().stages().size()), false);
        }
        return 1;
    }

    private static int validate(CommandContext<CommandSourceStack> ctx) {
        net.minecraft.core.RegistryAccess access = ctx.getSource().getServer().registryAccess();
        List<String> problems = new ArrayList<>();

        List<EntryCheck> entries = new ArrayList<>();
        for (Holder.Reference<IResearchEntry> holder : ResearchManager.allEntries(access)) {
            entries.add(new EntryCheck(holder.key().location(), holder.value()));
        }
        Set<ResourceLocation> entryIds = new HashSet<>();
        for (EntryCheck entry : entries) {
            if (!entryIds.add(entry.id())) {
                problems.add("duplicate entry: " + entry.id());
            }
        }
        Set<Integer> indices = new HashSet<>();
        for (Holder.Reference<IResearchCategory> category : ResearchManager.allCategories(access)) {
            ResourceLocation id = category.key().location();
            if (!indices.add(category.value().index())) {
                problems.add("duplicate category index " + category.value().index() + " (" + id + ")");
            }
            if (category.value().icon() == null) {
                problems.add("category without icon: " + id);
            }
            category.value().requiredResearch().ifPresent(required -> {
                if (!entryIds.contains(required)) {
                    problems.add("category " + id + " requires missing research " + required);
                }
            });
        }
        for (EntryCheck entry : entries) {
            if (entry.value().stages().isEmpty()) {
                problems.add("entry without stages: " + entry.id());
            }
            for (ResearchParent parent : entry.value().parents()) {
                if (!entryIds.contains(parent.id())) {
                    problems.add("entry " + entry.id() + " has missing parent " + parent.id());
                }
            }
            for (IResearchStage stage : entry.value().stages()) {
                for (ResourceLocation required : stage.requiredResearch()) {
                    if (!entryIds.contains(required)) {
                        problems.add("entry " + entry.id() + " requires missing research " + required);
                    }
                }
                if (stage.textKey() == null || stage.textKey().isBlank()) {
                    problems.add("entry " + entry.id() + " has a stage without a text key");
                }
            }
            for (ResourceLocation recipe : entry.value().unlocksRecipes()) {
                if (ctx.getSource().getServer().getRecipeManager().byKey(recipe).isEmpty()) {
                    problems.add("entry " + entry.id() + " unlocks unknown recipe " + recipe);
                }
            }
        }

        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.translatable("research.lobotomycorp.command.validate.summary",
                entries.size(), ResearchManager.allCategories(access).size(),
                ResearchRecipeLocks.managedRecipeCount()).withStyle(ChatFormatting.GRAY), false);
        if (problems.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("research.lobotomycorp.command.validate.ok")
                    .withStyle(ChatFormatting.GREEN), false);
            return 1;
        }
        for (String problem : problems) {
            source.sendFailure(Component.literal(problem));
        }
        return 0;
    }

    private record EntryCheck(ResourceLocation id, IResearchEntry value) {
    }

    // ------------------------------------------------------------------
    // 工具
    // ------------------------------------------------------------------

    private static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> argument(
            String name, SuggestionProvider<CommandSourceStack> suggestions) {
        return Commands.argument(name, ResourceLocationArgument.id()).suggests(suggestions);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> withTarget(
            ArgumentBuilder<CommandSourceStack, ?> node,
            com.mojang.brigadier.Command<CommandSourceStack> onSelf,
            com.mojang.brigadier.Command<CommandSourceStack> onOther) {
        node.executes(onSelf);
        node.then(Commands.argument("target", EntityArgument.player()).executes(onOther));
        return node;
    }

    private static ServerPlayer self(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ctx.getSource().getPlayerOrException();
    }

    private static ServerPlayer target(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return EntityArgument.getPlayer(ctx, "target");
    }

    @Nullable
    private static ServerPlayer firstPlayer(CommandContext<CommandSourceStack> ctx) {
        List<ServerPlayer> players = ctx.getSource().getServer().getPlayerList().getPlayers();
        return players.isEmpty() ? null : players.getFirst();
    }

    private static ResourceLocation entryId(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ResourceLocationArgument.getId(ctx, "entry");
    }

    private static ResourceLocation categoryId(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return ResourceLocationArgument.getId(ctx, "category");
    }

    private static int stage(CommandContext<CommandSourceStack> ctx) {
        return IntegerArgumentType.getInteger(ctx, "stage");
    }

    private static int level(CommandContext<CommandSourceStack> ctx) {
        return IntegerArgumentType.getInteger(ctx, "level");
    }

    private static int amount(CommandContext<CommandSourceStack> ctx) {
        return IntegerArgumentType.getInteger(ctx, "amount");
    }

    private static KnowledgeType knowledgeType(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String raw = StringArgumentType.getString(ctx, "type");
        for (KnowledgeType type : KnowledgeType.values()) {
            if (type.getSerializedName().equalsIgnoreCase(raw)) {
                return type;
            }
        }
        throw UNKNOWN_KNOWLEDGE_TYPE.create();
    }

    @Nullable
    private static Holder<IResearchCategory> holder(ServerPlayer player, ResourceKey<IResearchCategory> key) {
        for (Holder.Reference<IResearchCategory> candidate : ResearchManager.allCategories(player)) {
            if (candidate.key().equals(key)) {
                return candidate;
            }
        }
        return null;
    }

    private static int report(CommandContext<CommandSourceStack> ctx, String key, Object... args) {
        ctx.getSource().sendSuccess(() -> Component.translatable(key, args), true);
        return 1;
    }
}
