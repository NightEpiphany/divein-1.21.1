package com.moigferdsrte.divein.events;

import com.moigferdsrte.divein.Divein;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class DiveinEvent {

    public static final Event<DiveinWaterCallback> DIVEIN_WATER_EVENT =
            EventFactory.createArrayBacked(DiveinWaterCallback.class, call -> (player, level, c) -> {
                if (player.getDeltaMovement().y < Divein.config.triggerSensitivity - 1.0F)
                    for (DiveinWaterCallback callback : call) {
                        callback.update(player, level, c);
                    }
            });

    public @interface SyncForServer {
        boolean value() default true;
    }
}
