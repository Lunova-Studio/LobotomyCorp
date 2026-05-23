package net.lunovastudio.lobotomycorp.mixin;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.items.ego.EgoItem;
import net.lunovastudio.lobotomycorp.utils.DamageUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"), cancellable = true)
    private void onGetFinalDamage(DamageSource source, float originalDamage, CallbackInfoReturnable<Float> cir) {
//        var target = (LivingEntity)(Object)this;
//        float vanillaFinalDamage = cir.getReturnValueF();
//
//        if (target.level().isClientSide())
//            return;
//
//        var directEntity = source.getDirectEntity();
//        if (directEntity instanceof Player || directEntity instanceof Mob) {
//            Item item = ((LivingEntity) directEntity)
//                    .getMainHandItem()
//                    .getItem();
//            if (item instanceof EgoItem ego) { // 直接计算
//                var damage = DamageUtils.getDamage(
//                        ego.getTier(),
//                        ego.getDamageType(),
//                        1,
//                        ego.getMinDamage(),
//                        ego.getMaxDamage(),
//                        1,
//                        target.getMaxHealth());
//
//                LobotomyCorp.LOGGER.debug("Damage info: {}", damage);
//                cir.setReturnValue(damage.damageValue());
//
//                LobotomyCorp.LOGGER.debug("After set, return value: {}", cir.getReturnValueF());
//            }
//        }
    }
}
