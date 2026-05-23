package net.lunovastudio.lobotomycorp.items.ego.mace;

import net.lunovastudio.lobotomycorp.enums.EgoDamageType;
import net.lunovastudio.lobotomycorp.items.ego.EgoItem;
import net.lunovastudio.lobotomycorp.items.ego.EgoTier;

/**
单手单柄武器，通过敲击来造成伤害。该模板武器的基础攻击速度为普通，攻击距离一般为近
*/
public class MaceItem extends EgoItem {
    public MaceItem(EgoTier tier, EgoDamageType damageType, Properties properties) {
        super(tier, damageType, properties);
    }
}