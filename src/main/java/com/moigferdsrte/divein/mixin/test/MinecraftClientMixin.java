package com.moigferdsrte.divein.mixin.test;

import com.moigferdsrte.divein.DiveinClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void ATTACK(CallbackInfoReturnable<Boolean> cir) {
        System.out.println("MinecraftClientMixin - startAttack");
        //DiveinClient.playDiveAnimation(true);
        cir.setReturnValue(true);
    }
}
