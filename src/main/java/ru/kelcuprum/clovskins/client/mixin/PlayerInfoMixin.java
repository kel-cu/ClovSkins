package ru.kelcuprum.clovskins.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
//#if MC >= 12109
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
//#else
//$$ import net.minecraft.client.resources.PlayerSkin;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.clovskins.client.ClovSkins;
import java.util.function.Supplier;

@Mixin(value = PlayerInfo.class)
public class PlayerInfoMixin {
    //#if MC < 12109
//$$    @Inject(method = "createSkinLookup", at=@At("TAIL"), cancellable = true)
//$$    private static void createSkinLookup(GameProfile gameProfile, CallbackInfoReturnable<Supplier<PlayerSkin>> cir){
//$$        PlayerSkin defaultSkin = cir.getReturnValue().get();
//$$        cir.setReturnValue(() -> {
//$$            PlayerSkin skin = defaultSkin;
//$$            if (gameProfile.getId().equals(AlinLib.MINECRAFT.player.getGameProfile().getId())) {
//$$                try {
//$$                    ClovSkins.defaultSkin = defaultSkin;
//$$                    if (ClovSkins.currentSkin != null) {
//$$                        try {
//$$                            skin = ClovSkins.currentSkin.getPlayerSkin();
//$$                        } catch (Exception ignored) {
//$$                        }
//$$                    }
//$$                } catch (Exception ex) {
//$$                    ex.printStackTrace();
//$$                }
//$$            } else {
//$$                if (ClovSkins.playerSkins.containsKey(gameProfile.getId())) {
//$$                    try {
//$$                        skin = ClovSkins.playerSkins.get(gameProfile.getId()).getPlayerSkin();
//$$                    } catch (Exception ignored) {
//$$                    }
//$$                }
//$$            }
//$$            return skin;
//$$        });
//$$    }
    //#endif
}
