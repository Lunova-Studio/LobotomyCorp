package net.lunovastudio.lobotomycorp.content.research;

import net.lunovastudio.lobotomycorp.LCIds;
import net.lunovastudio.lobotomycorp.registry.LCAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 把“待同步”标记在玩家 tick 末尾兑现，保证同一 tick 内多次改动只发一个包。
 */
@EventBusSubscriber(modid = LCIds.MODID)
public final class ResearchSyncEvents {
    private ResearchSyncEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.getData(LCAttachments.knowledge()).takeSyncPending()) {
            player.syncData(LCAttachments.knowledge());
        }
    }
}
