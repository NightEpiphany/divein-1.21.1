package com.moigferdsrte.divein.network;

import com.moigferdsrte.divein.extension.AnimationEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientNetwork {

    public static void handleDiveAnimation(Packets.DiveAnimation packet) {
        var client = Minecraft.getInstance();
        client.execute(() -> {
            assert client.level != null;
            var entity = client.level.getEntity(packet.playerId());
            if (entity instanceof Player player) {
                AnimationEffect.playVisuals(packet.visuals(), player, packet.velocity());
            }
        });
    }
}
