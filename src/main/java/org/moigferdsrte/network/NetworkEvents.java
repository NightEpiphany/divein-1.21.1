package org.moigferdsrte.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.moigferdsrte.Divein;

@EventBusSubscriber(modid = Divein.MODID)
public class NetworkEvents {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(Packets.AnimationPublish.TYPE, Packets.AnimationPublish.CODEC, (packet, ctx) -> {
            var player = (ServerPlayer) ctx.player();
            var server = player.server;
            ServerNetwork.handleDivePublish(packet, server, player);
        });

        registrar.playToClient(Packets.DiveAnimation.TYPE, Packets.DiveAnimation.CODEC, (packet, ctx) -> {
            ClientNetwork.handleDiveAnimation(packet);
        });
    }
}
