package org.moigferdsrte;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;


@EventBusSubscriber(modid = Divein.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue TRIGGER_SENSITIVITY = BUILDER
            .comment("Animation Trigger Sensitivity")
            .defineInRange("triggerSensitivity", 0.615d, 0.0d, 1.0d);

    public static final ModConfigSpec.IntValue DETECT_HEIGHT = BUILDER
            .comment("Fluid Level Detect Height")
            .defineInRange("fluidLevelDetectHeight", 28, 0, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int fluidLevelDetectHeight;

    public static double triggerSensitivity;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            fluidLevelDetectHeight = DETECT_HEIGHT.get();
            triggerSensitivity = TRIGGER_SENSITIVITY.get();
        }
    }
}
