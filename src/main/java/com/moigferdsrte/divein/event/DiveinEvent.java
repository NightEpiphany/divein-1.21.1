package com.moigferdsrte.divein.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class DiveinEvent {


    public static final Event<DiveinWaterCallback> DIVEIN_WATER_EVENT =
            EventFactory.createArrayBacked(DiveinWaterCallback.class, call -> (player, level, controller) -> {
                if (player.getDeltaMovement().y < 0)
                    for (DiveinWaterCallback callback : call) {
                        callback.update(player, level, controller);
                    }
            });

    public @interface SyncForServer {
        boolean value() default true;
    }
}
