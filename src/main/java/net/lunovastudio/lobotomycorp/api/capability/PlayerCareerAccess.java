package net.lunovastudio.lobotomycorp.api.capability;

import net.lunovastudio.lobotomycorp.api.ApiBinding;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

/**
 * 取玩家 {@link PlayerCareer} 的静态入口。
 */
public final class PlayerCareerAccess {
    private static final ApiBinding<Function<Player, PlayerCareer>> BINDING =
            new ApiBinding<>("PlayerCareerAccess");

    private PlayerCareerAccess() {
    }

    /**
     * @param player 客户端或服务端玩家
     * @return 玩家档案，永不为 null
     * @throws IllegalStateException 在模组绑定实现之前调用时
     */
    public static PlayerCareer of(Player player) {
        return BINDING.get().apply(player);
    }

    /**
     * 安装实现。本模组在构造期调用一次，附属模组不应调用。
     */
    public static void bind(Function<Player, PlayerCareer> impl) {
        BINDING.bind(impl);
    }
}
