package dev.fpsflow.rendering;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.optimization.OptimizationModule;
import net.minecraft.client.MinecraftClient;

/**
 * Monitors runtime FPS and dynamically adjusts optimization aggressiveness.
 * When FPS drops below thresholds, additional culling steps are activated
 * without requiring the player to manually change their profile.
 */
public final class AdaptiveRenderer implements OptimizationModule {

    private static final AdaptiveRenderer INSTANCE = new AdaptiveRenderer();

    private static final double LOW_FPS_THRESHOLD = 30.0;
    private static final double CRITICAL_FPS_THRESHOLD = 15.0;

    private AdaptiveRenderer() {}

    public static AdaptiveRenderer getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId() {
        return "adaptive-renderer";
    }

    @Override
    public void initialize() {
        FPSFlow.LOGGER.debug("[FPSFlow] AdaptiveRenderer ready");
    }

    @Override
    public void shutdown() {}

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().getConfig().renderCaching.enabled;
    }

    /**
     * Returns how aggressively FPSFlow should cull entities this frame.
     * 0 = normal, 1 = aggressive (FPS < 30), 2 = very aggressive (FPS < 15).
     */
    public int getCullingLevel() {
        if (!isEnabled()) return 0;
        double fps = SmartRenderScheduler.getInstance().getSmoothedFps();
        if (fps < CRITICAL_FPS_THRESHOLD) return 2;
        if (fps < LOW_FPS_THRESHOLD) return 1;
        return 0;
    }

    /**
     * True when the integrated server is running (singleplayer / LAN host).
     * In singleplayer the render thread shares CPU with the server thread, so
     * more aggressive culling frees headroom for chunk generation.
     */
    private boolean isSingleplayer() {
        return ConfigManager.getInstance().getConfig().singleplayerOpt.enabled
                && MinecraftClient.getInstance().isIntegratedServerRunning();
    }

    public double getEntityDistanceMultiplier() {
        return switch (getCullingLevel()) {
            case 2 -> isSingleplayer() ? 0.4 : 0.5;
            case 1 -> isSingleplayer() ? 0.65 : 0.8;
            default -> 1.0;
        };
    }

    public double getBlockEntityDistanceMultiplier() {
        return switch (getCullingLevel()) {
            case 2 -> isSingleplayer() ? 0.5 : 0.6;
            case 1 -> isSingleplayer() ? 0.7 : 0.85;
            default -> 1.0;
        };
    }

    public double getParticleDistanceMultiplier() {
        if (!isEnabled()) return 1.0;
        return switch (getCullingLevel()) {
            case 2 -> isSingleplayer() ? 0.4 : 0.5;
            case 1 -> isSingleplayer() ? 0.6 : 0.75;
            default -> 1.0;
        };
    }

    public double getLODDistanceMultiplier() {
        if (!isEnabled()) return 1.0;
        return switch (getCullingLevel()) {
            case 2 -> isSingleplayer() ? 0.4 : 0.5;
            case 1 -> isSingleplayer() ? 0.6 : 0.7;
            default -> 1.0;
        };
    }
}
