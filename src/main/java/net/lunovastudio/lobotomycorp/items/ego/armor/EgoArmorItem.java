package net.lunovastudio.lobotomycorp.items.ego.armor;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import net.lunovastudio.lobotomycorp.enums.ego.EgoTiers;
import net.lunovastudio.lobotomycorp.items.ego.EgoTier;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Map;

/**
 * E.G.O 护甲基类。
 * <p>
 * 用于在自定义护甲槽位中判断是否为模组护甲（{@code instanceof EgoArmorItem}），
 * 并提供 E.G.O 等级与四色抗性数据。模型 / 贴图暂未制作，先以占位值接入渲染管线。
 */
public class EgoArmorItem extends ArmorItem {
    private final EgoTier tier;
    private final Map<EgoDamageType, Float> resistances;

    public EgoArmorItem(EgoTier tier, Map<EgoDamageType, Float> resistances,
                        Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
        this.tier = tier;
        this.resistances = resistances;
    }

    public EgoTier getTier() {
        return tier;
    }

    public float getResistance(EgoDamageType damageType) {
        return resistances.getOrDefault(damageType, 1.0F);
    }

    /**
     * 护甲模型 id（接入真实模型时由子类覆写）。
     */
    public ResourceLocation getArmorModelId() {
        return ResourceLocation.fromNamespaceAndPath(LobotomyCorp.MODID, "ego_armor");
    }

    /**
     * 护甲贴图（接入真实贴图时由子类覆写，当前为原版铁甲占位）。
     */
    public ResourceLocation getArmorTexture() {
        return ResourceLocation.withDefaultNamespace("textures/models/armor/iron_layer_1.png");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String levelColor = switch (tier) {
            case EgoTiers.ZAYIN -> "§a";
            case EgoTiers.TETH -> "§9";
            case EgoTiers.HE -> "§e";
            case EgoTiers.WAW -> "§5";
            case EgoTiers.ALEPH -> "§c";
            default -> null;
        };
        tooltip.add(Component.literal(levelColor + tier.getLevelName().toUpperCase()));
    }
}
