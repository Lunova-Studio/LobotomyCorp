package net.lunovastudio.lobotomycorp.utils;

import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import net.lunovastudio.lobotomycorp.items.ego.DamageValue;
import net.lunovastudio.lobotomycorp.items.ego.EgoTier;

import java.util.Random;

public class DamageUtils {
    private static final Random RANDOM = new Random();

    /**
     * 获取最终伤害
     * @param resistance 目标抗性
     */
    public static DamageValue getDamage(
            EgoTier tier,
            EgoDamageType damageType,
            int targetLevel,
            float damage,
            float resistance,
            float targetMaxHealth) {
        float suppression = getSuppressionMultiplier(tier.getLevelCode(), targetLevel);
        float finalDamage;
        switch (damageType) {
            case RED, WHITE, BLACK ->
                    finalDamage = damage * suppression * resistance;
            case PALE -> {
                float percent = damage / 100.0f;
                finalDamage = (targetMaxHealth * percent) * suppression * resistance;
            }
            default -> finalDamage = 0;
        }

        return new DamageValue(damageType, finalDamage);
    }

    /**
     * 等级压制倍率计算
     * @return 伤害倍率
     */
    private static float getSuppressionMultiplier(int weaponLevel, int targetLevel) {
        int diff = targetLevel - weaponLevel;
        int clampedDiff = Math.clamp(diff, -4, 4);

        return switch (clampedDiff) {
            case 4 -> 0.4f;
            case 3 -> 0.6f;
            case 2 -> 0.7f;
            case 1 -> 0.8f;
            case -2 -> 1.2f;
            case -3 -> 1.5f;
            case -4 -> 2.0f;
            default -> 1.0f; // 1 -1
        };
    }
}
