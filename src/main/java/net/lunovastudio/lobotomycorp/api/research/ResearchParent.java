package net.lunovastudio.lobotomycorp.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearch;
import net.minecraft.resources.ResourceLocation;

/**
 * 条目对父条目的引用。
 *
 * <p>序列化形式是单个字符串：{@code "namespace:path"} 要求父条目完全完成，
 * {@code "namespace:path@2"} 要求父条目至少推进到阶段 2（从 1 开始，{@code @1} 表示已开始），
 * 前缀 {@code "~"} 表示只作为进度要求、界面不画连接线。</p>
 *
 * @param id      父条目的 id
 * @param stage   需要父条目到达的最低阶段，0 表示要求完全完成
 * @param inherit 是否隐藏界面连接线
 */
public record ResearchParent(ResourceLocation id, int stage, boolean inherit) {
    public static final Codec<ResearchParent> CODEC =
            Codec.STRING.comapFlatMap(ResearchParent::parse, ResearchParent::serialize);

    /** 创建一个要求父条目完全完成的引用。 */
    public static ResearchParent of(ResourceLocation id) {
        return new ResearchParent(id, 0, false);
    }

    /**
     * 判断给定研究进度是否满足本引用。带阶段的引用也可以被“完全完成”满足。
     */
    public boolean isSatisfiedBy(PlayerResearch knowledge) {
        if (knowledge.hasCompleted(id)) {
            return true;
        }
        return stage > 0 && knowledge.hasReachedStage(id, stage - 1);
    }

    private static DataResult<ResearchParent> parse(String value) {
        boolean inherit = value.startsWith("~");
        String body = inherit ? value.substring(1) : value;
        int at = body.lastIndexOf('@');
        int stage = 0;
        if (at >= 0) {
            try {
                stage = Integer.parseInt(body.substring(at + 1));
            } catch (NumberFormatException e) {
                return DataResult.error(() -> "Invalid parent stage in '" + value + "'");
            }
            body = body.substring(0, at);
        }
        ResourceLocation id = ResourceLocation.tryParse(body);
        if (id == null) {
            return DataResult.error(() -> "Invalid parent identifier '" + value + "'");
        }
        return DataResult.success(new ResearchParent(id, stage, inherit));
    }

    private String serialize() {
        return (inherit ? "~" : "") + id + (stage > 0 ? "@" + stage : "");
    }
}
