package net.lunovastudio.lobotomycorp.api.research;

import net.lunovastudio.lobotomycorp.api.capability.PlayerResearch;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeAccess;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * 条目解锁的附加条件。
 *
 * <p>通过 {@link ResearchUnlockConditions#require} 注册；同一条目的所有条件都必须通过。</p>
 */
@FunctionalInterface
public interface UnlockRequirement {
    /**
     * 判断条件是否满足。
     *
     * @param player   玩家
     * @param research 玩家研究进度
     * @return 满足时为 true
     */
    boolean isMet(Player player, PlayerResearch research);

    /** 要求某个研究条目已完成。 */
    static UnlockRequirement completed(ResourceLocation entry) {
        return (player, research) -> research.hasCompleted(entry);
    }

    /** 要求某个研究条目至少推进到给定阶段。 */
    static UnlockRequirement reachedStage(ResourceLocation entry, int stage) {
        return (player, research) -> research.hasReachedStage(entry, stage);
    }

    /** 要求某分类的某类知识达到给定等级。 */
    static UnlockRequirement knowledgeLevel(KnowledgeType type,
                                           ResourceKey<IResearchCategory> category,
                                           int level) {
        return (player, research) -> KnowledgeAccess.level(player, type, category) >= level;
    }

    /** 玩家等级达到给定值。 */
    static UnlockRequirement employeeGrade(int grade) {
        return (player, research) ->
                net.lunovastudio.lobotomycorp.api.capability.PlayerCareerAccess.of(player).employeeGrade() >= grade;
    }

    /** 与另一个条件同时成立。 */
    default UnlockRequirement and(UnlockRequirement other) {
        return (player, research) -> isMet(player, research) && other.isMet(player, research);
    }
}
