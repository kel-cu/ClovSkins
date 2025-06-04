package ru.kelcuprum.clovskins.client.gui.screen.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Level;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBooleanBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.button.ButtonBuilder;
import ru.kelcuprum.alinlib.gui.components.builder.editbox.EditBoxBuilder;
import ru.kelcuprum.alinlib.gui.components.text.CategoryBox;
import ru.kelcuprum.alinlib.gui.screens.ConfigScreenBuilder;
import ru.kelcuprum.clovskins.client.ClovSkins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class MainConfigs {
    public static Screen build(Screen parent){
        ConfigScreenBuilder screenBuilder = new ConfigScreenBuilder(parent, Component.literal("ClovSkins"));
        screenBuilder.setCategoryTitle(Component.translatable("clovskins.config.main"));
        screenBuilder.addPanelWidget(new ButtonBuilder(Component.translatable("clovskins.config.main"), (s) -> AlinLib.MINECRAFT.setScreen(MainConfigs.build(parent))));
        screenBuilder.addPanelWidget(new ButtonBuilder(Component.translatable("clovskins.config.menu"), (s) -> AlinLib.MINECRAFT.setScreen(MenuConfigs.build(parent))));

        screenBuilder.addWidget(new CategoryBox(Component.translatable("clovskins.config.main.data"))
                .addValue(new ButtonBooleanBuilder(Component.translatable("clovskins.config.main.data.use_global"), false).setConfig(ClovSkins.pathConfig, "USE_GLOBAL"))
                .addValue(new EditBoxBuilder(Component.translatable("clovskins.config.main.data.path")).setValue("{HOME}/ClovSkins").setConfig(ClovSkins.pathConfig, "PATH"))
                .addValue(new EditBoxBuilder(Component.translatable("clovskins.config.main.data.path.unix")).setValue("/home/{USER}/ClovSkins").setConfig(ClovSkins.pathConfig, "PATH.UNIX"))
                .addValue(new ButtonBuilder(Component.translatable("clovskins.config.main.data.move")).setOnPress((s) -> {
                    try {
                        if(!new File(ClovSkins.getPath()).exists()) Files.createDirectory(Path.of(ClovSkins.getPath()));
                        if(new File("config/ClovSkins/config.json").exists()) Files.copy(Path.of("config/ClovSkins/config.json"), Path.of(ClovSkins.getPath()+"/config.json"), REPLACE_EXISTING);
                        if(new File("config/ClovSkins/skins").exists()) {
                            if(!new File(ClovSkins.getPath()+"/skins").exists()) Files.copy(Path.of("config/ClovSkins/skins"), Path.of(ClovSkins.getPath()+"/skins"), REPLACE_EXISTING);
                            for(File file : new File("config/ClovSkins/skins").listFiles()) Files.copy(file.toPath(), Path.of(ClovSkins.getPath()+"/skins/"+file.getName()), REPLACE_EXISTING);
                        }
                        ClovSkins.config = new Config(ClovSkins.getPath()+"/config.json");
                        AlinLib.MINECRAFT.setScreen(build(parent));
                    } catch (IOException e) {
                        ClovSkins.logger.log(e.getMessage(), Level.ERROR);
                        e.printStackTrace();
                    }
                }))
                .addValue(new ButtonBuilder(Component.translatable("clovskins.config.main.data.update_config")).setOnPress((s) -> {
                    ClovSkins.config = new Config(ClovSkins.getPath()+"/config.json");
                    AlinLib.MINECRAFT.setScreen(build(parent));
                })));
        return screenBuilder.build();
    }
}
