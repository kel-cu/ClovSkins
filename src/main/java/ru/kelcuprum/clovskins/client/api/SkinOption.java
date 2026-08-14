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
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import ru.kelcuprum.alinlib.AlinLib;
import ru.kelcuprum.alinlib.gui.GuiUtils;
import ru.kelcuprum.alinlib.gui.toast.ToastBuilder;
import ru.kelcuprum.alinlib.info.Player;
import ru.kelcuprum.clovskins.client.ClovSkins;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
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
        Identifier skin = playerSkin == null ? getTextureSkin() :
                //#if MC < 12109
                //$$ playerSkin.texture();
                //#else
                playerSkin.body().texturePath();
        //#endif
        Identifier cape = playerSkin == null ? getTextureCape() :
                //#if MC < 12109
                //$$ playerSkin.capeTexture();
                //#else
                playerSkin.cape().texturePath();
        //#endif
        Identifier elytra = playerSkin == null ? getTextureCape() :
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
    public ClientAsset.Texture getTexture(Identifier resourceLocation){
        if(resourceLocation == null) return null;
        return new ClientAsset.DownloadedTexture(resourceLocation, "");
    }
    //#endif



    public Identifier getTextureSkin() throws IOException {
        Identifier location = GuiUtils.getResourceLocation("clovskins", "skins_"+ (file == null ? name.toLowerCase() : file.getName()));
        if(skin.isBlank()) return DefaultPlayerSkin.getDefaultTexture();
        if(!ClovSkins.cacheIdentifiers.containsKey(skin)) {
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
            ClovSkins.cacheIdentifiers.put(skin, location);
        }
        return location;
    }

    public Identifier getTextureCape(){
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
        ClovSkins.cacheIdentifiers.remove(skin);
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
            if(AlinLib.MINECRAFT.level != null && !AlinLib.MINECRAFT.hasSingleplayerServer() && !AlinLib.MINECRAFT.isLocalServer())
                new ToastBuilder().setTitle(Component.literal("ClovSkins")).setMessage(Component.translatable("clovskins.upload.multiplayer")).setType(ToastBuilder.Type.WARN).setDisplayTime(15000).buildAndShow();
            new ToastBuilder().setTitle(Component.literal("ClovSkins")).setMessage(Component.translatable("clovskins.upload.done", name)).buildAndShow();
        } else ClovSkins.logger.debug("Не чет не хочу");
    }

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public void uploadSkinToMojangAPI()  {
        try {
        if (!config.getBoolean("UPLOAD_TO_MOJANG", true)) return;

        String accessToken = AlinLib.MINECRAFT.getUser().getAccessToken();
        String variant = model.getSerializedName().equals("wide") ? "classic" : "slim";

        // Подготовка файла
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(getSourceSkin(), "png", baos);
        byte[] imageBytes = baos.toByteArray();

        Path tempPath = Path.of(getPath(), "temp", System.currentTimeMillis() + ".png");
        Files.createDirectories(tempPath.getParent());
        Files.write(tempPath, imageBytes);
        File file = tempPath.toFile();

            // Формирование multipart
            String boundary = "----" + Instant.now().toEpochMilli();
            String CRLF = "\r\n";
            StringBuilder bodyBuilder = new StringBuilder();

            // variant
            bodyBuilder.append("--").append(boundary).append(CRLF)
                    .append("Content-Disposition: form-data; name=\"variant\"").append(CRLF).append(CRLF)
                    .append(variant).append(CRLF);

            // file
            bodyBuilder.append("--").append(boundary).append(CRLF)
                    .append("Content-Disposition: form-data; name=\"file\"; filename=\"skin.png\"").append(CRLF)
                    .append("Content-Type: image/png").append(CRLF).append(CRLF);

            byte[] header = bodyBuilder.toString().getBytes();
            byte[] footer = (CRLF + "--" + boundary + "--" + CRLF).getBytes();

            long totalLength = header.length + imageBytes.length + footer.length;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/skins"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(
                            concatBytes(header, imageBytes, footer)))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ClovSkins.logger.log("ok");
            } else {
                throw new RuntimeException("[SKIN] " + response.statusCode() + " " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // Удаляем временный файл
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public void activeCapeToMojangAPI() {
        try {
            if (!config.getBoolean("UPLOAD_TO_MOJANG", true)) return;

            String accessToken = AlinLib.MINECRAFT.getUser().getAccessToken();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("capeId", cape);
            String jsonBody = jsonObject.toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/capes/active"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ClovSkins.logger.log("ok");
            } else {
                throw new RuntimeException("[CAPE] " + response.statusCode() + " " + response.body());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void hideCapeToMojangAPI() {
        try {
            if (!config.getBoolean("UPLOAD_TO_MOJANG", true)) return;

            String accessToken = AlinLib.MINECRAFT.getUser().getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/capes/active"))
                    .header("Authorization", "Bearer " + accessToken)
                    .DELETE()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ClovSkins.logger.log("ok");
            } else {
                throw new RuntimeException("[CAPE] " + response.statusCode() + " " + response.body());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // Вспомогательный метод для конкатенации байтовых массивов
    private byte[] concatBytes(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] arr : arrays) {
            totalLength += arr.length;
        }
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, result, offset, arr.length);
            offset += arr.length;
        }
        return result;
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
        ClovSkins.cacheIdentifiers.remove(skinOption.skin);
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
