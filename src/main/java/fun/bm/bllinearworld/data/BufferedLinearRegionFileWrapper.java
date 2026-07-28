package fun.bm.bllinearworld.data;

import fun.bm.bllinearworld.config.RegionFormatConfig;
import fun.bm.bllinearworld.utils.RegionCreatorInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.file.Path;

/**
 * Wraps a {@link BufferedLinearRegionFile} while extending vanilla {@link RegionFile}
 * so that it can be stored in the vanilla region cache and passed to Moonrise code
 * without ClassCastExceptions.
 * <p>
 * Instances are allocated via {@link sun.misc.Unsafe#allocateInstance} to skip the
 * vanilla constructor (which would attempt to open a .mca header on a .b_linear file).
 * All overridden methods delegate directly to the wrapped blinear backend.
 */
public class BufferedLinearRegionFileWrapper extends RegionFile implements fun.bm.bllinearworld.data.RegionFile {

    private static final sun.misc.Unsafe UNSAFE;

    static {
        try {
            Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (sun.misc.Unsafe) f.get(null);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final Field DELEGATE_FIELD;

    static {
        try {
            DELEGATE_FIELD = BufferedLinearRegionFileWrapper.class.getDeclaredField("delegate");
            DELEGATE_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private BufferedLinearRegionFile delegate;

    /**
     * Private – instances are created via {@link #create}.
     */
    private BufferedLinearRegionFileWrapper() throws IOException {
        // Never called at runtime – Unsafe.allocateInstance bypasses the constructor.
        super(null, null, null, false);
    }

    /**
     * Allocates a wrapper without invoking the vanilla RegionFile constructor,
     * then wires up the blinear delegate.
     */
    public static BufferedLinearRegionFileWrapper create(RegionCreatorInfo info) throws IOException {
        try {
            BufferedLinearRegionFileWrapper wrapper =
                    (BufferedLinearRegionFileWrapper) UNSAFE.allocateInstance(BufferedLinearRegionFileWrapper.class);
            BufferedLinearRegionFile delegate = new BufferedLinearRegionFile(
                    info.filePath(), RegionFormatConfig.compressionLevel, BLinearHolder.blinearFlusher);
            DELEGATE_FIELD.set(wrapper, delegate);
            return wrapper;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to create BufferedLinearRegionFileWrapper", e);
        }
    }

    // ── Delegated overrides ────────────────────────────────────────────

    @Override
    public Path getPath() {
        return this.delegate.getPath();
    }

    @Nullable
    @Override
    public DataInputStream getChunkDataInputStream(ChunkPos pos) throws IOException {
        return this.delegate.getChunkDataInputStream(pos);
    }

    @Override
    public DataOutputStream getChunkDataOutputStream(ChunkPos pos) throws IOException {
        return this.delegate.getChunkDataOutputStream(pos);
    }

    @Override
    public void write(ChunkPos pos, ByteBuffer buf) throws IOException {
        this.delegate.write(pos, buf);
    }

    @Override
    public void flush() throws IOException {
        if (this.delegate != null) this.delegate.flush();
    }

    @Override
    public void close() throws IOException {
        if (this.delegate != null) {
            this.delegate.close();
        }
    }

    @Override
    public void clear(ChunkPos pos) throws IOException {
        this.delegate.clear(pos);
    }

    @Override
    public boolean hasChunk(ChunkPos pos) {
        return this.delegate.hasChunk(pos);
    }

    @Override
    public boolean doesChunkExist(ChunkPos pos) {
        try {
            return this.delegate.doesChunkExist(pos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ── No-op overrides for MCA-specific features ──────────────────────

    @Override
    public CompoundTag getOversizedData(int x, int z) {
        return null;
    }

    @Override
    public boolean isOversized(int x, int z) {
        return false;
    }

    @Override
    public boolean recalculateHeader() {
        return false;
    }

    @Override
    public void setOversized(int x, int z, boolean oversized) {
    }

    @Override
    public int getRecalculateCount() {
        return 0;
    }
}
