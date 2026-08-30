package net.lunovastudio.lobotomycorp.client.render;

import net.lunovastudio.lobotomycorp.client.model.ModLayerDefinitions;
import net.lunovastudio.lobotomycorp.items.ego.armor.EgoArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * E.G.O 护甲渲染：把槽位中的护甲模型烘焙出来，继承玩家姿态并渲染。
 * <p>
 * 模型与贴图暂为占位（人体模型 + 原版铁甲贴图），由 {@link EgoArmorItem} 提供数据。
 */
public class EgoArmorRenderHandler {
    private static HumanoidModel<Player> model;

    public static HumanoidModel<Player> getModel() {
        if (model == null) {
            ModelPart part = Minecraft.getInstance().getEntityModels().bakeLayer(ModLayerDefinitions.EGO_ARMOR_LAYER);
            model = new HumanoidModel<>(part);
        }
        return model;
    }

    @SuppressWarnings("unchecked")
    public static void render(EgoArmorItem armor, ItemStack stack, Player player,
                              PoseStack poseStack, MultiBufferSource buffer, int light,
                              float limbSwing, float limbSwingAmount, float ageInTicks,
                              float netHeadYaw, float headPitch, EntityModel<?> parentModel) {
        HumanoidModel<Player> egoModel = getModel();

        if (parentModel instanceof HumanoidModel<?> humanoid) {
            ((HumanoidModel<Player>) humanoid).copyPropertiesTo(egoModel);
        }

        egoModel.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        RenderType renderType = RenderType.ARMOR_CUTOUT_NO_CULL.apply(armor.getArmorTexture());
        egoModel.renderToBuffer(poseStack, buffer.getBuffer(renderType), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
