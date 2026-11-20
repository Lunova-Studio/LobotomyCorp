package net.lunovastudio.lobotomycorp.client.render.research;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * 研究界面的切角矩形、条纹带与颜色工具。
 *
 * <p>界面沿用“生命之树”的视觉：深色切角方块 + 部门色警示条纹 + 细灰连线。
 * 全部用 {@link GuiGraphics#fill} 的逐行扫描绘制，不依赖额外贴图。</p>
 */
public final class ChamferGeometry {
    private ChamferGeometry() {
    }

    /**
     * 绘制切角矩形。
     *
     * @param cut 每个角的切边长度
     */
    public static void fillChamfered(GuiGraphics graphics, int x, int y, int width, int height,
                                     int cut, int color) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int effectiveCut = Math.max(0, Math.min(cut, Math.min(width, height) / 2));
        for (int row = 0; row < height; row++) {
            int inset = insetFor(row, height, effectiveCut);
            graphics.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
        }
    }

    /**
     * 绘制带边框的切角矩形。
     */
    public static void fillChamferedBordered(GuiGraphics graphics, int x, int y, int width, int height,
                                             int cut, int borderColor, int fillColor) {
        fillChamfered(graphics, x, y, width, height, cut, borderColor);
        fillChamfered(graphics, x + 1, y + 1, width - 2, height - 2, Math.max(0, cut - 1), fillColor);
    }

    /**
     * 绘制警示条纹带（部门色斜纹），用作节点顶部的色带。
     */
    public static void hazardBand(GuiGraphics graphics, int x, int y, int width, int height,
                                  int color, int stripeWidth, int gap) {
        if (width <= 0 || height <= 0) {
            return;
        }
        graphics.fill(x, y, x + width, y + height, color);
        int shade = multiply(color, 0.55F);
        int period = Math.max(2, stripeWidth + gap);
        int limit = x + width;
        for (int row = 0; row < height; row++) {
            int shift = row * 2;
            for (int offset = -period; offset < width + period; offset += period) {
                int start = x + offset + shift;
                int end = Math.min(limit, start + stripeWidth);
                int begin = Math.max(x, start);
                if (begin < end) {
                    graphics.fill(begin, y + row, end, y + row + 1, shade);
                }
            }
        }
    }

    /**
     * 绘制 1 像素直线（逐点填充，只用于界面连线）。
     */
    public static void line(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps == 0) {
            graphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            return;
        }
        for (int step = 0; step <= steps; step++) {
            int x = x1 + (x2 - x1) * step / steps;
            int y = y1 + (y2 - y1) * step / steps;
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    /**
     * 给颜色乘一个透明度。
     */
    public static int withAlpha(int color, float alpha) {
        int a = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * ((color >>> 24) & 0xFF));
        return (a << 24) | (color & 0x00FFFFFF);
    }

    /**
     * 按比例调暗颜色。
     */
    public static int multiply(int color, float factor) {
        int a = (color >>> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * 在两个颜色之间插值。
     */
    public static int lerp(int from, int to, float t) {
        float clamped = Mth.clamp(t, 0.0F, 1.0F);
        int a = Mth.lerpInt(clamped, (from >>> 24) & 0xFF, (to >>> 24) & 0xFF);
        int r = Mth.lerpInt(clamped, (from >> 16) & 0xFF, (to >> 16) & 0xFF);
        int g = Mth.lerpInt(clamped, (from >> 8) & 0xFF, (to >> 8) & 0xFF);
        int b = Mth.lerpInt(clamped, from & 0xFF, to & 0xFF);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static float easeOutCubic(float value) {
        float clamped = Mth.clamp(value, 0.0F, 1.0F);
        float inverted = 1.0F - clamped;
        return 1.0F - inverted * inverted * inverted;
    }

    private static int insetFor(int row, int height, int cut) {
        int fromTop = row;
        int fromBottom = height - 1 - row;
        return Math.max(0, cut - Math.min(fromTop, fromBottom));
    }
}
