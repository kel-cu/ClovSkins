package ru.kelcuprum.clovskins.common.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record SkinPresetPacketPayload(String json) implements CustomPacketPayload {
    public static final Identifier SUMMON_LIGHTNING_PAYLOAD_ID = Identifier.fromNamespaceAndPath("clovskins", "skin_preset");
    public static final CustomPacketPayload.Type<SkinPresetPacketPayload> ID = new CustomPacketPayload.Type<>(SUMMON_LIGHTNING_PAYLOAD_ID);
    public static final StreamCodec<FriendlyByteBuf, SkinPresetPacketPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, SkinPresetPacketPayload::json, SkinPresetPacketPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
