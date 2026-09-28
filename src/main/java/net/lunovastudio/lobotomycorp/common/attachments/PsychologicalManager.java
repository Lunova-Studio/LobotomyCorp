package net.lunovastudio.lobotomycorp.common.attachments;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.common.attachments.datas.PsychologicalData;
import net.lunovastudio.lobotomycorp.common.attributes.ModAttributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/*
 *  玩家理智管理器
 */
@EventBusSubscriber(modid = LobotomyCorp.MODID)
public class PsychologicalManager {
    @SubscribeEvent
    public static void onPlayerChangedGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        var data = event.getEntity().getData(ModAttachments.PSYCHOLOGICAL);
        LobotomyCorp.LOGGER.debug("player PSYCHOLOGICAL is <{}>", data.psychological());
    }

    public static int getMaxValue(@NotNull Player player) {
        var result = player.getAttribute(ModAttributes.MAX_PSYCHOLOGICAL);
        if (result != null)
            return (int)result.getValue();

        return -1;
    }

    public static int getCurrentValue(@NotNull Player player) {
        var result = player.getData(ModAttachments.PSYCHOLOGICAL);
        return result.psychological();
    }

    public static void reset(@NotNull Player player) {
        player.setData(ModAttachments.PSYCHOLOGICAL, new PsychologicalData(PsychologicalData.MAX_PSYCHOLOGICAL));
    }

    public static void update(@NotNull Player player, int value) {
        player.setData(ModAttachments.PSYCHOLOGICAL, new PsychologicalData(value));
    }
}