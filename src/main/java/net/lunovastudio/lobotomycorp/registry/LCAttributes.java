package net.lunovastudio.lobotomycorp.registry;

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
public class LCAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(Registries.ATTRIBUTE, LobotomyCorp.MODID);

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
        event.add(EntityType.PLAYER, LCAttributes.MAX_PSYCHOLOGICAL);
        event.add(EntityType.PLAYER, LCAttributes.MAX_EGO_DAMAGE);
    }
}