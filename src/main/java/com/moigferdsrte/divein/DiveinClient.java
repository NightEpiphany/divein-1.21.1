package com.moigferdsrte.divein;

import com.moigferdsrte.divein.event.DiveinEvent;
import com.moigferdsrte.divein.network.ClientNetwork;
import com.moigferdsrte.divein.network.Packets;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class DiveinClient implements ClientModInitializer {

    private static final Map<UUID, Boolean> playerAnimationStates = new HashMap<>();


    @Override
    public void onInitializeClient() {

        ClientPlayNetworking.registerGlobalReceiver(Packets.DiveAnimation.TYPE, (packet, context) -> ClientNetwork.handleDiveAnimation(packet));


        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "animation"), 45, (player) -> {
            if (player instanceof LocalPlayer) {
                ModifierLayer<IAnimation> testAnimation =  new ModifierLayer<>();

                testAnimation.addModifierBefore(new SpeedModifier(0.8523f)); //This will be medium speed
                testAnimation.addModifierBefore(new MirrorModifier(true));
                return testAnimation;
            }
            return null;
        });

    }

    @DiveinEvent.SyncForServer(value = false)
    public static void playDiveAnimation(boolean isWater) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        playDiveAnimation(player.getUUID(), isWater);
    }
    @DiveinEvent.SyncForServer(value = false)
    public static void playDiveAnimation(UUID uuid, boolean isWater) {
        if (playerAnimationStates.getOrDefault(uuid, false)) return;
        playerAnimationStates.put(uuid, true);

        try {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Player player = level.getPlayerByUUID(uuid);
            if (!(player instanceof AbstractClientPlayer)) return;

            ModifierLayer<IAnimation> animationLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData((AbstractClientPlayer) player)
                    .get(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "animation"));

            if (animationLayer != null) {
                KeyframeAnimation diveAnimation = (KeyframeAnimation) PlayerAnimationRegistry
                        .getAnimation(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, isWater ? "dive" : "lava_dive"));

                if (diveAnimation != null) {
                    animationLayer.replaceAnimationWithFade(
                            AbstractFadeModifier.functionalFadeIn(100, (modelName, type, value) -> value),
                            new KeyframeAnimationPlayer(diveAnimation)
                                    .setFirstPersonMode(FirstPersonMode.DISABLED)
                                    .setFirstPersonConfiguration(new FirstPersonConfiguration()
                                            .setShowRightArm(true)
                                            .setShowLeftItem(false))
                    );


                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            playerAnimationStates.put(uuid, false);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            Divein.LOGGER.error("Failed to play dive animation for player {}", uuid, e);
            playerAnimationStates.put(uuid, false);
        }
    }
}
