package com.moigferdsrte.divein;

import com.moigferdsrte.divein.config.DiveinConfig;
import com.moigferdsrte.divein.event.DiveinEvent;
import com.moigferdsrte.divein.network.DiveinPosePayload;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
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

    public static ConfigHolder<DiveinConfig> configHolder;
    public static DiveinConfig config;

	@Override
	public void onInitialize() {

        AutoConfig.register(DiveinConfig.class, GsonConfigSerializer::new);
        configHolder = AutoConfig.getConfigHolder(DiveinConfig.class);
        config = configHolder.getConfig();

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
            float sensitivity = Divein.config.triggerSensitivity;
            if (sensitivity < 0) sensitivity = 0;
            if (sensitivity > 1) sensitivity = 1;

            boolean isFalling = player.getDeltaMovement().y < sensitivity - 1.0f
                    && !player.onGround()
                    && level.getBlockState(player.blockPosition().below()).is(BlockTags.AIR)
                    && !player.getAbilities().flying;

            boolean isWaterBelow = checkWaterBelow(player, Divein.config.fluidLevelDetectHeight);
            boolean isLavaBelow = checkLavaBelow(player, Divein.config.fluidLevelDetectHeight * 2);

            if (isFalling && !hasTriggeredDive) {
                hasTriggeredDive = true;
                if (isWaterBelow) {
                    DiveinClient.playDiveAnimation(true);
                    if (player.level().isClientSide()) {
                        ClientPlayNetworking.send(new DiveinPosePayload(player.getUUID(), true));
                    }
                }else if (isLavaBelow) {
                    DiveinClient.playDiveAnimation(false);
                    if (player.level().isClientSide()) {
                        ClientPlayNetworking.send(new DiveinPosePayload(player.getUUID(), false));
                    }
                }
            }

            if (!isFalling || !isWaterBelow || !isLavaBelow) {
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

    private boolean checkLavaBelow(Player player, int blocks) {
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