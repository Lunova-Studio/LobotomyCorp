package net.lunovastudio.lobotomycorp.api.capability;

import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

/**
 * 单个玩家的研究进度：已学会的条目、每条目所处的阶段、界面标记，以及按分类累计的知识点。
 *
 * <p>通过 {@link PlayerResearchAccess#of} 获取。服务端持有权威副本，随玩家存档并在死亡后保留；
 * 客户端在登录、切换维度以及服务端 {@link #queueSync} 后收到副本，其他玩家永远收不到。</p>
 *
 * <p>会改变进度的方法在真正产生变化时返回 {@code true}。修改不会自动广播：
 * 服务端改动后由调用方调用 {@link #queueSync}。</p>
 */
public interface PlayerResearch {
    /**
     * 查询某个条目的状态。
     *
     * @param entry 研究条目 id
     * @return 状态；未学会时返回 {@link ResearchStatus#UNKNOWN}
     */
    ResearchStatus status(ResourceLocation entry);

    /**
     * 玩家是否学会了某个条目，不论阶段。
     */
    boolean knows(ResourceLocation entry);

    /**
     * 玩家是否完成了某个条目。
     */
    boolean hasCompleted(ResourceLocation entry);

    /**
     * 玩家是否学会了某个条目并推进到至少给定阶段。
     *
     * @param entry 研究条目 id
     * @param stage 通过所需的最低阶段
     */
    boolean hasReachedStage(ResourceLocation entry, int stage);

    /**
     * 玩家在某个条目上到达的阶段。
     *
     * @return 已学会但未设置阶段时为 {@code 0}；未学会时为 {@code -1}
     */
    int stageOf(ResourceLocation entry);

    /**
     * 玩家学会的全部条目（含已完成）。
     *
     * @return 只读的实时视图
     */
    Set<ResourceLocation> knownEntries();

    /**
     * 让玩家学会某个条目，但不完成它、也不设置阶段。绕过父条目与解锁条件，不触发研究事件。
     *
     * @return 之前没学会时为 {@code true}
     */
    boolean learn(ResourceLocation entry);

    /**
     * 设置已学会条目的阶段。
     *
     * @param stage 新阶段，必须为正数
     * @return 存储的阶段确实变化时为 {@code true}
     */
    boolean setStage(ResourceLocation entry, int stage);

    /**
     * 从玩家记录中移除条目及其阶段、完成状态与标记。
     */
    boolean forget(ResourceLocation entry);

    /**
     * 查询条目的界面标记。
     */
    boolean hasFlag(ResourceLocation entry, ResearchFlag flag);

    /**
     * 设置界面标记。条目不需要已学会。
     */
    boolean addFlag(ResourceLocation entry, ResearchFlag flag);

    /**
     * 清除界面标记。
     */
    boolean removeFlag(ResourceLocation entry, ResearchFlag flag);

    /**
     * 某一分类下某类知识的等级（点数除以 {@link KnowledgeType#progression()} 向下取整）。
     */
    int knowledgeLevel(KnowledgeType type, ResourceKey<IResearchCategory> category);

    /**
     * 某一分类下某类知识的原始点数。
     */
    int knowledgePoints(KnowledgeType type, ResourceKey<IResearchCategory> category);

    /**
     * 增减某一分类下的原始点数。负数会扣点；点数不会低于零。不会触发获得提示。
     *
     * @return 存储的点数确实变化时为 {@code true}
     */
    boolean addKnowledgePoints(KnowledgeType type, ResourceKey<IResearchCategory> category, int amount);

    /**
     * 清空全部进度：条目、阶段、完成状态、标记与知识点。
     */
    void wipe();

    /**
     * 安排把本进度同步给拥有者。同一 tick 内多次调用只会发一次。
     *
     * @param owner 进度所属玩家
     */
    void queueSync(ServerPlayer owner);
}
