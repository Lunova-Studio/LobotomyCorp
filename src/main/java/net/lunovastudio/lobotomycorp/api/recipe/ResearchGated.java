package net.lunovastudio.lobotomycorp.api.recipe;

import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * 带研究门槛的配方实现接口。
 *
 * <p>本模组的配方都是原版配方类，无法实现本接口；门槛改为由
 * {@code content.research.ResearchRecipeLocks} 反向索引建立。该接口保留给
 * 后续自定义配方类型使用。</p>
 */
public interface ResearchGated {

    Optional<ResearchGate> researchGate();

    default boolean doesPassGate(Player player) {
        return ResearchGate.passes(player, researchGate().orElse(null));
    }
}
