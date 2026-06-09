package dev.fpsflow.entities;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.optimization.OptimizationModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.WeakHashMap;

public final class NameplateCullingManager implements OptimizationModule {

    private static final NameplateCullingManager INSTANCE = new NameplateCullingManager();

    // Render-thread-only; no synchronisation needed
    private static final WeakHashMap<Entity, Boolean> VISIBILITY_CACHE = new WeakHashMap<>();
    private static final WeakHashMap<Entity, Long>    LAST_CHECK_TICK  = new WeakHashMap<>();

    private NameplateCullingManager() {}

    public static NameplateCullingManager getInstance() {
        return INSTANCE;
    }

    @Override public String getId()       { return "nameplate-culling"; }
    @Override public void  onTick()       {}

    @Override
    public void initialize() {
        FPSFlow.LOGGER.debug("[FPSFlow] NameplateCullingManager ready");
    }

    @Override
    public void shutdown() {
        VISIBILITY_CACHE.clear();
        LAST_CHECK_TICK.clear();
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().getConfig().nameplateCulling.enabled;
    }

    /**
     * Returns true if the nameplate SHOULD be visible.
     *
     * @param squaredDistanceToCam  camera-to-entity squared distance (already computed
     *                              by MC's EntityRenderer.hasLabel caller — pass it
     *                              directly to avoid a second sqrt-less recalculation)
     */
    public boolean shouldShowNameplate(Entity entity, double squaredDistanceToCam) {
        if (!isEnabled()) return true;

        // Non-player entities whose nameplate is set always-visible by the server must
        // never be culled: culling them races against server metadata and causes flicker.
        if (!(entity instanceof PlayerEntity) && entity.isCustomNameVisible()) {
            return true;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        long currentTick = (client.world != null) ? client.world.getTime() : 0L;

        FPSFlowConfig.NameplateCullingConfig cfg =
                ConfigManager.getInstance().getConfig().nameplateCulling;

        int interval = Math.max(1, cfg.checkIntervalTicks);

        Long    lastCheck = LAST_CHECK_TICK.get(entity);
        Boolean cached    = VISIBILITY_CACHE.get(entity);

        if (cached != null && lastCheck != null && (currentTick - lastCheck) < interval) {
            return cached;
        }

        // 20 % hysteresis dead-band around maxDistance.
        // Entities hovering near the threshold can't toggle on every recalculation.
        double maxDist = cfg.maxDistance;
        double buffer  = maxDist * 0.20;
        boolean wasVisible = (cached == null) || cached;

        boolean nowVisible;
        if (wasVisible) {
            double outer = maxDist + buffer;
            nowVisible = squaredDistanceToCam <= outer * outer;
        } else {
            double inner = maxDist - buffer;
            nowVisible = squaredDistanceToCam <= inner * inner;
        }

        VISIBILITY_CACHE.put(entity, nowVisible);
        LAST_CHECK_TICK.put(entity, currentTick);

        return nowVisible;
    }

    public void invalidate(Entity entity) {
        VISIBILITY_CACHE.remove(entity);
        LAST_CHECK_TICK.remove(entity);
    }
}
