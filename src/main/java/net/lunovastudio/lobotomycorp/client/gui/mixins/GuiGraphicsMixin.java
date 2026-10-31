package net.lunovastudio.lobotomycorp.client.gui.mixins;

import net.lunovastudio.lobotomycorp.client.gui.EgoWeaponTooltip;
import net.lunovastudio.lobotomycorp.common.items.ego.weapon.EgoWeaponItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 让 EGO 武器 tooltip 按“背景尺寸”参与屏幕定位，并把文字/图片向右下移动。
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Shadow
    private ItemStack tooltipStack;

    private boolean lobotomycorp$isEgoWeaponTooltip() {
        return this.tooltipStack.getItem() instanceof EgoWeaponItem;
    }

    /**
     * 原版用文字宽高定位 tooltip。这里加上背景内边距，避免背景被屏幕边缘裁切。
     */
    @Redirect(
        method = "renderTooltipInternal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;"
        )
    )
    private Vector2ic lobotomycorp$positionEgoWeaponTooltip(
        ClientTooltipPositioner positioner,
        int screenWidth,
        int screenHeight,
        int mouseX,
        int mouseY,
        int width,
        int height
    ) {
        if (this.lobotomycorp$isEgoWeaponTooltip()) {
            return positioner.positionTooltip(
                screenWidth,
                screenHeight,
                mouseX,
                mouseY,
                width + EgoWeaponTooltip.PADDING_WIDTH,
                height + EgoWeaponTooltip.PADDING_HEIGHT
            );
        }

        return positioner.positionTooltip(screenWidth, screenHeight, mouseX, mouseY, width, height);
    }

    /**
     * 文字向右下偏移，落在背景的内边距内。
     */
    @Redirect(
        method = "renderTooltipInternal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;renderText(Lnet/minecraft/client/gui/Font;IILorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"
        )
    )
    private void lobotomycorp$offsetEgoWeaponText(
        ClientTooltipComponent component,
        Font font,
        int x,
        int y,
        Matrix4f matrix,
        MultiBufferSource.BufferSource bufferSource
    ) {
        if (this.lobotomycorp$isEgoWeaponTooltip()) {
            component.renderText(
                font,
                x + EgoWeaponTooltip.PADDING_LEFT,
                y + EgoWeaponTooltip.PADDING_TOP,
                matrix,
                bufferSource
            );
        } else {
            component.renderText(font, x, y, matrix, bufferSource);
        }
    }

    /**
     * 图片型 tooltip 组件（如物品图标、刷怪蛋）同样偏移。
     */
    @Redirect(
        method = "renderTooltipInternal",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;renderImage(Lnet/minecraft/client/gui/Font;IILnet/minecraft/client/gui/GuiGraphics;)V"
        )
    )
    private void lobotomycorp$offsetEgoWeaponImage(
        ClientTooltipComponent component,
        Font font,
        int x,
        int y,
        GuiGraphics graphics
    ) {
        if (this.lobotomycorp$isEgoWeaponTooltip()) {
            component.renderImage(
                font,
                x + EgoWeaponTooltip.PADDING_LEFT,
                y + EgoWeaponTooltip.PADDING_TOP,
                graphics
            );
        } else {
            component.renderImage(font, x, y, graphics);
        }
    }
}
