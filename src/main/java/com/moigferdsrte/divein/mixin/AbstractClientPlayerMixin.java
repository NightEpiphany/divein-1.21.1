package com.moigferdsrte.divein.mixin;

import com.moigferdsrte.divein.Divein;
import com.moigferdsrte.divein.event.DiveinEvent;
import com.moigferdsrte.divein.extension.AdjustmentModifier;
import com.moigferdsrte.divein.extension.AnimatablePlayer;
import com.mojang.authlib.GameProfile;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player implements AnimatablePlayer {

    @Unique
    private static final Map<UUID, Boolean> playerAnimationStates = new HashMap<>();

    @Unique
    private final ModifierLayer<KeyframeAnimationPlayer> base = new ModifierLayer<>(null);
    @Unique
    private final SpeedModifier speedModifier = new SpeedModifier();

    @Unique
    private Vec3 divingDirection;

    public AbstractClientPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @DiveinEvent.SyncForServer
    @Override
    public void divein_1_21_1$playDiveAnimation(String animationName, Vec3 direction) {
        if (playerAnimationStates.getOrDefault(uuid, false)) return;
        playerAnimationStates.put(uuid, true);
        try {
            KeyframeAnimation animation = (KeyframeAnimation) PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, animationName));

            KeyframeAnimation.AnimationBuilder copy;
            if (animation == null)
                return;
            copy = animation.mutableCopy();

            divingDirection = direction;

            speedModifier.speed = 0.8523f;
            base.replaceAnimationWithFade(
                    AbstractFadeModifier.functionalFadeIn(100, (modelName, type, value) -> value),
                    new KeyframeAnimationPlayer(copy.build())
                            .setFirstPersonMode(FirstPersonMode.DISABLED)
                            .setFirstPersonConfiguration(new FirstPersonConfiguration()
                                    .setShowRightArm(true)
                                    .setShowLeftItem(false)));

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    playerAnimationStates.put(uuid, false);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } catch (Exception e) {
            Divein.LOGGER.error("Failed to play dive animation for player ", e);
            playerAnimationStates.put(uuid, false);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void postInit(ClientLevel clientLevel, GameProfile gameProfile, CallbackInfo ci) {
        @SuppressWarnings("UnstableApiUsage") var stack = ((IAnimatedPlayer) this).getAnimationStack();
        //base.addModifier(createAdjustmentModifier(), 0);
        base.addModifier(speedModifier, 0);
        speedModifier.speed = 1.02f;
        stack.addAnimLayer(1000, base);
    }

    @Unique
    private double angleWithSignBetween(Vec3 a, Vec3 b, Vec3 planeNormal) {
        var cosineTheta = a.dot(b) / (a.length() * b.length());
        var angle = Math.toDegrees(Math.acos(cosineTheta));
        var cross = a.cross(b);
        angle *= Math.signum(cross.dot(planeNormal));
        if (Double.isNaN(angle)) {
            return 0;
        }
        return angle;
    }

//    @Inject(method = "tick", at = @At("HEAD"))
//    public void tick(CallbackInfo ci) {
//        DiveinEvent.DIVEIN_WATER_EVENT.invoker().update(this, this.level());
//    }

    @Unique
    private AdjustmentModifier createAdjustmentModifier() {
        var player = (Player)this;
        return new AdjustmentModifier((partName) -> {
            float rotationX = 0;
            float rotationY = 0;
            float rotationZ = 0;
            float offsetX = 0;
            float offsetY = 0;
            float offsetZ = 0;

            if (partName.equals("body")) {
                if (divingDirection != null) {
                    var absoluteOrientation = new Vec3(0, 0, 1).yRot((float) Math.toRadians(-1.0 * player.yBodyRot));
                    float angle = (float) angleWithSignBetween(absoluteOrientation, divingDirection, new Vec3(0, 1, 0));
                    rotationY = (float) Math.toRadians(angle);
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }

            return Optional.of(new AdjustmentModifier.PartModifier(
                    new Vec3f(rotationX, rotationY, rotationZ),
                    new Vec3f(offsetX, offsetY, offsetZ))
            );
        });
    }
}
