package net.lunovastudio.lobotomycorp.common.items.ego.weapon.mace;

import net.lunovastudio.lobotomycorp.common.enums.EgoAttackType;
import net.lunovastudio.lobotomycorp.common.enums.EgoLevel;
import net.lunovastudio.lobotomycorp.common.items.ego.weapon.EgoWeaponItem;

/**
 单手单柄武器，通过敲击来造成伤害。该模板武器的基础攻击速度为普通，攻击距离一般为近
 */
public class MaceItem extends EgoWeaponItem {
    public MaceItem(EgoLevel level, EgoAttackType attackType, float minDamage, float maxDamage) {
        super(level);
    }
}