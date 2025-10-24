package org.moigferdsrte.event;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import org.moigferdsrte.Config;

public class TriggerEventHandler {
    private static boolean hasTriggeredDive = false;
    static double sensitivity = Config.triggerSensitivity;
//    @SubscribeEvent
//    public static void onPlayerFall(PlayerTickEvent.Post event)
//    {
//        if (event.getEntity() instanceof Player player) {
//            Level level = event.getEntity().level();
//            boolean isFalling = player.getDeltaMovement().y < sensitivity - 1.0f
//                    && !player.onGround()
//                    && level.getBlockState(player.blockPosition().below()).is(BlockTags.AIR)
//                    && !player.getAbilities().flying;
//            if (!player.level().isClientSide() || !isFalling) return;
//
//            boolean isWaterBelow = checkWaterBelow(player, Config.fluidLevelDetectHeight);
//            boolean isLavaBelow = checkLavaBelow(player, Config.fluidLevelDetectHeight * 2);
//
//            if (!hasTriggeredDive) {
//                hasTriggeredDive = true;
//                if (isWaterBelow) {
//                    DiveinClient.playDiveAnimation(true);
//                }else if (isLavaBelow) {
//                    DiveinClient.playDiveAnimation(false);
//                }
//            }
//
//            if (!isWaterBelow || !isLavaBelow) {
//                hasTriggeredDive = false;
//            }
//        }
//    }

    public static boolean checkWaterBelow(Player player, int blocks) {
        if (player.isInWater()) return true;
        for (int i = 1; i <= blocks; i++) {
            if (player.level().getBlockState(player.blockPosition().below(i)).is(Blocks.WATER) &&
                    player.level().getBlockState(player.blockPosition().below(i - 1)).is(BlockTags.AIR)) {
                if (!player.level().getBlockState(player.blockPosition().below(i + 1)).is(Blocks.WATER)) return false;
                for (int j = 1; j <= Config.triggerDepth; j++) {
                    if (player.level().getBlockState(player.blockPosition().below(i - 1 + j)).getFluidState().getType() != Fluids.WATER) return false;
                }
                return true;
            }
            if (!player.level().getBlockState(player.blockPosition().below(i)).is(BlockTags.AIR)) break;
        }
        return false;
    }

    public static boolean checkLavaBelow(Player player, int blocks) {
        if (player.isInLava()) return true;
        for (int i = 1; i <= blocks; i++) {
            if (player.level().getBlockState(player.blockPosition().below(i)).getFluidState().getType() == Fluids.LAVA &&
                    player.level().getBlockState(player.blockPosition().below(i - 1)).is(BlockTags.AIR)) {
                for (int j = 1; j <= Config.triggerDepth; j++) {
                    if (player.level().getBlockState(player.blockPosition().below(i - 1 + j)).getFluidState().getType() != Fluids.LAVA) return false;
                }
                return true;
            }
            if (!player.level().getBlockState(player.blockPosition().below(i)).is(BlockTags.AIR)) break;
        }
        return false;
    }

    public static boolean isHasTriggeredDive() {
        return hasTriggeredDive;
    }

    public static void setHasTriggeredDive(boolean hasTriggeredDive) {
        TriggerEventHandler.hasTriggeredDive = hasTriggeredDive;
    }

    public @interface SyncForServer {
        boolean value() default true;
    }
}
