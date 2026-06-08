package dev.fpsflow.entities;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.join.WorldJoinOptimizer;
import dev.fpsflow.optimization.OptimizationModule;
import dev.fpsflow.rendering.AdaptiveRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.WeakHashMap;

/**
 * Manages nameplate (entity label) culling with server override detection.
 * When a server forces nameplate visibility via packets or game rules,
 * this manager gracefully backs off to avoid conflicts.
 */
public final class NameplateCullingManager implements OptimizationModule {

    private static final NameplateCullingManager INSTANCE = new NameplateCullingManager();

    // Render-thread-only; no synchronisation needed
    private static final WeakHashMap<Entity, Boolean> VISIBILITY_CACHE = new WeakHashMap<>();
    private static final WeakHashMap<Entity, Long> LAST_CHECK_TICK = new WeakHashMap<>();

    // Server override detection: tracks entities where server forces visibility
    private static final WeakHashMap<Entity, Boolean> SERVER_FORCED_VISIBILITY = new WeakHashMap<>();

    private NameplateCullingManager() {}

    public static NameplateCullingManager getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId() {
        return "nameplate-culling";
    }

    @Override
    public void initialize() {
        FPSFlow.LOGGER.debug("[FPSFlow] NameplateCullingManager ready");
    }

    @Override
    public void shutdown() {
        VISIBILITY_CACHE.clear();
        LAST_CHECK_TICK.clear();
        SERVER_FORCED_VISIBILITY.clear();
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().getConfig().nameplateCulling.enabled;
    }

    @Override
    public void onTick() {
        // Cleanup old entries periodically
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.world.getTime() % 200 == 0) {
            VISIBILITY_CACHE.clear();
            LAST_CHECK_TICK.clear();
            // Don't clear SERVER_FORCED_VISIBILITY, it uses WeakHashMap
        }
    }

    /**
     * Determines if a nameplate should be visible based on distance and server state.
     * Returns true if the nameplate SHOULD be shown.
     */
    public boolean shouldShowNameplate(Entity entity, double squaredDistanceToCam) {
        if (!isEnabled()) return true;

        MinecraftClient client = MinecraftClient.getInstance();
        long currentTick = (client.world != null) ? client.world.getTime() : 0L;

        FPSFlowConfig.NameplateCullingConfig cfg =
                ConfigManager.getInstance().getConfig().nameplateCulling;

        int interval = Math.max(1, cfg.checkIntervalTicks);

        // Check if server is forcing visibility for this entity
        Boolean serverForced = SERVER_FORCED_VISIBILITY.get(entity);
        if (serverForced != null && serverForced) {
            // Server is forcing visibility - don't cull, but keep cache fresh
            VISIBILITY_CACHE.put(entity, true);
            LAST_CHECK_TICK.put(entity, currentTick);
            return true;
        }

        Long lastCheck = LAST_CHECK_TICK.get(entity);
        Boolean cached = VISIBILITY_CACHE.get(entity);

        // Within the interval: return the cached decision without recalculating
        if (cached != null && lastCheck != null && (currentTick - lastCheck) < interval) {
            return cached;
        }

        // Apply hysteresis: widen the hide-threshold and narrow the show-threshold by 15%
        // This creates a dead-band around maxDistance so entities that hover at the
        // boundary don't toggle every few ticks.
        double maxDist = cfg.maxDistance;
        double buffer = maxDist * 0.15;
        boolean wasVisible = (cached == null) || cached;

        boolean nowVisible;
        if (wasVisible) {
            // Already visible → only hide when clearly past the outer edge
            double outer = maxDist + buffer;
            nowVisible = squaredDistanceToCam <= outer * outer;
        } else {
            // Already hidden → only show again when clearly inside the inner edge
            double inner = maxDist - buffer;
            nowVisible = squaredDistanceToCam <= inner * inner;
        }

        VISIBILITY_CACHE.put(entity, nowVisible);
        LAST_CHECK_TICK.put(entity, currentTick);

        return nowVisible;
    }

    /**
     * Mark an entity as having server-forced nameplate visibility.
     * This is called when we detect the server is overriding our culling.
     */
    public void markServerForcedVisibility(Entity entity, boolean forced) {
        SERVER_FORCED_VISIBILITY.put(entity, forced);
        if (forced) {
            VISIBILITY_CACHE.put(entity, true);
        }
    }

    /**
     * Check if a specific entity has server-forced visibility.
     */
    public boolean isServerForced(Entity entity) {
        Boolean forced = SERVER_FORCED_VISIBILITY.get(entity);
        return forced != null && forced;
    }

    public void invalidate(Entity entity) {
        VISIBILITY_CACHE.remove(entity);
        LAST_CHECK_TICK.remove(entity);
    }
}
