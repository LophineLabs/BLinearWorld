package fun.bm.blinearworld.config;

public class RegionFormatConfig {
    @ConfigInfo(name = "compression_level")
    public static int compressionLevel = 1;

    @ConfigInfo(name = "io_flush_delay_ms")
    public static int ioFlushDelayMs = 3000;

    @ConfigInfo(name = "io_thread_count")
    public static int ioThreadCount = 6;
}