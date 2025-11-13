package ru.kelcuprum.clovskins.client.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.clovskins.client.ClovSkins;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(value = SkinManager.class)
public abstract class SkinManagerMixin {
    @Shadow
    public abstract CompletableFuture<Optional<PlayerSkin>> get(GameProfile profile);

    //#if MC >= 12109
    @Inject(method = "createLookup", at=@At("HEAD"), cancellable = true)
    private void createSkinLookup(GameProfile gameProfile, boolean requireSecure, CallbackInfoReturnable<Supplier<PlayerSkin>> cir){
        cir.setReturnValue(() -> {
            PlayerSkin skin = null;
            if (gameProfile.id().equals(AlinLib.MINECRAFT.player.getGameProfile().id())) {
                try {
                    ClovSkins.defaultSkin = skin;
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
                if (ClovSkins.playerSkins.containsKey(gameProfile.id().toString())) {
                    try {
                        skin = ClovSkins.playerSkins.get(gameProfile.id().toString()).getPlayerSkin();
                    } catch (Exception ignored) {}
                }
            }
            return skin == null ? createLookup(gameProfile).get() : skin;
        });
    }
    @Unique
    private Supplier<PlayerSkin> createLookup(GameProfile profile) {
        Minecraft minecraft = Minecraft.getInstance();
        SkinManager skinManager = minecraft.getSkinManager();
        CompletableFuture<Optional<PlayerSkin>> completableFuture = skinManager.get(profile);
        boolean bl = !minecraft.isLocalPlayer(profile.id());
        PlayerSkin playerSkin = DefaultPlayerSkin.get(profile);
        return () -> {
            PlayerSkin playerSkin2 = (PlayerSkin)((Optional)completableFuture.getNow(Optional.empty())).orElse(playerSkin);
            return bl && !playerSkin2.secure() ? playerSkin : playerSkin2;
        };
    }
    //#endif
}
