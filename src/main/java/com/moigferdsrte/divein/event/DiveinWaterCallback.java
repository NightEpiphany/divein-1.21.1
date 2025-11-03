package com.moigferdsrte.divein.event;

import dev.kosmx.playerAnim.api.layered.IActualAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface DiveinWaterCallback {

    void update(Player player, Level level, ModifierLayer<? extends IActualAnimation<?>> layer);
}
