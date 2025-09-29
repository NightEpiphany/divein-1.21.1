package org.moigferdsrte;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.moigferdsrte.network.DiveinPosePayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(value = Divein.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Divein.MODID, value = Dist.CLIENT)
public class DiveinClient {

    private static final Map<UUID, Boolean> playerAnimationStates = new HashMap<>();

    public DiveinClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ResourceLocation.fromNamespaceAndPath(Divein.MODID, "animation"), 45, (player) -> {
            if (player instanceof LocalPlayer) {
                ModifierLayer<IAnimation> testAnimation =  new ModifierLayer<>();

                testAnimation.addModifierBefore(new SpeedModifier(0.8523f));
                testAnimation.addModifierBefore(new MirrorModifier(true));
                return testAnimation;
            }
            return null;
        });
    }

    public static void playDiveAnimation(boolean isWater) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        playDiveAnimation(player.getUUID(), isWater);
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
                    .get(ResourceLocation.fromNamespaceAndPath(Divein.MODID, "animation"));

            if (animationLayer != null) {
                KeyframeAnimation diveAnimation = (KeyframeAnimation) PlayerAnimationRegistry
                        .getAnimation(ResourceLocation.fromNamespaceAndPath(Divein.MODID, isWater ? "dive" : "lava_dive"));

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
