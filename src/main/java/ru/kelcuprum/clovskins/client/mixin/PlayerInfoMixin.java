package ru.kelcuprum.clovskins.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
//#if MC >= 12109
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
//#else
//$$ import net.minecraft.client.resources.PlayerSkin;
//#endif
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.clovskins.client.ClovSkins;
import java.util.function.Supplier;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.client.resources.SkinManager;

@Mixin(value = PlayerInfo.class)
public class PlayerInfoMixin {
    //#if MC < 12109
//$$    @Inject(method = "createSkinLookup", at=@At("HEAD"), cancellable = true)
//$$    private static void createSkinLookup(GameProfile gameProfile, CallbackInfoReturnable<Supplier<PlayerSkin>> cir){
//$$        cir.setReturnValue(() -> {
//$$            PlayerSkin skin = null;
//$$            if (gameProfile.getId().equals(AlinLib.MINECRAFT.player.getGameProfile().getId())) {
//$$                try {
//$$                    ClovSkins.defaultSkin = skin;
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
//$$                if (ClovSkins.playerSkins.containsKey(gameProfile.getId().toString())) {
//$$                    try {
//$$                        skin = ClovSkins.playerSkins.get(gameProfile.getId().toString()).getPlayerSkin();
//$$                    } catch (Exception ignored) {
//$$                    }
//$$                }
//$$            }
//$$            return skin == null ? createLookup(gameProfile).get() : skin;
//$$        });
//$$    }
//$$    @Unique
//$$    private static Supplier<PlayerSkin> createLookup(GameProfile profile) {
//$$        Minecraft minecraft = Minecraft.getInstance();
//$$        SkinManager skinManager = minecraft.getSkinManager();
//$$        CompletableFuture<Optional<PlayerSkin>> completableFuture = skinManager.getOrLoad(profile);
//$$        boolean bl = !minecraft.isLocalPlayer(profile.getId());
//$$        PlayerSkin playerSkin = DefaultPlayerSkin.get(profile);
//$$        return () -> {
//$$            PlayerSkin playerSkin2 = (PlayerSkin)((Optional)completableFuture.getNow(Optional.empty())).orElse(playerSkin);
//$$            return bl && !playerSkin2.secure() ? playerSkin : playerSkin2;
//$$        };
//$$    }
    //#endif
}
