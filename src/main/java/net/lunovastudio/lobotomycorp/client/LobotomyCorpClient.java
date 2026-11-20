package net.lunovastudio.lobotomycorp.client;

import net.lunovastudio.lobotomycorp.LCIds;
import net.lunovastudio.lobotomycorp.client.input.LCKeyMappings;
import net.lunovastudio.lobotomycorp.client.tooltip.EgoWeaponTooltipHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * 客户端入口：按键、tooltip 与配置界面。
 */
@Mod(value = LCIds.MODID, dist = Dist.CLIENT)
public final class LobotomyCorpClient {
    public LobotomyCorpClient(ModContainer container, IEventBus modBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        NeoForge.EVENT_BUS.register(EgoWeaponTooltipHandler.class);

        modBus.addListener(LCKeyMappings::register);
        NeoForge.EVENT_BUS.addListener(LCKeyMappings::onClientTick);
    }
}
