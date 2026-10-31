package net.lunovastudio.lobotomycorp.common.enums;

import net.minecraft.network.chat.Component;

import java.util.Objects;

public enum EgoAttackType {
    RED("red"),
    WHITE("white"),
    BLACK("black"),
    PALE("pale"),
    COLORFUL("colorful");

    private final String name;

    EgoAttackType(String name) {
        this.name = name;
    }

    public Component getDisplayName() {
        if(name.equals("colorful"))
            return Component.literal("???");

        return Component.translatable("tooltip.lobotomy.damage_type." + name);
    }
}