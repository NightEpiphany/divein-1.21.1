package com.moigferdsrte.divein.extension;


import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractModifier;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.math.Vec3f;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public final class AdjustmentModifier extends AbstractModifier {
    public record PartModifier(
            Vec3f rotation,
            Vec3f offset
    ) {};

    public boolean enabled = true;

    private final Function<String, Optional<PartModifier>> source;

    public AdjustmentModifier(Function<String, Optional<PartModifier>> source) {
        this.source = source;
    }

    private float getFadeIn(float delta) {
        float fadeIn = 1;
        if(this.getAnim() instanceof PlayerAnimationController player) {
            float currentTick = player.getAnimationTicks() + delta;
            fadeIn = currentTick / player.getAnimationData().getPartialTick();
            fadeIn = Math.min(fadeIn, 1F);
        }
        return fadeIn;
    }

    private float getFadeOut(float delta) {
        float fadeOut = 1;
        if(this.getAnim() instanceof PlayerAnimationController player) {
            float currentTick = player.getAnimationTicks() + delta;

            float position = (-1F) * (currentTick - player.getAnimationData().getPartialTick());
            float length = player.getAnimationData().getPartialTick();
            if (length > 0) {
                fadeOut = position / length;
                fadeOut = Math.min(fadeOut, 1F);
            }
        }
        return fadeOut;
    }



    private Vec3f transformVector(Vec3f vector, TransformType type, PartModifier partModifier, float fade) {
        switch (type) {
            case POSITION -> {
                return vector.add(partModifier.offset);
            }
            case ROTATION -> {
                return vector.add(partModifier.rotation.mul(fade));
            }
            case BEND -> {
                return vector;
            }
        }
        return vector;
    }
}
