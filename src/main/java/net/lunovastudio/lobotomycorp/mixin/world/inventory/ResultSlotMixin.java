package net.lunovastudio.lobotomycorp.mixin.world.inventory;

import net.lunovastudio.lobotomycorp.content.research.ResearchRecipeLocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 服务端合成门控。{@link Slot#mayPickup} 是普通点击、Shift 点击与配方书放置共用的唯一出口，
 * 在这里拒绝可以保证结果槽物品不会被取走。
 */
@Mixin(Slot.class)
public abstract class ResultSlotMixin {
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void lobotomycorp$gateResearchRecipe(Player player, CallbackInfoReturnable<Boolean> callback) {
        if (!((Object) this instanceof ResultSlot resultSlot)) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!ResearchRecipeLocks.canCraft(serverPlayer, resultSlot.getItem())) {
            callback.setReturnValue(false);
        }
    }
}
