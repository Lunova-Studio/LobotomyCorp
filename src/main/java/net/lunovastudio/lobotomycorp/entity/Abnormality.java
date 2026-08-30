package net.lunovastudio.lobotomycorp.entity;

import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import net.lunovastudio.lobotomycorp.items.ego.EgoTier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * 异想体基类。
 * <p>
 * 暂以原版猪（{@link Pig}）作为模型、AI 与属性的来源，接入真实模型后改为独立路径。
 * 提供风险等级与四色抗性数据，供伤害系统（免疫 / 抗性）判断。
 */
public abstract class Abnormality extends Pig {
    private final EgoTier riskLevel;
    private final Map<EgoDamageType, Float> resistances;

    protected Abnormality(EntityType<? extends Abnormality> type, Level level, EgoTier riskLevel) {
        super(type, level);
        this.riskLevel = riskLevel;
        this.resistances = new HashMap<>();
        for (EgoDamageType damageType : EgoDamageType.values()) {
            this.resistances.put(damageType, 1.0F);
        }
    }

    public EgoTier getRiskLevel() {
        return riskLevel;
    }

    /**
     * 对指定伤害类型的抗性倍率（1.0 = 普通）。
     */
    public float getResistance(EgoDamageType damageType) {
        return resistances.getOrDefault(damageType, 1.0F);
    }

    protected void setResistance(EgoDamageType damageType, float resistance) {
        resistances.put(damageType, resistance);
    }
}
