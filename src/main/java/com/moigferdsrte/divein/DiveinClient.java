package com.moigferdsrte.divein;

import com.moigferdsrte.divein.extension.AnimationEffect;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DiveinClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSidePacketRegistry.INSTANCE.register(Divein.ANIMATION_S2C,
                (packetContext, attachedData) -> {
                    var id = attachedData.readInt();
                    var animationName = attachedData.readUtf();
                    var vec = attachedData.readVector3f();
                    packetContext.getTaskQueue().execute(() -> {
                        var client = Minecraft.getInstance();
                        assert client.level != null;
                        var entity = client.level.getEntity(id);
                        if (entity instanceof Player player) {
                            AnimationEffect.playVisuals(new AnimationEffect.Visuals(animationName, AnimationEffect.Particles.DIVE), player, new Vec3(vec.x, vec.y, vec.x));
                        }
                    });
                });
    }
}
