package fun.bm.bllinearworld.utils;

import fun.bm.bllinearworld.data.RegionFile;

import java.io.IOException;

@FunctionalInterface
public interface RegionFileFactory {
    RegionFile newFile(RegionCreatorInfo info) throws IOException;
}