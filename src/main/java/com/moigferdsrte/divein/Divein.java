package com.moigferdsrte.divein;

import com.moigferdsrte.divein.event.DiveinEvent;
import com.moigferdsrte.divein.network.DiveinPosePayload;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Divein implements ModInitializer {
	public static final String MOD_ID = "divein";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static boolean hasTriggeredDive = false;

	@Override
	public void onInitialize() {

        PayloadTypeRegistry.playC2S().register(DiveinPosePayload.TYPE, DiveinPosePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DiveinPosePayload.TYPE, DiveinPosePayload.CODEC);


        ServerPlayNetworking.registerGlobalReceiver(DiveinPosePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                for (var serverPlayer : context.server().getPlayerList().getPlayers()) {
                    if (serverPlayer != context.player()) {
                        ServerPlayNetworking.send(serverPlayer, payload);
                    }
                }
            });
        });

        DiveinEvent.DIVEIN_WATER_EVENT.register((player, level) -> {
            if (!player.level().isClientSide()) return;


            boolean isFalling = player.getDeltaMovement().y < -0.385f
                    && !player.onGround()
                    && level.getBlockState(player.blockPosition().below()).is(BlockTags.AIR)
                    && !player.getAbilities().flying;

            boolean isWaterBelow = checkWaterBelow(player, 28);

            if (isFalling && isWaterBelow && !hasTriggeredDive) {
                hasTriggeredDive = true;
                DiveinClient.playDiveAnimation();
                if (player.level().isClientSide()) {
                    ClientPlayNetworking.send(new DiveinPosePayload(player.getUUID()));
                }
            }

            if (!isFalling || !isWaterBelow) {
                hasTriggeredDive = false;
            }
        });

		LOGGER.info("Divein!");
	}

    private boolean checkWaterBelow(Player player, int blocks) {
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
}