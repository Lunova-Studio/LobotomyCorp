package net.lunovastudio.lobotomycorp.items.ego.axe;

import net.lunovastudio.lobotomycorp.enums.EgoDamageType;
import net.lunovastudio.lobotomycorp.items.ego.EgoItem;
import net.lunovastudio.lobotomycorp.items.ego.EgoTier;

/**
 单手单柄武器，通过劈砍来造成伤害，游戏内只有一把斧模板武器：血之渴望。该模板武器的基础攻击速度为极快，攻击距离为极近
 */

public class AxeItem extends EgoItem {

    public AxeItem(EgoTier tier, EgoDamageType damageType, Properties properties) {
        super(tier, damageType, properties);
    }
}
