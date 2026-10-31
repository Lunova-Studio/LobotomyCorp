package net.lunovastudio.lobotomycorp.common.items;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.common.enums.EgoAttackType;
import net.lunovastudio.lobotomycorp.common.enums.EgoLevel;
import net.lunovastudio.lobotomycorp.common.items.ego.weapon.mace.MaceItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items EGO =
            DeferredRegister.createItems(LobotomyCorp.MODID);

    // Weapon
    public static final DeferredItem<MaceItem> EGO_PENITENCE =
            EGO.register("lc_item_penitence", () -> new MaceItem(EgoLevel.ZAYIN, EgoAttackType.WHITE,
                    5, 7));

    public static void register(IEventBus bus) {
        EGO.register(bus);
    }
}
