package fun.bm.bllinearworld.data;

import com.mojang.logging.LogUtils;
import fun.bm.bllinearworld.config.RegionFormatConfig;
import fun.bm.bllinearworld.utils.RegionFileFactory;

public class BLinearHolder {
    public static BufferedLinearRegionFileFlusher blinearFlusher = null;

    public static final String argument = "b_linear";
    public static final RegionFileFactory creator = (info) -> new BufferedLinearRegionFile(info.filePath(), RegionFormatConfig.compressionLevel, blinearFlusher);

    public static void init() {
        blinearFlusher = new BufferedLinearRegionFileFlusher(RegionFormatConfig.ioThreadCount, 20, RegionFormatConfig.ioFlushDelayMs);

        checkCompressionLevel();

        // we don't need to consider that it will be reloaded more than once as this config is unreloadable
        Runtime.getRuntime().addShutdownHook(new Thread(() -> blinearFlusher.shutdown()));
    }

    private static void checkCompressionLevel() {
        if (RegionFormatConfig.compressionLevel > 23 || RegionFormatConfig.compressionLevel < 1) {
            LogUtils.getLogger().error("Linear or BufferedLinear region compression level should be between 1 and 22 in config: {}", RegionFormatConfig.compressionLevel);
            LogUtils.getLogger().error("Falling back to compression level 1.");
            RegionFormatConfig.compressionLevel = 1;
        }
    }
}
