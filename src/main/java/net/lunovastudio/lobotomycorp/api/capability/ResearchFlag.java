package net.lunovastudio.lobotomycorp.api.capability;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * 挂在玩家研究记录上、按条目区分的界面标记。
 *
 * <p>标记与完成度相互独立：一个已完成的条目仍然可以带着 {@link #POPUP}，
 * 直到玩家在界面里看过它。</p>
 */
public enum ResearchFlag implements StringRepresentable {
    /** 玩家已在研究界面里读过这个条目。 */
    PAGE("page"),
    /** 该条目正在进行中。 */
    RESEARCH("research"),
    /** 该条目有待展示的提示。 */
    POPUP("popup");

    public static final Codec<ResearchFlag> CODEC = StringRepresentable.fromEnum(ResearchFlag::values);

    public static final StreamCodec<ByteBuf, ResearchFlag> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> values()[i], ResearchFlag::ordinal);

    private final String name;

    ResearchFlag(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
