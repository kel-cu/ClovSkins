package ru.kelcuprum.clovskins.client.gui.screen.select;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.components.ConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.VerticalConfigureScrolWidget;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.api.SkinOption;
import ru.kelcuprum.clovskins.client.gui.components.SkinPresetButton;

import java.util.ArrayList;
import java.util.List;

import static ru.kelcuprum.alinlib.gui.Colors.BLACK_ALPHA;
import static ru.kelcuprum.alinlib.gui.Icons.EXIT;

public class SelectSkinPreset extends Screen {
    public Screen parent;
    public SelectSkinPreset(Screen parent) {
        super(Component.translatable("clovskins.select"));
        this.parent = parent;
    }

    private VerticalConfigureScrolWidget scroller_pages;
    private List<AbstractWidget> widgets_pages = new ArrayList<>();

    @Override
    protected void init() {
        widgets_pages = new ArrayList<>();
        int heightComponent = height - 60 - font.lineHeight - 10;
        if(ClovSkins.config.getBoolean("MENU.TWO_ONE_SLOT", true)) heightComponent = (heightComponent-4) / 2;
        int widthComponent = heightComponent / 2 + 20;
        this.scroller_pages = addRenderableWidget(new VerticalConfigureScrolWidget(0,  27+font.lineHeight, width, 3, Component.empty(), scroller -> {
            scroller.innerHeight = 5;
            int y = 35 + font.lineHeight;
            for (AbstractWidget widget : widgets_pages) {
                if (widget.visible) {
                    if(y>= height - 35) {
                        y = 35+ font.lineHeight;
                        scroller.innerHeight += (widget.getWidth() + 5);
                    }
                    widget.setPosition(((int) (scroller.innerHeight - scroller.scrollAmount())), y);
                    y+=widget.getHeight()+4;
                } else widget.setY(-widget.getHeight());
            }
            scroller.innerHeight += (widgets_pages.getLast().getWidth() + 5);
            scroller.innerHeight -= 11;
        }));
        for (String name : ClovSkins.skinOptions.keySet()) {
            SkinOption skinOption = ClovSkins.skinOptions.get(name);
            widgets_pages.add(new SkinPresetButton(-widthComponent, 35 + font.lineHeight, widthComponent, heightComponent, skinOption, name, false, true, false));
        }
        addWidgetsToScroller(widgets_pages, scroller_pages);

        addRenderableWidget(new ButtonBuilder(Component.translatable("clovskins.select.create"), (s) -> AlinLib.MINECRAFT.setScreen(new CreateSkin(this))).setWidth(200).setPosition(width/2-100, height-25).build());
        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_BACK, (s) -> onClose()).setSprite(EXIT).setSize(20, 20).setPosition(width/2+105, height-25).build());
    }

    public void addWidgetsToScroller(List<AbstractWidget> widgets) {
        addWidgetsToScroller(widgets, this.scroller_pages);
    }


    public void addWidgetsToScroller(AbstractWidget widget) {
        addWidgetsToScroller(widget, this.scroller_pages);
    }

    public void addWidgetsToScroller(List<AbstractWidget> widgets, ConfigureScrolWidget scroller) {
        for (AbstractWidget widget : widgets) addWidgetsToScroller(widget, scroller);
    }

    public void addWidgetsToScroller(AbstractWidget widget, ConfigureScrolWidget scroller) {
        widget.setY(-100);
        scroller.addWidget(widget);
        this.addWidget(widget);
    }

    public void addWidgetsToScroller(List<AbstractWidget> widgets, VerticalConfigureScrolWidget scroller) {
        for (AbstractWidget widget : widgets) addWidgetsToScroller(widget, scroller);
    }

    public void addWidgetsToScroller(AbstractWidget widget, VerticalConfigureScrolWidget scroller) {
        widget.setY(-100);
        scroller.addWidget(widget);
        this.addWidget(widget);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        guiGraphics.fill(0, 0, width, height, BLACK_ALPHA);
        guiGraphics.fill(0, 30 + font.lineHeight, width, height - 30, BLACK_ALPHA);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        int y = 15;
        if(ClovSkins.connectedSupportedServer) y-=((2+font.lineHeight)/2);
        guiGraphics.drawCenteredString(font, title, width/2, y, -1);
        if(ClovSkins.connectedSupportedServer){
            y+=(2+font.lineHeight);
            guiGraphics.drawCenteredString(font, Component.translatable("clovskins.select.server_supported"), width/2, y, -1);
        }
//        guiGraphics.enableScissor(0, 30+font.lineHeight, width, height-30);
        if (scroller_pages != null) for (AbstractWidget widget : scroller_pages.widgets) widget.render(guiGraphics, i, j, f);
//        guiGraphics.disableScissor();
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scr = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        if(!scr && scroller_pages != null && mouseY > 30+ font.lineHeight && mouseY < height-30) scr = scroller_pages.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        return scr;
    }

    @Override
    public void tick() {
        if (scroller_pages != null) scroller_pages.onScroll.accept(scroller_pages);
        super.tick();
    }

    @Override
    public void onClose() {
        if(minecraft == null) return;
        minecraft.setScreen(parent);
    }
}
