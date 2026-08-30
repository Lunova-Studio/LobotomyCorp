package net.lunovastudio.lobotomycorp.items;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import net.lunovastudio.lobotomycorp.enums.ego.EgoTiers;
import net.lunovastudio.lobotomycorp.entity.ModEntities;
import net.lunovastudio.lobotomycorp.items.ego.EgoItem;
import net.lunovastudio.lobotomycorp.items.ego.armor.TestEgoArmorItem;
import net.lunovastudio.lobotomycorp.items.ego.weapon.hammer.HammerItem;
import net.lunovastudio.lobotomycorp.items.ego.weapon.mace.MaceItem;
import net.lunovastudio.lobotomycorp.items.ego.weapon.spear.SpearItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items EGO =
            DeferredRegister.createItems(LobotomyCorp.MODID);

    public static final DeferredItem<MaceItem> EGO_PENITENCE =
            EGO.register("lc_item_penitence", () -> new MaceItem(EgoTiers.ZAYIN, EgoDamageType.WHITE,
                    new Item.Properties().attributes(EgoItem.createAttributes(-2.0d, 6d))));

    public static final DeferredItem<MaceItem> EGO_WINGBEAT =
            EGO.register("lc_item_wingbeat", () -> new MaceItem(EgoTiers.ZAYIN, EgoDamageType.RED,
                    new Item.Properties().attributes(EgoItem.createAttributes(-2.0d, 6d))));



    public static final DeferredItem<HammerItem> EGO_REGRET =
            EGO.register("lc_item_regret", () -> new HammerItem(EgoTiers.TETH, EgoDamageType.RED,
                    new Item.Properties().attributes(EgoItem.createAttributes(-2.0d, 6d))));

    public static final DeferredItem<SpearItem> EGO_HORN =
            EGO.register("lc_item_horn", () -> new SpearItem(EgoTiers.TETH, EgoDamageType.RED,
                    new Item.Properties().attributes(EgoItem.createAttributes(-2.0d, 6d))));

    public static final DeferredItem<SpearItem> EGO_SOMEWHERESPEAR =
            EGO.register("lc_item_somewherespear", () -> new SpearItem(EgoTiers.TETH, EgoDamageType.BLACK,
                    new Item.Properties().attributes(EgoItem.createAttributes(-2.0d, 6d))));

    public static final DeferredItem<HammerItem> EGO_LANTERN =
            EGO.register("lc_item_lantern", () -> new HammerItem(EgoTiers.TETH, EgoDamageType.BLACK,
                    new Item.Properties().attributes(EgoItem.createAttributes(-2.0d, 6d))));

    public static final DeferredItem<MaceItem> EGO_CHRISTMAS =
            EGO.register("lc_item_christmas", () -> new MaceItem(EgoTiers.HE, EgoDamageType.WHITE,
                    new Item.Properties().attributes(EgoItem.createAttributes(-2.0d, 6d))));

    public static final DeferredItem<TestEgoArmorItem> EGO_ARMOR_TEST =
            EGO.register("lc_armor_test", TestEgoArmorItem::new);

    public static final DeferredItem<SpawnEggItem> TEST_ABNORMALITY_EGG =
            EGO.register("test_abnormality_spawn_egg",
                    () -> new SpawnEggItem(ModEntities.TEST_ABNORMALITY.get(), 0x7A3B2E, 0xE88358, new Item.Properties()));



    public static void register(IEventBus bus) {
        EGO.register(bus);
    }
}