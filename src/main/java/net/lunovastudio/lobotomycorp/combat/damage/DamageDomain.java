package net.lunovastudio.lobotomycorp.combat.damage;

/**
 * 伤害所属的领域。
 * <p>
 * MINECRAFT：原版及第三方 Mod 伤害 → 走 Minecraft 防御规则
 * LOBOTOMY： E.G.O / 异想体伤害 → 走四色抗性规则
 * HYBRID：   同时涉及两套规则的伤害（预留）
 */
public enum DamageDomain {
    MINECRAFT,
    LOBOTOMY,
    HYBRID
}
