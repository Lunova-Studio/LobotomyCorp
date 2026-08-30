package net.lunovastudio.lobotomycorp.mixin;

import net.lunovastudio.lobotomycorp.attachments.ModAttachments;
import net.lunovastudio.lobotomycorp.client.render.EgoArmorRenderHandler;
import net.lunovastudio.lobotomycorp.items.ego.armor.EgoArmorItem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * 自定义护甲渲染：
 * <ul>
 *     <li>玩家穿戴 E.G.O 护甲槽位护甲时，取消全部原版护甲渲染；</li>
 *     <li>渲染槽位中的 E.G.O 护甲模型。</li>
 * </ul>
 */
@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {
    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
            at = @At("HEAD"), cancellable = true)
    private void lobotomycorp$hideVanillaArmor(PoseStack poseStack, MultiBufferSource buffer, LivingEntity entity,
                                               EquipmentSlot slot, int light, net.minecraft.client.model.HumanoidModel<?> model,
                                               float limbSwing, float limbSwingAmount, float ageInTicks,
                                               float netHeadYaw, float headPitch, float f, CallbackInfo ci) {
        if (entity instanceof Player player
                && player.getData(ModAttachments.EGO_ARMOR.get()).getItem() instanceof EgoArmorItem) {
            ci.cancel();
        }
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("TAIL"))
    private void lobotomycorp$renderEgoArmor(PoseStack poseStack, MultiBufferSource buffer, int light, LivingEntity entity,
                                             float limbSwing, float limbSwingAmount, float ageInTicks,
                                             float netHeadYaw, float headPitch, float f, CallbackInfo ci) {
        if (!(entity instanceof Player player)) {
            return;
        }

        ItemStack stack = player.getData(ModAttachments.EGO_ARMOR.get());
        if (stack.getItem() instanceof EgoArmorItem armor) {
            EntityModel<?> parent = ((RenderLayer<?, ?>) (Object) this).getParentModel();
            EgoArmorRenderHandler.render(armor, stack, player, poseStack, buffer, light,
                    limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, parent);
        }
    }
}
