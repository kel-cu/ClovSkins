package ru.kelcuprum.clovskins.client.gui.cicada;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
//#if MC >= 12106
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
//#endif
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
//#if MC >= 12106
import net.minecraft.client.renderer.RenderPipelines;
//#endif
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.api.SkinOption;
import ru.kelcuprum.clovskins.client.models.FakePlayerModel;

import java.io.IOException;
import java.lang.Math;
import java.security.SecureRandom;
import java.util.function.Consumer;

import static net.minecraft.world.entity.Pose.STANDING;
import static ru.kelcuprum.clovskins.client.ClovSkins.TICKS;
import static ru.kelcuprum.clovskins.client.ClovSkins.logger;

public class GuiEntityRenderer {
    public static void drawModel(
            PoseStack matrices, int x, int y, int size, float rotation, double mouseX, double mouseY, SkinOption skinOption, float tick) {
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
            matrices.translate(x, y + yGreh, -950.0);
            matrices.mulPose((new Matrix4f()).scaling((float) size, (float) size, (float) (-size)));
            matrices.translate(0.0F, -1.0F, 0.0F);
            matrices.mulPose(entityRotation);
            matrices.translate(0.0F, -1.0F, 0.0F);
            //#if MC <= 12105
            //$$ Lighting.setupForEntityInInventory();
            //#endif
            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            if (pitchRotation != null) {
                pitchRotation.conjugate();
                dispatcher.overrideCameraOrientation(pitchRotation);
            }
            var modelData = PlayerModel.createMesh(CubeDeformation.NONE, skinOption.model == PlayerSkin.Model.SLIM);
            FakePlayerModel fakePlayerModel = new FakePlayerModel(LayerDefinition.create(modelData, 64, 64).bakeRoot(), skinOption.model == PlayerSkin.Model.SLIM);
            fakePlayerModel.swingArmsGently((long) tick);
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
                if (cape != null) {
                    var capeModelData = PlayerCapeModel.createCapeLayer();
                    PlayerCapeModel capeModel = new PlayerCapeModel(capeModelData.bakeRoot());
                    matrices.pushPose();
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
            //#if MC <= 12105
            //$$ Lighting.setupFor3DItems();
            //#endif
            modelViewStack.popMatrix();
            RenderSystem.getModelViewMatrix();
        }
    }

    //#if MC >= 12106
    public static void drawModel(GuiGraphics guiGraphics, int x, int y, int size, float rotation, double mouseX, double mouseY, SkinOption skinOption, float tick) {
        float yaw = (float) (Math.atan2(mouseX, 120.0F));
        float pitch = (float) (Math.atan2(-mouseY, 120.0F));
        try {

//            AlinLib.LOG.log(guiGraphics.guiRenderState.submitPicturesInPictureState(););
            PlayerRenderState renderState = createPlayerRenderState(skinOption.getPlayerSkin(), yaw * 10.0F * 0.017453292F, pitch * 10.0F * 0.017453292F, rotation, tick);

            Quaternionf baseRotation = new Quaternionf();
            baseRotation.rotationZ((float) (Math.PI * 1.0f));

            Vector3f entityPosition = new Vector3f(0.0F, 1.1F, 0.0F);

            // Render the entity within the scissor area
            int size5Percent = (int) (size * 0.35);
            guiGraphics.enableScissor(x-size5Percent, y - size5Percent, x + size+size5Percent, y + (size * 2));
            GuiEntityRenderState state = new GuiEntityRenderState(renderState, entityPosition, baseRotation, null, x-size5Percent, y - size5Percent, x + size+size5Percent, y + (size * 2), (float) size, guiGraphics.scissorStack.peek());

            guiGraphics.guiRenderState.submitPicturesInPictureState(state);
            guiGraphics.guiRenderState.up();
        guiGraphics.disableScissor();
        } catch (Exception ex) {

        }
    }
    //#endif

    private static PlayerRenderState createPlayerRenderState(PlayerSkin skin, float headYaw, float headPitch, float rotation, float tick) throws IOException {
        PlayerRenderState state = new PlayerRenderState();

        // Basic entity properties
        state.ageInTicks = tick;
        state.boundingBoxWidth = 0.6F;
        state.boundingBoxHeight = 1.8F;
        state.eyeHeight = 1.62F;
        state.isInvisible = false;
        state.isDiscrete = false;
        state.displayFireAnimation = false;

        // Body orientation - keep body facing forward
        state.bodyRot = headYaw * 0.1f + rotation;
        state.yRot = headYaw; // Only the head turns with mouse
        state.xRot = headPitch; // Head pitch
        state.deathTime = 0.0F;

        // Gentle arm swaying animation
        float animationTime = tick * 0.067F;
        state.walkAnimationPos = Mth.sin(animationTime) * 0.05F;
        state.walkAnimationSpeed = 0.1F;

        // Standard entity state
        state.scale = 1.0F;
        state.ageScale = 1.0F;
        state.isUpsideDown = false; // Ensure this is explicitly false
        state.isFullyFrozen = false;
        state.isBaby = false;
        state.isInWater = false;
        state.isCrouching = false;
        state.hasRedOverlay = false;
        state.isInvisibleToPlayer = false;
        state.appearsGlowing = false;
        state.bedOrientation = null;
        state.customName = null;
        state.pose = STANDING;

        // Biped state - keep everything neutral
        state.swimAmount = 0.0F;
        state.attackTime = 0.0F;
        state.speedValue = 1.0F;
        state.maxCrossbowChargeDuration = 0.0F;
        state.ticksUsingItem = 0;
        state.isCrouching = false;
        state.isFallFlying = false;
        state.isVisuallySwimming = false;
        state.isPassenger = false;
        state.isUsingItem = false;
        state.elytraRotX = 0.0F;
        state.elytraRotY = 0.0F;
        state.elytraRotZ = 0.0F;

        // Player-specific properties
        state.skin = skin;
        state.name = String.valueOf(skin.textureUrl());
        state.isSpectator = false;
        state.arrowCount = 0;
        state.stingerCount = 0;
        state.useItemRemainingTicks = 0;
        state.swinging = false;
        state.fallFlyingTimeInTicks = 0.0F;
        state.shouldApplyFlyingYRot = false;
        state.flyingYRot = 0.0F;

        // Skin layer visibility
        state.showHat = true;
        state.showJacket = true;
        state.showLeftPants = true;
        state.showRightPants = true;
        state.showLeftSleeve = true;
        state.showRightSleeve = true;
        state.showCape = skin.capeTexture() != null;

        // Misc properties
        state.scoreText = Component.literal(skin.textureUrl());
        state.parrotOnLeftShoulder = null;
        state.parrotOnRightShoulder = null;
        state.id = (int) new SecureRandom().nextFloat(tick);

        return state;
    }

