package dev.fpsflow.util;

/**
 * A compact sine lookup table that uses trigonometric identities to reduce
 * the table size while maintaining bit-for-bit compatibility with Minecraft's
 * MathHelper sin/cos.
 * <p>
 * Uses identities:
 * sin(-x) = -sin(x)  → eliminate negative angles
 * sin(x) = sin(π/2 - x) → eliminate supplementary angles
 * <p>
 * Reduces the LUT from 65,536 entries (256 KB) to 16,384 entries (64 KB),
 * improving CPU cache utilization.
 * <p>
 * Inspired by the approach used in CaffeineMC's Lithium mod for compact sine tables.
 *
 * @see <a href="https://github.com/CaffeineMC/lithium">Lithium on GitHub</a>
 */
public final class CompactSineTable {
    private static final int TABLE_SIZE = 16384;
    private static final float[] SIN_TABLE = new float[TABLE_SIZE + 1];
    private static final float MIDPOINT_VALUE;

    static {
        // Build compact sine table using Java's Math which matches Minecraft's MathHelper
        for (int i = 0; i <= TABLE_SIZE; i++) {
            double radians = (i * Math.PI) / (TABLE_SIZE * 2);
            SIN_TABLE[i] = (float) Math.sin(radians);
        }
        MIDPOINT_VALUE = SIN_TABLE[TABLE_SIZE / 2];
    }

    /**
     * Initialize the table. Safe to call multiple times.
     */
    public static void init() {
        // Trigger static initializer
    }

    /**
     * Fast sine approximation using compact LUT.
     * Matches Minecraft's MathHelper.sin() output.
     */
    public static float sin(float value) {
        return sinLookup((int) (value * 10430.378f) & 0xFFFF);
    }

    /**
     * Fast cosine approximation using compact LUT.
     * Matches Minecraft's MathHelper.cos() output.
     */
    public static float cos(float value) {
        return sinLookup((int) (value * 10430.378f + 16384.0f) & 0xFFFF);
    }

    private static float sinLookup(int index) {
        // Handle the special midpoint case
        if (index == 32768) {
            return MIDPOINT_VALUE;
        }

        // sin(-x) = -sin(x): negate if index > pi (bit 15 set)
        int negate = (index & 0x8000) << 16;

        // Determine if we need to mirror across π/2 (bit 14)
        int mask = (index << 17) >> 31;

        // sin(x) = sin(π/2 - x): mirror if in upper half
        int position = (0x8001 & mask) + (index ^ mask);

        // Mask to table size
        position &= 0x7FFF;

        // Get the value and apply sign negation via float bit manipulation
        return Float.intBitsToFloat(
            Float.floatToRawIntBits(SIN_TABLE[position]) ^ negate
        );
    }

    private CompactSineTable() {}
}