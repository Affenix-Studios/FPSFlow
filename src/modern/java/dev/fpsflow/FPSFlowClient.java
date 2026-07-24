package dev.fpsflow;

import net.fabricmc.api.ClientModInitializer;

public class FPSFlowClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FPSFlow.LOGGER.info("[FPSFlow] Client entrypoint ready");
    }
}
