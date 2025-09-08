package ru.kelcuprum.clovskins.client.gui.screen.select;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.selector.SelectorBuilder;
import ru.kelcuprum.alinlib.gui.components.buttons.Button;
import ru.kelcuprum.alinlib.gui.components.editbox.EditBox;
import ru.kelcuprum.alinlib.gui.components.selector.SelectorButton;
import ru.kelcuprum.alinlib.gui.screens.ConfirmScreen;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.api.SkinOption;
import ru.kelcuprum.clovskins.client.gui.cicada.GuiEntityRenderer;

import java.io.File;
import java.util.Arrays;

import static com.mojang.blaze3d.Blaze3D.getTime;
import static ru.kelcuprum.alinlib.gui.Colors.BLACK_ALPHA;
import static ru.kelcuprum.alinlib.gui.Icons.DONT;
import static ru.kelcuprum.clovskins.client.ClovSkins.getPath;

public class EditSkinPreset extends Screen {
    public Screen parent;
    public SkinOption skinOption;
    public final SkinOption skinOptionOriginal;
    public String key;
    public boolean isSelected;
    public boolean isDeleted = false;
    public EditSkinPreset(Screen screen, SkinOption skinOption, String key, Boolean isSelected) {
        super(Component.translatable("clovskins.edit"));
        this.parent = screen;
        this.skinOption = skinOption;
        this.skinOptionOriginal = skinOption;
        this.key = key;
        this.isSelected = isSelected;
    }

    Button file;
    EditBox editBox;
    SelectorButton selectorType;
    public static String[] skinTypes = new String[]{
            "nickname",
            "url",
            "file"
    };


    @Override
    protected void init() {
        int playerHeight = (int) ((height - 20 - font.lineHeight - 30) * 0.8);
        int pageSize = this.width-70-playerHeight/2;
        int componentSize =  Math.min(310, pageSize - 10);
        int x = ((pageSize - componentSize) / 2)+playerHeight/2+40;
        int buttonsHeight = 80+15;
        int y = height/2-buttonsHeight/2;

        //
        addRenderableWidget(new EditBoxBuilder(Component.translatable("clovskins.edit.name"), (s) -> skinOption.name = s).setValue(skinOption.name).setWidth(componentSize).setPosition(x, y).build());
        y+=25;
        editBox = (EditBox) addRenderableWidget(new EditBoxBuilder(Component.translatable("clovskins.edit.url"), (s) -> skinOption.setSkinTexture(skinOption.type, s)).setValue(skinOption.skin).setWidth(componentSize).setPosition(x, y).build());
        file = (Button) addRenderableWidget(new ButtonBuilder(Component.translatable("clovskins.edit.file_select"), (s) -> {
            openTrackEditor();
        }).setSprite(GuiUtils.getResourceLocation("clovskins", "texture/icons/file.png")).setWidth(20).setPosition(x+componentSize+5, y).build());
        y+=25;
        selectorType = (SelectorButton) addRenderableWidget(new SelectorBuilder(Component.translatable("clovskins.edit.type")).setList(new String[] {
            Component.translatable("clovskins.edit.type.nickname").getString(),
                "URL",
                "File"
        }).setValue(Arrays.stream(skinTypes).toList().indexOf(skinOption.toJSON().get("type").getAsString())).setOnPress((s) -> {
            skinOption.setSkinTexture(s.getPosition() == 0 ? SkinOption.SkinType.NICKNAME : s.getPosition() == 1 ? SkinOption.SkinType.URL : SkinOption.SkinType.FILE, skinOption.skin);
        }).setWidth(componentSize/2-2).setPosition(x,y).build());
        addRenderableWidget(new SelectorBuilder(Component.translatable("clovskins.edit.model")).setList(new String[] {
                "Default",
                "Slim"
        }).setOnPress((s) -> skinOption.model = s.getPosition() == 0 ? PlayerSkin.Model.WIDE : PlayerSkin.Model.SLIM).setValue(skinOption.model == PlayerSkin.Model.WIDE ? 0 : 1).setWidth(componentSize/2-2).setPosition(x+componentSize/2+2, y).build());
        addRenderableWidget(new ButtonBuilder(Component.translatable("clovskins.edit.remove"), (s) -> {
            AlinLib.MINECRAFT.setScreen(new ConfirmScreen(parent, Component.translatable("clovskins.edit.remove.title"), Component.translatable("clovskins.edit.remove.description"), (b) -> {
                if(b){
                    this.isDeleted = true;
                    onClose();
                }
            }));
        }).setSprite(DONT).setWidth(20).setPosition(x+componentSize+5, y).build());
        y+=25;
        addRenderableWidget(new ButtonBuilder(Component.translatable("clovskins.edit.select_cape"), (s) -> AlinLib.MINECRAFT.setScreen(new SelectCape(AlinLib.MINECRAFT.screen, skinOption, key))).setWidth(componentSize).setPosition(x, y).build());
    }

