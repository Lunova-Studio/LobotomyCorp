package net.lunovastudio.lobotomycorp.client.model;

import net.lunovastudio.lobotomycorp.LobotomyCorp;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * E.G.O 护甲的模型层定义。
 * <p>
 * 当前为占位模型（人体模型 + 0.5 外扩避免与原版身体 z-fighting），
 * 接入真实护甲模型时替换 {@link #registerLayerDefinitions} 中的 MeshDefinition 即可。
 */
public class ModLayerDefinitions {
    public static final ModelLayerLocation EGO_ARMOR_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(LobotomyCorp.MODID, "ego_armor"), "main");

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EGO_ARMOR_LAYER,
                () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE.extend(0.5F), 0.0F), 64, 32));
    }
}
