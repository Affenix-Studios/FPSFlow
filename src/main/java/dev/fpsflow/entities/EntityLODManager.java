package dev.fpsflow.entities;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.optimization.OptimizationModule;
import dev.fpsflow.rendering.AdaptiveRenderer;

public final class EntityLODManager implements OptimizationModule {

    private static final EntityLODManager INSTANCE = new EntityLODManager();

    private int currentTick = 0;

    private EntityLODManager() {}

    public static EntityLODManager getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId() {
        return "entity-lod";
    }

    @Override
    public void initialize() {
        FPSFlow.LOGGER.debug("[FPSFlow] EntityLODManager ready");
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().getConfig().entityLOD.enabled;
    }

    @Override
    public void shutdown() {}

    @Override
    public void onTick() {
        currentTick++;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    /**
     * Returns true if this entity's render should be skipped this tick.
     * XOR of entity ID and tick counter distributes skips evenly across entities,
     * so not all distant entities freeze on the same tick.
     */
    public boolean shouldThrottleRender(int entityId, double distSq) {
        if (!isEnabled()) return false;
        FPSFlowConfig.EntityLODConfig lod = ConfigManager.getInstance().getConfig().entityLOD;
        // When FPS is low the LOD thresholds shrink so throttling kicks in earlier,
        // reducing render load without requiring a manual profile change.
        double lodMult = AdaptiveRenderer.getInstance().getLODDistanceMultiplier();
        double farDist = lod.farLODDistance * lodMult;
        if (distSq > farDist * farDist) {
            // Beyond the LOD threshold: skip every other render tick (50 % render rate).
            // At this distance entity movement is tiny on screen — the skipped frames
            // are imperceptible while the GPU saving is significant.
            return (entityId ^ currentTick) % 2 != 0;
        }
        return false;
    }
}
