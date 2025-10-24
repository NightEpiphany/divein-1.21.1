package com.moigferdsrte.divein.mixin;

import com.moigferdsrte.divein.Divein;
import com.moigferdsrte.divein.event.DiveinEvent;
import com.moigferdsrte.divein.extension.AdjustmentModifier;
import com.moigferdsrte.divein.extension.AnimatablePlayer;
import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player implements AnimatablePlayer, ClientAvatarEntity {

    @Unique
    private static final Map<UUID, Boolean> playerAnimationStates = new HashMap<>();

    @Unique
    private final ExecutorService asyncPool = Executors.newSingleThreadExecutor();


    @Unique
    private final SpeedModifier speedModifier = new SpeedModifier(Divein.config.speedModifier);

    @Unique
    private Vec3 divingDirection;

    public AbstractClientPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @DiveinEvent.SyncForServer
    @Override
    public void divein_1_21_1$playDiveAnimation(String animationName, Vec3 direction) {
        if (Minecraft.getInstance().player == null) return;
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, animationName);
        PlayerAnimationController controller = (PlayerAnimationController) PlayerAnimationAccess
                .getPlayerAnimationLayer((AbstractClientPlayer)(Object) this, loc);
        if (this.isSwimming()) controller.stop();
        if (playerAnimationStates.getOrDefault(uuid, false)) return;
        playerAnimationStates.put(uuid, true);
        try {
            controller.setFirstPersonConfiguration(new FirstPersonConfiguration()
                    .setShowRightArm(true)
                    .setShowLeftArm(true)
                    .setShowLeftItem(false));
            if (!controller.getModifiers().contains(speedModifier))
                controller.addModifier(speedModifier, 0);
            controller.setFirstPersonMode(FirstPersonMode.DISABLED);
            controller.replaceAnimationWithFade(AbstractFadeModifier.functionalFadeIn(10, (modelName, tick) -> tick), loc);
            asyncPool.execute(() -> {
                try {
                    Thread.sleep(2000);
                    playerAnimationStates.put(uuid, false);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        } catch (Exception e) {
            Divein.LOGGER.error("Failed to play dive animation for player ", e);
            playerAnimationStates.put(uuid, false);
        }
    }

    @Unique
    protected float calculateProgress(float f, int length) {
        return (f + tickCount) / length;
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

    @Unique
    @Deprecated
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
