package ru.kelcuprum.clovskins.common;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import ru.kelcuprum.clovskins.common.packets.HellolPacketPayload;
import ru.kelcuprum.clovskins.common.packets.SkinPresetPacketPayload;

public class ClovSkinsCommon implements ModInitializer {
    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(SkinPresetPacketPayload.ID, SkinPresetPacketPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SkinPresetPacketPayload.ID, SkinPresetPacketPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(HellolPacketPayload.ID, HellolPacketPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(HellolPacketPayload.ID, HellolPacketPayload.CODEC);
    }
}
