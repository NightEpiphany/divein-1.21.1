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
    private static boolean isPlayingAnimation = false;

    private static final Map<UUID, Boolean> playerAnimationStates = new HashMap<>();


    @Override
    public void onInitializeClient() {


        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "animation"), 45, (player) -> {
            if (player instanceof LocalPlayer) {
                ModifierLayer<IAnimation> testAnimation =  new ModifierLayer<>();

                testAnimation.addModifierBefore(new SpeedModifier(0.852f)); //This will be medium speed
                testAnimation.addModifierBefore(new MirrorModifier(true));
                return testAnimation;
            }
            return null;
        });

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "rollback"), 45, (player) -> {
            if (player instanceof LocalPlayer) {
                ModifierLayer<IAnimation> testAnimation =  new ModifierLayer<>();

                testAnimation.addModifierBefore(new SpeedModifier(0.52f));
                testAnimation.addModifierBefore(new MirrorModifier(true));
                return testAnimation;
            }
            return null;
        });

        ClientPlayNetworking.registerGlobalReceiver(DiveinPosePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                LocalPlayer localPlayer = Minecraft.getInstance().player;
                if (localPlayer != null && !localPlayer.getUUID().equals(payload.uuid())) {
                    playDiveAnimation(payload.uuid());
                }
            });
        });

    }

    public static void stopAnimation(UUID playerId) {
        playerAnimationStates.put(playerId, false);

        try {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Player player = level.getPlayerByUUID(playerId);
            if (player == null || !(player instanceof AbstractClientPlayer)) return;

            ModifierLayer<IAnimation> animationLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
                    .getPlayerAssociatedData((AbstractClientPlayer) player)
                    .get(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "rollback"));

            if (animationLayer != null) {
                animationLayer.replaceAnimationWithFade(
                        AbstractFadeModifier.functionalFadeIn(25, (modelName, type, value) -> value),
                        null
                );
            }
        } catch (Exception e) {
            Divein.LOGGER.error("Failed to stop dive animation for player {}", playerId, e);
        }
    }

    public static void playDiveAnimation() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        playDiveAnimation(player.getUUID());

        ClientPlayNetworking.send(new DiveinPosePayload(player.getUUID()));
    }

    public static void playDiveAnimation(UUID uuid) {
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
                        .getAnimation(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "dive"));

                if (diveAnimation != null) {
                    animationLayer.replaceAnimationWithFade(
                            AbstractFadeModifier.functionalFadeIn(60, (modelName, type, value) -> value),
                            new KeyframeAnimationPlayer(diveAnimation)
                                    .setFirstPersonMode(FirstPersonMode.DISABLED)
                                    .setFirstPersonConfiguration(new FirstPersonConfiguration()
                                            .setShowRightArm(true)
                                            .setShowLeftItem(false))
                    );


                    new Thread(() -> {
                        try {
                            Thread.sleep(200);
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


//    public static void stopAnimation() {
//        isPlayingAnimation = false;
//
//        try {
//            LocalPlayer player = Minecraft.getInstance().player;
//            if (player == null) return;
//
//            ModifierLayer<IAnimation> animationLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
//                    .getPlayerAssociatedData(player)
//                    .get(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "rollback"));
//
//
//                    animationLayer.replaceAnimationWithFade(
//                            AbstractFadeModifier.functionalFadeIn(25, (modelName, type, value) -> value),
//                            null
//                    );
//
//        } catch (Exception e) {
//            Divein.LOGGER.error("Failed to play dive animation", e);
//            isPlayingAnimation = false;
//        }
//    }
//
//    public static void playDiveAnimation() {
//        if (isPlayingAnimation) return;
//
//        isPlayingAnimation = true;
////        ModifierLayer<IAnimation> testAnimation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(Minecraft.getInstance().player).get(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "animation"));
////
////
////        testAnimation.replaceAnimationWithFade(AbstractFadeModifier.functionalFadeIn(20, (modelName, type, value) -> value),
////                new KeyframeAnimationPlayer((KeyframeAnimation) PlayerAnimationRegistry.getAnimation(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "dive")))
////                        .setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL)
////                        .setFirstPersonConfiguration(new FirstPersonConfiguration().setShowRightArm(true).setShowLeftItem(false))
////        );
//
//        try {
//            LocalPlayer player = Minecraft.getInstance().player;
//            if (player == null) return;
//
//            ModifierLayer<IAnimation> animationLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
//                    .getPlayerAssociatedData(player)
//                    .get(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "animation"));
//
//            if (animationLayer != null) {
//                KeyframeAnimation diveAnimation = (KeyframeAnimation) PlayerAnimationRegistry
//                        .getAnimation(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "dive"));
//
//                if (diveAnimation != null) {
//                    animationLayer.replaceAnimationWithFade(
//                            AbstractFadeModifier.functionalFadeIn(60, (modelName, type, value) -> value),
//                            new KeyframeAnimationPlayer(diveAnimation)
//                                    .setFirstPersonMode(FirstPersonMode.DISABLED)
//                                    .setFirstPersonConfiguration(new FirstPersonConfiguration()
//                                            .setShowRightArm(true)
//                                            .setShowLeftItem(false))
//                    );
//                    //ClientPlayNetworking.send(new DiveinPosePayload(player.getUUID()));
//
//
//                    new Thread(() -> {
//                        try {
//                            Thread.sleep(20);
//                            isPlayingAnimation = false;
//                        } catch (InterruptedException e) {
//                            Thread.currentThread().interrupt();
//                        }
//                    }).start();
//                }
//            }
//        } catch (Exception e) {
//            Divein.LOGGER.error("Failed to play dive animation", e);
//            isPlayingAnimation = false;
//        }
//    }
//
//    //may need to be called in server env
//    public static void playDiveAnimation(UUID uuid) {
//        if (isPlayingAnimation) return;
//
//        isPlayingAnimation = true;
//
//        try {
//            Level level = Minecraft.getInstance().level;
//            assert level != null;
//            Player player = level.getPlayerByUUID(uuid);
//            if (player == null) return;
//
//            ModifierLayer<IAnimation> animationLayer = (ModifierLayer<IAnimation>) PlayerAnimationAccess
//                    .getPlayerAssociatedData((AbstractClientPlayer) player)
//                    .get(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "animation"));
//
//            if (animationLayer != null) {
//                KeyframeAnimation diveAnimation = (KeyframeAnimation) PlayerAnimationRegistry
//                        .getAnimation(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "dive"));
//
//                if (diveAnimation != null) {
//                    animationLayer.replaceAnimationWithFade(
//                            AbstractFadeModifier.functionalFadeIn(60, (modelName, type, value) -> value),
//                            new KeyframeAnimationPlayer(diveAnimation)
//                                    .setFirstPersonMode(FirstPersonMode.DISABLED)
//                                    .setFirstPersonConfiguration(new FirstPersonConfiguration()
//                                            .setShowRightArm(true)
//                                            .setShowLeftItem(false))
//                    );
//                    ClientPlayNetworking.send(new DiveinPosePayload(player.getUUID()));
//
//
//                    new Thread(() -> {
//                        try {
//                            Thread.sleep(20);
//                            isPlayingAnimation = false;
//                        } catch (InterruptedException e) {
//                            Thread.currentThread().interrupt();
//                        }
//                    }).start();
//                }
//            }
//        } catch (Exception e) {
//            Divein.LOGGER.error("Failed to play dive animation", e);
//            isPlayingAnimation = false;
//        }
//
//    }
}
