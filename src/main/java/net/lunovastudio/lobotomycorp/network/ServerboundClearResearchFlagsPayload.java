package net.lunovastudio.lobotomycorp.network;

import net.lunovastudio.lobotomycorp.LCIds;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearch;
import net.lunovastudio.lobotomycorp.api.capability.PlayerResearchAccess;
import net.lunovastudio.lobotomycorp.api.capability.ResearchFlag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端请求清除某个条目的界面标记（玩家已经读过条目详情时）。
 */
public record ServerboundClearResearchFlagsPayload(ResourceLocation entry) implements CustomPacketPayload {

    public static final Type<ServerboundClearResearchFlagsPayload> TYPE =
            new Type<>(LCIds.rl("clear_research_flags"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundClearResearchFlagsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    ServerboundClearResearchFlagsPayload::entry,
                    ServerboundClearResearchFlagsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundClearResearchFlagsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PlayerResearch knowledge = PlayerResearchAccess.of(player);
                boolean changed = knowledge.removeFlag(payload.entry(), ResearchFlag.PAGE);
                changed |= knowledge.removeFlag(payload.entry(), ResearchFlag.POPUP);
                if (changed) {
                    knowledge.queueSync(player);
                }
            }
        });
    }
}
