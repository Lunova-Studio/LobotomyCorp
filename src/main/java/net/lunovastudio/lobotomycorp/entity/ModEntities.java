package net.lunovastudio.lobotomycorp.entity;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, LobotomyCorp.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<TestAbnormality>> TEST_ABNORMALITY =
            ENTITY_TYPES.register("test_abnormality",
                    () -> EntityType.Builder.of(TestAbnormality::new, MobCategory.CREATURE)
                            .sized(0.9F, 0.9F)
                            .clientTrackingRange(10)
                            .build("test_abnormality"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(TEST_ABNORMALITY.get(), Pig.createAttributes().build());
    }
}
