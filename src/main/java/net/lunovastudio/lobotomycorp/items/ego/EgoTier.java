package net.lunovastudio.lobotomycorp.items.ego;

import net.minecraft.world.item.crafting.Ingredient;

public interface EgoTier {
    /**
     * 获取E.G.O等级代码（ZAYIN=1, TETH=2, HE=3, WAW=4, ALEPH=5）
     */
    int getLevelCode();

    /**
     * 获取等级名称
     */
    String getLevelName();


    /**
     * 获取武器耐久度
     */
    int getUses();

    /**
     * 获取附魔能力
     */
    int getEnchantmentValue();

    /**
     * 获取修复材料
     */
    Ingredient getRepairIngredient();
}
