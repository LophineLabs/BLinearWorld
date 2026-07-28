package fun.bm.blinearworld;

import fun.bm.blinearworld.config.ConfigManager;
import fun.bm.blinearworld.config.RegionFormatConfig;
import fun.bm.blinearworld.data.BLinearHolder;
import net.fabricmc.api.ModInitializer;

public class BlinearWorld implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigManager.load(RegionFormatConfig.class);
        BLinearHolder.init();
    }
}
