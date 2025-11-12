package ru.kelcuprum.clovskins.common;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import ru.kelcuprum.clovskins.common.packets.HellolPacketPayload;
import ru.kelcuprum.clovskins.common.packets.SkinPresetPacketPayload;

public class ClovSkinsCommon implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(SkinPresetPacketPayload.ID, SkinPresetPacketPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SkinPresetPacketPayload.ID, SkinPresetPacketPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HellolPacketPayload.ID, HellolPacketPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HellolPacketPayload.ID, HellolPacketPayload.CODEC);
    }
}
