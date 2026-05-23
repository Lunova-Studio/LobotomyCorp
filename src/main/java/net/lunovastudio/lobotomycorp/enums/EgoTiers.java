package net.lunovastudio.lobotomycorp.enums;

import net.lunovastudio.lobotomycorp.items.ego.EgoTier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public enum EgoTiers implements EgoTier {
    ZAYIN(1, "zayin", 200, 10, Ingredient.of(Items.IRON_INGOT)),
    TETH(2, "teth", 300, 12, Ingredient.of(Items.GOLD_INGOT)),
    HE(3, "he", 400, 14, Ingredient.of(Items.DIAMOND)),
    WAW(4, "waw", 500, 16, Ingredient.of(Items.NETHERITE_INGOT)),
    ALEPH(5, "aleph", 600, 18, Ingredient.of(Items.NETHER_STAR));

    private final int levelCode;
    private final String levelName;
    private final int uses;
    private final int enchantmentValue;
    private final Ingredient repairIngredient;

    EgoTiers(int levelCode, String levelName, int uses, int enchantmentValue, Ingredient repairIngredient) {
        this.levelCode = levelCode;
        this.levelName = levelName;
        this.uses = uses;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getLevelCode() {
        return levelCode;
    }

    @Override
    public String getLevelName() {
        return levelName;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient;
    }
}