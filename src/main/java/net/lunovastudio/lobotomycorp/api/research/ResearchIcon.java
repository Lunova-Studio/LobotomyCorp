package net.lunovastudio.lobotomycorp.api.research;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

/**
 * 研究条目节点上显示的图标。序列化形式是一个字符串：
 * 以 {@code .png} 结尾的是贴图路径，其余按物品 id 解析。
 *
 * <p>一个条目可以声明多个图标，界面会按固定节奏轮播。</p>
 */
public record ResearchIcon(ResourceLocation id, Kind kind) {
    public static final Codec<ResearchIcon> CODEC = Codec.STRING.xmap(ResearchIcon::parse, ResearchIcon::serialize);

    /** 图标 id 的解析方式。 */
    public enum Kind {
        /** {@link ResearchIcon#id()} 是物品 id。 */
        ITEM,
        /** {@link ResearchIcon#id()} 是完整贴图路径。 */
        TEXTURE
    }

    public static ResearchIcon ofTexture(ResourceLocation id) {
        return new ResearchIcon(id, Kind.TEXTURE);
    }

    public static ResearchIcon ofItem(ResourceLocation id) {
        return new ResearchIcon(id, Kind.ITEM);
    }

    public boolean texture() {
        return kind == Kind.TEXTURE;
    }

    private static ResearchIcon parse(String value) {
        ResourceLocation id = ResourceLocation.parse(value);
        return new ResearchIcon(id, id.getPath().endsWith(".png") ? Kind.TEXTURE : Kind.ITEM);
    }

    private static String serialize(ResearchIcon icon) {
        return icon.id.toString();
    }
}
