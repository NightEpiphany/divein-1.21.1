package org.moigferdsrte.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.moigferdsrte.Config;
import org.moigferdsrte.extension.AnimationEffect;
import org.moigferdsrte.network.Packets;
import org.moigferdsrte.network.ServerNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static org.moigferdsrte.event.TriggerEventHandler.checkLavaBelow;
import static org.moigferdsrte.event.TriggerEventHandler.checkWaterBelow;


@OnlyIn(Dist.CLIENT)
@Mixin(value = Minecraft.class, priority = 500)
public abstract class ClientPacketSenderMixin {


    @Shadow @Nullable
    public LocalPlayer player;

    @Unique
    private boolean divein_1_21_1_neo$waterDrop;
    @Unique
    private boolean divein_1_21_1_neo$lavaDrop;

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        if (player == null) return;
        divein_1_21_1_neo$waterDrop = checkWaterBelow(player, Config.fluidLevelDetectHeight);
        divein_1_21_1_neo$lavaDrop = checkLavaBelow(player, Config.fluidLevelDetectHeight * 2);
        if (divein_1_21_1_neo$waterDrop || divein_1_21_1_neo$lavaDrop) {
            if (player.getDeltaMovement().y < 0)
                divein_1_21_1_neo$tryPlayAnimationOnServerside();
        }
    }

    @Unique
    private void divein_1_21_1_neo$tryPlayAnimationOnServerside() {
        var client = (Minecraft) ((Object)this);
        if (player == null || client.isPaused() || client.screen != null) {
            return;
        }
        boolean hasRes = player.hasEffect(MobEffects.FIRE_RESISTANCE);
        boolean isWearingElytra = player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
        double sensitivity = Config.triggerSensitivity;
        if (sensitivity < 0) sensitivity = 0;
        if (sensitivity > 1) sensitivity = 1;
        boolean isFalling = player.getDeltaMovement().y < sensitivity - 1.0f
                && !player.onGround()
                && Objects.requireNonNull(client.level).getBlockState(player.blockPosition().below()).is(BlockTags.AIR)
                && !player.getAbilities().flying
                && !isWearingElytra;
        if (isFalling || hasRes) {
            if (divein_1_21_1_neo$waterDrop) {
                var visuals = new AnimationEffect.Visuals("dive", AnimationEffect.Particles.DIVE);
                ServerNetwork.networkC2S_Send(new Packets.AnimationPublish(player.getId(), visuals, player.getDeltaMovement()));
                AnimationEffect.playVisuals(visuals, player, player.getDeltaMovement());
            }
            if (divein_1_21_1_neo$lavaDrop) {
                var visuals = new AnimationEffect.Visuals("lava_dive", AnimationEffect.Particles.DIVE);
                ServerNetwork.networkC2S_Send(new Packets.AnimationPublish(player.getId(), visuals, player.getDeltaMovement()));
                AnimationEffect.playVisuals(visuals, player, player.getDeltaMovement());
            }
        }
    }
}
