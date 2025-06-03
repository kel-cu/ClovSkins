package ru.kelcuprum.clovskins.client.gui.screen.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;

public class MenuConfigs {
    public static Screen build(Screen parent){
        ConfigScreenBuilder screenBuilder = new ConfigScreenBuilder(parent, Component.literal("ClovSkins"));
        screenBuilder.setCategoryTitle(Component.translatable("clovskins.configs.menu"));
        screenBuilder.addPanelWidget(new ButtonBuilder(Component.translatable("clovskins.config.main")));
        screenBuilder.addPanelWidget(new ButtonBuilder(Component.translatable("clovskins.config.menu")));
        return screenBuilder.build();
    }
}
