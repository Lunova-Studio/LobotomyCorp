package net.lunovastudio.lobotomycorp.client.render.research;

import net.lunovastudio.lobotomycorp.api.research.ResearchIcon;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

/**
 * 研究节点绘制：生命之树风格的切角方块。
 *
 * <p>节点由四部分组成：部门色描边、深色底、顶部部门色条纹带、居中标题与状态行。
 * 悬停缩放与打开动画由调用方通过 {@code PoseStack} 处理。</p>
 */
public final class EntryNodeRenderer {
    public static final int CUT = 5;
    public static final int BAND_HEIGHT = 6;
    private static final int BACKGROUND = 0xE6101010;
    private static final int ICON_SIZE = 16;
    private static final int ICON_INSET = 6;

    private EntryNodeRenderer() {
    }

    /**
     * 画一个节点。
     *
     * @param color         部门主题色（已乘过透明度）
     * @param title         标题
     * @param subtitle      状态行
     * @param subtitleColor 状态行颜色
     * @param icon          左侧图标，可为 null
     */
    public static void draw(GuiGraphics graphics, Font font, int x, int y, int width, int height,
                            int color, Component title, Component subtitle, int subtitleColor,
                            @Nullable ResearchIcon icon) {
        ChamferGeometry.fillChamferedBordered(graphics, x, y, width, height, CUT,
                ChamferGeometry.multiply(color, 0.85F), BACKGROUND);
        ChamferGeometry.hazardBand(graphics, x + 1, y + 1, width - 2, BAND_HEIGHT, color, 4, 3);

        int contentTop = y + BAND_HEIGHT + 4;
        int textWidth = width - 12;
        int textX = x + 6;
        if (icon != null) {
            ResearchIconRenderer.draw(graphics, icon, x + ICON_INSET, contentTop + 2, ICON_SIZE);
            textX = x + ICON_INSET + ICON_SIZE + 4;
            textWidth = width - (textX - x) - 6;
        }
        int titleY = contentTop + 2;
        graphics.drawCenteredString(font, fit(font, title, textWidth), x + width / 2, titleY, 0xFFFFFFFF);
        if (subtitle != null) {
            graphics.drawCenteredString(font, fit(font, subtitle, textWidth), x + width / 2,
                    titleY + 12, subtitleColor);
        }
    }

    /**
     * 把文本裁剪到给定宽度，超长时以省略号结尾。
     */
    public static Component fit(Font font, Component text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String plain = text.getString();
        String suffix = "...";
        int budget = maxWidth - font.width(suffix);
        if (budget <= 0) {
            return Component.literal(suffix);
        }
        StringBuilder builder = new StringBuilder();
        for (char character : plain.toCharArray()) {
            if (font.width(builder.toString() + character) > budget) {
                break;
            }
            builder.append(character);
        }
        return Component.literal(builder + suffix);
    }
}
