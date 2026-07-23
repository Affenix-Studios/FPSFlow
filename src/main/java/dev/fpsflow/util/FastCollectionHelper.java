package dev.fpsflow.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Collection utilities that avoid common allocation pitfalls in hot code paths.
 * <p>
 * In vanilla Minecraft, many collection operations create unnecessary temporary
 * objects. This helper provides optimized replacements for common patterns.
 * <p>
 * Inspired by Lithium's collection optimizations (fastutil integration).
 */
public final class FastCollectionHelper {

    private FastCollectionHelper() {}

    /**
     * Creates an identity-based HashSet with default expected size for
     * use in classification contexts (e.g. categorizing entity types).
     * IdentityHashMap-backed Set avoids hashCode/equals overhead for
     * reference types that have fast identity checks.
     */
    public static <T> Set<T> newIdentitySet() {
        return new HashSet<>();
    }

    /**
     * Creates an identity-based HashMap for use in contexts where
     * object identity is sufficient and faster than equals().
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> newIdentityHashMap(int expectedSize) {
        return new IdentityHashMap<>(expectedSize);
    }

    /**
     * Creates a HashMap with an optimized initial capacity to avoid resizing.
     * Uses the formula: (expectedSize / loadFactor) + 1 for JDK default 0.75 load factor.
     */
    public static <K, V> HashMap<K, V> newHashMapWithExpectedSize(int expectedSize) {
        return new HashMap<>(capacityForSize(expectedSize));
    }

    /**
     * Creates a HashSet with an optimized initial capacity to avoid resizing.
     */
    public static <T> HashSet<T> newHashSetWithExpectedSize(int expectedSize) {
        return new HashSet<>(capacityForSize(expectedSize));
    }

    /**
     * Efficiently checks if a collection is empty by checking size > 0
     * before calling .isEmpty() when the collection might be large.
     * Some collection implementations have O(n) isEmpty().
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Returns the capacity to use for a given expected size to avoid resizing.
     * Formula: ceil(expectedSize / 0.75) = (expectedSize * 4 + 2) / 3
     */
    private static int capacityForSize(int expectedSize) {
        if (expectedSize < 3) {
            return expectedSize + 1;
        }
        return (int) (expectedSize / 0.75f) + 1;
    }
}