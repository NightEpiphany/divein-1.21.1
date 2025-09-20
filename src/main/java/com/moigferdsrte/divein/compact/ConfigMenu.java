package com.moigferdsrte.divein.compact;

import com.moigferdsrte.divein.config.DiveinConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class ConfigMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return s -> AutoConfig.getConfigScreen(DiveinConfig.class, s).get();
    }
}
