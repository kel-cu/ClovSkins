package ru.kelcuprum.clovskins.client.gui.cicada;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
//#if MC >= 12109
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.player.PlayerModelType;
//#else
//$$ import net.minecraft.client.resources.PlayerSkin;
//#endif
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier;
import org.joml.*;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.clovskins.client.api.SkinOption;
import ru.kelcuprum.clovskins.client.models.FakePlayerModel;

import java.io.IOException;
import java.lang.Math;
import java.security.SecureRandom;

import static net.minecraft.world.entity.Pose.STANDING;

public class GuiEntityRenderer {
    //#if MC >= 12106
    public static void drawModel(GuiGraphicsExtractor guiGraphics, int x, int y, int size, float rotation, double mouseX, double mouseY, SkinOption skinOption, float tick) {
        try {
            int size5Percent = (int) (size * 0.35);
//            guiGraphics.guiRenderState.submitPicturesInPictureState(state);
            var modelData = PlayerModel.createMesh(CubeDeformation.NONE, skinOption.model ==
                    //#if MC < 12109
                    //$$PlayerSkin.Model
                    //#else
                    PlayerModelType
                            //#endif
                            .SLIM);
            FakePlayerModel fakePlayerModel = new FakePlayerModel(LayerDefinition.create(modelData, 64, 64).bakeRoot(), skinOption.model ==
                    //#if MC < 12109
                    //$$PlayerSkin.Model
                    //#else
                    PlayerModelType
                            //#endif
                            .SLIM);
            Model.Simple simple = new Model.Simple(fakePlayerModel.root(), fakePlayerModel.renderType());
            guiGraphics.skin(simple, skinOption.getTextureSkin(), size, 0, rotation, 0f, x-size5Percent, y - size5Percent, x + size+size5Percent, y + (size * 2));
        } catch (Exception ex) {

        }
    }
    //#endif
}
