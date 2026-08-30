package net.lunovastudio.lobotomycorp.combat.damage;

import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import net.lunovastudio.lobotomycorp.items.ego.EgoTier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * 一次伤害事件的统一描述。
 * <p>
 * MINECRAFT 域（僵尸攻击、摔落、火焰、第三方 Mod 武器）通常没有 EGO 伤害类型与等级，
 * 因此 damageType 与 tier 允许为 null。
 */
public record DamageContext(
        @Nullable LivingEntity attacker,
        LivingEntity target,
        DamageSource source,
        float baseDamage,
        @Nullable EgoDamageType damageType,
        @Nullable EgoTier tier,
        DamageDomain domain
) {}
