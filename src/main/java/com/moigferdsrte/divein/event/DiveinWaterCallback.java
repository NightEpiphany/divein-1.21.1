package com.moigferdsrte.divein.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface DiveinWaterCallback {

    void update(Player player, Level level);
}
