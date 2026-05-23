package net.lunovastudio.lobotomycorp.utils;

import net.lunovastudio.lobotomycorp.enums.EgoDamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

//WIP
public class PlayerUtils {
    /**
     * 获取玩家的防御等级（基于穿戴的最高级E.G.O护甲）
     * @param player 玩家
     * @return 等级代码（1-5），无护甲返回1（ZAYIN）
     */
    public static int getPlayerDefenseLevel(Player player) {
        int highestLevel = 1; // 默认ZAYIN

        for (ItemStack armorStack : player.getArmorSlots()) {
            if (armorStack.getItem() instanceof ArmorItem armor) {
                int levelCode = 2;//armor.getTier().getLevelCode();
                if (levelCode > highestLevel) {
                    highestLevel = levelCode;
                }
            }
        }

        return highestLevel;
    }

    /**
     * 获取玩家对特定伤害类型的抗性
     */
    public static float getPlayerResistance(Player player, EgoDamageType damageType) {
//        float totalResistance = 1.0f;
//        int piecesCount = 0;
//
//        for (ItemStack armorStack : player.getArmorSlots()) {
//            if (armorStack.getItem() instanceof ArmorItem armor) {
//                totalResistance *= armor.getResistance(damageType);
//                piecesCount++;
//            }
//        }
//
//        if (piecesCount > 0) {
//            // 返回几何平均
//            return (float) Math.pow(totalResistance, 1.0 / piecesCount);
//        }
//
        return 1.0f; // 无护甲，无抗性
    }
}
