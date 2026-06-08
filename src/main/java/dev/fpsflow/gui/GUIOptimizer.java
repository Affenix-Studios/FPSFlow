package dev.fpsflow.gui;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.optimization.OptimizationModule;

public final class GUIOptimizer implements OptimizationModule {

    private static final GUIOptimizer INSTANCE = new GUIOptimizer();

    private int tick = 0;
    private boolean forceDirtyUpdate = true; // true on startup so first tick always evaluates

    private GUIOptimizer() {}

    public static GUIOptimizer getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId() {
        return "gui-optimizer";
    }

    @Override
    public void initialize() {
        HUDCache.getInstance().reset();
        FPSFlow.LOGGER.debug("[FPSFlow] GUIOptimizer ready");
    }

    @Override
    public void shutdown() {
        HUDCache.getInstance().reset();
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().getConfig().guiOptimization.enabled;
    }

    @Override
    public void onTick() {
        tick++;
    }

    /**
     * Called when the HUD cache detects that a player stat actually changed.
     * Forces the next throttle check to return true so the change is reflected immediately.
     */
    public void reportDirty() {
        forceDirtyUpdate = true;
    }

    /**
     * Returns true if non-critical HUD elements should be re-evaluated this tick.
     * Forces an update when player stats have changed since the last render,
     * otherwise gates to every other tick when throttling is enabled.
     */
    public boolean isHUDUpdateTick() {
        if (!ConfigManager.getInstance().getConfig().guiOptimization.hudUpdateThrottling) {
            return true;
        }
        if (forceDirtyUpdate) {
            forceDirtyUpdate = false;
            return true;
        }
        return (tick & 1) == 0;
    }

    public HUDCache getCache() {
        return HUDCache.getInstance();
    }
}
