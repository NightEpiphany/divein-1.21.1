package com.moigferdsrte.divein.event;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface DiveinWaterCallback {

    void update(Player player, Level level, @Nullable PlayerAnimationController controller);
}
