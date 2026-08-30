package net.lunovastudio.lobotomycorp.combat.damage;

import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import org.jetbrains.annotations.Nullable;

/**
 * 一次伤害事件的结算结果。
 * <p>
 * psychologicalDamage 用于承载 WHITE 伤害中应进入理智系统的部分，
 * 为 Phase 7 Psychological System 预留，当前阶段与 finalDamage 等价。
 */
public record DamageResult(
        float finalDamage,
        @Nullable EgoDamageType damageType,
        DamageDomain domain,
        boolean resisted,
        boolean modified,
        float psychologicalDamage
) {
}
