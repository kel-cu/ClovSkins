package ru.kelcuprum.clovskins.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.styles.AbstractStyle;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.gui.cicada.DummyClientPlayerEntity;
import ru.kelcuprum.clovskins.client.gui.cicada.GuiEntityRenderer;
import ru.kelcuprum.clovskins.client.gui.screen.MainScreen;

import java.util.UUID;

import static com.mojang.blaze3d.Blaze3D.getTime;

public class PlayerButton extends AbstractButton {
    int size;
    boolean showItem;
    boolean rotate;
    boolean autoRotate;
    boolean followMouse;
    private final double currentTime;

    public PlayerButton(int x, int y, int size) {
        this(x, y, size, false);
    }

    public PlayerButton(int x, int y, int size, boolean showItem) {
        this(x, y, size, showItem, false);
    }

    public PlayerButton(int x, int y, int size, boolean showItem, boolean rotate) {
        this(x, y, size, showItem, rotate, true);
    }

    public PlayerButton(int x, int y, int size, boolean showItem, boolean rotate, boolean autoRotate) {
        this(x, y, size, showItem, rotate, autoRotate, true);
    }

    public PlayerButton(int x, int y, int size, boolean showItem, boolean rotate, boolean autoRotate, boolean followMouse) {
        super(x, y, size, size * 2 + 20, Component.translatable("clovskins.open_gui"));
        this.size = size;
        this.rotate = rotate;
        this.showItem = showItem;
        this.currentTime = getTime();
        this.autoRotate = autoRotate;
        this.followMouse = followMouse;
    }

    float rotation = 0;

    public float getRotation() {
        if (autoRotate) rotation = (float) ((getTime() - currentTime) * 30.0f);
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public static UUID SillyUUID = UUID.randomUUID();
    public DummyClientPlayerEntity entity;

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        PlayerSkin playerSkin = AlinLib.MINECRAFT.getSkinManager().getInsecureSkin(AlinLib.MINECRAFT.getGameProfile());
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
        try {
            if(entity == null) entity = new DummyClientPlayerEntity(null, SillyUUID, ClovSkins.currentSkin == null ? playerSkin : ClovSkins.currentSkin.getPlayerSkin(), AlinLib.MINECRAFT.options, showItem);
            else entity.setSkin(ClovSkins.currentSkin == null ? playerSkin : ClovSkins.currentSkin.getPlayerSkin());
//            GuiEntityRenderer.drawEntity(
//                    guiGraphics.pose(), this.getX() + (this.getWidth() / 2), this.getY()+this.height-25,
//                    size, rotation, followX, followY, entity
//            );
            GuiEntityRenderer.drawModel(
                    guiGraphics.pose(), this.getX() + (this.getWidth() / 2), this.getY()+this.height-25,
                    size, rotation, followX, followY, ClovSkins.currentSkin == null ? ClovSkins.safeSkinOption : ClovSkins.currentSkin
            );
        } catch (Exception ignored){}
        guiGraphics.pose().popPose();
        AbstractStyle style = ClovSkins.config.getBoolean("MENU.ALINLIB", false) ? GuiUtils.getSelected() : ClovSkins.vanillaLikeStyle;
        style.renderBackground$widget(guiGraphics, getX(), getBottom() - 20, width, 20, active, isHovered && mouseY > getBottom() - 20);
        if (GuiUtils.isDoesNotFit(getMessage(), width, 20))
            renderScrollingString(guiGraphics, AlinLib.MINECRAFT.font, 2, style.getTextColor(active));
        else GuiUtils.drawCenteredString(guiGraphics, AlinLib.MINECRAFT.font, getMessage(), getX()+width/2, getBottom() - 14, style.getTextColor(active), true);
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int i, int j) {
        int k = this.getX() + i;
        int l = this.getX() + this.getWidth() - i;
        renderScrollingString(guiGraphics, font, this.getMessage(), k, this.getBottom()-20, l, this.getY() + this.getHeight(), j);
    }

    OnPress onPress;

    public void setOnPress(OnPress onPress) {
        this.onPress = onPress;
        if (onPress != null) active = true;
    }

    @Override
    public void onPress() {
        AlinLib.MINECRAFT.setScreen(new MainScreen(AlinLib.MINECRAFT.screen));
    }

    @Environment(EnvType.CLIENT)
    public interface OnPress {
        void onPress(PlayerButton button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
