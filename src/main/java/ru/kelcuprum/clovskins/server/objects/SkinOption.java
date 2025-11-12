package ru.kelcuprum.clovskins.server.objects;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import java.awt.*;
import java.io.*;

import static ru.kelcuprum.alinlib.utils.GsonHelper.getStringInJSON;

public class SkinOption {
    public String name;
    public String skin;
    public String cape;
    public String model;
    public SkinType type;

    public SkinOption(String name, String skin, String cape, String model, SkinType type){
        this.name = name;
        this.skin = skin;
        this.cape = cape;
        this.model = model;
        this.type = type;
    }

    // Хуета
    public JsonObject toJSON(){
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("skin", skin);
        json.addProperty("cape", cape);
        json.addProperty("type", type == SkinType.URL ? "url" : type == SkinType.FILE ? "file" : type == SkinType.BASE64 ? "base_64" :"nickname");
        json.addProperty("model", model);
        return json;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public static SkinOption getSkinOption(JsonObject json) {
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
                getStringInJSON("model", json, "default"),
                skinType
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
