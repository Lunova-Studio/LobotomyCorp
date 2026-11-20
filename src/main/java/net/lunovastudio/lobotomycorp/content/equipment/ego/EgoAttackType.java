package net.lunovastudio.lobotomycorp.content.equipment.ego;

import net.minecraft.network.chat.Component;

public enum EgoAttackType {
    RED,
    WHITE,
    BLACK,
    PALE,
    COLORFUL;

    public Component getDisplayName() {
        if (this == COLORFUL) {
            return Component.literal("???");
        }

        return Component.literal(this.name());
    }
}
