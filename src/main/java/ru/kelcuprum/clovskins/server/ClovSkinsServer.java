package ru.kelcuprum.clovskins.server;

import com.google.gson.JsonObject;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionCheckTypes;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.ARGB;
import net.minecraft.util.GsonHelper;
import ru.kelcuprum.alinlib.AlinLogger;
import ru.kelcuprum.alinlib.config.Config;
import ru.kelcuprum.clovskins.server.mixin.PlayerAccessor;
import ru.kelcuprum.clovskins.server.objects.PlayerUtils;
import ru.kelcuprum.clovskins.server.objects.SkinOption;
import ru.kelcuprum.clovskins.common.packets.HellolPacketPayload;
import ru.kelcuprum.clovskins.common.packets.SkinPresetPacketPayload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static java.lang.Thread.sleep;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ClovSkinsServer implements DedicatedServerModInitializer {
    public static AlinLogger logger = new AlinLogger("ClovSkins");
    public static Config playersSkins = new Config("./config/clovskins/players.json");
    public static HashMap<ServerPlayer, SkinOption> skinOptions = new HashMap<>();
    public static MinecraftServer server;
    @Override
    public void onInitializeServer() {
        ServerPlayNetworking.registerGlobalReceiver(HellolPacketPayload.ID, (packet, context) -> {
            ServerPlayer player = context.player();
            ServerPlayNetworking.send(player, packet);
            for(ServerPlayer serverPlayer : skinOptions.keySet()){
                JsonObject jsonObject = skinOptions.get(serverPlayer).toJSON();
                jsonObject.addProperty("player", serverPlayer.getStringUUID());
                SkinPresetPacketPayload skinPresetPacketPayload = new SkinPresetPacketPayload(jsonObject.toString());
                        ServerPlayNetworking.send(player, skinPresetPacketPayload);
                        logger.log("[%s] %s - %s", player.getName().getString(), serverPlayer.getName().getString(), skinPresetPacketPayload.json());
                    }
        });
        ServerPlayNetworking.registerGlobalReceiver(SkinPresetPacketPayload.ID, (packet, context) -> {
            JsonObject jsonObject = GsonHelper.parse(packet.json());
            jsonObject.addProperty("name", String.format("%s_%s", context.player().getName().getString(), System.currentTimeMillis()));
            SkinOption skinOption = SkinOption.getSkinOption(jsonObject);
            skinOptions.put(context.player(), skinOption);
            jsonObject.addProperty("player", context.player().getStringUUID());
            SkinPresetPacketPayload skinPresetPacketPayload = new SkinPresetPacketPayload(jsonObject.toString());
            for(ServerPlayer player : context.server().getPlayerList().getPlayers()){
                logger.log("[share %s] %s - %s", context.player().getName().getString(), player.getName().getString(), skinPresetPacketPayload.json());
                ServerPlayNetworking.send(player, skinPresetPacketPayload);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((packet, s) -> {
            if(skinOptions.containsKey(packet.player)) logger.log("SkinPreset %s has been removed from memory", packet.player.getName().getString());
            skinOptions.remove(packet.player);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, s1, s2) ->{
            dispatcher.register(literal("clovskins")
                    .then(literal("set")
                            .then(argument("url", string()).executes((s) -> {
                                try {
                                    loadURLSkin(getString(s, "url"), 2, s.getSource().getPlayer());
                                    return 0;
                                } catch (Exception ex){
                                    ex.printStackTrace();
                                    s.getSource().sendFailure(Component.literal("Error: "+ex.getMessage()));
                                    return 1;
                                }
                            }).then(argument("player", EntityArgument.player()).requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)).executes((s) -> {
                                try {
                                    loadURLSkin(getString(s, "url"), 2, EntityArgument.getPlayer(s, "player"));
                                    return 0;
                                } catch (Exception ex){
                                    ex.printStackTrace();
                                    s.getSource().sendFailure(Component.literal("Error: "+ex.getMessage()));
                                    return 1;
                                }
                            })).then(argument("slim", BoolArgumentType.bool()).executes((s) -> {
                                try {
                                    loadURLSkin(getString(s, "url"), BoolArgumentType.getBool(s, "slim") ? 1 : 0, s.getSource().getPlayer());
                                    return 0;
                                } catch (Exception ex){
                                    ex.printStackTrace();
                                    s.getSource().sendFailure(Component.literal("Error: "+ex.getMessage()));
                                    return 1;
                                }
                            }).then(argument("player", EntityArgument.player()).requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)).executes((s) -> {
                                try {
                                    loadURLSkin(getString(s, "url"), BoolArgumentType.getBool(s, "slim") ? 1 : 0, EntityArgument.getPlayer(s, "player"));
                                    return 0;
                                } catch (Exception ex){
                                    ex.printStackTrace();
                                    s.getSource().sendFailure(Component.literal("Error: "+ex.getMessage()));
                                    return 1;
                                }
                            }))))
                    )
            );
        });
    }

    public static void loadURLSkin(String url, int model, ServerPlayer player) throws URISyntaxException, IOException {
            if(skinOptions.containsKey(player)) throw new RuntimeException("Скин не может быть поставлен, т.к. у игрока стоит модификация");
            boolean slim = model == 1;
            if(model == 2){
                BufferedImage image = ImageIO.read(new URI(url).toURL());
                slim = ARGB.alpha(image.getRGB(46, 63)) == 0;
            }
            setURLSkin(url, slim, player);
    }

    public static void setURLSkin(String url, boolean slim, ServerPlayer currentPlayer){
        SkinOption skinOption = new SkinOption("url", url, "", slim ? "slim" : "default", SkinOption.SkinType.URL);
        JsonObject jsonObject = skinOption.toJSON();
        playersSkins.setJsonObject(currentPlayer.getName().getString(), jsonObject);
        jsonObject.addProperty("player", currentPlayer.getStringUUID());
        for(ServerPlayer player : server.getPlayerList().getPlayers()){
            SkinPresetPacketPayload skinPresetPacketPayload = new SkinPresetPacketPayload(jsonObject.toString());
            ServerPlayNetworking.send(player, skinPresetPacketPayload);
        }
        JsonObject playerData = new JsonObject();
        playerData.addProperty("timestamp", System.currentTimeMillis());
        playerData.addProperty("profileId", currentPlayer.getStringUUID().replace("-", ""));
        playerData.addProperty("profileName", currentPlayer.getGameProfile().name());
        playerData.addProperty("signatureRequired", false);
        JsonObject textures = new JsonObject();
        JsonObject skin = new JsonObject();
        JsonObject metadata = new JsonObject();
        skin.addProperty("url", url);
        if(slim) {
            metadata.addProperty("model", "slim");
            skin.add("metadata", metadata);
        }
        textures.add("SKIN", skin);
        playerData.add("textures", textures);
        String base = Base64.getEncoder().encodeToString(playerData.toString().getBytes(StandardCharsets.UTF_8));

        ((PlayerAccessor) currentPlayer).setGameProfile(PlayerUtils.applyRestoredSkin(currentPlayer.getGameProfile(), new Property("textures", base)));
        PlayerUtils.refreshPlayer(currentPlayer);
    }
}
