package net.lunovastudio.lobotomycorp.api.capability;

/**
 * 某个玩家对某个研究条目的状态。
 */
public enum ResearchStatus {
    /** 玩家尚未解锁该条目。 */
    UNKNOWN,
    /** 玩家已解锁该条目但还没走完所有阶段。 */
    IN_PROGRESS,
    /** 玩家已解锁该条目并完成了所有阶段。 */
    COMPLETE
}
