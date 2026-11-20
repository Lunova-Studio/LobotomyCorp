package net.lunovastudio.lobotomycorp.content.research;

import net.lunovastudio.lobotomycorp.api.capability.PlayerResearch;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearchAccess;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.lunovastudio.lobotomycorp.api.research.IResearchEntry;
import net.lunovastudio.lobotomycorp.api.research.IResearchStage;
import net.lunovastudio.lobotomycorp.api.research.ResearchParent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 批量研究进度操作：按依赖顺序一次性发放、清空、按分类完成等。
 *
 * <p>命令与调试功能都走这里，保证依赖顺序正确。</p>
 */
public final class ResearchProgression {

    private ResearchProgression() {
    }

    /**
     * 清空全部研究进度并重新应用自动解锁条目。
     */
    public static void reset(ServerPlayer player) {
        PlayerResearchAccess.of(player).wipe();
        PlayerResearchAccess.of(player).queueSync(player);
        ResearchManager.applyAutoUnlock(player);
    }

    /**
     * 完成某条目及其全部依赖，返回完成的条目数。
     */
    public static int grant(ServerPlayer player, ResourceLocation research) {
        Set<ResourceLocation> ordered = new LinkedHashSet<>();
        collect(entries(player), research, new HashSet<>(), ordered);
        return completeAll(player, ordered);
    }

    /**
     * 把某条目的依赖全部完成，然后只解锁该条目本身（便于从第一阶段开始体验）。
     */
    public static int prepare(ServerPlayer player, ResourceLocation research) {
        Set<ResourceLocation> ordered = new LinkedHashSet<>();
        collect(entries(player), research, new HashSet<>(), ordered);
        ordered.remove(research);
        int completed = completeAll(player, ordered);
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        if (knowledge.forget(research)) {
            knowledge.queueSync(player);
        }
        ResearchManager.unlock(player, research);
        return completed;
    }

    /**
     * 撤销某条目及其全部下游依赖，返回被移除的条目数。
     */
    public static int revoke(ServerPlayer player, ResourceLocation research) {
        Map<ResourceLocation, List<ResourceLocation>> dependents = new HashMap<>();
        entries(player).listElements().forEach(entry -> {
            for (ResourceLocation requirement : requirements(entry.value())) {
                dependents
                        .computeIfAbsent(requirement, key -> new ArrayList<>())
                        .add(entry.key().location());
            }
        });
        Set<ResourceLocation> affected = new LinkedHashSet<>();
        Deque<ResourceLocation> queue = new ArrayDeque<>();
        queue.add(research);
        while (!queue.isEmpty()) {
            ResourceLocation next = queue.poll();
            if (affected.add(next)) {
                queue.addAll(dependents.getOrDefault(next, List.of()));
            }
        }
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        int removed = 0;
        for (ResourceLocation id : affected) {
            if (knowledge.forget(id) && !id.equals(research)) {
                removed++;
            }
        }
        knowledge.queueSync(player);
        return removed;
    }

    /**
     * 完成某分类下全部条目（按依赖顺序）。
     */
    public static int completeCategory(ServerPlayer player, ResourceKey<IResearchCategory> category) {
        HolderLookup.RegistryLookup<IResearchEntry> entries = entries(player);
        Set<ResourceLocation> visited = new HashSet<>();
        Set<ResourceLocation> ordered = new LinkedHashSet<>();
        entries.listElements()
                .filter(entry -> entry.value().category().is(category))
                .forEach(entry -> collect(entries, entry.key().location(), visited, ordered));
        return completeAll(player, ordered);
    }

    private static HolderLookup.RegistryLookup<IResearchEntry> entries(ServerPlayer player) {
        return player.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY);
    }

    private static void collect(
            HolderLookup.RegistryLookup<IResearchEntry> entries,
            ResourceLocation research,
            Set<ResourceLocation> visited,
            Set<ResourceLocation> ordered) {
        if (!visited.add(research)) {
            return;
        }
        entries.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, research)).ifPresent(entry -> {
            for (ResourceLocation requirement : requirements(entry.value())) {
                collect(entries, requirement, visited, ordered);
            }
        });
        ordered.add(research);
    }

    private static List<ResourceLocation> requirements(IResearchEntry entry) {
        List<ResourceLocation> requirements = new ArrayList<>();
        entry.category().value().requiredResearch().ifPresent(requirements::add);
        for (ResearchParent parent : entry.parents()) {
            requirements.add(parent.id());
        }
        for (IResearchStage stage : entry.stages()) {
            requirements.addAll(stage.requiredResearch());
        }
        return requirements;
    }

    private static int completeAll(ServerPlayer player, Collection<ResourceLocation> research) {
        int completed = 0;
        for (ResourceLocation id : research) {
            if (ResearchManager.complete(player, id)) {
                completed++;
            }
        }
        return completed;
    }

    /**
     * 取某个分类下已完成的条目数。
     */
    public static int completedInCategory(ServerPlayer player, ResourceKey<IResearchCategory> category) {
        PlayerResearch knowledge = PlayerResearchAccess.of(player);
        return (int) entries(player).listElements()
                .filter(entry -> entry.value().category().is(category))
                .filter(entry -> knowledge.hasCompleted(entry.key().location()))
                .count();
    }

    /**
     * 取某个分类下的条目总数。
     */
    public static int totalInCategory(ServerPlayer player, ResourceKey<IResearchCategory> category) {
        return (int) entries(player).listElements()
                .filter(entry -> entry.value().category().is(category))
                .count();
    }

}
