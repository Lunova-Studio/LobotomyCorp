package net.lunovastudio.lobotomycorp.items;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItemGroups {
    public static final DeferredRegister<CreativeModeTab> LC_CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LobotomyCorp.MODID);

    public static final Supplier<CreativeModeTab> LC_TAB =
            LC_CREATIVE_TABS.register("lc_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.EGO_PENITENCE.get()))
                    .title(Component.translatable("itemGroup.lc_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.EGO_PENITENCE.get());
                        output.accept(ModItems.EGO_WINGBEAT.get());

                        output.accept(ModItems.EGO_REGRET.get());
                        output.accept(ModItems.EGO_SOMEWHERESPEAR.get());
                        output.accept(ModItems.EGO_HORN.get());
                        output.accept(ModItems.EGO_CHRISTMAS.get());
                        output.accept(ModItems.EGO_LANTERN.get());
                    }).build());

    public static void register(IEventBus bus) {
        LC_CREATIVE_TABS.register(bus);
    }
}
