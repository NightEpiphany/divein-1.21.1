package com.moigferdsrte.divein.mixin;

import com.moigferdsrte.divein.Divein;
import com.moigferdsrte.divein.extension.AnimationEffect;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
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
public class ClientPacketSenderMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public ClientLevel level;
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
            if (player.getDeltaMovement().y < 0)
                tryPlayAnimationOnServerside();
        }
    }

    @Unique
    private void tryPlayAnimationOnServerside() {
        var client = (Minecraft) ((Object)this);
        if (player == null || client.isPaused()) {
            return;
        }
        boolean hasRes = player.hasEffect(MobEffects.FIRE_RESISTANCE);
        float sensitivity = Divein.config.triggerSensitivity;
        if (sensitivity < 0) sensitivity = 0;
        if (sensitivity > 1) sensitivity = 1;
        boolean isFalling = player.getDeltaMovement().y < sensitivity - 1.0f
                && !player.onGround()
                && (Objects.requireNonNull(client.level).getBlockState(player.blockPosition().below()).is(Blocks.AIR) || Objects.requireNonNull(client.level).getBlockState(player.blockPosition().below()).is(Blocks.CAVE_AIR))
                && !player.getAbilities().flying;
        if (isFalling) {
            if (waterDrop || hasRes) {
                var visuals = new AnimationEffect.Visuals("dive", AnimationEffect.Particles.DIVE);
                //ServerNetwork.networkC2S_Send(new Packets.AnimationPublish(player.getId(), visuals, player.getDeltaMovement()));
                FriendlyByteBuf passedData = new FriendlyByteBuf(Unpooled.buffer());
                passedData.writeInt(player.getId());
                passedData.writeUtf("dive");
                passedData.writeVector3f(player.getDeltaMovement().toVector3f());
                ClientSidePacketRegistry.INSTANCE.sendToServer(Divein.ANIMATION_C2S, passedData);
                AnimationEffect.playVisuals(visuals, player, player.getDeltaMovement());
            }
            if (lavaDrop) {
                var visuals = new AnimationEffect.Visuals("lava_dive", AnimationEffect.Particles.DIVE);
                //ServerNetwork.networkC2S_Send(new Packets.AnimationPublish(player.getId(), visuals, player.getDeltaMovement()));
                FriendlyByteBuf passedData = new FriendlyByteBuf(Unpooled.buffer());
                passedData.writeInt(player.getId());
                passedData.writeUtf("lava_dive");
                passedData.writeVector3f(player.getDeltaMovement().toVector3f());
                ClientSidePacketRegistry.INSTANCE.sendToServer(Divein.ANIMATION_C2S, passedData);
                AnimationEffect.playVisuals(visuals, player, player.getDeltaMovement());
            }
        }
    }
}