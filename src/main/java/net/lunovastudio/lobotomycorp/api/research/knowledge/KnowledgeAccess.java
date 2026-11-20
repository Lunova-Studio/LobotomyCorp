package net.lunovastudio.lobotomycorp.api.research.knowledge;

import net.lunovastudio.lobotomycorp.api.ApiBinding;
import net.lunovastudio.lobotomycorp.api.research.IResearchCategory;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * 知识产出的静态入口。
 *
 * <p>默认实现直接读写玩家研究进度里的知识点计数器；为本项目后续的 aspect/知识体系
 * 预留了替换点：绑定自己的 {@link Sink} 即可接管知识点的产出、查询与消耗。</p>
 */
public final class KnowledgeAccess {
    /**
     * 知识后端。
     */
    public interface Sink {
        /**
         * 增加知识点。
         *
         * @param player   目标玩家
         * @param type     知识种类
         * @param category 目标分类，可为 null 表示全局
         * @param amount   增量，可为负
         * @return 点数确实变化时为 true
         */
        boolean grant(ServerPlayer player, KnowledgeType type, @Nullable Holder<IResearchCategory> category, int amount);

        /** 查询原始点数。 */
        int points(Player player, KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category);

        /** 查询可见等级。 */
        int level(Player player, KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category);
    }

    private static final ApiBinding<Sink> BINDING = new ApiBinding<>("KnowledgeAccess");

    private KnowledgeAccess() {
    }

    public static boolean grant(ServerPlayer player, KnowledgeType type,
                                @Nullable Holder<IResearchCategory> category, int amount) {
        return BINDING.get().grant(player, type, category, amount);
    }

    public static int points(Player player, KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category) {
        return BINDING.get().points(player, type, category);
    }

    public static int level(Player player, KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category) {
        return BINDING.get().level(player, type, category);
    }

    /**
     * 安装实现。本模组在构造期调用一次，附属模组不应调用。
     */
    public static void bind(Sink impl) {
        BINDING.bind(impl);
    }

    public static boolean isBound() {
        return BINDING.isBound();
    }
}
