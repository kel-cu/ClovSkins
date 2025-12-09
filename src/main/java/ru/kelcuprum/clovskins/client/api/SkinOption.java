package ru.kelcuprum.clovskins.client.api;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
//#if MC < 12109
//$$ import net.minecraft.client.resources.PlayerSkin;
//#else
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.core.ClientAsset;
//#endif
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.alinlib.info.Player;
import ru.kelcuprum.clovskins.client.ClovSkins;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;

import static ru.kelcuprum.alinlib.utils.GsonHelper.getStringInJSON;
import static ru.kelcuprum.alinlib.utils.GsonHelper.jsonElementIsNull;
import static ru.kelcuprum.clovskins.client.ClovSkins.config;
import static ru.kelcuprum.clovskins.client.ClovSkins.getPath;

public class SkinOption {
    public String name;
    public String skin;
    public String cape;
    public
    //#if MC < 12109
    //$$PlayerSkin.Model
    //#else
    PlayerModelType
            //#endif
            model;
    public PlayerSkin playerSkin;
    public SkinType type;
    public File file;

    public SkinOption(String name, String skin, String cape,
                      //#if MC < 12109
                      //$$PlayerSkin.Model
                      //#else
                      PlayerModelType
                      //#endif
                      model, SkinType type, File file){
        this.name = name;
        this.skin = skin;
        this.cape = cape;
        this.model = model;
        this.type = type;
        this.file = file;
    }
    public SkinOption(String name, PlayerSkin playerSkin, File file){
        this.name = name;
        this.playerSkin = playerSkin;
        this.skin =
                //#if MC < 12109
                //$$ playerSkin.textureUrl();
                //#else
                playerSkin.body().texturePath().toString();
                //#endif
        this.cape = "";
        this.model = playerSkin.model();
        this.type =
                //#if MC < 12109
                //$$ SkinType.URL;
                //#else
                SkinType.BASE64;
                //#endif
        this.file = file;
    }

    public PlayerSkin getPlayerSkin() throws IOException {
        ResourceLocation skin = playerSkin == null ? getTextureSkin() :
                //#if MC < 12109
                //$$ playerSkin.texture();
                //#else
                playerSkin.body().texturePath();
        //#endif
        ResourceLocation cape = playerSkin == null ? getTextureCape() :
                //#if MC < 12109
                //$$ playerSkin.capeTexture();
                //#else
                playerSkin.cape().texturePath();
        //#endif
        ResourceLocation elytra = playerSkin == null ? getTextureCape() :
        //#if MC < 12109
        //$$ playerSkin.elytraTexture();
                //#else
                playerSkin.elytra().texturePath();
        //#endif

        //#if MC < 12109
        //$$ return new PlayerSkin(skin, name, cape, elytra, model, true);
        //#else
        return new PlayerSkin(getTexture(skin), getTexture(cape), getTexture(elytra), model, true);
        //#endif
    }

    //#if MC >= 12109
    public ClientAsset.Texture getTexture(ResourceLocation resourceLocation){
        if(resourceLocation == null) return null;
        return new ClientAsset.DownloadedTexture(resourceLocation, "");
    }
    //#endif



    public ResourceLocation getTextureSkin() throws IOException {
        ResourceLocation location = GuiUtils.getResourceLocation("clovskins", "skins_"+ (file == null ? name.toLowerCase() : file.getName()));
        if(skin.isBlank()) return DefaultPlayerSkin.getDefaultTexture();
        if(!ClovSkins.cacheResourceLocations.containsKey(skin)) {
            BufferedImage image = getSourceSkin();
            if(image == null) return DefaultPlayerSkin.getDefaultTexture();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", byteArrayOutputStream);
            InputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            NativeImage nativeImage = NativeImage.read(is);
            Minecraft.getInstance().execute(() -> {
                DynamicTexture texture =
                        //#if MC >= 12105
                        new DynamicTexture(() -> name, nativeImage);
                //#else
                //$$ new DynamicTexture(nativeImage);
                //#endif
                Minecraft.getInstance().getTextureManager().register(location, texture);
            });
            ClovSkins.cacheResourceLocations.put(skin, location);
        }
        return location;
    }

    public ResourceLocation getTextureCape(){
        return ClovSkins.capes.getOrDefault(cape, null);
    }

    public BufferedImage getSourceSkin() throws IOException {
        BufferedImage image = getTexture();
        if(image == null){
            return null;
        }
        if(image.getWidth() > 64 || image.getHeight() > 64) throw new RuntimeException("Too high skin resolution!");
        return image;
    }

