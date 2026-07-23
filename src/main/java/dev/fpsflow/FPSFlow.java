package dev.fpsflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fpsflow.compatibility.CompatibilityChecker;
import dev.fpsflow.compatibility.MinecraftVersionCompat;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.optimization.OptimizationManager;
import net.fabricmc.api.ModInitializer;

public class FPSFlow implements ModInitializer {

    public static final String MOD_ID = "fpsflow";
    public static final String MOD_NAME = "FPSFlow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[FPSFlow] Initializing {}", MOD_NAME);

        try {
            ConfigManager.getInstance().load();
        } catch (Throwable t) {
            LOGGER.warn("[FPSFlow] Config loading failed", t);
        }

        try {
            CompatibilityChecker.getInstance().check();
        } catch (Throwable t) {
            LOGGER.warn("[FPSFlow] Compatibility check failed", t);
        }

        LOGGER.info("[FPSFlow] Minecraft runtime: {} (supported: {})",
                MinecraftVersionCompat.getGameVersion(), MinecraftVersionCompat.isSupportedRuntime());

        try {
            OptimizationManager.getInstance().initialize();
        } catch (Throwable t) {
            LOGGER.warn("[FPSFlow] Optimization initialization failed", t);
        }

        // Update checker is initialised in FPSFlowClient (client-only)
        LOGGER.info("[FPSFlow] {} initialized – profile: {}",
                MOD_NAME, ConfigManager.getInstance().getConfig().selectedProfile);
    }
}
