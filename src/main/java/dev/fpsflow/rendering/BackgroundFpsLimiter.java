package dev.fpsflow.rendering;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.optimization.OptimizationModule;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/**
 * Reduces frame rate when the Minecraft window is unfocused or minimised.
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
     * When the window is unfocused or minimised, sleeps long enough to keep
     * the effective frame rate at or below the configured cap.
     */
    public void onFrameRendered() {
        if (!isEnabled()) return;

        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        boolean focused = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_FOCUSED) == 1;
        if (focused) {
            lastFrameNanos = System.nanoTime();
            return;
        }

        boolean iconified = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_ICONIFIED) == 1;
        FPSFlowConfig.BackgroundFpsConfig cfg = ConfigManager.getInstance().getConfig().backgroundFps;
        int targetFps = iconified ? cfg.minimizedFpsCap : cfg.unfocusedFpsCap;
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
