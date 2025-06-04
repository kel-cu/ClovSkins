package ru.kelcuprum.clovskins.client.gui.screen.select;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.text.TextBuilder;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.api.SkinOption;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static ru.kelcuprum.alinlib.gui.Colors.BLACK_ALPHA;
import static ru.kelcuprum.clovskins.client.ClovSkins.getPath;

public class CreateSkin extends Screen {
    public final Screen parent;
    public CreateSkin(Screen parent) {
        super(Component.translatable("clovskins.create"));
        this.parent = parent;
    }

    public String name = "";

    @Override
    protected void init() {
        int componentsWidth = width / 2;
        int x = width / 4;
        int y = height / 2 - 34;
        addRenderableWidget(new EditBoxBuilder(Component.translatable("clovskins.create.file_name"), (s) -> name = s).setValue(name).setWidth(componentsWidth).setPosition(x, y).build());
        y+=24;
        addRenderableWidget(new TextBuilder(Component.translatable("clovskins.create.file_name.description")).setType(TextBuilder.TYPE.BLOCKQUOTE).setWidth(componentsWidth).setPosition(x, y).build());
        y+=24;
        addRenderableWidget(new ButtonBuilder(CommonComponents.GUI_BACK, (s) -> onClose()).setWidth(componentsWidth / 2 -2).setPosition(x, y).build());
        addRenderableWidget(new ButtonBuilder(Component.translatable("clovskins.create.execute"), (s) -> create()).setWidth(componentsWidth / 2 -2).setPosition(x+2+componentsWidth/2, y).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(font, title, width/2, 15, -1);
    }

    public void create(){
        name = name.toLowerCase();
        if(ResourceLocation.isValidPath(name)){
            File file = new File(getPath()+"/skins/"+name+".json");
            try {
                Files.writeString(file.toPath(), ClovSkins.skinOptions.get("default").toString(), StandardCharsets.UTF_8);
                SkinOption skinOption = SkinOption.getSkinOption(file);
                ClovSkins.skinOptions.put(name, skinOption);
                AlinLib.MINECRAFT.setScreen(new EditSkinPreset(parent, skinOption, name, false));
            } catch (Exception ex){
                new ToastBuilder().setTitle(Component.literal("ClovSkins")).setMessage(Component.literal(ex.getMessage())).setType(ToastBuilder.Type.ERROR).buildAndShow();
            }
        } else {
            new ToastBuilder().setTitle(Component.literal("ClovSkins")).setMessage(Component.literal("Non [a-z0-9_.-] character in namespace of location: "+name)).setType(ToastBuilder.Type.ERROR).buildAndShow();
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        guiGraphics.fill(0,0,width,height, BLACK_ALPHA);
    }

    @Override
    public void onClose() {
        if(minecraft == null) return;
        minecraft.setScreen(parent);
    }
}