    public static void draw2DModel(GuiGraphics guiGraphics, int x, int y, int size, SkinOption skinOption){
        double onePixelSize = size / 16;
        double w = onePixelSize % 1;
        if(w != 0) onePixelSize += (1-w);
        w = onePixelSize % 2;
        onePixelSize-=w;
        if(onePixelSize < 1.1){
            String[] fruiks = {"(:", "[:","<:", ":D", "d:", ":3"};
            guiGraphics.drawCenteredString(AlinLib.MINECRAFT.font, fruiks[new SecureRandom().nextInt(fruiks.length)], x, y+size, -1);
            return;
        }
        try {
            ResourceLocation skin = skinOption.getTextureSkin();
            // first layer
            // - Голова и тело
            guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x+onePixelSize*4.5), (int) (y+(onePixelSize/2)), 8, 8, (int) (onePixelSize*7), (int) (onePixelSize*7), 8, 8, 64, 64);
            guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x+onePixelSize*4.5), (int) (y+onePixelSize*7.5), 20, 20, (int) (onePixelSize*7), (int) (onePixelSize*11), 8, 12, 64, 64);
            // - Ноги
            guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x+onePixelSize*4.5), (int) (y+onePixelSize*18.5), 4, 20, (int) (onePixelSize*3.5), (int) (onePixelSize*11), 4, 12, 64, 64);
            guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x+onePixelSize*8), (int) (y+onePixelSize*18.5), 20, 52, (int) (onePixelSize*3.5), (int) (onePixelSize*11), 4, 12, 64, 64);
            // - Руки
            if(skinOption.model == PlayerSkin.Model.WIDE) {
                guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x + onePixelSize), (int) (y + onePixelSize * 7.5), 44, 20, (int) (onePixelSize * 3.5), (int) (onePixelSize * 11), 4, 12, 64, 64);
                guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x + onePixelSize * 11.5), (int) (y + onePixelSize * 7.5), 36, 52, (int) (onePixelSize * 3.5), (int) (onePixelSize * 11), 4, 12, 64, 64);
            } else {
                guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x + onePixelSize*1.9), (int) (y + onePixelSize * 7.5), 44, 20, (int) (onePixelSize *2.75), (int) (onePixelSize * 11), 3, 12, 64, 64);
                guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x + onePixelSize * 11.5), (int) (y + onePixelSize * 7.5), 36, 52, (int) (onePixelSize * 2.75), (int) (onePixelSize * 11), 3, 12, 64, 64);
            }
            // not first layer
            // - Ноги
            guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x+onePixelSize*4), (int) (y+onePixelSize*18), 4, 36, (int) (onePixelSize*4), (int) (onePixelSize*12), 4, 12, 64, 64);
            guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x+onePixelSize*8), (int) (y+onePixelSize*18), 4, 52, (int) (onePixelSize*4), (int) (onePixelSize*12), 4, 12, 64, 64);
            // - Руки
            if(skinOption.model == PlayerSkin.Model.WIDE) {
                guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x + onePixelSize * 0.75), (int) (y + onePixelSize * 7), 44, 36, (int) (onePixelSize * 4), (int) (onePixelSize * 12), 4, 12, 64, 64);
                guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x + onePixelSize * 11.25), (int) (y + onePixelSize * 7), 52, 52, (int) (onePixelSize * 4), (int) (onePixelSize * 12), 4, 12, 64, 64);
            } else {
                guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x + onePixelSize * 1.5), (int) (y + onePixelSize * 7), 44, 36, (int) (onePixelSize * 3.25), (int) (onePixelSize * 12), 3, 12, 64, 64);
                guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x + onePixelSize * 11.25), (int) (y + onePixelSize * 7), 52, 52, (int) (onePixelSize * 3.25), (int) (onePixelSize * 12), 3, 12, 64, 64);
            }
            // - Голова и Тело
            guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x+onePixelSize*4), (int) (y+onePixelSize*7.5), 20, 36, (int) (onePixelSize*8), (int) (onePixelSize*11.25), 8, 12, 64, 64);
            guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         skin, (int) (x+onePixelSize*4), y, 40, 8, (int) (onePixelSize*8), (int) (onePixelSize*8), 8, 8, 64, 64);
        } catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
