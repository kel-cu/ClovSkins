package ru.kelcuprum.clovskins.client.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import ru.kelcuprum.alinlib.WebAPI;
import ru.kelcuprum.clovskins.client.ClovSkins;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

import static ru.kelcuprum.alinlib.utils.GsonHelper.*;

public class MojangAPI {
    public static HashMap<String, JsonObject> cache = new HashMap<>();
    public static JsonObject getSkinURL(String name){
        if(cache.containsKey(name)) return cache.get(name);
        try {
            JsonObject baseUserInfo = WebAPI.getJsonObject(String.format("https://api.minecraftservices.com/minecraft/profile/lookup/name/%s", name));
            if(!jsonElementIsNull("id", baseUserInfo)){
                JsonObject userInfo = WebAPI.getJsonObject(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s", getStringInJSON("id", baseUserInfo, "")));
                if(!jsonElementIsNull("properties", userInfo)){
                    JsonArray properties = userInfo.getAsJsonArray("properties");
                    JsonObject info = properties.get(0).getAsJsonObject();
                    String value = getStringInJSON("value", info);
                    String unbase = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
                    JsonObject valueInfo = GsonHelper.parse(unbase);
                    JsonObject infoPlayer = null;
                    if(valueInfo.has("textures") && valueInfo.get("textures").getAsJsonObject().has("SKIN"))
                        infoPlayer = valueInfo.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject();
                    cache.put(name, infoPlayer);
                    return infoPlayer;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
