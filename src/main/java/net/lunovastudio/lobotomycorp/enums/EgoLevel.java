package net.lunovastudio.lobotomycorp.enums;

/**
 * E.G.O 装备等级枚举
 */
public enum EgoLevel {
    ZAYIN(1),
    TETH(2),
    HE(3),
    WAW(4),
    ALEPH(5);

    private final int level;

    EgoLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}