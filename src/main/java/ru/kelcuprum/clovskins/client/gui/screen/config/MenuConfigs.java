package ru.kelcuprum.clovskins.client.gui.screen.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBooleanBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.clovskins.client.ClovSkins;

public class MenuConfigs {
    public static Screen build(Screen parent){
        ConfigScreenBuilder screenBuilder = new ConfigScreenBuilder(parent, Component.literal("ClovSkins"));
        screenBuilder.setCategoryTitle(Component.translatable("clovskins.config.menu"));
        screenBuilder.addPanelWidget(new ButtonBuilder(Component.translatable("clovskins.config.main"), (s) -> AlinLib.MINECRAFT.setScreen(MainConfigs.build(parent))));
        screenBuilder.addPanelWidget(new ButtonBuilder(Component.translatable("clovskins.config.menu"), (s) -> AlinLib.MINECRAFT.setScreen(MenuConfigs.build(parent))));

        screenBuilder.addWidget(new ButtonBooleanBuilder(Component.translatable("clovskins.config.menu.change_default_ui"), true).setConfig(ClovSkins.config, "MENU.CHANGE_DEFAULT_UI"));
        screenBuilder.addWidget(new ButtonBooleanBuilder(Component.translatable("clovskins.config.menu.alinlib"), false).setConfig(ClovSkins.config, "MENU.ALINLIB"));
        screenBuilder.addWidget(new ButtonBooleanBuilder(Component.translatable("clovskins.config.menu.two_one_slot"), true).setConfig(ClovSkins.config, "MENU.TWO_ONE_SLOT"));
        screenBuilder.addWidget(new ButtonBooleanBuilder(Component.translatable("clovskins.config.menu.title"), true).setConfig(ClovSkins.config, "MENU.TITLE"));
        screenBuilder.addWidget(new ButtonBooleanBuilder(Component.translatable("clovskins.config.menu.pause"), true).setConfig(ClovSkins.config, "MENU.PAUSE"));
        return screenBuilder.build();
    }
}
