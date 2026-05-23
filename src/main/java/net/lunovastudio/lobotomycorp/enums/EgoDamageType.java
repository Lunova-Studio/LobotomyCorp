package net.lunovastudio.lobotomycorp.enums;

public enum EgoDamageType {
    /**
     * 物理伤害
     */
    RED,

    /**
     * 精神伤害
     */
    WHITE,

    /**
     * 侵蚀伤害
     */
    BLACK,

    /**
     * 灵魂伤害
     */
    PALE;

    /**
     * 是否按照生命百分比计算
     */
    public boolean isPercentBased() {
        return this == PALE;
    }
}
