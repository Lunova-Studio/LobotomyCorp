package net.lunovastudio.lobotomycorp.api.research;

import net.lunovastudio.lobotomycorp.api.capability.PlayerResearch;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * 阶段级附加条件。
 *
 * <p>阶段默认只检查数据里声明的收集/合成/前置研究/知识点数；需要额外规则时
 * （例如本项目后续自建体系里的资源消耗、理智代价）注册 {@code StageRequirement} 即可，
 * 具体见 {@link ResearchStageRequirements}。</p>
 */
public interface StageRequirement {
    /**
     * 判断条件是否满足。
     *
     * @param player     玩家
     * @param research   玩家研究进度
     * @param entry      条目 id
     * @param stageIndex 阶段下标
     * @return 满足时为 true
     */
    boolean isMet(Player player, PlayerResearch research, net.minecraft.resources.ResourceLocation entry, int stageIndex);

    /**
     * 阶段推进成功后扣除代价。默认不做任何事。
     */
    default void consume(ServerPlayer player, net.minecraft.resources.ResourceLocation entry, int stageIndex) {
    }

    /**
     * 条件不满足时显示给玩家的提示。返回 null 表示不显示。
     */
    default @Nullable Component lockedMessage() {
        return null;
    }
}
