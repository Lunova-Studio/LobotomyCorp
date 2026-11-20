package net.lunovastudio.lobotomycorp.network;

import net.lunovastudio.lobotomycorp.LCIds;
import net.lunovastudio.lobotomycorp.content.research.ResearchManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端请求解锁某个条目。
 */
public record ServerboundUnlockResearchPayload(ResourceLocation entry) implements CustomPacketPayload {

    public static final Type<ServerboundUnlockResearchPayload> TYPE =
            new Type<>(LCIds.rl("unlock_research"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundUnlockResearchPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    ServerboundUnlockResearchPayload::entry,
                    ServerboundUnlockResearchPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundUnlockResearchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ResearchManager.unlockRequested(player, payload.entry());
            }
        });
    }
}
