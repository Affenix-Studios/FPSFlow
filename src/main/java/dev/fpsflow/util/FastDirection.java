package dev.fpsflow.util;

/**
 * Optimized direction operations that avoid enum values() array copies
 * in hot code paths. Every call to Direction.values() creates a defensive
 * copy of the array, which adds unnecessary GC pressure.
 * <p>
 * This provides pre-cached arrays and fast lookup for common direction operations.
 */
public final class FastDirection {
    // Cached Direction IDs for fast lookup (0-5 matching vanilla Direction ordinal)
    public static final int DOWN = 0;
    public static final int UP = 1;
    public static final int NORTH = 2;
    public static final int SOUTH = 3;
    public static final int WEST = 4;
    public static final int EAST = 5;

    // Pre-allocated array to avoid Direction.values() which clones every call
    private static final int[] ALL_IDS = {DOWN, UP, NORTH, SOUTH, WEST, EAST};

    // Horizontal direction IDs for iteration
    public static final int[] HORIZONTAL_IDS = {NORTH, SOUTH, WEST, EAST};

    private FastDirection() {}

    /**
     * Returns cached direction IDs instead of calling Direction.values().
     * Avoids a 6-element array allocation on every call.
     */
    public static int[] all() {
        return ALL_IDS;
    }

    /**
     * Returns cached horizontal direction IDs.
     */
    public static int[] horizontal() {
        return HORIZONTAL_IDS;
    }

    /**
     * Pre-computed offset arrays for directions.
     * Index by direction ID (ordinal).
     */
    public static final int[] STEP_X = {0, 0, 0, 0, -1, 1};
    public static final int[] STEP_Y = {-1, 1, 0, 0, 0, 0};
    public static final int[] STEP_Z = {0, 0, -1, 1, 0, 0};

    /**
     * Gets the opposite direction ID without enum lookup.
     */
    public static int getOpposite(int directionId) {
        return switch (directionId) {
            case DOWN -> UP;
            case UP -> DOWN;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
            default -> directionId;
        };
    }
}