package net.lunovastudio.lobotomycorp.api.research;

import net.lunovastudio.lobotomycorp.api.capability.PlayerResearch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 阶段级附加条件的注册表。
 *
 * <p>每条条件绑定到“某个条目 + 某个阶段”；阶段为 {@link #ANY_STAGE} 时对该条目的所有阶段生效。
 * 注册应在通用初始化阶段完成，例如 {@code FMLCommonSetupEvent} 的 {@code enqueueWork} 里。</p>
 */
public final class ResearchStageRequirements {
    /** 表示“该条目的所有阶段”。 */
    public static final int ANY_STAGE = -1;

    private record Registered(StageRequirement requirement) {
    }

    private static final Map<ResourceLocation, List<Registered>> REQUIREMENTS = new ConcurrentHashMap<>();

    private ResearchStageRequirements() {
    }

    /**
     * 为某个条目的某个阶段注册条件。
     *
     * @param entry       条目 id
     * @param stageIndex  阶段下标，或 {@link #ANY_STAGE}
     * @param requirement 条件
     */
    public static void require(ResourceLocation entry, int stageIndex, StageRequirement requirement) {
        Objects.requireNonNull(entry);
        Objects.requireNonNull(requirement);
        ResourceLocation key = key(entry, stageIndex);
        REQUIREMENTS.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(new Registered(requirement));
    }

    /**
     * 判断某条目某阶段的全部附加条件是否满足。
     */
    public static boolean passes(Player player, PlayerResearch research, ResourceLocation entry, int stageIndex) {
        for (Registered registered : lookup(entry, stageIndex)) {
            if (!registered.requirement().isMet(player, research, entry, stageIndex)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 阶段推进成功后扣除全部附加条件代价。
     */
    public static void consume(ServerPlayer player, ResourceLocation entry, int stageIndex) {
        for (Registered registered : lookup(entry, stageIndex)) {
            registered.requirement().consume(player, entry, stageIndex);
        }
    }

    /**
     * 收集未满足条件的锁定提示。
     */
    public static List<Component> lockedMessages(Player player, PlayerResearch research,
                                                 ResourceLocation entry, int stageIndex) {
        List<Component> messages = new ArrayList<>();
        for (Registered registered : lookup(entry, stageIndex)) {
            Component message = registered.requirement().lockedMessage();
            if (message != null
                    && !registered.requirement().isMet(player, research, entry, stageIndex)) {
                messages.add(message);
            }
        }
        return messages;
    }

    private static List<Registered> lookup(ResourceLocation entry, int stageIndex) {
        List<Registered> exact = REQUIREMENTS.getOrDefault(key(entry, stageIndex), List.of());
        List<Registered> any = REQUIREMENTS.getOrDefault(key(entry, ANY_STAGE), List.of());
        if (any.isEmpty()) {
            return exact;
        }
        if (exact.isEmpty()) {
            return any;
        }
        List<Registered> merged = new ArrayList<>(any.size() + exact.size());
        merged.addAll(any);
        merged.addAll(exact);
        return merged;
    }

    private static ResourceLocation key(ResourceLocation entry, int stageIndex) {
        return ResourceLocation.fromNamespaceAndPath(entry.getNamespace(), entry.getPath() + "#" + stageIndex);
    }

    /** 测试与调试用：清空全部注册。 */
    public static void clear() {
        REQUIREMENTS.clear();
    }

    /** 已注册阶段的条件数量，用于状态输出。 */
    public static int registrationCount() {
        return REQUIREMENTS.values().stream().mapToInt(List::size).sum();
    }
}
