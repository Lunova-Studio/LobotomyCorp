package net.lunovastudio.lobotomycorp.handlers;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.attachments.ModAttachments;
import net.lunovastudio.lobotomycorp.attachments.datas.PsychologicalData;
import net.lunovastudio.lobotomycorp.enums.EgoDamageType;
import net.lunovastudio.lobotomycorp.items.ego.EgoItem;
import net.lunovastudio.lobotomycorp.utils.DamageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = LobotomyCorp.MODID)
public class AttackHandler {
    @SubscribeEvent
    public static void onHurtEvent(LivingDamageEvent.Pre event) {
        var source = event.getSource();
        var attacker = source.getDirectEntity();
        var targetEntity = event.getEntity();

        if (attacker instanceof Player || attacker instanceof Mob) {
            var itemStack = ((LivingEntity) attacker).getMainHandItem();
            var item = itemStack.getItem();

            if (item instanceof EgoItem ego) { // ego 直接计算
                var damage = DamageUtils.getDamage(
                        ego.getTier(),
                        ego.getDamageType(),
                        1,
                        event.getOriginalDamage(),
                        1.0f,
                        targetEntity.getMaxHealth());

                LobotomyCorp.LOGGER.debug("item is {}, original damage is {}, new damage is {}, damage type is {}",
                        itemStack.getDisplayName().getString(), event.getOriginalDamage(), damage.damageValue(), damage.damageType());

                if (targetEntity.hasData(ModAttachments.PSYCHOLOGICAL) &&
                        damage.damageType() == EgoDamageType.WHITE) {
                    event.setNewDamage(0.0f);
                    LobotomyCorp.LOGGER.debug("fuck PSYCHOLOGICAL");
                } else {
                    event.setNewDamage(damage.damageValue());
                }
            }
        }
    }
}
