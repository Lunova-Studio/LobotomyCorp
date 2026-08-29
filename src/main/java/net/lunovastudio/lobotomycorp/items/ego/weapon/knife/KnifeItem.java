package net.lunovastudio.lobotomycorp.items.ego.weapon.knife;

import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import net.lunovastudio.lobotomycorp.items.ego.EgoItem;
import net.lunovastudio.lobotomycorp.items.ego.EgoTier;

/*
*单手近战武器，通过戳击或切割来造成伤害，游戏内只有一把该模板武器：割腕者。该模板武器的基础攻击速度为极快，攻击距离为极近
*/
public class KnifeItem extends EgoItem {
    public KnifeItem(EgoTier tier, EgoDamageType damageType, Properties properties) {
        super(tier, damageType, properties);
    }
}
