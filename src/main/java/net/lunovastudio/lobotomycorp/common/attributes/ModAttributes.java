package net.lunovastudio.lobotomycorp.common.attributes;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.minecraft.core.registries.Registries;
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
            DeferredRegister.create(
                    Registries.ATTRIBUTE,
                    LobotomyCorp.MODID
            );

    public static final DeferredHolder<Attribute, Attribute> MAX_PSYCHOLOGICAL =
            ATTRIBUTES.register("max_psychological",
                    () -> new RangedAttribute(
                            "lobotomycorp.attribute.max_psychological",
                            20.0,
                            1.0,
                            1024.0
                    ).setSyncable(true));

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
    }

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ModAttributes.MAX_PSYCHOLOGICAL);
    }
}