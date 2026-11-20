package net.lunovastudio.lobotomycorp.content.psychology;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.registry.LCAttachments;
import net.lunovastudio.lobotomycorp.registry.LCAttributes;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/*
 *  玩家理智管理器
 */
@EventBusSubscriber(modid = LobotomyCorp.MODID)
public class PsychologyManager {
    @SubscribeEvent
    public static void onPlayerChangedGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        var data = event.getEntity().getData(LCAttachments.psychological());
        LobotomyCorp.LOGGER.debug("player PSYCHOLOGICAL is <{}>", data.psychological());
    }

    public static int getMaxValue(@NotNull Player player) {
        var result = player.getAttribute(LCAttributes.MAX_PSYCHOLOGICAL);
        if (result != null)
            return (int)result.getValue();

        return -1;
    }

    public static int getCurrentValue(@NotNull Player player) {
        var result = player.getData(LCAttachments.psychological());
        return result.psychological();
    }

    public static void reset(@NotNull Player player) {
        player.setData(LCAttachments.psychological(), new PsychologicalData(PsychologicalData.MAX_PSYCHOLOGICAL));
    }

    public static void update(@NotNull Player player, int value) {
        player.setData(LCAttachments.psychological(), new PsychologicalData(value));
    }
}