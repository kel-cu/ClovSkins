package ru.kelcuprum.clovskins.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
//#if MC >= 12106
import net.minecraft.client.renderer.RenderPipelines;
//#endif
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.styles.AbstractStyle;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.api.SkinOption;

import static ru.kelcuprum.alinlib.gui.Icons.DONT;

public class NonCapeButton extends AbstractButton {
    int size;
    public final String keySkin;
    public SkinOption skinOption;

    public NonCapeButton(int x, int y, int width, int height, SkinOption skinOption, String keySkin) {
        super(x, y, width, height, Component.translatable("clovskins.select.cape.inactive"));
        if (width > height) this.size = (width - 10) / 10 * 16;
        else this.size = (height - 30);
        this.keySkin = keySkin;
        this.skinOption = skinOption;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        AbstractStyle style = GuiUtils.getSelected();
        style.renderBackground$widget(guiGraphics, getX(), getY(), width, height, active, isHovered);
        if (GuiUtils.isDoesNotFit(getMessage(), width, 20))
            renderScrollingString(guiGraphics, AlinLib.MINECRAFT.font, getMessage(), style.getTextColor(active), getY(), getY() + 20);
        else
            GuiUtils.drawCenteredString(guiGraphics, AlinLib.MINECRAFT.font, getMessage(), getX() + width / 2, getY() + 7, style.getTextColor(active), true);
        if(skinOption.cape.isBlank()) guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         GuiUtils.getResourceLocation("textures/gui/sprites/icon/checkmark.png"), getRight()-14, getY()+5, 0f, 0f, 9, 8, 9, 8);
        guiGraphics.blit(
                        //#if MC >= 12106
                        RenderPipelines.GUI_TEXTURED,
                        //#elseif MC >= 12102
                        //$$ RenderType::guiTextured,
                        //#endif
                         DONT, getX() + getWidth() / 2 - 20, getY() + getHeight() / 2 - 20, 0, 0, 40, 40, 40, 40);
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int i, int j) {
        int k = this.getX() + i;
        int l = this.getX() + this.getWidth() - i;
        renderScrollingString(guiGraphics, font, this.getMessage(), k, this.getBottom() - 20, l, this.getBottom(), j);
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, Component msg, int j, int y, int bottom) {
        int k = this.getX() + 2;
        int l = this.getX() + this.getWidth() - 2;
        renderScrollingString(guiGraphics, font, msg, k, y, l, bottom, j);
    }

    public void onClick(double d, double e) {
        if (ClovSkins.currentSkin == skinOption) skinOption.cape = "";
        skinOption.cape = "";
        ClovSkins.skinOptions.put(keySkin, skinOption);
    }

    @Override
    public void onPress() {
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