    public static HashMap<String, BufferedImage> resourceLocationMap = new HashMap<>();
    public static HashMap<String, Boolean> urls = new HashMap<>();
    public BufferedImage getTexture() {
        if (resourceLocationMap.containsKey(skin)) return resourceLocationMap.get(skin);
        else {
            if (!urls.getOrDefault(skin, false)) {
                    urls.put(skin, true);
                    new Thread(() -> {
                        BufferedImage image = null;
                        try {
                            switch (type){
                                case URL -> image = ImageIO.read(new URL(skin));
                                case FILE -> image = ImageIO.read(new File(skin));
                                case NICKNAME -> {
                                    try {
                                            JsonObject json = MojangAPI.getSkinURL(skin);
                                            if (json == null) return;
                                            model = jsonElementIsNull("metadata.model", json) ?
                                                    //#if MC < 12109
                                                    //$$PlayerSkin.Model
                                                    //#else
                                                    PlayerModelType
                                                            //#endif
                                                            .WIDE :
                                                    //#if MC < 12109
                                                    //$$PlayerSkin.Model
                                                    //#else
                                                    PlayerModelType
                                                            //#endif
                                                            .SLIM;
                                            image = ImageIO.read(new URL(getStringInJSON("url", json, "https://textures.minecraft.net/texture/d5c4ee5ce20aed9e33e866c66caa37178606234b3721084bf01d13320fb2eb3f")));
                                    } catch (Exception exception){
                                        exception.printStackTrace();
                                    }
                                }
                                case BASE64 -> {
                                    byte[] what = Base64.getDecoder().decode(skin);
                                    ByteArrayInputStream hell = new ByteArrayInputStream(what);
                                    image = ImageIO.read(hell);
                                }
                            }
                        } catch (Exception ex){
                            ex.printStackTrace();
                        }
                        resourceLocationMap.put(skin, image);
                    }).start();
            }
            return null;
        }
    }

    public SkinOption setSkinTexture(SkinType type, String skin){
        this.type = type;
        ClovSkins.cacheResourceLocations.remove(skin);
        this.skin = skin;
        return this;
    }

    public SkinOption setCape(String cape){
        this.cape = cape;
        return this;
    }

    public void save() throws IOException {
        if(file == null) throw new RuntimeException("File == null");
        else Files.writeString(file.toPath(), toString(), StandardCharsets.UTF_8);
    }

    public void delete() throws IOException {
        if(file == null) throw new RuntimeException("File == null");
        else Files.delete(file.toPath());
    }

    public void uploadToMojangAPI() throws IOException {
        if(!config.getBoolean("UPLOAD_TO_MOJANG", true)) return;
        if(Player.isLicenseAccount()){
            uploadSkinToMojangAPI();
            if(cape.isBlank()) hideCapeToMojangAPI();
            else activeCapeToMojangAPI();
            if(AlinLib.MINECRAFT.level != null && !AlinLib.MINECRAFT.isSingleplayer() && !AlinLib.MINECRAFT.isLocalServer())
                new ToastBuilder().setTitle(Component.literal("ClovSkins")).setMessage(Component.translatable("clovskins.upload.multiplayer")).setType(ToastBuilder.Type.WARN).setDisplayTime(15000).buildAndShow();
            new ToastBuilder().setTitle(Component.literal("ClovSkins")).setMessage(Component.translatable("clovskins.upload.done", name)).buildAndShow();
        } else ClovSkins.logger.debug("Не чет не хочу");
    }

