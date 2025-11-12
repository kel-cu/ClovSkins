package ru.kelcuprum.clovskins.client.mixin;

import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.clovskins.client.ClovSkins;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(value = SkinManager.class)
public class SkinManagerMixin {
    //#if MC >= 12109
    @Inject(method = "createLookup", at=@At("TAIL"), cancellable = true)
    private static void createSkinLookup(GameProfile gameProfile, boolean requireSecure, CallbackInfoReturnable<Supplier<PlayerSkin>> cir){
        PlayerSkin defaultSkin = cir.getReturnValue().get();
        cir.setReturnValue(() -> {
            PlayerSkin skin = defaultSkin;
            if (gameProfile.id().equals(AlinLib.MINECRAFT.player.getGameProfile().id())) {
                try {
                    ClovSkins.defaultSkin = defaultSkin;
                    if (ClovSkins.currentSkin != null) {
                        try {
                            skin = ClovSkins.currentSkin.getPlayerSkin();
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                if (ClovSkins.playerSkins.containsKey(gameProfile.id())) {
                    try {
                        skin = ClovSkins.playerSkins.get(gameProfile.id()).getPlayerSkin();
                    } catch (Exception ignored) {
                    }
                }
            }
            return skin;
        });
    }
    //#endif
}
