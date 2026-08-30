package net.lunovastudio.lobotomycorp;

import net.lunovastudio.lobotomycorp.client.model.ModLayerDefinitions;
import net.lunovastudio.lobotomycorp.entity.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = LobotomyCorp.MODID, dist = Dist.CLIENT)
public class LobotomyCorpClient {
    public LobotomyCorpClient(ModContainer container, IEventBus modBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::onRegisterLayerDefinitions);
        modBus.addListener(this::onRegisterRenderers);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        LobotomyCorp.LOGGER.info("HELLO FROM CLIENT SETUP");
        LobotomyCorp.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    private void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ModLayerDefinitions.registerLayerDefinitions(event);
    }

    private void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TEST_ABNORMALITY.get(), PigRenderer::new);
    }
}
