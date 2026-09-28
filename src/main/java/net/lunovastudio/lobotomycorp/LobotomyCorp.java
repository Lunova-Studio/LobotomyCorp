package net.lunovastudio.lobotomycorp;

import net.lunovastudio.lobotomycorp.common.attachments.ModAttachments;
import net.lunovastudio.lobotomycorp.common.attributes.ModAttributes;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(LobotomyCorp.MODID)
public class LobotomyCorp {
    public static final String MODID = "lobotomycorp";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LobotomyCorp(IEventBus bus) {
        ModAttachments.register(bus);
        ModAttributes.register(bus);

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}
