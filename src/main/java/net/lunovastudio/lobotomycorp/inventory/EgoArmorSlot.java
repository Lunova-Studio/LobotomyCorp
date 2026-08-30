package net.lunovastudio.lobotomycorp.inventory;

import net.lunovastudio.lobotomycorp.items.ego.armor.EgoArmorItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 背包界面副手槽位（{@code (77, 62)}）上方的 E.G.O 护甲槽位（{@code (77, 44)}）。
 * <p>
 * 仅接受 {@link EgoArmorItem}，单件上限。
 */
public class EgoArmorSlot extends Slot {
    public EgoArmorSlot(Player owner, int x, int y) {
        super(new EgoArmorContainer(owner), 0, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof EgoArmorItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
