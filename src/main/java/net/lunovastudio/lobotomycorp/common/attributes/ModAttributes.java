package net.lunovastudio.lobotomycorp.common.attributes;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = LobotomyCorp.MODID)
public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(Registries.ATTRIBUTE, LobotomyCorp.MODID);

    /**
     * EGO 武器基础伤害修饰符的 ID。
     *
     * <p>把物品修饰符的 ID 与属性自己的
     * {@link net.neoforged.neoforge.common.extensions.IAttributeExtension#getBaseId()} 对齐后，
     * NeoForge 会按原版攻击速度/攻击伤害的“基础属性”格式显示，
     * 而不是显示成 {@code +7} 这样的增益修饰符。</p>
     */
    public static final ResourceLocation BASE_EGO_MAX_DAMAGE_ID =
        ResourceLocation.fromNamespaceAndPath(LobotomyCorp.MODID, "ego_max_damage");

    public static final ResourceLocation BASE_EGO_MIN_DAMAGE_ID =
        ResourceLocation.fromNamespaceAndPath(LobotomyCorp.MODID, "ego_min_damage");

    public static final DeferredHolder<Attribute, Attribute> MAX_PSYCHOLOGICAL =
        ATTRIBUTES.register("max_psychological", () -> new RangedAttribute(
            "lobotomycorp.attribute.max_psychological",
            20.0,
            1.0,
            1024.0).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> MAX_EGO_DAMAGE =
        ATTRIBUTES.register("lobotomycorp.attribute.max_ego_damage", () -> new EgoAttribute(
                "lobotomycorp.attribute.max_ego_damage",
                0.0,
                0.0,
                2048.0,
                BASE_EGO_MAX_DAMAGE_ID));

//    public static final Holder<Attribute> MIN_EGO_DAMAGE =
//        register("lobotomycorp.attribute.name.generic.min_ego_damage",
//            new EgoAttribute(
//                "lobotomycorp.attribute.min_ego_damage",
//                0.0,
//                0.0,
//                2048.0,
//                BASE_EGO_MIN_DAMAGE_ID));

    /**
     * 带有固定“基础修饰符 ID”的属性。
     *
     * <p>原版只给攻击伤害、攻击速度和交互距离提供了基础修饰符格式。
     * 自定义属性通过覆写
     * {@link net.neoforged.neoforge.common.extensions.IAttributeExtension#getBaseId()}
     * 也能使用同一套显示逻辑。</p>
     */
    public static class EgoAttribute extends RangedAttribute {
        private final ResourceLocation baseId;

        public EgoAttribute(String descriptionId, double defaultValue, double minValue, double maxValue,
                            ResourceLocation baseId) {
            super(descriptionId, defaultValue, minValue, maxValue);
            this.baseId = baseId;
        }

        @Override
        public ResourceLocation getBaseId() {
            return this.baseId;
        }
    }

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
    }

    private static Holder<Attribute> register(String name, Attribute attribute) {
        return Registry.registerForHolder(
            BuiltInRegistries.ATTRIBUTE,
            ResourceLocation.withDefaultNamespace(name), attribute);
    }

    // EVENT
    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ModAttributes.MAX_PSYCHOLOGICAL);
        event.add(EntityType.PLAYER, ModAttributes.MAX_EGO_DAMAGE);
    }
}