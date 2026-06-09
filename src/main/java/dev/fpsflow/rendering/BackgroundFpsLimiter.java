package dev.fpsflow.rendering;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.optimization.OptimizationModule;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/**
 * Reduces frame rate when the Minecraft window is unfocused, minimised, or on
 * a non-game screen (loading screen, title screen, server list, etc.).
 * Inspired by Dynamic FPS: adds a sleep after each rendered frame so the
 * render loop naturally idles instead of spinning at full speed for no one.
 *
 * The sleep happens on the render/main thread AFTER the current frame is
 * complete, so game ticks still catch up normally (MC defers missed ticks
 * via its built-in loop catch-up logic).
 */
public final class BackgroundFpsLimiter implements OptimizationModule {

    private static final BackgroundFpsLimiter INSTANCE = new BackgroundFpsLimiter();

    private long lastFrameNanos = 0L;

    private BackgroundFpsLimiter() {}

    public static BackgroundFpsLimiter getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId() {
        return "background-fps-limiter";
    }

    @Override
    public void initialize() {
        FPSFlow.LOGGER.debug("[FPSFlow] BackgroundFpsLimiter ready");
    }

    @Override
    public void shutdown() {
        lastFrameNanos = 0L;
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().getConfig().backgroundFps.enabled;
    }

    @Override
    public void onTick() {}

    /**
     * Called at the end of every rendered frame (from GameRendererMixin).
     * Applies an FPS cap based on the current window state:
     *   - minimised → minimizedFpsCap
     *   - unfocused  → unfocusedFpsCap
     *   - focused on a non-game screen (loading, title, menus) → titleScreenFpsCap
     *   - focused in-game → no cap
     */
    public void onFrameRendered() {
        if (!isEnabled()) return;

        FPSFlowConfig.BackgroundFpsConfig cfg = ConfigManager.getInstance().getConfig().backgroundFps;
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        boolean focused = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_FOCUSED) == 1;

        if (!focused) {
            boolean iconified = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == 1;
            applyFpsCap(iconified ? cfg.minimizedFpsCap : cfg.unfocusedFpsCap);
            return;
        }

        // When focused but not in a world, cap FPS to reduce GPU spinning during
        // loading and title screens. This frees thermal/power headroom so loading
        // threads finish faster without the GPU burning unnecessary cycles on
        // rendering a static menu at thousands of FPS.
        MinecraftClient client = MinecraftClient.getInstance();
        if (cfg.titleScreenFpsCap > 0 && client.player == null) {
            applyFpsCap(cfg.titleScreenFpsCap);
            return;
        }

        lastFrameNanos = System.nanoTime();
    }

    private void applyFpsCap(int targetFps) {
        if (targetFps <= 0) {
            lastFrameNanos = System.nanoTime();
            return;
        }
        long targetFrameNanos = 1_000_000_000L / targetFps;
        long now = System.nanoTime();
        long sleepNanos = targetFrameNanos - (now - lastFrameNanos);
        if (sleepNanos > 1_000_000L) {
            try {
                Thread.sleep(sleepNanos / 1_000_000L, (int)(sleepNanos % 1_000_000L));
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        lastFrameNanos = System.nanoTime();
    }
}
