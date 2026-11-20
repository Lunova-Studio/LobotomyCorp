package net.lunovastudio.lobotomycorp.content.equipment.ego.weapon;

import net.lunovastudio.lobotomycorp.registry.LCAttributes;
import net.lunovastudio.lobotomycorp.content.equipment.ego.EgoAttackType;
import net.lunovastudio.lobotomycorp.content.equipment.ego.EgoLevel;
import net.lunovastudio.lobotomycorp.content.equipment.ego.IEgoItem;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EgoWeaponItem extends Item implements IEgoItem {
    private static final String LEVEL_KEY = "tooltip.lobotomycorp.ego_weapon.level";
    private static final String DAMAGE_TYPE_KEY = "tooltip.lobotomycorp.ego_weapon.damage_type";
    private static final String HOLD_SHIFT_KEY = "tooltip.lobotomycorp.ego_weapon.hold_shift";
    private static final String NO_DESCRIPTION_KEY = "tooltip.lobotomycorp.ego_weapon.no_description";

    private final EgoLevel egoLevel;
    private final EgoAttackType attackType;

    public EgoWeaponItem(EgoLevel level) {
        this(level, EgoAttackType.WHITE);
    }

    public EgoWeaponItem(EgoLevel level, EgoAttackType attackType) {
        super(new Properties().stacksTo(1).setNoRepair().attributes(createAttributes(attackType, 5, 7, 1.6f)));

        this.egoLevel = level;
        this.attackType = attackType;
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
                LCAttributes.MAX_EGO_DAMAGE,
                new AttributeModifier(LCAttributes.BASE_EGO_MAX_DAMAGE_ID, maxDamage, AttributeModifier.Operation.ADD_VALUE),
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

    public EgoAttackType getAttackType() {
        return attackType;
    }

    /**
     * 武器描述的翻译键。默认基于物品自身的 description id，
     * 子类可以覆写以使用独立的描述键。
     */
    public String getDescriptionKey() {
        return this.getDescriptionId() + ".desc";
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // 等级与伤害类型优先显示，描述/展开提示放在它们下方。
        tooltip.add(Component.empty()
            .append(Component.translatable(LEVEL_KEY).withStyle(ChatFormatting.GRAY))
            .append(egoLevel.getDisplayName())
            .append(Component.literal("   "))
            .append(Component.translatable(DAMAGE_TYPE_KEY).withStyle(ChatFormatting.GRAY))
            .append(attackType.getDisplayName()));

        tooltip.add(Component.empty());
        appendDescription(tooltip, flag);
    }

    private void appendDescription(List<Component> tooltip, TooltipFlag flag) {
        if (flag.hasShiftDown()) {
            String descriptionKey = getDescriptionKey();
            String description = Language.getInstance().getOrDefault(
                Language.getInstance().has(descriptionKey) ? descriptionKey : NO_DESCRIPTION_KEY);

            // 描述中的换行符拆成独立的 tooltip 行，避免长文本把背景撑得过宽。
            for (String line : description.split("\\n")) {
                if (!line.isEmpty()) {
                    tooltip.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
                }
            }
        } else {
            tooltip.add(Component.translatable(HOLD_SHIFT_KEY).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }
}
