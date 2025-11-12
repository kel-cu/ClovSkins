package ru.kelcuprum.clovskins.common.packets;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record HellolPacketPayload(String key) implements CustomPacketPayload {
    public static final ResourceLocation SUMMON_LIGHTNING_PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath("clovskins", "hellol");
    public static final Type<HellolPacketPayload> ID = new Type<>(SUMMON_LIGHTNING_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, HellolPacketPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, HellolPacketPayload::key, HellolPacketPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
