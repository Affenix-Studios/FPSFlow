package dev.fpsflow.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/**
 * Optimized BlockPos operations that reduce allocation overhead
 * by avoiding unnecessary object creation in hot code paths.
 * <p>
 * Provides reusable mutable positions for iteration and comparison,
 * reducing GC pressure in hot loops like chunk iteration and block scanning.
 * <p>
 * Inspired by the fast BlockPos optimizations in CaffeineMC's Lithium mod.
 *
 * @see <a href="https://github.com/CaffeineMC/lithium">Lithium on GitHub</a>
 */
public final class FastBlockPos {
    private static final ThreadLocal<MutablePos> CURSOR = ThreadLocal.withInitial(MutablePos::new);

    private FastBlockPos() {}

    /**
     * Returns a reusable mutable BlockPos for iteration.
     * Must not be stored - contents change on each call.
     */
    public static MutablePos cursor() {
        return CURSOR.get();
    }

    /**
     * Efficiently computes Manhattan distance between two block positions
     * without allocating any objects.
     */
    public static int manhattanDistance(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX())
             + Math.abs(a.getY() - b.getY())
             + Math.abs(a.getZ() - b.getZ());
    }

    /**
     * Efficiently computes squared distance between two block positions.
     */
    public static long squaredDistance(BlockPos a, BlockPos b) {
        long dx = (long) a.getX() - b.getX();
        long dy = (long) a.getY() - b.getY();
        long dz = (long) a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Packs a BlockPos into a single long for use in hash-based collections.
     * Compatible with ChunkPos and World's long key format.
     */
    public static long pack(BlockPos pos) {
        return pack(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Packs x,y,z into a single long for efficient storage.
     * Uses the standard ChunkPos packing: z(28) | y(24) | x(28)
     */
    public static long pack(int x, int y, int z) {
        long packed = 0L;
        packed |= ((long) x & 0xFFFFFFF) << 0;   // x: 28 bits
        packed |= ((long) y & 0xFFFFFF) << 12;   // y: 24 bits
        packed |= ((long) z & 0xFFFFFFF) << 24;  // z: 28 bits
        return packed;
    }

    public static int unpackX(long packed) {
        return (int) (packed << 36 >> 36);
    }

    public static int unpackY(long packed) {
        return (int) (packed << 52 >> 52);
    }

    public static int unpackZ(long packed) {
        return (int) (packed << 40 >> 40);
    }

    /**
     * Reusable mutable BlockPos that avoids allocation in hot loops.
     * Based on similar concepts from Lithium's optimized BlockPos utilities.
     */
    public static final class MutablePos extends BlockPos {
        private int x;
        private int y;
        private int z;

        public MutablePos() {
            super(0, 0, 0);
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }

        public MutablePos set(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public MutablePos set(BlockPos pos) {
            return set(pos.getX(), pos.getY(), pos.getZ());
        }

        public MutablePos set(long packed) {
            return set(unpackX(packed), unpackY(packed), unpackZ(packed));
        }

        public MutablePos offset(Direction direction) {
            return set(x + direction.getOffsetX(), y + direction.getOffsetY(), z + direction.getOffsetZ());
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getZ() {
            return z;
        }

        @Override
        public BlockPos toImmutable() {
            return new BlockPos(x, y, z);
        }

        @Override
        public String toString() {
            return "MutablePos{" + x + "," + y + "," + z + "}";
        }
    }
}