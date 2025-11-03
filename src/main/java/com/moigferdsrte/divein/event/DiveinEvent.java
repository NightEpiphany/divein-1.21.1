package com.moigferdsrte.divein.event;

import com.moigferdsrte.divein.Divein;
import com.moigferdsrte.divein.config.DiveinConfig;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class DiveinEvent {


    public static final Event<DiveinWaterCallback> DIVEIN_WATER_EVENT =
            EventFactory.createArrayBacked(DiveinWaterCallback.class, call -> (player, level, controller) -> {
                if (player.getDeltaMovement().y < Divein.config.triggerSensitivity - 1.0f)
                    for (DiveinWaterCallback callback : call) {
                        callback.update(player, level, controller);
                    }
            });

    public @interface SyncForServer {
        boolean value() default true;
    }
}
