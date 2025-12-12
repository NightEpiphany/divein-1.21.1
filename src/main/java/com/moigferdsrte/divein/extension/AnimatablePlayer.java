package com.moigferdsrte.divein.extension;

import com.moigferdsrte.divein.events.DiveinEvent;
import net.minecraft.world.phys.Vec3;

public interface AnimatablePlayer {
    @DiveinEvent.SyncForServer
    void divein_1_21_1$playDiveAnimation(String animationName, Vec3 direction);
}
