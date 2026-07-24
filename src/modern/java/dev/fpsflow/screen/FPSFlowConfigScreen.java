package dev.fpsflow.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FPSFlowConfigScreen extends Screen {
    private final Screen parent;

    public FPSFlowConfigScreen(Screen parent) {
        super(Component.literal("FPSFlow"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 - 75, height - 30, 150, 20)
                .build());
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
    }
}
