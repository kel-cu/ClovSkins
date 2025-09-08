package ru.kelcuprum.clovskins.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.styles.AbstractStyle;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.gui.cicada.GuiEntityRenderer;
import ru.kelcuprum.clovskins.client.gui.screen.MainScreen;

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

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int scale = size / 45;
        float followX = (float) (this.getX() + (this.getWidth() / 2)) - mouseX;
        float followY = (float) (((float) (this.getY() + (this.height / 2.5)) - mouseY) - (7.5 * scale * AlinLib.MINECRAFT.options.guiScale().get()));
        float rotation =
                //#if MC <= 12105
                //$$ 0f
                //#else
                180f
                //#endif
                ;
        if (!followMouse) followX = followY = 0;
        if (rotate) {
            followX = followY = 0;
            rotation = getRotation();
        }

        //#if MC <= 12105
        //$$ guiGraphics.pose().pushPose();
        //#else
        Matrix3x2f matrix3x2f = guiGraphics.pose().pushMatrix();
        PoseStack pose = new PoseStack(); 
        //#endif
        try {
            GuiEntityRenderer.drawModel(
                    //#if MC <= 12105
                    //$$ guiGraphics.pose()
                    //#else
                    guiGraphics
                    //#endif
                    , this.getX()
                    //#if MC <= 12105
                    //$$+ (this.getWidth() / 2)
                    //#endif
                    , this.getY()
                    //#if MC <= 12105
                    //$$+this.height-25
                    //#endif
                    ,
                    size, rotation, followX, followY, ClovSkins.currentSkin == null ? ClovSkins.safeSkinOption : ClovSkins.currentSkin, partialTicks
            );
        } catch (Exception ignored){}
        //#if MC <= 12105
        //$$ guiGraphics.pose().popPose();
        //#else
        guiGraphics.pose().popMatrix();
        //#endif
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

    @Override
    public void onPress() {
        AlinLib.MINECRAFT.setScreen(new MainScreen(AlinLib.MINECRAFT.screen));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
