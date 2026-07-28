package fun.bm.blinearworld.mixin;

import com.mojang.logging.LogUtils;
import fun.bm.blinearworld.data.BLinearHolder;
import fun.bm.blinearworld.data.BufferedLinearRegionFileWrapper;
import fun.bm.blinearworld.utils.RegionCreatorInfo;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Intercepts {@link RegionFileStorage} to:
 * <ul>
 *   <li>Change the region file path from {@code .mca} to {@code .b_linear}
 *       by redirecting {@code Path.resolve(...)}.</li>
 *   <li>Replace {@code new RegionFile(...)} with {@link BufferedLinearRegionFileWrapper}
 *       instances that delegate I/O to the blinear backend.</li>
 * </ul>
 */
@Mixin(RegionFileStorage.class)
public abstract class RegionFileStorageMixin {

    @Unique
    private static final Logger BLINEAR_LOGGER = LogUtils.getLogger();

    // ── File path change ───────────────────────────────────────────────

    /**
     * In vanilla 26.2 the file name is hardcoded inline inside
     * {@code getRegionFile(ChunkPos)} as:
     * <pre>
     *   this.folder.resolve("r." + regionX + "." + regionZ + ".mca")
     * </pre>
     * This redirect intercepts {@code Path.resolve(String)} and replaces the
     * file name with the {@code .b_linear} extension.
     */
    @Redirect(
            method = "getRegionFile",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/file/Path;resolve(Ljava/lang/String;)Ljava/nio/file/Path;"
            )
    )
    private Path blinear$changeFileName(Path folder, String originalName) {
        // originalName is "r.X.Z.mca" — extract coordinates and rebuild with .b_linear
        String[] parts = originalName.split("\\.");
        if (parts.length >= 3) {
            String newName = "r." + parts[1] + "." + parts[2] + "." + BLinearHolder.argument;
            return folder.resolve(newName);
        }
        // Fallback — should never happen with vanilla code
        return folder.resolve(originalName);
    }

    // ── Constructor interception ───────────────────────────────────────

    /**
     * Redirects {@code new RegionFile(info, file, folder, sync)} to our
     * factory which creates a {@link BufferedLinearRegionFileWrapper}.
     */
    @Redirect(
            method = "getRegionFile",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/level/chunk/storage/RegionFile"
            )
    )
    private RegionFile blinear$createRegionFile(
            RegionStorageInfo info, Path path, Path folder, boolean sync
    ) throws IOException {
        BLINEAR_LOGGER.debug("Creating blinear region file: {}", path);
        return BufferedLinearRegionFileWrapper.create(
                new RegionCreatorInfo(info, path, folder, sync)
        );
    }
}
