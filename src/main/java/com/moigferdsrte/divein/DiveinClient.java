package com.moigferdsrte.divein;

import com.moigferdsrte.divein.network.DiveinPosePayload;
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
import net.minecraft.server.level.ServerPlayer;
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


        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "animation"), 45, (player) -> {
            if (player instanceof LocalPlayer) {
                ModifierLayer<IAnimation> testAnimation =  new ModifierLayer<>();

                testAnimation.addModifierBefore(new SpeedModifier(0.8523f)); //This will be medium speed
                testAnimation.addModifierBefore(new MirrorModifier(true));
                return testAnimation;
            }
            return null;
        });

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "animation2"), 45, (player) -> {
            if (player instanceof LocalPlayer) {
                ModifierLayer<IAnimation> testAnimation =  new ModifierLayer<>();

                testAnimation.addModifierBefore(new SpeedModifier(0.5f));
                testAnimation.addModifierBefore(new MirrorModifier(true));
                return testAnimation;
            }
            return null;
        });

        ClientPlayNetworking.registerGlobalReceiver(DiveinPosePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                if (localPlayer != null && !localPlayer.getUUID().equals(payload.uuid())) {
                    playDiveAnimation(payload.uuid(), payload.isWater());
                }
            });
        });

    }

    public static void playWaterFloatingAnimation() {
        ModifierLayer<IAnimation> testAnimation = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                .getPlayerAssociatedData(Minecraft.getInstance().player)
                .get(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "animation2"));

        assert testAnimation != null;
        testAnimation.replaceAnimationWithFade(AbstractFadeModifier.functionalFadeIn(40, (modelName, type, value) -> value),
                new KeyframeAnimationPlayer((KeyframeAnimation) PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "floating")))
                        .setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
                        .setFirstPersonConfiguration(new FirstPersonConfiguration().setShowRightArm(true).setShowLeftItem(false))
        );
    }

    public static void playDiveAnimation(boolean isWater) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        playDiveAnimation(player.getUUID(), isWater);

        //ClientPlayNetworking.send(new DiveinPosePayload(player.getUUID(), isWater));
    }

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
