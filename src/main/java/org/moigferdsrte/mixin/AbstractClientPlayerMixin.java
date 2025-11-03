package org.moigferdsrte.mixin;

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
import org.moigferdsrte.Config;
import org.moigferdsrte.Divein;
import org.moigferdsrte.event.TriggerEventHandler;
import org.moigferdsrte.extension.AdjustmentModifier;
import org.moigferdsrte.extension.AnimatablePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player implements AnimatablePlayer {

    @Unique
    private static final Map<UUID, Boolean> divein_1_21_1_neo$playerAnimationStates = new HashMap<>();

    @Unique
    private final ExecutorService divein_1_21_1_neo$executorService = Executors.newSingleThreadExecutor();

    @Unique
    private final ModifierLayer<KeyframeAnimationPlayer> divein_1_21_1_neo$base = new ModifierLayer<>(null);
    @Unique
    private final SpeedModifier divein_1_21_1_neo$speedModifier = new SpeedModifier();

    @Unique
    private Vec3 divein_1_21_1_neo$lastRollDirection;

    public AbstractClientPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @TriggerEventHandler.SyncForServer
    @Override
    public void divein_1_21_1$playDiveAnimation(String animationName, Vec3 direction) {
        if (divein_1_21_1_neo$playerAnimationStates.getOrDefault(uuid, false)) return;
        divein_1_21_1_neo$playerAnimationStates.put(uuid, true);
        try {
            KeyframeAnimation animation = (KeyframeAnimation) PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath(Divein.MODID, animationName));
            assert animation != null;
            var copy = animation.mutableCopy();
            divein_1_21_1_neo$lastRollDirection = direction;

            divein_1_21_1_neo$speedModifier.speed = (float) Config.speedModifier;
            divein_1_21_1_neo$base.replaceAnimationWithFade(
                    AbstractFadeModifier.functionalFadeIn(100, (modelName, type, value) -> value),
                    new KeyframeAnimationPlayer(copy.build())
                            .setFirstPersonMode(FirstPersonMode.DISABLED)
                            .setFirstPersonConfiguration(new FirstPersonConfiguration()
                                    .setShowRightArm(true)
                                    .setShowLeftArm(true)
                                    .setShowLeftItem(false)));

            divein_1_21_1_neo$executorService.execute(() -> {
                try {
                    Thread.sleep(1500);
                    divein_1_21_1_neo$playerAnimationStates.put(uuid, false);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        } catch (Exception e) {
            Divein.LOGGER.error("Failed to play dive animation for player ", e);
            divein_1_21_1_neo$playerAnimationStates.put(uuid, false);
        }
    }

    @Inject(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;", shift = At.Shift.AFTER))
    private void tick(CallbackInfo ci) {
        if (divein_1_21_1_neo$base.getAnimation() == null) return;
        if (this.isFallFlying() || (this.onGround() && !this.isInWater()) || this.isSwimming()) {
            if (divein_1_21_1_neo$base.getAnimation() instanceof KeyframeAnimationPlayer keyframeAnimationPlayer) {
                keyframeAnimationPlayer.stop();
            }
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void postInit(ClientLevel clientLevel, GameProfile gameProfile, CallbackInfo ci) {
        @SuppressWarnings("UnstableApiUsage") var stack = ((IAnimatedPlayer) this).getAnimationStack();
        divein_1_21_1_neo$base.addModifier(divein_1_21_1_neo$speedModifier, 0);
        divein_1_21_1_neo$speedModifier.speed = 1.02f;
        stack.addAnimLayer(1000, divein_1_21_1_neo$base);
    }

    @Unique
    private double divein_1_21_1_neo$angleWithSignBetween(Vec3 a, Vec3 b, Vec3 planeNormal) {
        var cosineTheta = a.dot(b) / (a.length() * b.length());
        var angle = Math.toDegrees(Math.acos(cosineTheta));
        var cross = a.cross(b);
        angle *= Math.signum(cross.dot(planeNormal));
        if (Double.isNaN(angle)) {
            return 0;
        }
        return angle;
    }

    @Unique
    private AdjustmentModifier divein_1_21_1_neo$createAdjustmentModifier() {
        var player = (Player)this;
        return new AdjustmentModifier((partName) -> {
            float rotationX = 0;
            float rotationY = 0;
            float rotationZ = 0;
            float offsetX = 0;
            float offsetY = 0;
            float offsetZ = 0;

            if (partName.equals("body")) {
                if (divein_1_21_1_neo$lastRollDirection != null) {
                    var absoluteOrientation = new Vec3(0, 0, 1).yRot((float) Math.toRadians(-1.0 * player.yBodyRot));
                    float angle = (float) divein_1_21_1_neo$angleWithSignBetween(absoluteOrientation, divein_1_21_1_neo$lastRollDirection, new Vec3(0, 1, 0));
                    rotationY = (float) Math.toRadians(angle); // + 180;
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
