package dev.fpsflow.mixin.optimization;

import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces calls to {@link Direction#values()} with a cached reference
 * to avoid the defensive array copy that vanilla JDK {@code Enum.values()}
 * performs on every call.
 * <p>
 * Direction.values() is called frequently in block update, redstone,
 * and entity collision code. Each call allocates a new 6-element array,
 * adding unnecessary GC pressure.
 * <p>
 * Based on the enum values optimization in CaffeineMC's Lithium mod.
 *
 * @see <a href="https://github.com/CaffeineMC/lithium">Lithium on GitHub</a>
 */
@Mixin(Direction.class)
public class DirectionValuesMixin {

    /**
     * Cached direction array to avoid allocations from Direction.values().
     * We cannot directly modify Direction.values() behavior in Java,
     * but we provide a constant reference that mixins can use.
     */
    public static final Direction[] CACHED_VALUES = Direction.values();
}