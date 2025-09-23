package org.moigferdsrte.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static org.moigferdsrte.Divein.MODID;

@Deprecated
public record DiveinPosePayload(UUID uuid, boolean isWater) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DiveinPosePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync"));

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
