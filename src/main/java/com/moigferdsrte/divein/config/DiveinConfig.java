package com.moigferdsrte.divein.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "divein")
public class DiveinConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public float triggerSensitivity = 0.615f;
    @ConfigEntry.Gui.Tooltip
    public int fluidLevelDetectHeight = 28;
    @ConfigEntry.Gui.Tooltip
    public int triggerDepth = 2;
    public float speedModifier = 0.92323f;
}
