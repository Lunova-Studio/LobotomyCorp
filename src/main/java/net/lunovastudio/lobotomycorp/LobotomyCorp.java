package net.lunovastudio.lobotomycorp;

import net.lunovastudio.lobotomycorp.attachments.ModAttachments;
import net.lunovastudio.lobotomycorp.commands.PsychologicalCommand;
import net.lunovastudio.lobotomycorp.entity.ModEntities;
import net.lunovastudio.lobotomycorp.items.ModItemGroups;
import net.lunovastudio.lobotomycorp.items.ModItems;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(LobotomyCorp.MODID)
public class LobotomyCorp {
    public static final String MODID = "lobotomycorp";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LobotomyCorp(IEventBus bus) {
        ModItems.register(bus);
        ModItemGroups.register(bus);
        ModAttachments.register(bus);
        ModEntities.register(bus);

        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        bus.addListener(LobotomyCorp::registerEntityAttributes);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    private static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        ModEntities.registerAttributes(event);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        PsychologicalCommand.register(event.getDispatcher());
    }
}
