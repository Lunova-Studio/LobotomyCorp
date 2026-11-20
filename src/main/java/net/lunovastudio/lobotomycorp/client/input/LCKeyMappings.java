package net.lunovastudio.lobotomycorp.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.lunovastudio.lobotomycorp.client.screen.research.ResearchBrowserScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端按键：{@code H} 打开研究界面。
 */
public final class LCKeyMappings {
    public static final String CATEGORY = "key.categories.lobotomycorp";

    public static final KeyMapping OPEN_RESEARCH = new KeyMapping(
            "key.lobotomycorp.open_research",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY);

    private LCKeyMappings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_RESEARCH);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN_RESEARCH.consumeClick()) {
            if (minecraft.player == null || minecraft.screen != null) {
                continue;
            }
            minecraft.setScreen(ResearchBrowserScreen.open());
        }
    }
}
