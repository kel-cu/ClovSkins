package ru.kelcuprum.clovskins.server;

import com.google.gson.JsonObject;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import ru.kelcuprum.alinlib.AlinLogger;
import ru.kelcuprum.clovskins.server.objects.SkinOption;
import ru.kelcuprum.clovskins.common.packets.HellolPacketPayload;
import ru.kelcuprum.clovskins.common.packets.SkinPresetPacketPayload;

import java.util.HashMap;

import static java.lang.Thread.sleep;

public class ClovSkinsServer implements DedicatedServerModInitializer {
    public static AlinLogger logger = new AlinLogger("ClovSkins");
    public static HashMap<ServerPlayer, SkinOption> skinOptions = new HashMap<>();
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
    }
}
