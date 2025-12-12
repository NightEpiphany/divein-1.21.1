package com.moigferdsrte.divein;

import com.moigferdsrte.divein.config.DiveinConfig;
import com.moigferdsrte.divein.events.DiveinEvent;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import io.netty.buffer.Unpooled;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Divein implements ModInitializer {
	public static final String MOD_ID = "divein";

    public static final TagKey<Block> AIRS = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "air"));

    public static ConfigHolder<DiveinConfig> configHolder;
    public static DiveinConfig config;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ResourceLocation ANIMATION_S2C = new ResourceLocation(MOD_ID, "anim_s2c");
    public static final ResourceLocation ANIMATION_C2S = new ResourceLocation(MOD_ID, "anim_c2s");

	@Override
	public void onInitialize() {
        ServerSidePacketRegistry.INSTANCE.register(ANIMATION_C2S, (packetContext, attachedData) -> {
            var id = attachedData.readInt();
            var animationName = attachedData.readUtf();
            var vec = attachedData.readVector3f();
            
            packetContext.getTaskQueue().execute(() -> {
                Player player = packetContext.getPlayer();
                PlayerStream.watching(player.level(), player.blockPosition()).forEach(sp -> {
                    try {
                        if (sp.getId() != player.getId() && ServerPlayNetworking.canSend((ServerPlayer) sp, ANIMATION_S2C)) {
                            FriendlyByteBuf passedData = new FriendlyByteBuf(Unpooled.buffer());
                            passedData.writeInt(id);
                            passedData.writeUtf(animationName);
                            passedData.writeVector3f(vec);
                            ServerSidePacketRegistry.INSTANCE.sendToPlayer(sp, ANIMATION_S2C, passedData);
                        }
                    }catch (Exception e) {
                        Divein.LOGGER.error("Failed to send dive animation packet to {}", sp.getDisplayName().getString());
                    }
                });
            });
        });
        AutoConfig.register(DiveinConfig.class, GsonConfigSerializer::new);
        configHolder = AutoConfig.getConfigHolder(DiveinConfig.class);
        config = configHolder.getConfig();

        DiveinEvent.DIVEIN_WATER_EVENT.register((player, level, controller) -> {
            if (controller.getAnimation() == null) return;
            if (player.isFallFlying() || player.isSwimming() || (player.onGround() && player.isInWater())) {
                if (controller.getAnimation() instanceof KeyframeAnimationPlayer k) {
                    k.stop();
                }
            }
        });

		LOGGER.info("Hello Fabric world!");
	}

    public static boolean checkWaterBelow(Player player, int blocks) {
        if (player.isInWater()) return true;
        for (int i = 1; i <= blocks; i++) {
            if (player.level().getBlockState(player.blockPosition().below(i)).is(Blocks.WATER) &&
                    (player.level().getBlockState(player.blockPosition().below(i - 1)).is(Blocks.AIR) || player.level().getBlockState(player.blockPosition().below(i - 1)).is(Blocks.CAVE_AIR))
            ) {
                if (!player.level().getBlockState(player.blockPosition().below(i + 1)).is(Blocks.WATER)) return false;
                for (int j = 1; j <= Divein.config.triggerDepth; j++) {
                    if (player.level().getBlockState(player.blockPosition().below(i - 1 + j)).getFluidState().getType() != Fluids.WATER) return false;
                }
                return true;
            }
            if (!(player.level().getBlockState(player.blockPosition().below(i)).is(Blocks.AIR) || player.level().getBlockState(player.blockPosition().below(i)).is(Blocks.CAVE_AIR))) break;
        }
        return false;
    }

    public static boolean checkLavaBelow(Player player, int blocks) {
        if (player.isInLava()) return true;
        for (int i = 1; i <= blocks; i++) {
            if (player.level().getBlockState(player.blockPosition().below(i)).getFluidState().getType() == Fluids.LAVA &&
                    (player.level().getBlockState(player.blockPosition().below(i - 1)).is(Blocks.AIR) || player.level().getBlockState(player.blockPosition().below(i - 1)).is(Blocks.CAVE_AIR))) {
                for (int j = 1; j <= Divein.config.triggerDepth; j++) {
                    if (player.level().getBlockState(player.blockPosition().below(i - 1 + j)).getFluidState().getType() != Fluids.LAVA) return false;
                }
                return true;
            }
            if (!(player.level().getBlockState(player.blockPosition().below(i)).is(Blocks.AIR) || player.level().getBlockState(player.blockPosition().below(i)).is(Blocks.CAVE_AIR))) break;
        }
        return false;
    }
}