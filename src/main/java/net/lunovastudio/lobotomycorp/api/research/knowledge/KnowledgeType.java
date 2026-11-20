package net.lunovastudio.lobotomycorp.api.research.knowledge;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * 玩家在具体研究条目之外、按分类累计的知识种类。
 *
 * <p>每种知识在分类上保存一个原始计数器，可见等级为
 * {@code 原始点数 / progression()}。示例：每 32 点理论值折算 1 级。</p>
 *
 * <p>这是为本项目后续自建体系预留的接入点：新的知识来源可以沿用这里的两种类型，
 * 也可以通过 {@link KnowledgeAccess} 绑定自己的实现来扩展。</p>
 */
public enum KnowledgeType implements StringRepresentable {
    THEORY("theory", "T", 32),
    OBSERVATION("observation", "O", 16);

    public static final Codec<KnowledgeType> CODEC = StringRepresentable.fromEnum(KnowledgeType::values);

    public static final StreamCodec<ByteBuf, KnowledgeType> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> values()[i], KnowledgeType::ordinal);

    private final String name;
    private final String abbreviation;
    private final int progression;

    KnowledgeType(String name, String abbreviation, int progression) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.progression = progression;
    }

    /** 小写稳定名称，JSON 中的枚举值。 */
    @Override
    public String getSerializedName() {
        return name;
    }

    /** 单字母缩写，HUD 与持久化键使用。 */
    public String abbreviation() {
        return abbreviation;
    }

    /** 把原始点数折算成可见等级的除数，恒为正。 */
    public int progression() {
        return progression;
    }

    /** 显示名的翻译键。 */
    public String translationKey() {
        return "knowledge_type.lobotomycorp." + name;
    }
}
