package org.moigferdsrte.event;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.moigferdsrte.Config;
import org.moigferdsrte.DiveinClient;
import org.moigferdsrte.network.DiveinPosePayload;


public class TriggerEventHandler {
    private static boolean hasTriggeredDive = false;
    static double sensitivity = Config.triggerSensitivity;
    @SubscribeEvent
    public static void onPlayerFall(PlayerTickEvent.Post event)
    {
        if (event.getEntity() instanceof Player player) {
            Level level = event.getEntity().level();
            boolean isFalling = player.getDeltaMovement().y < sensitivity - 1.0f
                    && !player.onGround()
                    && level.getBlockState(player.blockPosition().below()).is(BlockTags.AIR)
                    && !player.getAbilities().flying;
            if (!player.level().isClientSide() || !isFalling) return;

            boolean isWaterBelow = checkWaterBelow(player, Config.fluidLevelDetectHeight);
            boolean isLavaBelow = checkLavaBelow(player, Config.fluidLevelDetectHeight * 2);

            if (!hasTriggeredDive) {
                hasTriggeredDive = true;
                if (isWaterBelow) {
                    DiveinClient.playDiveAnimation(true);
                    if (player.level().isClientSide()) {
                        PacketDistributor.sendToServer(new DiveinPosePayload(player.getUUID(), true));
                    }
                }else if (isLavaBelow) {
                    DiveinClient.playDiveAnimation(false);
                    if (player.level().isClientSide()) {
                        PacketDistributor.sendToServer(new DiveinPosePayload(player.getUUID(), false));
                    }
                }
            }

            if (!isFalling || !isWaterBelow || !isLavaBelow) {
                hasTriggeredDive = false;
            }
        }
    }

    private static boolean checkWaterBelow(Player player, int blocks) {
        if (player.isInWater()) return true;
        for (int i = 1; i <= blocks; i++) {
            if (player.level().getBlockState(player.blockPosition().below(i)).getFluidState().getType() == Fluids.WATER &&
                    player.level().getBlockState(player.blockPosition().below(i - 1)).is(BlockTags.AIR)) {
                return true;
            }
            if (!player.level().getBlockState(player.blockPosition().below(i)).is(BlockTags.AIR)) break;
        }
        return false;
    }

    private static boolean checkLavaBelow(Player player, int blocks) {
        if (player.isInLava()) return true;
        for (int i = 1; i <= blocks; i++) {
            if (player.level().getBlockState(player.blockPosition().below(i)).getFluidState().getType() == Fluids.LAVA &&
                    player.level().getBlockState(player.blockPosition().below(i - 1)).is(BlockTags.AIR)) {
                return true;
            }
            if (!player.level().getBlockState(player.blockPosition().below(i)).is(BlockTags.AIR)) break;
        }
        return false;
    }
}
