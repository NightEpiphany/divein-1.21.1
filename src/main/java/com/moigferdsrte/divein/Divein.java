package com.moigferdsrte.divein;

import com.moigferdsrte.divein.config.DiveinConfig;
import com.moigferdsrte.divein.event.DiveinEvent;
import com.moigferdsrte.divein.network.Packets;
import com.moigferdsrte.divein.network.ServerNetwork;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

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
    public static boolean hasTriggeredDive = false;

    public static ConfigHolder<DiveinConfig> configHolder;
    public static DiveinConfig config;

	@Override
	public void onInitialize() {

        PayloadTypeRegistry.playC2S().register(Packets.AnimationPublish.TYPE, Packets.AnimationPublish.CODEC);
        PayloadTypeRegistry.playS2C().register(Packets.DiveAnimation.TYPE, Packets.DiveAnimation.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(Packets.AnimationPublish.TYPE, (packet, context) -> ServerNetwork.handleDivePublish(packet, context.server(), context.player()));

        AutoConfig.register(DiveinConfig.class, GsonConfigSerializer::new);
        configHolder = AutoConfig.getConfigHolder(DiveinConfig.class);
        config = configHolder.getConfig();



		LOGGER.info("Divein!");
	}

    @Deprecated
    public void eventHook() {
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
                }else if (isLavaBelow) {
                    DiveinClient.playDiveAnimation(false);
                }
            }

            if (!isFalling || !isWaterBelow || !isLavaBelow) {
                hasTriggeredDive = false;
            }
        });
    }

    public static boolean checkWaterBelow(Player player, int blocks) {
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

    public static boolean checkLavaBelow(Player player, int blocks) {
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