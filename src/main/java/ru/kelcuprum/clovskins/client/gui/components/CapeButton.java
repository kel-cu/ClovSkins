package ru.kelcuprum.clovskins.client.gui.components;

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
import net.minecraft.resources.ResourceLocation;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.styles.AbstractStyle;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.api.SkinOption;

public class CapeButton extends AbstractButton {
    int size;
    public final String key;
    public final String keySkin;
    public final ResourceLocation cape;
    public final String name;
    public final SkinOption skinOption;

    public CapeButton(int x, int y, int width, int height, ResourceLocation cape, SkinOption skinOption, String key, String keySkin, String name) {
        super(x, y, width, height, Component.translatable("clovskins.select.edit"));
        if(width > height) this.size = (width-10) / 10 * 16;
        else this.size = (height - 30);
        this.key = key;
        this.keySkin = keySkin;
        this.cape = cape;
        this.name = name;
        this.skinOption = skinOption;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        AbstractStyle style = GuiUtils.getSelected();
        style.renderBackground$widget(guiGraphics, getX(), getY(), width, height, active, isHovered);
        if (GuiUtils.isDoesNotFit(Component.literal(name), width, 20))
            renderScrollingString(guiGraphics, AlinLib.MINECRAFT.font, Component.literal(name), style.getTextColor(active), getY(), getY()+20);
        else GuiUtils.drawCenteredString(guiGraphics, AlinLib.MINECRAFT.font, Component.literal(name), getX()+width/2, getY()+7, style.getTextColor(active), true);
        if(skinOption.cape.equals(key)) guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         GuiUtils.getResourceLocation("textures/gui/sprites/icon/checkmark.png"), getRight()-14, getY()+5, 0f, 0f, 9, 8, 9, 8);
        int capeWidth = size / 16 * 10;
        guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         cape, getX()+getWidth()/2-capeWidth/2, getY()+25, 1.0F, 1.0F, capeWidth, size, 10, 16, 64, 32);
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int i, int j) {
        int k = this.getX() + i;
        int l = this.getX() + this.getWidth() - i;
        renderScrollingString(guiGraphics, font, this.getMessage(), k, this.getBottom()-20, l, this.getBottom(), j);
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, Component msg, int j, int y, int bottom) {
        int k = this.getX() + 2;
        int l = this.getX() + this.getWidth() - 2;
        renderScrollingString(guiGraphics, font, msg, k, y, l, bottom, j);
    }

    //#if MC < 12109
    //$$public void onClick(double d, double e) {
    //#else
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        //#endif
                if(ClovSkins.currentSkin == skinOption) skinOption.cape = key;
                skinOption.cape = key;
                ClovSkins.skinOptions.put(keySkin, skinOption);
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
