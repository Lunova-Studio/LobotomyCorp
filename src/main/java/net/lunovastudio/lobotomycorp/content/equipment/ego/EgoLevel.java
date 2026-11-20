package net.lunovastudio.lobotomycorp.content.equipment.ego;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum EgoLevel {
    ZAYIN(ChatFormatting.GREEN),
    TETH(ChatFormatting.BLUE),
    HE(ChatFormatting.YELLOW),
    WAW(ChatFormatting.LIGHT_PURPLE),
    ALEPH(ChatFormatting.RED);

    private final ChatFormatting color;

    EgoLevel(ChatFormatting color) {
        this.color = color;
    }

    public ChatFormatting getColor() {
        return color;
    }

    /**
     * 等级名称保持英文，只通过颜色区分等级。
     */
    public Component getDisplayName() {
        return Component.literal(this.name()).withStyle(this.color);
    }
}
