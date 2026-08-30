package net.lunovastudio.lobotomycorp.handlers;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.lunovastudio.lobotomycorp.attachments.ModAttachments;
import net.lunovastudio.lobotomycorp.attachments.datas.PsychologicalData;
import net.lunovastudio.lobotomycorp.combat.damage.DamageContext;
import net.lunovastudio.lobotomycorp.combat.damage.DamageDomain;
import net.lunovastudio.lobotomycorp.combat.damage.DamageResult;
import net.lunovastudio.lobotomycorp.combat.visual.EgoFloatingDamageText;
import net.lunovastudio.lobotomycorp.entity.Abnormality;
import net.lunovastudio.lobotomycorp.enums.ego.EgoDamageType;
import net.lunovastudio.lobotomycorp.items.ego.EgoItem;
import net.lunovastudio.lobotomycorp.utils.DamageUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = LobotomyCorp.MODID)
public class AttackHandler {
    @SubscribeEvent
    public static void onHurtEvent(LivingDamageEvent.Pre event) {
        var source = event.getSource();
        var attacker = source.getDirectEntity();
        var targetEntity = event.getEntity();

        if (attacker instanceof LivingEntity livingAttacker && livingAttacker != targetEntity) {
            Item item = livingAttacker.getMainHandItem().getItem();

            // 异想体免疫：非 E.G.O 武器无法造成伤害
            if (targetEntity instanceof Abnormality && !(item instanceof EgoItem)) {
                event.setNewDamage(0);
                if (targetEntity.level() instanceof ServerLevel serverLevel) {
                    EgoFloatingDamageText.spawnImmune(serverLevel, targetEntity);
                }
                return;
            }

            if (item instanceof EgoItem ego) { // ego 直接计算
                DamageContext context = new DamageContext(
                        livingAttacker,
                        targetEntity,
                        source,
                        event.getOriginalDamage(),
                        ego.getDamageType(),
                        ego.getTier(),
                        DamageDomain.LOBOTOMY);

                float resistance = targetEntity instanceof Abnormality abnormality
                        ? abnormality.getResistance(ego.getDamageType())
                        : 1.0f;

                var damage = DamageUtils.getDamage(
                        context.tier(),
                        context.damageType(),
                        1,
                        context.baseDamage(),
                        resistance,
                        targetEntity.getMaxHealth());

                DamageResult result = new DamageResult(
                        damage.damageValue(),
                        damage.damageType(),
                        context.domain(),
                        false,
                        false,
                        damage.damageType() == EgoDamageType.WHITE ? damage.damageValue() : 0.0f);

                if (result.domain() == DamageDomain.LOBOTOMY && result.damageType() != null
                        && result.finalDamage() > 0 && targetEntity.level() instanceof ServerLevel serverLevel) {
                    EgoFloatingDamageText.spawn(serverLevel, targetEntity, result.damageType(), result.finalDamage(), resistance);
                }

                LobotomyCorp.LOGGER.debug("item is {}, original damage is {}, new damage is {}, damage type is {}",
                        livingAttacker.getMainHandItem().getDisplayName().getString(), context.baseDamage(), result.finalDamage(), result.damageType());

                if (targetEntity.hasData(ModAttachments.PSYCHOLOGICAL.get()) &&
                        (result.damageType() == EgoDamageType.WHITE)) {
                    //TODO: 回复理智
                    int newDamage = targetEntity.getData(ModAttachments.PSYCHOLOGICAL.get())
                            .psychological() - (int) result.psychologicalDamage();
                    int newDamageBounds = Math.clamp(newDamage, 0, PsychologicalData.MAX_PSYCHOLOGICAL);

                    targetEntity.setData(ModAttachments.PSYCHOLOGICAL.get(), new PsychologicalData(newDamageBounds));
                    event.setNewDamage(0);
                } else {
                    event.setNewDamage(result.finalDamage());
                }
            }
        }
    }
}
