package org.moigferdsrte.extension;


import net.minecraft.world.phys.Vec3;
import org.moigferdsrte.event.TriggerEventHandler;

public interface AnimatablePlayer {
    @TriggerEventHandler.SyncForServer
    void divein_1_21_1$playDiveAnimation(String animationName, Vec3 direction);
}