    public void openTrackEditor(){
        MemoryStack stack = MemoryStack.stackPush();
        PointerBuffer filters = stack.mallocPointer(1);
        filters.put(stack.UTF8("*.png"));

        filters.flip();
        File defaultPath = new File(getPath()).getAbsoluteFile();
        String defaultString = defaultPath.getAbsolutePath();
        if(defaultPath.isDirectory() && !defaultString.endsWith(File.separator)){
            defaultString += File.separator;
        }

        String result = TinyFileDialogs.tinyfd_openFileDialog(Component.translatable("clovskins.edit.file_selector").getString(), defaultString, filters, Component.translatable("clovskins.edit.file_selector.description").getString(), false);
        if(result == null) return;
        File file = new File(result);
        if(file.exists()) {
            editBox.setValue(file.getAbsolutePath());
            skinOption.type = SkinOption.SkinType.FILE;
            selectorType.setPosition(2);
        }
        else new ToastBuilder().setIcon(DONT).setType(ToastBuilder.Type.ERROR).setTitle(Component.literal("ClovSkins")).setMessage(Component.translatable("clovskins.edit.file_not_exist")).buildAndShow();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderBackground(guiGraphics, i, j, f);
        int playerHeight = (int) ((height - 20 - font.lineHeight - 30) * 0.8);
        guiGraphics.fill(0, 0, width, height, BLACK_ALPHA);
        guiGraphics.fill(0, height/2-playerHeight/2-15, playerHeight/2+30, height/2+playerHeight/2+15, BLACK_ALPHA);
        guiGraphics.drawCenteredString(font, Component.translatable("clovskins.edit"), width/2, 15, -1);

        //#if MC <= 12105
        //$$ guiGraphics.pose().pushPose();
        //$$ guiGraphics.pose().translate(0, 0, 1900.0);
        //#else
        Matrix3x2f matrix3x2f = guiGraphics.pose().pushMatrix();
        //#endif
        renderPlayer(guiGraphics, 15, height/2-playerHeight/2, playerHeight/2, f);
        //#if MC <= 12105
        //$$ guiGraphics.pose().translate(0, 0, -1900.0);
        //$$ guiGraphics.pose().popPose();
        //#else
        guiGraphics.pose().popMatrix();
        //#endif
    }

    public double currentTime = getTime();
    public void renderPlayer(GuiGraphics guiGraphics, int x, int y, int size, float tick){
        float rotation = (float) ((getTime() - currentTime) * 35.0f);
        try {
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
                        , x
                        //#if MC <= 12105
                        //$$ + (size / 2)
                        //#endif
                        , y
                        //#if MC <= 12105
                        //$$+size*2+15
                        //#else
                                +15
                        //#endif
                        ,
                        size, rotation, 0, 0, skinOption, tick
                );
            } catch (Exception ignored){}
            //#if MC <= 12105
            //$$ guiGraphics.pose().popPose();
            //#else
            guiGraphics.pose().popMatrix();
            //#endif
        } catch (Exception ignored){}
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void onClose() {
        try {
            if(isDeleted){
                if(ClovSkins.currentSkin == skinOptionOriginal) {
                    ClovSkins.currentSkin = ClovSkins.skinOptions.getOrDefault("default", ClovSkins.safeSkinOption);
                    ClovSkins.config.setString("SELECTED", "default");
                    ClovSkins.currentSkin.uploadToMojangAPI();
                }
                skinOption.delete();
                ClovSkins.skinOptions.remove(key);
                AlinLib.MINECRAFT.setScreen(parent);
            } else {
                skinOption.save();
                if(ClovSkins.currentSkin == skinOptionOriginal) {
                    if(!skinOption.equals(skinOptionOriginal)) skinOption.uploadToMojangAPI();
                    ClovSkins.currentSkin = skinOption;
                }
                ClovSkins.skinOptions.put(key, skinOption);
            }
        } catch (Exception exception){
            exception.printStackTrace();
        }
        AlinLib.MINECRAFT.setScreen(parent);
    }
}
