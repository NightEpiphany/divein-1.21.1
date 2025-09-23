package org.moigferdsrte;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.moigferdsrte.event.TriggerEventHandler;
import org.moigferdsrte.network.DiveinPosePayload;
import org.slf4j.Logger;

import static org.moigferdsrte.DiveinClient.playDiveAnimation;

@Mod(Divein.MODID)
@EventBusSubscriber(modid = Divein.MODID)
public class Divein {
    public static final String MODID = "divein";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Divein(IEventBus modEventBus, ModContainer modContainer) {

        //NeoForge.EVENT_BUS.addListener(TriggerEventHandler::onPlayerFall);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }


    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {

    }
}
