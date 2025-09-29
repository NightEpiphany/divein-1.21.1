package com.moigferdsrte.divein.extension;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
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
        if(this.getAnim() instanceof KeyframeAnimationPlayer player) {
            float currentTick = player.getTick() + delta;
            fadeIn = currentTick / (float) player.getData().beginTick;
            fadeIn = Math.min(fadeIn, 1F);
        }
        return fadeIn;
    }

    private float getFadeOut(float delta) {
        float fadeOut = 1;
        if(this.getAnim() instanceof KeyframeAnimationPlayer player) {
            float currentTick = player.getTick() + delta;

            float position = (-1F) * (currentTick - player.getData().stopTick);
            float length = player.getData().stopTick - player.getData().endTick;
            if (length > 0) {
                fadeOut = position / length;
                fadeOut = Math.min(fadeOut, 1F);
            }
        }
        return fadeOut;
    }

    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        if (!enabled) {
            return super.get3DTransform(modelName, type, tickDelta, value0);
        }

        var partModifier = source.apply(modelName);

        var modifiedVector = value0;
        var fade = getFadeIn(tickDelta) * getFadeOut(tickDelta);
        if (partModifier.isPresent()) {
            modifiedVector = super.get3DTransform(modelName, type, tickDelta, modifiedVector);
            return transformVector(modifiedVector, type, partModifier.get(), fade);
        } else {
            return super.get3DTransform(modelName, type, tickDelta, value0);
        }
    }

    private Vec3f transformVector(Vec3f vector, TransformType type, PartModifier partModifier, float fade) {
        switch (type) {
            case POSITION -> {
                return vector.add(partModifier.offset);
            }
            case ROTATION -> {
                return vector.add(partModifier.rotation.scale(fade));
            }
            case BEND -> {
                return vector;
            }
        }
        return vector;
    }
}
