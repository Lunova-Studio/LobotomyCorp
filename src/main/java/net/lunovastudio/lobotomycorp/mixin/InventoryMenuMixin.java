package net.lunovastudio.lobotomycorp.mixin;

import net.lunovastudio.lobotomycorp.inventory.EgoArmorSlot;
import net.lunovastudio.lobotomycorp.items.ego.armor.EgoArmorItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 在背包界面副手槽位（{@code 77,62}）上方追加一个 E.G.O 护甲槽位（{@code 77,44}），
 * 并支持把护甲 shift-放入 / 取出该槽位。
 * <p>
 * 通过继承 {@link AbstractContainerMenu} 以直接调用其 protected 的 {@code addSlot} / {@code moveItemStackTo}。
 */
@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu {
    /** EGO 护甲槽位的 menu slot id（追加在原本 46 个槽位之后） */
    private static final int EGO_ARMOR_SLOT_ID = 46;

    protected InventoryMenuMixin() {
        super(null, 0);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lobotomycorp$addEgoArmorSlot(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        EgoArmorSlot slot = new EgoArmorSlot(owner, 77, 44);
        slot.setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
        this.addSlot(slot);
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void lobotomycorp$handleEgoArmorQuickMove(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        Slot slot = this.getSlot(index);
        if (slot == null || !slot.hasItem()) {
            return;
        }

        ItemStack stack = slot.getItem();

        // 从护甲槽位移出到背包/快捷栏（通过 remove 触发 attachment 同步）
        if (index == EGO_ARMOR_SLOT_ID) {
            ItemStack armor = slot.remove(1);
            if (!armor.isEmpty()) {
                this.moveItemStackTo(armor, 9, 45, true);
                if (!armor.isEmpty()) {
                    // 放不下的部分放回护甲槽位
                    if (slot.getItem().isEmpty()) {
                        slot.setByPlayer(armor);
                    } else {
                        player.drop(armor, false);
                    }
                }
            }
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        // 从背包/快捷栏尝试把 EGO 护甲移入护甲槽位
        if (index >= 9 && index < 45 && stack.getItem() instanceof EgoArmorItem) {
            Slot egoSlot = this.getSlot(EGO_ARMOR_SLOT_ID);
            if (!egoSlot.hasItem() && egoSlot.mayPlace(stack)) {
                egoSlot.setByPlayer(stack.split(1));
                cir.setReturnValue(ItemStack.EMPTY);
            }
        }
    }
}
