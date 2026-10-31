package net.lunovastudio.lobotomycorp.common.items.ego.weapon;

import net.lunovastudio.lobotomycorp.common.attributes.ModAttributes;
import net.lunovastudio.lobotomycorp.common.enums.EgoAttackType;
import net.lunovastudio.lobotomycorp.common.enums.EgoLevel;
import net.lunovastudio.lobotomycorp.common.items.ego.IEgoItem;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;

public class EgoWeaponItem extends Item implements IEgoItem {
    private final EgoLevel egoLevel;

    public EgoWeaponItem(EgoLevel level) {
        super(new Properties().stacksTo(1).setNoRepair().attributes(createAttributes(EgoAttackType.WHITE, 5, 7, 1.6f)));

        this.egoLevel = level;
    }

    public static ItemAttributeModifiers createAttributes(
        EgoAttackType attackType,
        int minDamage,
        int maxDamage,
        double attackSpeed) {
        return ItemAttributeModifiers.builder()
            .add(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .add(
                ModAttributes.MAX_EGO_DAMAGE,
                new AttributeModifier(ModAttributes.BASE_EGO_MAX_DAMAGE_ID, maxDamage, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND)
            .build();
    }

//    public float getMinDamage() {
//        return minDamage;
//    }
//
//    public float getMaxDamage() {
//        return maxDamage;
//    }

    public EgoLevel getLevel() {
        return egoLevel;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }
}
