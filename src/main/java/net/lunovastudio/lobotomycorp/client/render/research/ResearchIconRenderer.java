package net.lunovastudio.lobotomycorp.client.render.research;

import net.lunovastudio.lobotomycorp.api.research.ResearchIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 研究图标绘制：物品图标按物品渲染，贴图图标按 32×32 原图整体缩放。
 *
 * <p>本模组的研究贴图约定为 32×32（现有部门与项目图标都是这个尺寸）。</p>
 */
public final class ResearchIconRenderer {
    public static final int NATIVE_TEXTURE_SIZE = 32;

    private ResearchIconRenderer() {
    }

    /**
     * 在指定位置画一个图标。
     *
     * @param size 目标边长
     */
    public static void draw(GuiGraphics graphics, ResearchIcon icon, int x, int y, int size) {
        if (icon == null) {
            return;
        }
        switch (icon.kind()) {
            case TEXTURE -> drawTexture(graphics, icon.id(), x, y, size);
            case ITEM -> drawItem(graphics, icon.id(), x, y, size);
        }
    }

    /**
     * 画一个贴图图标。
     */
    public static void drawTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int size) {
        if (size <= 0) {
            return;
        }
        graphics.blit(texture, x, y, size, size, 0.0F, 0.0F,
                NATIVE_TEXTURE_SIZE, NATIVE_TEXTURE_SIZE, NATIVE_TEXTURE_SIZE, NATIVE_TEXTURE_SIZE);
    }

    /**
     * 画一个物品图标。
     */
    public static void drawItem(GuiGraphics graphics, ResourceLocation itemId, int x, int y, int size) {
        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        ItemStack stack = new ItemStack(item);
        if (size == 16) {
            graphics.renderItem(stack, x, y);
            return;
        }
        float scale = size / 16.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }
}
