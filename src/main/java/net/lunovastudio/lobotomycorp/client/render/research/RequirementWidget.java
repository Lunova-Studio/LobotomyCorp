package net.lunovastudio.lobotomycorp.client.render.research;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * 条目详情里的“要求行”绘制。
 */
public final class RequirementWidget {
    private static final int LINE_HEIGHT = 10;

    private RequirementWidget() {
    }

    /**
     * 画一行要求文本，返回下一行的 y 坐标。
     *
     * @param bullet 行首符号，可为空
     */
    public static int drawLine(GuiGraphics graphics, Font font, int x, int y, int maxWidth,
                               String bullet, Component text, int color) {
        String prefix = bullet == null || bullet.isEmpty() ? "" : bullet + " ";
        Component line = Component.literal(prefix).append(text);
        for (var part : font.split(line, maxWidth)) {
            graphics.drawString(font, part, x, y, color, false);
            y += LINE_HEIGHT;
        }
        return y;
    }

    /**
     * 行高。
     */
    public static int lineHeight() {
        return LINE_HEIGHT;
    }
}
