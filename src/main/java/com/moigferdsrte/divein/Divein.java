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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Divein implements ModInitializer {
	public static final String MOD_ID = "divein";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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

    public static boolean checkWaterBelow(Player player, int blocks) {
        if (player.isInWater()) return true;
        for (int i = 1; i <= blocks; i++) {
            if (player.level().getBlockState(player.blockPosition().below(i)).is(Blocks.WATER) &&
                    player.level().getBlockState(player.blockPosition().below(i - 1)).is(BlockTags.AIR)) {
                if (!player.level().getBlockState(player.blockPosition().below(i + 1)).is(Blocks.WATER)) return false;
                for (int j = 1; j <= Divein.config.triggerDepth; j++) {
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
                for (int j = 1; j <= Divein.config.triggerDepth; j++) {
                    if (player.level().getBlockState(player.blockPosition().below(i - 1 + j)).getFluidState().getType() != Fluids.LAVA) return false;
                }
                return true;
            }
            if (!player.level().getBlockState(player.blockPosition().below(i)).is(BlockTags.AIR)) break;
        }
        return false;
    }
}