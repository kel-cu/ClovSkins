package ru.kelcuprum.clovskins.client.mixin.gui;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.clovskins.client.ClovSkins;
import ru.kelcuprum.clovskins.client.gui.components.PlayerButton;
import ru.kelcuprum.clovskins.client.mixin.AccessorGridLayout;

import java.util.List;

@Mixin(value = TitleScreen.class, priority = Integer.MAX_VALUE)
public class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("RETURN"))
    void createPauseMenu(CallbackInfo ci) {
        if(ClovSkins.config.getBoolean("MENU.TITLE", true)) {
            boolean isLeft = ClovSkins.config.getBoolean("MENU.TITLE.LEFT", false);
            int x =  isLeft ? width : 0;
            int y = 0;
            for (GuiEventListener widget : this.children) {
                if (widget instanceof Button && !(widget instanceof PlainTextButton)) {
                    x = isLeft ? Math.min(x, ((AbstractWidget) widget).getX() - 55) : Math.max(x, ((AbstractWidget) widget).getRight()+5);
                    y = Math.max(y, ((AbstractWidget) widget).getY());
                }
            }
            addRenderableWidget(new PlayerButton(x, y - 100, 50));
        }
    }
}
