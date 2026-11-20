package net.lunovastudio.lobotomycorp.content.research;

import net.lunovastudio.lobotomycorp.LCIds;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;

/**
 * 玩家登录/重生/换维度时的研究进度维持。
 */
@EventBusSubscriber(modid = LCIds.MODID)
public final class ResearchLifecycleEvents {
    private ResearchLifecycleEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResearchManager.applyAutoUnlock(player);
            ResearchRecipeLocks.rebuild(player.server);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResearchManager.applyAutoUnlock(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResearchManager.record(player).queueSync(player);
        }
    }
}
