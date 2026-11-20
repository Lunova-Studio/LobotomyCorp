package net.lunovastudio.lobotomycorp.api.research;

import net.lunovastudio.lobotomycorp.api.capability.PlayerResearch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 允许附属模组为条目解锁追加额外条件。
 *
 * <p>为某条目注册的每个条件都必须通过。注册应在通用初始化阶段完成。</p>
 *
 * <pre>{@code
 * event.enqueueWork(() -> ResearchUnlockConditions.require(
 *         ResourceLocation.fromNamespaceAndPath("my_addon", "some_entry"),
 *         UnlockRequirement.completed(ResourceLocation.fromNamespaceAndPath("my_addon", "prerequisite")),
 *         Component.translatable("tooltip.my_addon.requires_prerequisite")));
 * }</pre>
 */
public final class ResearchUnlockConditions {
    private record Registered(UnlockRequirement requirement, @Nullable Component lockedMessage) {
    }

    private static final Map<ResourceLocation, List<Registered>> CONDITIONS = new ConcurrentHashMap<>();

    private ResearchUnlockConditions() {
    }

    /**
     * 为条目追加一条解锁条件。
     *
     * @param entry         条目 id
     * @param requirement   条件
     * @param lockedMessage 未满足时显示给玩家的提示，可为 null
     */
    public static void require(ResourceLocation entry, UnlockRequirement requirement,
                               @Nullable Component lockedMessage) {
        Objects.requireNonNull(entry);
        Objects.requireNonNull(requirement);
        CONDITIONS
                .computeIfAbsent(entry, id -> new CopyOnWriteArrayList<>())
                .add(new Registered(requirement, lockedMessage));
    }

    /**
     * 判断条目全部附加条件是否满足。
     */
    public static boolean passes(Player player, PlayerResearch research, ResourceLocation entry) {
        List<Registered> requirements = CONDITIONS.get(entry);
        if (requirements == null) {
            return true;
        }
        for (Registered registered : requirements) {
            if (!registered.requirement().isMet(player, research)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 收集未满足条件的锁定提示。
     */
    public static List<Component> lockedMessages(Player player, PlayerResearch research, ResourceLocation entry) {
        List<Registered> requirements = CONDITIONS.get(entry);
        if (requirements == null) {
            return List.of();
        }
        List<Component> messages = new ArrayList<>();
        for (Registered registered : requirements) {
            if (registered.lockedMessage() != null && !registered.requirement().isMet(player, research)) {
                messages.add(registered.lockedMessage());
            }
        }
        return messages;
    }

    /**
     * 测试与调试用：清空全部注册。
     */
    public static void clear() {
        CONDITIONS.clear();
    }

    /**
     * 已注册条件的条目数量，用于状态输出。
     */
    public static int entryCount() {
        return CONDITIONS.size();
    }
}
