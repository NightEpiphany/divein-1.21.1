package org.moigferdsrte;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

@Mod(Divein.MODID)
@EventBusSubscriber(modid = Divein.MODID)
public class Divein {
    public static final String MODID = "divein";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Divein(IEventBus ignoredModEventBus, ModContainer modContainer) {

        //NeoForge.EVENT_BUS.addListener(TriggerEventHandler::onPlayerFall);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }


    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {

    }
}
