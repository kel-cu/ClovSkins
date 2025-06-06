package ru.kelcuprum.clovskins.client.gui.screen;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.info.Player;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.gui.cicada.DummyClientPlayerEntity;
import ru.kelcuprum.clovskins.client.gui.cicada.GuiEntityRenderer;
import ru.kelcuprum.clovskins.client.gui.components.PlayerWidget;
import ru.kelcuprum.clovskins.client.gui.screen.config.MainConfigs;
import ru.kelcuprum.clovskins.client.gui.screen.select.SelectSkinPreset;

import java.util.UUID;

import static com.mojang.blaze3d.Blaze3D.getTime;
import static ru.kelcuprum.alinlib.gui.Colors.BLACK_ALPHA;
import static ru.kelcuprum.alinlib.gui.Icons.EXIT;

public class MainScreen extends Screen {
    public final Screen screen;
    public MainScreen(Screen parent) {
        super(Component.empty());
        this.screen = parent;
    }
    PlayerWidget pb;
    @Override
    protected void init() {
        int y = height-25;
        int buttonsWidth = (int) (width * 0.75 - 10) / 3;
        int buttonsPlace = buttonsWidth*3 + 10;
        int x = (int) (width * 0.5 - (double) buttonsPlace / 2);
//        pb = addRenderableWidget(new PlayerButton(50, height/2-100, 100, true, true, false, false));
//        pb.active = false;

        addRenderableWidget(new ButtonBuilder(Component.translatable("clovskins.main.select"), (s) -> AlinLib.MINECRAFT.setScreen(new SelectSkinPreset(this))).setSize(buttonsWidth, 20).setPosition(x, y).build());
        x+=buttonsWidth+5;
        addRenderableWidget(new ButtonBuilder(Component.translatable("clovskins.main.options"), (s) -> AlinLib.MINECRAFT.setScreen(ClovSkins.getSkinCustom(this))).setSize(buttonsWidth, 20).setPosition(x, y).build());
        x+=buttonsWidth+5;
        addRenderableWidget(new ButtonBuilder(Component.translatable("clovskins.main.configs"), (s) -> AlinLib.MINECRAFT.setScreen(MainConfigs.build(this))).setSize(buttonsWidth, 20).setPosition(x, y).build());
        x+=buttonsWidth+5;
        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_BACK, (s) -> onClose()).setSprite(EXIT).setSize(20, 20).setPosition(x, y).build());
        y-=25;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        guiGraphics.fill(0, 0, width, height, BLACK_ALPHA);
    }
    public static DummyClientPlayerEntity entity;
    public static UUID SillyUUID = UUID.randomUUID();
    public static double currentTime = getTime();
    public static void renderPlayer(GuiGraphics guiGraphics, int x, int y, int size){
        PlayerSkin playerSkin = AlinLib.MINECRAFT.getSkinManager().getInsecureSkin(AlinLib.MINECRAFT.getGameProfile());
        float rotation = (float) ((getTime() - currentTime) * 35.0f);
        try {
            if(entity == null) entity = new DummyClientPlayerEntity(null, SillyUUID, ClovSkins.currentSkin == null ? playerSkin : ClovSkins.currentSkin.getPlayerSkin(), AlinLib.MINECRAFT.options, false);
            else entity.setSkin(ClovSkins.currentSkin == null ? playerSkin : ClovSkins.currentSkin.getPlayerSkin());
            guiGraphics.pose().pushPose();
//            GuiEntityRenderer.drawEntity(
//                    guiGraphics.pose(), x + (size / 2), y+size*2,
//                    size, rotation, 0, 0, entity
//            );
            GuiEntityRenderer.drawModel(
                    guiGraphics.pose(), x + (size / 2), y+size*2,
                    size, rotation, 0, 0, ClovSkins.currentSkin == null ? ClovSkins.safeSkinOption : ClovSkins.currentSkin
            );
            guiGraphics.pose().popPose();
        } catch (Exception ignored){}
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(font, Component.translatable("clovskins.main", FabricLoader.getInstance().isDevelopmentEnvironment() ? System.getProperty("user.name") : Player.getName()), width / 2, 10, -1);
        int playerHeight = (int) ((height - 20 - font.lineHeight - 30) * 0.8);
        renderPlayer(guiGraphics, width/2-playerHeight/4, height/2-playerHeight/2+10, playerHeight/2);
    }

    @Override
    public void onClose() {
        if(minecraft == null) return;
        minecraft.setScreen(screen);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        if(pb != null) pb.setRotation((float) (pb.getRotation()-f));

        return super.mouseDragged(d, e, i, f, g);
    }
}
