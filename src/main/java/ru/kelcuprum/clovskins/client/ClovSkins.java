package ru.kelcuprum.clovskins.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.nimbusds.jose.util.Resource;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.SkinCustomizationScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.AlinLogger;
import ru.kelcuprum.alinlib.WebAPI;
import ru.kelcuprum.alinlib.api.events.client.ClientLifecycleEvents;
import ru.kelcuprum.alinlib.api.events.client.GuiRenderEvents;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.alinlib.info.Player;
import ru.kelcuprum.alinlib.utils.GsonHelper;
import ru.kelcuprum.clovskins.client.api.SkinOption;
import ru.kelcuprum.clovskins.client.gui.screen.SkinCustomScreen;
import ru.kelcuprum.clovskins.client.gui.style.VanillaLikeStyle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import static ru.kelcuprum.alinlib.gui.Icons.WARN;
import static ru.kelcuprum.alinlib.gui.Icons.WARNING;
import static ru.kelcuprum.alinlib.utils.GsonHelper.getStringInJSON;
import static ru.kelcuprum.clovskins.client.api.SkinOption.SkinType.NICKNAME;
import static ru.kelcuprum.clovskins.client.api.SkinOption.SkinType.URL;

public class ClovSkins implements ClientModInitializer {
    public static Config pathConfig = new Config("config/ClovSkins/path.config.json");
    public static Config config = new Config(getPath()+"/config.json");
    public static AlinLogger logger = new AlinLogger("ClovSkins");

    public static PlayerSkin defaultSkin = DefaultPlayerSkin.getDefaultSkin();
    public static String cape;

    public static HashMap<String, ResourceLocation> cacheResourceLocations = new HashMap<>();
    public static SkinOption currentSkin = null;
    public static SkinOption safeSkinOption = new SkinOption("default", "MHF_Steve", "", PlayerSkin.Model.SLIM, NICKNAME, new File(getPath()+"/skins/safe.temp.json"));
    public static HashMap<String, SkinOption> skinOptions = new HashMap<>();
    public static VanillaLikeStyle vanillaLikeStyle = new VanillaLikeStyle();

    public static float TICKS = 0;

    public static String getPath(){
        String path = pathConfig.getBoolean("USE_GLOBAL", false) && !FabricLoader.getInstance().isDevelopmentEnvironment()? (
                System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win") ?
                        pathConfig.getString("PATH", "{HOME}/ClovSkins") :
                        pathConfig.getString("PATH.UNIX", "/home/${USER}/ClovSkins")
        ) : "config/ClovSkins";
        path = path.replace("{HOME}", System.getProperty("user.home")).replace("{USER}", System.getProperty("user.name"));
        return path;
    }

    @Override
    public void onInitializeClient() {
        // Логи
        if(AlinLib.isAprilFool()) logger.log("Welcome to the circus!");
        else logger.log("Welcome to hell!");
        // Проверка и создание
        checkFolders();
        // Регистрация ивентов
        ClientLifecycleEvents.CLIENT_FULL_STARTED.register((s) -> {
            defaultSkin = s.getSkinManager().getInsecureSkin(s.getGameProfile());
            try {
                loadCapes();
            } catch (Exception exception){
                exception.printStackTrace();
            }
            loadSkins();
            if(FabricLoader.getInstance().isDevelopmentEnvironment()){
                new ToastBuilder().setTitle(Component.translatable("clovskins"))
                        .setMessage(Component.translatable("clovskins.warning.test_mode")).setIcon(WARNING)
                        .setType(ToastBuilder.Type.WARN).setDisplayTime(10000).buildAndShow();
            }
        });
    }

    public void checkFolders(){
        File file = new File(getPath()+"/skins");
        File fileTemo = new File(getPath()+"/temp");
        if(!file.exists()) {
            try {
                Files.createDirectories(file.toPath());
            } catch (Exception exception){
                exception.printStackTrace();
                AlinLib.MINECRAFT.delayCrash(new CrashReport("Exception while creating a folder", exception.getCause()));
            }
        }
        if(!fileTemo.exists()) {
            try {
                Files.createDirectories(fileTemo.toPath());
            } catch (Exception exception){
                exception.printStackTrace();
                AlinLib.MINECRAFT.delayCrash(new CrashReport("Exception while creating a folder", exception.getCause()));
            }
        }
    }

