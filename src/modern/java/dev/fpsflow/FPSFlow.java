package dev.fpsflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class FPSFlow implements ModInitializer {
    public static final String MOD_ID = "fpsflow";
    public static final String MOD_NAME = "FPSFlow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[FPSFlow] {} loaded for Minecraft 26.2", MOD_NAME);
    }
}
