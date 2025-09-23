package com.moigferdsrte.divein.mixin;

import com.moigferdsrte.divein.Divein;
import com.moigferdsrte.divein.extension.AnimationEffect;
import com.moigferdsrte.divein.network.Packets;
import com.moigferdsrte.divein.network.ServerNetwork;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.BlockTags;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static com.moigferdsrte.divein.Divein.checkLavaBelow;
import static com.moigferdsrte.divein.Divein.checkWaterBelow;

@Environment(EnvType.CLIENT)
@Mixin(value = Minecraft.class, priority = 500)
public abstract class ClientPacketSenderMixin {


    @Shadow @Nullable
    public LocalPlayer player;

    @Unique
    private boolean waterDrop;
    @Unique
    private boolean lavaDrop;

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        if (player == null) return;
        waterDrop = checkWaterBelow(player, Divein.config.fluidLevelDetectHeight);
        lavaDrop = checkLavaBelow(player, Divein.config.fluidLevelDetectHeight * 2);
        if (waterDrop || lavaDrop) {
            tryPlayAnimationOnServerside();
        }
    }

    @Unique
    private void tryPlayAnimationOnServerside() {
        var client = (Minecraft) ((Object)this);
        if (player == null || client.isPaused() || client.screen != null) {
            return;
        }
        float sensitivity = Divein.config.triggerSensitivity;
        if (sensitivity < 0) sensitivity = 0;
        if (sensitivity > 1) sensitivity = 1;
        boolean isFalling = player.getDeltaMovement().y < sensitivity - 1.0f
                && !player.onGround()
                && Objects.requireNonNull(client.level).getBlockState(player.blockPosition().below()).is(BlockTags.AIR)
                && !player.getAbilities().flying;
        if (isFalling) {
            if (waterDrop) {
                var visuals = new AnimationEffect.Visuals("dive", AnimationEffect.Particles.DIVE);
                ServerNetwork.networkC2S_Send(new Packets.AnimationPublish(player.getId(), visuals, player.getDeltaMovement()));
                AnimationEffect.playVisuals(visuals, player, player.getDeltaMovement());
            }
            if (lavaDrop) {
                var visuals = new AnimationEffect.Visuals("lava_dive", AnimationEffect.Particles.DIVE);
                ServerNetwork.networkC2S_Send(new Packets.AnimationPublish(player.getId(), visuals, player.getDeltaMovement()));
                AnimationEffect.playVisuals(visuals, player, player.getDeltaMovement());
            }
        }
    }
}
