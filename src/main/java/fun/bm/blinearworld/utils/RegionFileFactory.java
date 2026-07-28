package fun.bm.blinearworld.utils;

import fun.bm.blinearworld.data.RegionFile;

import java.io.IOException;

@FunctionalInterface
public interface RegionFileFactory {
    RegionFile newFile(RegionCreatorInfo info) throws IOException;
}