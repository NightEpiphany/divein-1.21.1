package com.moigferdsrte.divein.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class DiveinEvent {

    public static final Event<DiveinWaterCallback> DIVEIN_WATER_EVENT =
            EventFactory.createArrayBacked(DiveinWaterCallback.class, call -> (player, level) -> {
                for (DiveinWaterCallback callback : call) {
                    callback.update(player, level);
                }
            });
}