    public void loadSkins(){
        SkinOption defaultSkin;
        if(Player.isLicenseAccount()) {
            JsonObject defaultSkinData = new JsonObject();
            defaultSkinData.addProperty("name", Player.getName());
            defaultSkinData.addProperty("skin", Player.getName());
            defaultSkinData.addProperty("cape", cape);
            defaultSkinData.addProperty("type", "nickname");
            defaultSkin = SkinOption.getSkinOption(defaultSkinData, new File(getPath()+"/skins/default.json"));
        }
        else defaultSkin = new SkinOption("Default skin", "MHF_Steve", "", PlayerSkin.Model.SLIM, NICKNAME, new File(getPath()+"/skins/default.json"));
        try {
            defaultSkin.getTexture();
        } catch (Exception ignored){}
        skinOptions.put("default", defaultSkin);
        File skins = new File(getPath()+"/skins");
        if(!skins.exists() || !skins.isDirectory()) return;
        for(File file : skins.listFiles()){
            if(file.isFile() && file.getName().toLowerCase().endsWith(".json")){
                try {
                    SkinOption skinOption = SkinOption.getSkinOption(file);
                    skinOption.getTexture();
                    skinOptions.put(file.getName().substring(0, file.getName().length()-5), skinOption);
                } catch (Exception ex){
                    logger.log("Error during file load");
                    ex.printStackTrace();
                }
            }
        }
        currentSkin = skinOptions.getOrDefault(config.getString("SELECTED", "default"), null);
        if(currentSkin == null) {
            currentSkin = skinOptions.get("default");
            config.setString("SELECTED", "default");
        }
    }
    public static HashMap<String, ResourceLocation> capes = new HashMap<>();
    public static HashMap<String, String> capesAliases = new HashMap<>();
    public void loadCapes() throws IOException, InterruptedException {
        if(Player.isLicenseAccount()){
            JsonObject jsonObject = WebAPI.getJsonObject(HttpRequest.newBuilder(URI.create("https://api.minecraftservices.com/minecraft/profile")).header("Authorization", "Bearer "+  AlinLib.MINECRAFT.getUser().getAccessToken()));
            logger.log(jsonObject.toString());
            if(!GsonHelper.jsonElementIsNull("capes", jsonObject)){
                for(JsonElement element : jsonObject.getAsJsonArray("capes")){
                    JsonObject data = (JsonObject) element;
                    String name = getStringInJSON("alias", data, "");
                    String id = getStringInJSON("id", data, "");
                    String url = getStringInJSON("url", data, "");
                    if(getStringInJSON("state", data, "INACTIVE").equalsIgnoreCase("active")) cape = id;
                    ResourceLocation location = GuiUtils.getResourceLocation("clovskins", "cape_"+id.toLowerCase().replace("-", "_"));
                    capesAliases.put(id, name);
                    BufferedImage image = ImageIO.read(new URL(url));
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", byteArrayOutputStream);
                    InputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                    NativeImage nativeImage = NativeImage.read(is);
                    DynamicTexture texture =
                            //#if MC >= 12105
                            new DynamicTexture(() -> name, nativeImage);
                            //#else
                            //$$ new DynamicTexture(nativeImage);
                            //#endif
                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().getTextureManager().register(location, texture));
                    ClovSkins.cacheResourceLocations.put(url, location);
                    capes.put(id, location);
                }
            }
        } else{
            if(FabricLoader.getInstance().isDevelopmentEnvironment()){
                capes.put("test", GuiUtils.getResourceLocation("clovskins", "cape/test.png"));
                capesAliases.put("test", "Тестовый плащ");
            }
        }
    }

    public static Screen getSkinCustom(Screen parent){
        return config.getBoolean("MENU.CHANGE_DEFAULT_UI", true) ? new SkinCustomScreen(parent, AlinLib.MINECRAFT.options) : new SkinCustomizationScreen(parent, AlinLib.MINECRAFT.options);
    }
}
