package net.lunovastudio.lobotomycorp.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * EGO 武器 tooltip 的尺寸与背景绘制。
 *
 * <p>背景使用 {@code textures/gui/sprites/tooltip/weapon.png} 九宫格贴图，
 * 文字与背景之间通过 {@link #PADDING_LEFT} 等内边距保持距离。</p>
 */
public final class EgoWeaponTooltip {
    public static final ResourceLocation BACKGROUND =
        ResourceLocation.fromNamespaceAndPath(LobotomyCorp.MODID, "tooltip/weapon");

    /**
     * 背景贴图的设计尺寸。tooltip 比该尺寸窄时，会按比例整体缩小背景，
     * 避免右下角装饰相对 tooltip 显得过大。
     */
    public static final float BACKGROUND_NATIVE_SIZE = 128.0F;

    public static final int PADDING_LEFT = 8;
    public static final int PADDING_TOP = 8;
    public static final int PADDING_RIGHT = 14;
    public static final int PADDING_BOTTOM = 8;

    public static final int PADDING_WIDTH = PADDING_LEFT + PADDING_RIGHT;
    public static final int PADDING_HEIGHT = PADDING_TOP + PADDING_BOTTOM;

    private EgoWeaponTooltip() {
    }

    /**
     * 绘制 tooltip 背景。传入的坐标是背景左上角，尺寸包含内边距。
     *
     * <p>背景不是简单拉伸到目标尺寸，而是保持贴图比例：
     * 当 tooltip 宽度小于原图宽度时，整个九宫格背景按同一比例缩小，
     * 右下角条纹、左上斜角、右上图标都会保持原始比例。</p>
     */
    public static void renderBackground(@NotNull GuiGraphics graphics, int x, int y, int width, int height) {
        float scale = Math.min(1.0F, Math.max(1, width) / BACKGROUND_NATIVE_SIZE);
        int scaledWidth = Math.max(1, (int) Math.ceil(width / scale));
        int scaledHeight = Math.max(1, (int) Math.ceil(height / scale));

        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.blitSprite(BACKGROUND, 0, 0, scaledWidth, scaledHeight);
        pose.popPose();

        RenderSystem.enableDepthTest();
    }

    /**
     * 与原版 {@code GuiGraphics#renderTooltipInternal} 保持一致的文字宽度计算。
     */
    public static int getBackgroundWidth(@NotNull List<ClientTooltipComponent> components, @NotNull Font font) {
        int width = 0;
        for (ClientTooltipComponent component : components) {
            width = Math.max(width, component.getWidth(font));
        }
        return width + PADDING_WIDTH;
    }

    /**
     * 与原版 {@code GuiGraphics#renderTooltipInternal} 保持一致的文字高度计算。
     */
    public static int getBackgroundHeight(@NotNull List<ClientTooltipComponent> components) {
        int height = components.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent component : components) {
            height += component.getHeight();
        }
        return height + PADDING_HEIGHT;
    }
}
