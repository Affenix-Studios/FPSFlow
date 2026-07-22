package dev.fpsflow.modmenu;

import dev.fpsflow.screen.FPSFlowConfigScreen;
import net.minecraft.client.gui.screen.Screen;

/**
 * ModMenu integration entry point.
 *
 * <p>ModMenu is an optional dependency. This class is registered as a ModMenu
 * entrypoint in {@code fabric.mod.json}. ModMenu discovers it via reflection
 * and calls {@code createConfigScreen(Screen)} when the player clicks the
 * "Config" button.</p>
 *
 * <p>This class intentionally does <strong>not</strong> import or reference
 * any ModMenu API classes at compile time. ModMenu's {@code ModMenuFactory}
 * interface is loaded reflectively at runtime, so the class compiles cleanly
 * without ModMenu on the classpath and still functions correctly when ModMenu
 * is installed.</p>
 */
public class FPSFlowModMenuIntegration {

    /**
     * Called by ModMenu via reflection. Returns the FPSFlow config screen.
     * The method signature matches ModMenu's {@code ModMenuFactory} interface
     * ({@code Screen createConfigScreen(Screen parent)}), but no compile-time
     * dependency on ModMenu is required.
     */
    public Screen createConfigScreen(Screen parent) {
        return new FPSFlowConfigScreen(parent);
    }
}
