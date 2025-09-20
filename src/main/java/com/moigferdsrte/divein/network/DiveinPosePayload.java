package com.moigferdsrte.divein.network;

import com.moigferdsrte.divein.Divein;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record DiveinPosePayload(UUID uuid, boolean isWater) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DiveinPosePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "sync"));

    public static final StreamCodec<FriendlyByteBuf, DiveinPosePayload> CODEC = StreamCodec.of(
            (buf, playLoad) -> {
                buf.writeUUID(playLoad.uuid);
                buf.writeBoolean(playLoad.isWater);
            },
            buf -> new DiveinPosePayload(buf.readUUID(), buf.readBoolean())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
