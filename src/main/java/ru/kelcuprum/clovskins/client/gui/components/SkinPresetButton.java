package ru.kelcuprum.clovskins.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//#if MC >= 12109
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
//#endif
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Matrix3x2f;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.styles.AbstractStyle;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.api.SkinOption;
import ru.kelcuprum.clovskins.client.gui.cicada.GuiEntityRenderer;
import ru.kelcuprum.clovskins.client.gui.screen.select.EditSkinPreset;
import ru.kelcuprum.clovskins.common.packets.SkinPresetPacketPayload;

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
        this.size =
                (int) ((height - 50)/2.20);
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
        if(ClovSkins.currentSkin == skinOption) guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         GuiUtils.getResourceLocation("textures/gui/sprites/icon/checkmark.png"), getRight()-14, getY()+5, 0f, 0f, 9, 8, 9, 8);
        //
        int scale = size / 45;
        float followX = (float) (this.getX() + (this.getWidth() / 2)) - mouseX;
        float followY = (float) (((float) (this.getY() + (this.height / 2.5)) - mouseY) - (7.5 * scale * AlinLib.MINECRAFT.options.guiScale().get()));
        float rotation = 0f;
        if (!followMouse) followX = followY = 0;
        if (rotate) {
            followX = followY = 0;
            rotation = getRotation();
        }

        //#if MC <= 12105
        //$$ guiGraphics.pose().pushPose();
        //$$ guiGraphics.pose().translate(0, 0, 950.0);
        //#else
        Matrix3x2f matrix3x2f = guiGraphics.pose().pushMatrix();
        PoseStack pose = new PoseStack();  pose.translate(0, 0, 950.0);
        //#endif
        try {
            int u = (width - (size))/2;
            GuiEntityRenderer.drawModel(
             guiGraphics
            , this.getX()+u, getBottom()-25-(size*2),
                    size, rotation, followX, followY, this.skinOption, partialTicks
            );
        } catch (Exception ignored){}
        //#if MC <= 12105
        //$$ guiGraphics.pose().translate(0, 0, -950.0);
        //$$ guiGraphics.pose().popPose();
        //#else
        pose.translate(0, 0, -950.0);
        guiGraphics.pose().popMatrix();
        //#endif
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

    @Override
    //#if MC < 12109
    //$$public void onClick(double d, double e) {
    //#else
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        double e = event.y();
        //#endif
        if(e <= getBottom()-20) {
            if(ClovSkins.currentSkin == skinOption){
                ClovSkins.logger.log("Skin is already set");
            } else {
                try {
                    skinOption.uploadToMojangAPI();
                    if(AlinLib.MINECRAFT.getConnection() != null)
                        ClientPlayNetworking.send(new SkinPresetPacketPayload(skinOption.toJSON(true).toString()));
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
    public void onPress(
            //#if MC >= 12109
            InputWithModifiers input
            //#endif
    ) {
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

}
