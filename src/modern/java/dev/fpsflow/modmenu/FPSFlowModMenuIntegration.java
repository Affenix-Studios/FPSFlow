package dev.fpsflow.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.fpsflow.screen.FPSFlowConfigScreen;
import net.minecraft.client.gui.screens.Screen;

public class FPSFlowModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<Screen> getModConfigScreenFactory() {
        return FPSFlowConfigScreen::new;
    }
}