    public void uploadSkinToMojangAPI() throws IOException{
        if(!config.getBoolean("UPLOAD_TO_MOJANG", true)) return;
        String accessToken = AlinLib.MINECRAFT.getUser().getAccessToken();
        HttpPost http = new HttpPost("https://api.minecraftservices.com/minecraft/profile/skins");
        HttpClient httpClient = HttpClientBuilder.create().build();
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("variant", model.
                //#if MC < 12109
                //$$id().equals("default")
                //#else
                       getSerializedName().equals("wide")
                //#endif
                 ? "classic" : "slim");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(getSourceSkin(), "png", byteArrayOutputStream);
        File file = new File(getPath()+"/temp/"+System.currentTimeMillis()+".png");
        Files.write(file.toPath(), byteArrayOutputStream.toByteArray());
        builder.addBinaryBody("file", file);
        http.setEntity(builder.build());
        http.addHeader("Authorization", "Bearer " + accessToken);
        HttpResponse response = httpClient.execute(http);
        if(response.getStatusLine().getStatusCode() == 200){
            ClovSkins.logger.log("ok");
            file.delete();
        }
        else {
            file.delete();
            throw new RuntimeException("[SKIN] "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        }
    }
    public void activeCapeToMojangAPI() throws IOException{
        if(!config.getBoolean("UPLOAD_TO_MOJANG", true)) return;
        String accessToken = AlinLib.MINECRAFT.getUser().getAccessToken();
        HttpPut http = new HttpPut("https://api.minecraftservices.com/minecraft/profile/capes/active");
        HttpClient httpClient = HttpClientBuilder.create().build();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("capeId", cape);
        http.setEntity(new StringEntity(jsonObject.toString()));
        http.addHeader("Authorization", "Bearer " + accessToken);
        http.addHeader("Content-Type", "application/json");
        HttpResponse response = httpClient.execute(http);
        if(response.getStatusLine().getStatusCode() == 200){
            ClovSkins.logger.log("ok");
        }
        else {
            throw new RuntimeException("[CAPE] "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        }
    }
    public void hideCapeToMojangAPI() throws IOException{
        if(!config.getBoolean("UPLOAD_TO_MOJANG", true)) return;
        String accessToken = AlinLib.MINECRAFT.getUser().getAccessToken();
        HttpDelete http = new HttpDelete("https://api.minecraftservices.com/minecraft/profile/capes/active");
        HttpClient httpClient = HttpClientBuilder.create().build();
        http.addHeader("Authorization", "Bearer " + accessToken);
        HttpResponse response = httpClient.execute(http);
        if(response.getStatusLine().getStatusCode() == 200){
            ClovSkins.logger.log("ok");
        }
        else {
            throw new RuntimeException("[CAPE] "+response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        }
    }

    // Хуета
    public JsonObject toJSON(){
        return toJSON(false);
    }
    public JsonObject toJSON(boolean isServer){
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("skin", skin);
        json.addProperty("cape", cape);
        json.addProperty("type", type == SkinType.URL ? "url" : type == SkinType.FILE ? "file" : type == SkinType.BASE64 ? "base_64" : "nickname");
        if(isServer){
            try {
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(getTexture(), "png", os);
                String name = Base64.getEncoder().encodeToString(os.toByteArray());
                json.addProperty("type", "base_64");
                json.addProperty("skin", name);
            } catch (Exception ex){ex.printStackTrace();}
        }
        json.addProperty("model",
                //#if MC < 12109
                //$$model.id()
                //#else
                model.getSerializedName().equals("wide") ? "default" : model.getSerializedName()
                //#endif
        );
        return json;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }
    public static void reset(SkinOption skinOption){
        resourceLocationMap.remove(skinOption.skin);
        urls.remove(skinOption.skin);
        ClovSkins.cacheResourceLocations.remove(skinOption.skin);
    }

    public static SkinOption getSkinOption(File file) throws IOException {
        JsonObject json = GsonHelper.parse(Files.readString(file.toPath()));
        SkinType skinType = switch (getStringInJSON("type", json, "file")){
            case "url" -> SkinType.URL;
            case "nickname" -> SkinType.NICKNAME;
            case "base_64" -> SkinType.BASE64;
            default -> SkinType.FILE;
        };
        return new SkinOption(
                getStringInJSON("name", json, "Player skin"),
                getStringInJSON("skin", json, ""),
                getStringInJSON("cape", json, ""),
                //#if MC < 12109
                //$$PlayerSkin.Model.byName(getStringInJSON("model", json, "default")),
                //#else
                getStringInJSON("model", json, "default").equals("default") ? PlayerModelType.WIDE : PlayerModelType.SLIM,
                //#endif
                skinType,
                file
        );
    }
    public static SkinOption getSkinOption(JsonObject json, File file) {
        SkinType skinType = switch (getStringInJSON("type", json, "file")){
            case "url" -> SkinType.URL;
            case "nickname" -> SkinType.NICKNAME;
            case "base_64" -> SkinType.BASE64;
            default -> SkinType.FILE;
        };
        return new SkinOption(
                getStringInJSON("name", json, "Player skin"),
                getStringInJSON("skin", json, ""),
                getStringInJSON("cape", json, ""),
                //#if MC < 12109
                //$$PlayerSkin.Model.byName(getStringInJSON("model", json, "default")),
                //#else
                getStringInJSON("model", json, "default").equals("default") ? PlayerModelType.WIDE : PlayerModelType.SLIM,
                //#endif
                skinType,
                file
        );
    }

    public enum SkinType {
        FILE(Component.translatable("clovskins.types.file")),
        URL(Component.translatable("clovskins.types.url")),
        NICKNAME(Component.translatable("clovskins.types.nickname")),
        BASE64(Component.literal("Base64"));
        final Component name;
        SkinType(Component name){
            this.name = name;
        }
    }
}
