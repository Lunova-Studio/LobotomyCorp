package net.lunovastudio.lobotomycorp.client.gui.handlers;

import net.lunovastudio.lobotomycorp.client.gui.EgoWeaponTooltip;
import net.lunovastudio.lobotomycorp.common.items.ego.weapon.EgoWeaponItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import java.util.List;

/**
 * 为使用 {@link EgoWeaponItem} 的物品替换 tooltip 背景。
 *
 * <p>原版背景由 {@code TooltipRenderUtil} 绘制，这里将它的颜色设为全透明，
 * 再在同一位置绘制 EGO 武器专用的九宫格贴图。</p>
 */
public final class EgoWeaponTooltipHandler {
    private EgoWeaponTooltipHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderTooltipColor(RenderTooltipEvent.Color event) {
        if (!(event.getItemStack().getItem() instanceof EgoWeaponItem)) {
            return;
        }

        // 隐藏原版渐变背景与边框
        event.setBackground(0);
        event.setBorderStart(0);
        event.setBorderEnd(0);

        Font font = event.getFont();
        List<ClientTooltipComponent> components = event.getComponents();
        int width = EgoWeaponTooltip.getBackgroundWidth(components, font);
        int height = EgoWeaponTooltip.getBackgroundHeight(components);

        EgoWeaponTooltip.renderBackground(event.getGraphics(), event.getX(), event.getY(), width, height);
    }
}
