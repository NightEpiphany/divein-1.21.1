package org.moigferdsrte.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.moigferdsrte.extension.AnimationEffect;

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
