package ru.kelcuprum.clovskins.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.gui.cicada.GuiEntityRenderer;

import static com.mojang.blaze3d.Blaze3D.getTime;

public class PlayerWidget extends AbstractButton {
    int size;
    boolean showItem;
    boolean rotate;
    boolean autoRotate;
    boolean followMouse;
    private final double currentTime;
    public PlayerWidget(int x, int y, int size) {
        this(x, y, size, false);
    }

    public PlayerWidget(int x, int y, int size, boolean showItem) {
        this(x, y, size, showItem, false);
    }
    public PlayerWidget(int x, int y, int size, boolean showItem, boolean rotate) {
        this(x, y, size, showItem, rotate, true);
    }
    public PlayerWidget(int x, int y, int size, boolean showItem, boolean rotate, boolean autoRotate) {
        this(x, y, size, showItem, rotate, autoRotate, true);
    }
    public PlayerWidget(int x, int y, int size, boolean showItem, boolean rotate, boolean autoRotate, boolean followMouse) {
        super(x, y, size, size*2, Component.empty());
        this.size = size;
        this.rotate = rotate;
        this.showItem = showItem;
        this.currentTime = getTime();
        this.autoRotate = autoRotate;
        this.followMouse = followMouse;
        active = false;
    }
    float rotation = 0;
    public float getRotation() {
        if(autoRotate) rotation = (float) ((getTime() - currentTime) * 30.0f);
        return rotation;
    }

    public void setRotation(float rotation){
        this.rotation = rotation;
    }
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            PlayerSkin playerSkin = AlinLib.MINECRAFT.getSkinManager().getInsecureSkin(AlinLib.MINECRAFT.getGameProfile());
            int scale = size/45;
            float followX = (float) (this.getX() + (this.getWidth() / 2)) - mouseX;
            float followY = (float) (((float) (this.getY() + (this.height/2.5)) - mouseY)-(7.5*scale*AlinLib.MINECRAFT.options.guiScale().get()));
            float rotation = 0;
            if(!followMouse) followX = followY = 0;
            if(rotate){
                followX = followY = 0;
                rotation = getRotation();
            }

            guiGraphics.pose().pushPose();
            try {
                GuiEntityRenderer.drawModel(
                        guiGraphics.pose(), this.getX() + (this.getWidth() / 2), this.getY()+this.height+10,
                        size, rotation, followX, followY, ClovSkins.currentSkin == null ? ClovSkins.safeSkinOption : ClovSkins.currentSkin, partialTicks
                );
            } catch (Exception ignored){}
            guiGraphics.pose().popPose();
    }

    OnPress onPress;

    public void setOnPress(OnPress onPress) {
        this.onPress = onPress;
        if(onPress != null) active = true;
    }

    @Override
    public void onPress() {
        if(onPress != null) onPress.onPress(this);
    }

    @Environment(EnvType.CLIENT)
    public interface OnPress {
        void onPress(PlayerWidget button);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
