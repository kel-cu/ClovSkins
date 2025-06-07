package ru.kelcuprum.clovskins.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.styles.AbstractStyle;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.api.SkinOption;
import ru.kelcuprum.clovskins.client.gui.cicada.DummyClientPlayerEntity;
import ru.kelcuprum.clovskins.client.gui.cicada.GuiEntityRenderer;
import ru.kelcuprum.clovskins.client.gui.screen.MainScreen;
import ru.kelcuprum.clovskins.client.gui.screen.select.EditSkinPreset;

import java.util.UUID;

import static com.mojang.blaze3d.Blaze3D.getTime;

public class SkinPresetButton extends AbstractButton {
    int size;
    SkinOption skinOption;
    boolean rotate;
    boolean autoRotate;
    boolean followMouse;
    private final double currentTime;
    public boolean isSelected = false;
    public final String key;

    public SkinPresetButton(int x, int y, int width, int height, SkinOption skinOption, String key, boolean rotate, boolean autoRotate, boolean followMouse) {
        super(x, y, width, height, Component.translatable("clovskins.select.edit"));
        this.size = (height - 50)/2;
        this.rotate = rotate;
        this.skinOption = skinOption;
        this.currentTime = getTime();
        this.autoRotate = autoRotate;
        this.followMouse = followMouse;
        this.key = key;
    }

    float rotation = 0;

    public float getRotation() {
        if (autoRotate) rotation = (float) ((getTime() - currentTime) * 30.0f);
        return rotation;
    }

    public UUID SillyUUID = UUID.randomUUID();
    public DummyClientPlayerEntity entity;

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        AbstractStyle style = GuiUtils.getSelected();
        style.renderBackground$widget(guiGraphics, getX(), getY(), width, height-20, active, isHovered && mouseY < getBottom() - 20);
        //
        style.renderBackground$widget(guiGraphics, getX(), getBottom() - 20, width, 20, active, isHovered && mouseY > getBottom() - 20);
        if (GuiUtils.isDoesNotFit(getMessage(), width, 20))
            renderScrollingString(guiGraphics, AlinLib.MINECRAFT.font, 2, style.getTextColor(active));
        else GuiUtils.drawCenteredString(guiGraphics, AlinLib.MINECRAFT.font, getMessage(), getX()+width/2, getBottom() - 14, style.getTextColor(active), true);
        // -=-=-=-
        if (GuiUtils.isDoesNotFit(Component.literal(skinOption.name), width, 20))
            renderScrollingString(guiGraphics, AlinLib.MINECRAFT.font, Component.literal(skinOption.name), 2 , style.getTextColor(active), getY(), getY()+20);
        else GuiUtils.drawCenteredString(guiGraphics, AlinLib.MINECRAFT.font, Component.literal(skinOption.name), getX()+width/2, getY()+7, style.getTextColor(active), true);
        if(ClovSkins.currentSkin == skinOption) guiGraphics.blit(RenderType::guiTextured, GuiUtils.getResourceLocation("textures/gui/sprites/icon/checkmark.png"), getRight()-14, getY()+5, 0f, 0f, 9, 8, 9, 8);
        //
        int scale = size / 45;
        float followX = (float) (this.getX() + (this.getWidth() / 2)) - mouseX;
        float followY = (float) (((float) (this.getY() + (this.height / 2.5)) - mouseY) - (7.5 * scale * AlinLib.MINECRAFT.options.guiScale().get()));
        float rotation = 0;
        if (!followMouse) followX = followY = 0;
        if (rotate) {
            followX = followY = 0;
            rotation = getRotation();
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 950.0);
        try {
            if(entity == null) entity = new DummyClientPlayerEntity(null, SillyUUID, skinOption.getPlayerSkin(), AlinLib.MINECRAFT.options, false);
            else entity.setSkin(skinOption.getPlayerSkin());
            GuiEntityRenderer.drawModel(
                    guiGraphics.pose(), this.getX() + (this.getWidth() / 2), this.getBottom()-20,
                    size, rotation, followX, followY, skinOption
            );
        } catch (Exception ignored){
            ignored.printStackTrace();
        }
        guiGraphics.pose().translate(0, 0, -950.0);
        guiGraphics.pose().popPose();
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int i, int j) {
        int k = this.getX() + i;
        int l = this.getX() + this.getWidth() - i;
        renderScrollingString(guiGraphics, font, this.getMessage(), k, this.getBottom()-20, l, this.getBottom(), j);
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, Component msg, int i, int j, int y, int bottom) {
        int k = this.getX() + i;
        int l = this.getX() + this.getWidth() - i;
        renderScrollingString(guiGraphics, font, msg, k, y, l, bottom, j);
    }

    public void onClick(double d, double e) {
        if(e <= getBottom()-20) {
            if(ClovSkins.currentSkin == skinOption){
                ClovSkins.logger.log("Skin is already set");
            } else {
                try {
                    skinOption.uploadToMojangAPI();
                    ClovSkins.currentSkin = skinOption;
                    ClovSkins.config.setString("SELECTED", key);
                } catch (Exception ex){
                    ex.printStackTrace();
                    new ToastBuilder().setTitle(Component.literal("ClovSkins")).setMessage(Component.literal(ex.getMessage())).setType(ToastBuilder.Type.ERROR).buildAndShow();
                }
            }
        } else AlinLib.MINECRAFT.setScreen(new EditSkinPreset(AlinLib.MINECRAFT.screen, skinOption, key, ClovSkins.currentSkin == skinOption));
    }

    @Override
    public void onPress() {
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

}
