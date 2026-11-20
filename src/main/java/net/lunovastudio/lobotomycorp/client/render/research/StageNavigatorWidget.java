package net.lunovastudio.lobotomycorp.client.render.research;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * 条目详情的阶段翻页控件。
 */
public final class StageNavigatorWidget {
    public static final int WIDTH = 12;
    public static final int HEIGHT = 12;
    private static final int GAP = 6;

    private StageNavigatorWidget() {
    }

    /**
     * 画左右箭头与“当前/总数”文本。返回控件占用的总宽度。
     */
    public static int draw(GuiGraphics graphics, Font font, int centerX, int y, int page, int total,
                           int color, boolean hoverLeft, boolean hoverRight) {
        int textWidth = font.width(pageLabel(page, total));
        int totalWidth = WIDTH * 2 + GAP * 2 + textWidth;
        int left = centerX - totalWidth / 2;
        drawArrow(graphics, font, left, y, false, hoverLeft ? 0xFFFFFFFF : color);
        graphics.drawString(font, pageLabel(page, total),
                left + WIDTH + GAP, y + 2, color, false);
        drawArrow(graphics, font, left + WIDTH + GAP + textWidth + GAP, y, true, hoverRight ? 0xFFFFFFFF : color);
        return totalWidth;
    }

    public static boolean hitLeft(double mouseX, double mouseY, int centerX, Font font, int y, int page, int total) {
        int textWidth = font.width(pageLabel(page, total));
        int totalWidth = WIDTH * 2 + GAP * 2 + textWidth;
        int left = centerX - totalWidth / 2;
        return inside(mouseX, mouseY, left, y);
    }

    public static boolean hitRight(double mouseX, double mouseY, int centerX, Font font, int y, int page, int total) {
        int textWidth = font.width(pageLabel(page, total));
        int totalWidth = WIDTH * 2 + GAP * 2 + textWidth;
        int left = centerX - totalWidth / 2 + WIDTH + GAP + textWidth + GAP;
        return inside(mouseX, mouseY, left, y);
    }

    public static Component pageLabel(int page, int total) {
        return Component.literal((page + 1) + "/" + Math.max(1, total));
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEIGHT;
    }

    private static void drawArrow(GuiGraphics graphics, Font font, int x, int y, boolean right, int color) {
        String glyph = right ? ">" : "<";
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0x60000000);
        graphics.drawCenteredString(font, glyph, x + WIDTH / 2, y + 2, color);
    }
}
