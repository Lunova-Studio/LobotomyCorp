package net.lunovastudio.lobotomycorp.items.ego.armor;

import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import net.lunovastudio.lobotomycorp.enums.ego.EgoTiers;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;

import java.util.Map;

/**
 * 占位测试护甲，用于验证自定义护甲槽位与渲染管线。
 */
public class TestEgoArmorItem extends EgoArmorItem {
    public TestEgoArmorItem() {
        super(EgoTiers.ZAYIN,
                Map.of(
                        EgoDamageType.RED, 1.0F,
                        EgoDamageType.WHITE, 1.0F,
                        EgoDamageType.BLACK, 1.0F,
                        EgoDamageType.PALE, 1.0F),
                ArmorMaterials.IRON,
                ArmorItem.Type.CHESTPLATE,
                new Item.Properties().durability(250));
    }
}
