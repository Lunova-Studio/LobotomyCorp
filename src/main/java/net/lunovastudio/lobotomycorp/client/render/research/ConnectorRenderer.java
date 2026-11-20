package net.lunovastudio.lobotomycorp.client.render.research;

import net.minecraft.client.gui.GuiGraphics;

/**
 * 部门之间的连线绘制。
 */
public final class ConnectorRenderer {
    private static final int UNLOCKED = 0xC0BFBFBF;
    private static final int LOCKED = 0x905A5A5A;

    private ConnectorRenderer() {
    }

    /**
     * 两个部门节点之间的连线。
     */
    public static void drawLink(GuiGraphics graphics, int x1, int y1, int x2, int y2, boolean unlocked) {
        ChamferGeometry.line(graphics, x1, y1, x2, y2, unlocked ? UNLOCKED : LOCKED);
    }

    /**
     * 条目之间的父级连线（部门内网格使用）。
     */
    public static void drawParentLink(GuiGraphics graphics, int x1, int y1, int x2, int y2, boolean satisfied) {
        ChamferGeometry.line(graphics, x1, y1, x2, y2, satisfied ? UNLOCKED : LOCKED);
    }
}
