package net.lunovastudio.lobotomycorp.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Map;
import java.util.Optional;

/**
 * 阶段上的单条“收集/合成”要求。
 *
 * <p>要求匹配 {@code items} 中的任意物品（标签或直接物品列表）。玩家需要累计获得
 * 至少 {@code amount} 个匹配物品，数量在匹配集合上求和。</p>
 *
 * <p>{@code components} 会进一步收窄匹配：声明的每个组件都必须存在且等于声明值，
 * 声明为“移除”的组件必须不存在。附魔组件是唯一的例外：按“下限”匹配，
 * 要求“时运 I”时任何等级的时运都满足，且忽略物品上的其他附魔。</p>
 *
 * @param items      满足要求的物品集合
 * @param components 候选物品栈必须携带的组件值
 * @param amount     最低累计数量，必须为正
 */
public record ResearchRequirement(HolderSet<Item> items, DataComponentPatch components, int amount) {
    public static final Codec<ResearchRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    RegistryCodecs.homogeneousList(Registries.ITEM)
                            .fieldOf("items")
                            .forGetter(ResearchRequirement::items),
                    DataComponentPatch.CODEC
                            .optionalFieldOf("components", DataComponentPatch.EMPTY)
                            .forGetter(ResearchRequirement::components),
                    Codec.INT.fieldOf("amount").forGetter(ResearchRequirement::amount))
            .apply(instance, ResearchRequirement::new));

    /**
     * 判断给定物品栈是否计入本要求。
     */
    public boolean matches(ItemStack stack) {
        return stack.is(items) && matchesComponents(stack);
    }

    private boolean matchesComponents(ItemStack stack) {
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : components.entrySet()) {
            DataComponentType<?> type = entry.getKey();
            Optional<?> required = entry.getValue();
            if (required.isEmpty()) {
                if (stack.has(type)) {
                    return false;
                }
                continue;
            }
            Object value = required.get();
            Object present = stack.get(type);
            if (value instanceof ItemEnchantments enchantments) {
                if (!hasAtLeast(enchantments, present)) {
                    return false;
                }
                continue;
            }
            if (!value.equals(present)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasAtLeast(ItemEnchantments required, Object present) {
        ItemEnchantments held =
                present instanceof ItemEnchantments enchantments ? enchantments : ItemEnchantments.EMPTY;
        for (Holder<Enchantment> enchantment : required.keySet()) {
            if (held.getLevel(enchantment) < required.getLevel(enchantment)) {
                return false;
            }
        }
        return true;
    }
}
