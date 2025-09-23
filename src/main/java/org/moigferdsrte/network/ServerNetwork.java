package org.moigferdsrte.network;

import com.google.common.collect.Iterables;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.moigferdsrte.Divein;
import org.moigferdsrte.event.api.Event;
import org.moigferdsrte.event.api.ServersideDiveEvents;

import java.util.Collection;

public class ServerNetwork {

    public static void handleDivePublish(Packets.AnimationPublish packet, MinecraftServer server, ServerPlayer player) {
        ServerLevel world = Iterables.tryFind(server.getAllLevels(), (element) -> element == player.level())
                .orNull();
        final var velocity = packet.velocity();
        final var forwardPacket = new Packets.DiveAnimation(player.getId(), packet.visuals(), packet.velocity());
        tracking(player).forEach(serverPlayer -> {
            try {
                if (serverPlayer.getId() != player.getId() && networkS2C_CanSend(serverPlayer, Packets.DiveAnimation.TYPE)) {
                    networkS2C_Send(serverPlayer, forwardPacket);
                }
            } catch (Exception e) {
                Divein.LOGGER.error("Failed to send dive animation packet to {}", serverPlayer.getDisplayName().getString());
            }
        });

        assert world != null;
        world.getServer().executeIfPossible(() -> {

            var proxy = (Event.Proxy<ServersideDiveEvents.PlayerStart>)ServersideDiveEvents.PLAYER_START;
            proxy.handlers.forEach(handler -> { handler.onPlayerStartedDiving(player, velocity);});
        });
    }

    public static Collection<ServerPlayer> tracking(ServerPlayer player) {
        return (Collection<ServerPlayer>) player.serverLevel().getPlayers((serverPlayer) -> serverPlayer.getId() != player.getId());
    }

    public static Collection<ServerPlayer> around(ServerLevel world, Vec3 origin, double distance) {
        return world.getPlayers((player) -> player.position().distanceToSqr(origin) <= (distance*distance));
    }

    public static boolean networkS2C_CanSend(ServerPlayer player, CustomPacketPayload.Type<?> packetId) {
        return true;
    }

    public static void networkS2C_Send(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void networkC2S_Send(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
