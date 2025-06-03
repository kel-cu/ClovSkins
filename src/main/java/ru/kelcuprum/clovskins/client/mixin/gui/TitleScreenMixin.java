package ru.kelcuprum.clovskins.client.mixin.gui;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.SpriteIconButton;
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

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("TAIL"))
    void createPauseMenu(CallbackInfo ci) {
        if(ClovSkins.config.getBoolean("MENU.TITLE", true)) {
            int x = 0;
            int y = 0;
            for (GuiEventListener widget : this.children) {
                if (widget instanceof SpriteIconButton) {
                    if (((SpriteIconButton) widget).sprite.equals(GuiUtils.getResourceLocation("icon/accessibility"))) {
                        x = ((AbstractWidget) widget).getRight() + 5;
                        y = ((AbstractWidget) widget).getY();
                    }
                }
            }
            addRenderableWidget(new PlayerButton(x, y - 88, 50));
        }
    }
}
