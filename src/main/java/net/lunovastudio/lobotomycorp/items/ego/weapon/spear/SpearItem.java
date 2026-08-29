package net.lunovastudio.lobotomycorp.items.ego.weapon.spear;

import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import net.lunovastudio.lobotomycorp.items.ego.EgoItem;
import net.lunovastudio.lobotomycorp.items.ego.EgoTier;

/*
* 双手近战武器，通过向前捅来造成伤害。相比其他近战武器它的起手速度更快。该模板武器的基础攻击速度为快，攻击距离为一般
* */
public class SpearItem extends EgoItem {
    public SpearItem(EgoTier tier, EgoDamageType damageType, Properties properties) {
        super(tier, damageType, properties);
    }
}
