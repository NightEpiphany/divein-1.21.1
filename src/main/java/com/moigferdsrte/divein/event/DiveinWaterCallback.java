package com.moigferdsrte.divein.event;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface DiveinWaterCallback {

    void update(Player player, Level level, PlayerAnimationController controller);
}
