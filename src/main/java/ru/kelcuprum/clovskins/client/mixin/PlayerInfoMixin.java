package ru.kelcuprum.clovskins.client.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.api.SkinOption;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import static ru.kelcuprum.clovskins.client.ClovSkins.getPath;
import static ru.kelcuprum.clovskins.client.api.SkinOption.SkinType.URL;

@Mixin(value = PlayerInfo.class)
public class PlayerInfoMixin {
    @Inject(method = "createSkinLookup", at=@At("RETURN"), cancellable = true)
    private static void createSkinLookup(GameProfile gameProfile, CallbackInfoReturnable<Supplier<PlayerSkin>> cir){
        if(gameProfile.getId().equals(AlinLib.MINECRAFT.getGameProfile().getId())){
            ClovSkins.defaultSkin = AlinLib.MINECRAFT.getSkinManager().getInsecureSkin(gameProfile);
            if(ClovSkins.currentSkin != null) cir.setReturnValue(() -> {
                try {
                    return ClovSkins.currentSkin.getPlayerSkin();
                } catch (Exception exception){
                    return DefaultPlayerSkin.get(gameProfile);
                }
            });
        }
    }
}
