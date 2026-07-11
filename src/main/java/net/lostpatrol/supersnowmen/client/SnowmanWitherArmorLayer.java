package net.lostpatrol.supersnowmen.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.SnowGolem;

public class SnowmanWitherArmorLayer extends RenderLayer<SnowGolem, SnowGolemModel<SnowGolem>> {
    private static final ResourceLocation WITHER_ARMOR =
            ResourceLocation.withDefaultNamespace("textures/entity/wither/wither_armor.png");
    private final SnowGolemModel<SnowGolem> model;

    public SnowmanWitherArmorLayer(RenderLayerParent<SnowGolem, SnowGolemModel<SnowGolem>> parent,
                                   EntityModelSet modelSet) {
        super(parent);
        this.model = new SnowGolemModel<>(modelSet.bakeLayer(ModelLayers.SNOW_GOLEM));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       SnowGolem snowGolem, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!ClientPoweredSnowmen.isPowered(snowGolem)) {
            return;
        }
        float age = snowGolem.tickCount + partialTick;
        model.prepareMobModel(snowGolem, limbSwing, limbSwingAmount, partialTick);
        getParentModel().copyPropertiesTo(model);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.energySwirl(
                WITHER_ARMOR,
                (float)(Math.cos(age * 0.02F) * 3.0D) % 1.0F,
                age * 0.01F % 1.0F
        ));
        model.setupAnim(snowGolem, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                0.5F, 0.5F, 0.5F, 1.0F);
    }
}
