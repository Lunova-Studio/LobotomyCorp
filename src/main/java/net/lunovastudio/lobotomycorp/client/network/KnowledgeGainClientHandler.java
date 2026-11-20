package net.lunovastudio.lobotomycorp.client.network;

import net.lunovastudio.lobotomycorp.client.hud.KnowledgeGainOverlay;
import net.lunovastudio.lobotomycorp.network.ClientboundKnowledgeGainPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端处理知识点获得提示。
 */
public final class KnowledgeGainClientHandler {
    private static final int BASE_DURATION_TICKS = 40;
    private static final int EXTRA_DURATION_SPREAD = 20;

    private KnowledgeGainClientHandler() {
    }

    public static void handle(ClientboundKnowledgeGainPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null || minecraft.level == null) {
                return;
            }
            for (int point = 0; point < payload.count(); point++) {
                KnowledgeGainOverlay.addTracker(
                        payload.knowledgeType(),
                        payload.category().orElse(null),
                        BASE_DURATION_TICKS + minecraft.level.getRandom().nextInt(EXTRA_DURATION_SPREAD),
                        minecraft.level.getRandom().nextLong());
            }
        });
    }
}
