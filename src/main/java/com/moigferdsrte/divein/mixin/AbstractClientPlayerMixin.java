package com.moigferdsrte.divein.mixin;

import com.moigferdsrte.divein.event.DiveinEvent;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player {

    public AbstractClientPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        DiveinEvent.DIVEIN_WATER_EVENT.invoker().update(this, this.level());
    }
}
