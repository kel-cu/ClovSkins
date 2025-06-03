package ru.kelcuprum.clovskins.client.mixin.gui;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.gui.components.PlayerButton;
import ru.kelcuprum.clovskins.client.mixin.AccessorGridLayout;

import java.util.List;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {

    protected PauseScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;visitWidgets(Ljava/util/function/Consumer;)V"))
    void createPauseMenu(CallbackInfo ci, @Local GridLayout gridLayout) {
        if(ClovSkins.config.getBoolean("MENU.PAUSE", true)){
            if (gridLayout != null) {
                final List<AbstractWidget> buttons = ((AccessorGridLayout) gridLayout).getChildren();
                int vanillaButtonsY = this.height / 4 + 72 - 16 + 1;
                int vanillaButtonsX = 0;
                for (AbstractWidget widget : buttons) {
                    if (widget.getMessage().contains(Component.translatable("menu.returnToMenu")) || widget.getMessage().contains(Component.translatable("menu.disconnect"))) {
                        vanillaButtonsY = widget.getY();
                        vanillaButtonsX = widget.getRight() + 30;
                    }
                }
                buttons.add(new PlayerButton(vanillaButtonsX, vanillaButtonsY-100, 50));
            }
        }
    }
}
