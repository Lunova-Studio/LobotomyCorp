package net.lunovastudio.lobotomycorp.items.ego.weapon.hammer;

import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import net.lunovastudio.lobotomycorp.items.ego.EgoItem;
import net.lunovastudio.lobotomycorp.items.ego.EgoTier;

/*
* 大型近战武器，通过锤击造成伤害，攻击到目标后会有以目标为中心小范围的溅射，锤的溅射也不会对友方或中立单位造成伤害。该模板武器的基础攻击速度为慢
* */
public class HammerItem extends EgoItem {
    public HammerItem(EgoTier tier, EgoDamageType damageType, Properties properties) {
        super(tier, damageType, properties);
    }
}
