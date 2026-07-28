package fun.bm.bllinearworld;

import fun.bm.bllinearworld.config.ConfigManager;
import fun.bm.bllinearworld.config.RegionFormatConfig;
import fun.bm.bllinearworld.data.BLinearHolder;
import net.fabricmc.api.ModInitializer;

public class Bllinearworld implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigManager.load(RegionFormatConfig.class);
        BLinearHolder.init();
    }
}
