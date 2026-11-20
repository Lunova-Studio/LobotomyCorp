package net.lunovastudio.lobotomycorp.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * 配方上的研究门槛：要求某个条目已完成，或推进到某个阶段。
 *
 * <p>本模组的原版配方不支持附加字段，因此门槛由条目数据里的
 * {@code unlocks_recipes} 反向索引建立，见 {@code content.research.ResearchRecipeLocks}。</p>
 *
 * @param entry  研究条目 id
 * @param stage  要求的最低阶段，空表示要求完全完成
 * @param negate 是否取反
 */
public record ResearchGate(ResourceLocation entry, Optional<Integer> stage, boolean negate) {
    public static final Codec<ResearchGate> CODEC = RecordCodecBuilder.create(i -> i.group(
                    ResourceLocation.CODEC.fieldOf("entry").forGetter(ResearchGate::entry),
                    Codec.INT.optionalFieldOf("stage").forGetter(ResearchGate::stage),
                    Codec.BOOL.optionalFieldOf("negate", false).forGetter(ResearchGate::negate))
            .apply(i, ResearchGate::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchGate> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            ResearchGate::entry,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
            ResearchGate::stage,
            ByteBufCodecs.BOOL,
            ResearchGate::negate,
            ResearchGate::new);

    private static volatile @Nullable BiPredicate<Player, ResearchGate> binding;

    /**
     * 绑定门槛判定实现。本模组在构造期调用，附属模组不应调用。
     *
     * @param impl 实现，接收的 gate 可能是 null（表示无门槛）
     * @throws IllegalStateException 已经绑定过时
     */
    public static void bind(BiPredicate<Player, ResearchGate> impl) {
        if (binding != null) {
            throw new IllegalStateException("ResearchGate already bound");
        }
        binding = impl;
    }

    /**
     * 判断玩家是否通过给定门槛。
     *
     * @param player 玩家
     * @param gate   门槛，null 表示无门槛
     * @return 通过时为 true；无门槛时恒为 true
     * @throws IllegalStateException 在实现绑定之前调用时
     */
    public static boolean passes(Player player, @Nullable ResearchGate gate) {
        BiPredicate<Player, ResearchGate> current = binding;
        if (current == null) {
            throw new IllegalStateException("ResearchGate accessed before binding");
        }
        return current.test(player, gate);
    }
}
