package ru.kelcuprum.clovskins.client.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.clovskins.client.ClovSkins;

import java.util.function.Supplier;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {
    @Inject(method = "openScreenButton", at= @At(value = "HEAD"), cancellable = true)
    void openScreenButton(Component component, Supplier<Screen> supplier, CallbackInfoReturnable<Button> cir){
        if(component.contains(Component.translatable("options.skinCustomisation"))) {
            cir.setReturnValue(Button.builder(component, (button) -> AlinLib.MINECRAFT.setScreen(ClovSkins.getSkinCustom((Screen) (Object) this))).build());
        }
    }
}
