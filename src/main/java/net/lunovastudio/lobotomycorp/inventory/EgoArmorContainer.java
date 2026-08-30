package net.lunovastudio.lobotomycorp.inventory;

import net.lunovastudio.lobotomycorp.attachments.ModAttachments;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 自定义护甲槽位的 {@link Container} 包装，读写玩家 {@link ModAttachments#EGO_ARMOR} attachment。
 */
public class EgoArmorContainer implements Container {
    private final Player player;

    public EgoArmorContainer(Player player) {
        this.player = player;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return player.getData(ModAttachments.EGO_ARMOR.get()).isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return player.getData(ModAttachments.EGO_ARMOR.get());
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack current = player.getData(ModAttachments.EGO_ARMOR.get()).copy();
        ItemStack removed = current.split(amount);
        player.setData(ModAttachments.EGO_ARMOR.get(), current);
        return removed;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack current = player.getData(ModAttachments.EGO_ARMOR.get());
        player.setData(ModAttachments.EGO_ARMOR.get(), ItemStack.EMPTY);
        return current;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        player.setData(ModAttachments.EGO_ARMOR.get(), stack.copy());
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        player.setData(ModAttachments.EGO_ARMOR.get(), ItemStack.EMPTY);
    }
}
