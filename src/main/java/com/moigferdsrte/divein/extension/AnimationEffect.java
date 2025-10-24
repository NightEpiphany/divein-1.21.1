package com.moigferdsrte.divein.extension;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record AnimationEffect() {
    public record Visuals(String animationName, Particles particles) { }

    public enum Particles {
        DIVE
    }

    public static void playVisuals(Visuals visuals, Player player, Vec3 direction) {
        ((AnimatablePlayer)player).divein_1_21_1$playDiveAnimation(visuals.animationName(), direction);
    }
}
