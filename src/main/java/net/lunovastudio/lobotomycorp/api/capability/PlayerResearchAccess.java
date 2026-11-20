package net.lunovastudio.lobotomycorp.api.capability;

import net.lunovastudio.lobotomycorp.api.ApiBinding;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

/**
 * 取玩家 {@link PlayerResearch} 的静态入口。
 *
 * <p>模组在构造期绑定实现，因此附属模组可以在之后任意事件里直接调用 {@link #of}，
 * 而不需要依赖本模组的附件注册表。</p>
 */
public final class PlayerResearchAccess {
    private static final ApiBinding<Function<Player, PlayerResearch>> BINDING =
            new ApiBinding<>("PlayerResearchAccess");

    private PlayerResearchAccess() {
    }

    /**
     * 取玩家的研究进度，玩家还没有进度时返回空进度。
     *
     * @param player 客户端或服务端玩家
     * @return 玩家进度，永不为 null
     * @throws IllegalStateException 在模组绑定实现之前调用时
     */
    public static PlayerResearch of(Player player) {
        return BINDING.get().apply(player);
    }

    /**
     * 安装实现。本模组在构造期调用一次，附属模组不应调用。
     */
    public static void bind(Function<Player, PlayerResearch> impl) {
        BINDING.bind(impl);
    }
}
