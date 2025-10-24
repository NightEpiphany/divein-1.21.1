package com.moigferdsrte.divein.event.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ServersideDiveEvents {
    public static final Event<PlayerStart> PLAYER_START = new Event.Proxy<>();

    public interface PlayerStart {
        void onPlayerStartedDiving(ServerPlayer player, Vec3 velocity);
    }
}
