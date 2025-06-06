package ru.kelcuprum.clovskins.client.gui.cicada;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.api.SkinOption;
import ru.kelcuprum.clovskins.client.models.FakePlayerModel;

public class GuiEntityRenderer {
    public static void drawEntity(PoseStack matrices, int x, int y, int size, float rotation, double mouseX, double mouseY, LivingEntity entity) {
        if (entity != null) {
            float yaw = (float) Math.atan(mouseX / 40.0);
            float pitch = (float) Math.atan(mouseY / 40.0);
            Quaternionf entityRotation = (new Quaternionf()).rotateZ(3.1415927F);
            Quaternionf pitchRotation = (new Quaternionf()).rotateX(pitch * 20.0F * 0.017453292F);
            entityRotation.mul(pitchRotation);
            float oldBodyYaw = entity.yBodyRot;
            float oldYaw = entity.getYRot();
            float oldPitch = entity.getXRot();
            float oldPrevHeadYaw = entity.yHeadRotO;
            float oldHeadYaw = entity.yHeadRot;
            entity.yBodyRot = 180.0F + yaw * 20.0F + rotation;
            entity.setYRot(180.0F + yaw * 40.0F + rotation);
            entity.setXRot(-pitch * 20.0F);
            entity.yHeadRot = entity.getYRot();
            entity.yHeadRotO = entity.getYRot();
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.translate(0.0F, 0.0F, 1000.0F);
            RenderSystem.backupProjectionMatrix();
            matrices.pushPose();
            matrices.translate(x, y, -950.0);
            matrices.mulPose((new Matrix4f()).scaling((float) size, (float) size, (float) (-size)));
            matrices.translate(0.0F, -1.0F, 0.0F);
            matrices.mulPose(entityRotation);
            matrices.translate(0.0F, -1.0F, 0.0F);
//            Lighting.setupFor3DItems();
            Lighting.setupForEntityInInventory();
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            if (pitchRotation != null) {
                pitchRotation.conjugate();
                dispatcher.overrideCameraOrientation(pitchRotation);
            }

            dispatcher.setRenderShadow(false);
            if (FabricLoader.getInstance().isModLoaded("entity_texture_features")) {
                ETFCompat.preventRenderLayerIssue();
            }

            MultiBufferSource.BufferSource vertexConsumers = Minecraft.getInstance().renderBuffers().bufferSource();
//            dispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, matrices, vertexConsumers, 15728880);
            dispatcher.render(entity, 0.0, 0.0, 0.0, 1.0F, matrices, vertexConsumers, 15728880);
            vertexConsumers.endBatch();
            dispatcher.setRenderShadow(true);
            matrices.popPose();
            Lighting.setupFor3DItems();
            modelViewStack.popMatrix();
            RenderSystem.getModelViewMatrix();
            entity.yBodyRot = oldBodyYaw;
            entity.setYRot(oldYaw);
            entity.setXRot(oldPitch);
            entity.yHeadRotO = oldPrevHeadYaw;
            entity.yHeadRot = oldHeadYaw;
        }
    }

    public static void drawModel(PoseStack matrices, int x, int y, int size, float rotation, double mouseX, double mouseY, SkinOption skinOption) {
        if (skinOption != null) {
            float yaw = (float) (Math.atan2(mouseX, 120.0F));
            float pitch = (float) (Math.atan2(-mouseY, 120.0F));
            Quaternionf entityRotation = new Quaternionf().rotateY(rotation * 0.025f);

            Quaternionf pitchRotation = new Quaternionf().rotateX(pitch * 10.0F * 0.017453292F);
            Quaternionf yawRotation = new Quaternionf().rotateY(yaw * 10.0F * 0.017453292F);
            entityRotation.mul(pitchRotation);
            entityRotation.mul(yawRotation);

            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.translate(0.0F, 0.0F, 1000.0F);
            RenderSystem.backupProjectionMatrix();
            matrices.pushPose();
            double yGreh = ((double) size / 50) * 20;
//            ClovSkins.logger.log(""+yGreh);
            matrices.translate(x, y+yGreh, -950.0);
            matrices.mulPose((new Matrix4f()).scaling((float) size, (float) size, (float) (-size)));
            matrices.translate(0.0F, -1.0F, 0.0F);
            matrices.mulPose(entityRotation);
            matrices.translate(0.0F, -1.0F, 0.0F);
            Lighting.setupForEntityInInventory();
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            if (pitchRotation != null) {
                pitchRotation.conjugate();
                dispatcher.overrideCameraOrientation(pitchRotation);
            }
            var modelData = PlayerModel.createMesh(CubeDeformation.NONE, skinOption.model == PlayerSkin.Model.SLIM);
            FakePlayerModel fakePlayerModel = new FakePlayerModel(LayerDefinition.create(modelData, 64, 64).bakeRoot(), skinOption.model == PlayerSkin.Model.SLIM);
            fakePlayerModel.swingArmsGently((long) ClovSkins.TICKS);
            fakePlayerModel.setRotate(yaw, pitch);

            dispatcher.setRenderShadow(false);
            if (FabricLoader.getInstance().isModLoaded("entity_texture_features")) {
                ETFCompat.preventRenderLayerIssue();
            }

            MultiBufferSource.BufferSource vertexConsumers = Minecraft.getInstance().renderBuffers().bufferSource();
            try {
                fakePlayerModel.renderToBuffer(
                        matrices,
                        vertexConsumers.getBuffer(RenderType.entityTranslucent(skinOption.getTextureSkin())),
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY,
                        0xFFFFFFFF
                );
                ResourceLocation cape = skinOption.getTextureCape();
                if(cape != null){
                    var capeModelData = PlayerCapeModel.createCapeLayer();
                    PlayerCapeModel capeModel = new PlayerCapeModel(capeModelData.bakeRoot());
                    matrices.pushPose();
//                    matrices.translate(0.0f, 0.0f, 0.2f);
                    capeModel.renderToBuffer(
                            matrices,
                            vertexConsumers.getBuffer(RenderType.entityTranslucent(cape)),
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY,
                            0xFFFFFFFF
                    );
                    matrices.popPose();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            vertexConsumers.endBatch();
            dispatcher.setRenderShadow(true);
            matrices.popPose();
            Lighting.setupFor3DItems();
            modelViewStack.popMatrix();
            RenderSystem.getModelViewMatrix();
        }
    }
}
