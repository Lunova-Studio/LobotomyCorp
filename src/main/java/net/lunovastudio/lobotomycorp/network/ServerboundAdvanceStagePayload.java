package net.lunovastudio.lobotomycorp.network;

import net.lunovastudio.lobotomycorp.LCIds;
import net.lunovastudio.lobotomycorp.content.research.ResearchManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端请求推进某个条目的当前阶段。
 */
public record ServerboundAdvanceStagePayload(ResourceLocation entry) implements CustomPacketPayload {

    public static final Type<ServerboundAdvanceStagePayload> TYPE =
            new Type<>(LCIds.rl("advance_stage"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundAdvanceStagePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    ServerboundAdvanceStagePayload::entry,
                    ServerboundAdvanceStagePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundAdvanceStagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResearchManager.advanceStage(player, payload.entry());
            }
        });
    }
}
