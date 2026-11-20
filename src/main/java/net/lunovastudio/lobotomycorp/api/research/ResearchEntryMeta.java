package net.lunovastudio.lobotomycorp.api.research;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * 条目级元标记，影响研究界面的显示与自动解锁行为。
 */
public enum ResearchEntryMeta implements StringRepresentable {
    /** 未解锁前不出现在所在分类的条目网格里。 */
    HIDDEN("hidden"),
    /** 玩家加载存档时自动加入记录（仍需通过解锁条件）。 */
    AUTOUNLOCK("autounlock");

    public static final Codec<ResearchEntryMeta> CODEC = StringRepresentable.fromEnum(ResearchEntryMeta::values);

    private final String name;

    ResearchEntryMeta(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
