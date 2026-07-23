package dev.fpsflow;

import dev.fpsflow.rendering.ResourcePackReloadTracker;
import dev.fpsflow.rendering.SmartRenderScheduler;
import dev.fpsflow.updates.UpdateChecker;
import dev.fpsflow.util.CompactSineTable;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

@Environment(EnvType.CLIENT)
public class FPSFlowClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        try {
            ResourcePackReloadTracker.register();
        } catch (Throwable t) {
            FPSFlow.LOGGER.warn("[FPSFlow] ResourcePackReloadTracker could not register", t);
        }

        try {
            UpdateChecker.getInstance().checkAsync();
        } catch (Throwable t) {
            FPSFlow.LOGGER.warn("[FPSFlow] UpdateChecker could not start", t);
        }

        try {
            ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                    UpdateChecker.getInstance().showPendingIfAny(client));
        } catch (Throwable t) {
            FPSFlow.LOGGER.warn("[FPSFlow] Join listener could not register", t);
        }

        // Initialize Lithium-inspired optimization utilities
        try {
            CompactSineTable.init();
            FPSFlow.LOGGER.info("[FPSFlow] Compact sine table initialized (64 KB LUT)");
        } catch (Throwable t) {
            FPSFlow.LOGGER.warn("[FPSFlow] Compact sine table could not initialize", t);
        }

        FPSFlow.LOGGER.info("[FPSFlow] Client subsystems ready");
    }
}
