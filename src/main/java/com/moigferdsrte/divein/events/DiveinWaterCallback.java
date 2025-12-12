package com.moigferdsrte.divein.events;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface DiveinWaterCallback {

    void update(Player player, Level level, ModifierLayer<? extends IAnimation> controller);
}
