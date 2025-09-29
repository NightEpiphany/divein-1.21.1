package com.moigferdsrte.divein.extension;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public record AnimationEffect() {
    public record Visuals(String animationName, Particles particles) { }

    public enum Particles {
        DIVE
    }

    private static final Random random = new Random();
    public static void playVisuals(Visuals visuals, Player player, Vec3 direction) {
        ((AnimatablePlayer)player).divein_1_21_1$playDiveAnimation(visuals.animationName(), direction);
    }
}
