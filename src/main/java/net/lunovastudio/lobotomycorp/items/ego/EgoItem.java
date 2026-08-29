package net.lunovastudio.lobotomycorp.items.ego;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import net.lunovastudio.lobotomycorp.enums.ego.EgoTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * EGO 武器基类
 */
public class EgoItem extends Item {
    private final EgoTier tier;
    private final EgoDamageType damageType;

    public static final ResourceLocation BASE_MIN_DAMAGE_ID =
            ResourceLocation.fromNamespaceAndPath(LobotomyCorp.MODID, "base_min_damage");

    public EgoItem(EgoTier tier, EgoDamageType damageType, Item.Properties properties) {
        super(properties
                .stacksTo(1)
                .component(DataComponents.TOOL, createToolProperties()));

        this.tier = tier;
        this.damageType = damageType;
    }

    public static Tool createToolProperties() {
        return new Tool(List.of(
                Tool.Rule.overrideSpeed(BlockTags.SWORD_EFFICIENT, 1.5F)),
                1.0F,
                2);
    }

    public static ItemAttributeModifiers createAttributes(double attackSpeed, double attackDamage) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
    }

    /**
     * 获取 EGO 等级
     */
    public EgoTier getTier() {
        return tier;
    }

    /**
     * 获取 EGO 伤害类型
     */
    public EgoDamageType getDamageType() {
        return damageType;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        String levelColor = switch (tier) {
            case EgoTiers.ZAYIN -> "§a";
            case EgoTiers.TETH -> "§9";
            case EgoTiers.HE -> "§e";
            case EgoTiers.WAW -> "§5";
            case EgoTiers.ALEPH -> "§c";
            default -> null;
        };

        String damageColor = switch (damageType) {
            case RED -> "§c";
            case WHITE -> "§f";
            case BLACK -> "§5";
            case PALE -> "§9";
        };//

        tooltip.add(Component.literal(levelColor + tier.getLevelName().toUpperCase()));
        tooltip.add(Component.literal(damageColor + damageType.name()));
    }
}