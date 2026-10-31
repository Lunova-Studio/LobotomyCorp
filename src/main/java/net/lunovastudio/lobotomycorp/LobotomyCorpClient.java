package net.lunovastudio.lobotomycorp;

import net.lunovastudio.lobotomycorp.client.gui.handlers.EgoWeaponTooltipHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = LobotomyCorp.MODID, dist = Dist.CLIENT)
public class LobotomyCorpClient {
    public LobotomyCorpClient(ModContainer container, IEventBus modBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        NeoForge.EVENT_BUS.register(EgoWeaponTooltipHandler.class);

        modBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        LobotomyCorp.LOGGER.info("HELLO FROM CLIENT SETUP");
        LobotomyCorp.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
