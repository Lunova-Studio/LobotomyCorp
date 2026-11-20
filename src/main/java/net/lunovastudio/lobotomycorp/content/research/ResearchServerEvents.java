package net.lunovastudio.lobotomycorp.content.research;

import net.lunovastudio.lobotomycorp.LCIds;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * 服务器生命周期钩子：维护配方门控索引。
 */
@EventBusSubscriber(modid = LCIds.MODID)
public final class ResearchServerEvents {
    private ResearchServerEvents() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ResearchRecipeLocks.rebuild(event.getServer());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ResearchRecipeLocks.rebuild(event.getPlayerList().getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ResearchRecipeLocks.clear();
    }
}
