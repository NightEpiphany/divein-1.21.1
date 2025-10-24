package com.moigferdsrte.divein;

import com.moigferdsrte.divein.network.ClientNetwork;
import com.moigferdsrte.divein.network.Packets;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class DiveinClient implements ClientModInitializer {

    private static final Map<UUID, Boolean> playerAnimationStates = new HashMap<>();


    @Override
    public void onInitializeClient() {

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "dive"), 1600,
                player -> new PlayerAnimationController(player,
                        (controller, state, animSetter) -> PlayState.STOP
                )
        );

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ResourceLocation.fromNamespaceAndPath(Divein.MOD_ID, "lava_dive"), 1600,
                player -> new PlayerAnimationController(player,
                        (controller, state, animSetter) -> PlayState.STOP
                )
        );

        ClientPlayNetworking.registerGlobalReceiver(Packets.DiveAnimation.TYPE, (packet, context) -> ClientNetwork.handleDiveAnimation(packet));
    }
}
