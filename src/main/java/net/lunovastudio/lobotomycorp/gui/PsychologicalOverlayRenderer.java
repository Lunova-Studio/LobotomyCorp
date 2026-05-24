package net.lunovastudio.lobotomycorp.gui;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.attachments.ModAttachments;
import net.lunovastudio.lobotomycorp.attachments.datas.PsychologicalData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = LobotomyCorp.MODID, value = Dist.CLIENT)
public class PsychologicalOverlayRenderer {
    public static ResourceLocation THIRST_ICONS =
            ResourceLocation.fromNamespaceAndPath(LobotomyCorp.MODID, "textures/gui/icons.png");

    private static int updateCounter;

    private static final RandomSource random = RandomSource.create();

    private static final Minecraft minecraft = Minecraft.getInstance();

    /**
     * 在客户端 Tick 结束时更新计数器（替代 gui.getGuiTicks()）psychological
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!minecraft.isPaused()) {
            updateCounter++;
        }
    }

    /**
     * 注册 HUD 层
     */
    @SubscribeEvent
    public static void registerThirstOverlay(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.AIR_LEVEL,
                ResourceLocation.fromNamespaceAndPath(LobotomyCorp.MODID, "psychological_id"),
                (guiGraphics, deltaTracker) -> {
                    onRenderPsychological(guiGraphics);
                }
        );
    }

    /**
     * 渲染逻辑
     */
    private static void onRenderPsychological(GuiGraphics guiGraphics) {
        if (minecraft.player == null) return;

        // 骑乘生物时隐藏
        boolean isMounted = minecraft.player.getVehicle() instanceof LivingEntity;
        if (isMounted || minecraft.options.hideGui || !minecraft.gameMode.getPlayerMode().isSurvival())
            return;

        PsychologicalData psychologicalData = minecraft.player.getData(ModAttachments.PSYCHOLOGICAL.get());

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        // 计算位置（右侧状态栏区域）
        int left = screenWidth / 2 + 91;
        int top = screenHeight - minecraft.gui.rightHeight;

        int level = psychologicalData.psychological();

        for (int i = 0; i < 10; ++i) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 10;
            int y = top;

            if (updateCounter % (level * 3 + 1) == 0) {
                y = top + (random.nextInt(3) - 1);
            }

            guiGraphics.blit(THIRST_ICONS, x, y, 22, 0, 10, 9);

            if (idx < level) {
                guiGraphics.blit(THIRST_ICONS, x, y, 0, 0, 10, 9);
            } else if (idx == level) {
                guiGraphics.blit(THIRST_ICONS, x, y, 11, 0, 10, 9);
            }
        }

        minecraft.gui.rightHeight += 10;
    }
}
