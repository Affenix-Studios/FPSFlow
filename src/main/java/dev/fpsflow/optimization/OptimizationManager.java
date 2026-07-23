package dev.fpsflow.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import dev.fpsflow.FPSFlow;


public final class OptimizationManager {

    private static final OptimizationManager INSTANCE = new OptimizationManager();

    private final List<OptimizationModule> modules = new ArrayList<>();
    private boolean initialized = false;

    private OptimizationManager() {}

    public static OptimizationManager getInstance() {
        return INSTANCE;
    }

    private static final String[] MODULE_CLASSES = {
        "dev.fpsflow.join.WorldJoinOptimizer",
        "dev.fpsflow.entities.EntityCullingManager",
        "dev.fpsflow.entities.EntityLODManager",
        "dev.fpsflow.blockentity.BlockEntityCullingManager",
        "dev.fpsflow.particles.ParticleOptimizer",
        "dev.fpsflow.gui.GUIOptimizer",
        "dev.fpsflow.rendering.AdaptiveRenderer",
        "dev.fpsflow.rendering.BackgroundFpsLimiter"
    };

    public void initialize() {
        if (initialized) return;

        for (String className : MODULE_CLASSES) {
            registerSafely(className);
        }

        for (OptimizationModule module : modules) {
            try {
                module.initialize();
                FPSFlow.LOGGER.info("[FPSFlow] Module '{}' initialized", module.getId());
            } catch (LinkageError e) {
                FPSFlow.LOGGER.warn("[FPSFlow] Module '{}' could not initialize because a Minecraft class is missing or incompatible: {}", module.getId(), e.toString());
            } catch (Exception e) {
                FPSFlow.LOGGER.error("[FPSFlow] Module '{}' failed to initialize", module.getId(), e);
            }
        }

        initialized = true;
    }

    private void registerSafely(String className) {
        try {
            Class<?> clazz = Class.forName(className, true, getClass().getClassLoader());
            Object instance = clazz.getMethod("getInstance").invoke(null);
            if (instance instanceof OptimizationModule module) {
                register(module);
            } else {
                FPSFlow.LOGGER.warn("[FPSFlow] Skipping optimization module because {} does not implement OptimizationModule", className);
            }
        } catch (LinkageError | ReflectiveOperationException e) {
            FPSFlow.LOGGER.warn("[FPSFlow] Skipping optimization module {} because a Minecraft class is missing or incompatible: {}", className, e.toString());
        } catch (Exception e) {
            FPSFlow.LOGGER.error("[FPSFlow] Failed to create optimization module {}", className, e);
        }
    }

    public void tick() {
        for (OptimizationModule module : modules) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    FPSFlow.LOGGER.error("[FPSFlow] Module '{}' threw during tick", module.getId(), e);
                }
            }
        }
    }

    public void shutdown() {
        for (OptimizationModule module : modules) {
            try {
                module.shutdown();
            } catch (Exception e) {
                FPSFlow.LOGGER.error("[FPSFlow] Module '{}' failed to shutdown cleanly", module.getId(), e);
            }
        }
        modules.clear();
        initialized = false;
    }

    private void register(OptimizationModule module) {
        modules.add(module);
    }
}
