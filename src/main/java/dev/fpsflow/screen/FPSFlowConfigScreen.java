package dev.fpsflow.screen;

import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.config.PerformanceProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class FPSFlowConfigScreen extends Screen {

    private static final int BTN_W = 150;
    private static final int BTN_H = 20;
    private static final int SPACING = 26;

    private final Screen parent;
    private FPSFlowConfig cfg;

    public FPSFlowConfigScreen(Screen parent) {
        super(Text.literal("FPSFlow Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        cfg = ConfigManager.getInstance().getConfig();

        int cx = width / 2;
        int y = 40;

        // Profile cycling button (full width)
        addDrawableChild(ButtonWidget.builder(profileText(), btn -> {
            cycleProfile();
            btn.setMessage(profileText());
        }).dimensions(cx - 100, y, 200, BTN_H).build());

        y += SPACING + 6;
        addDrawableChild(ButtonWidget.builder(Text.literal("Save Custom Profile"), btn -> {
            saveCurrentAsCustomProfile();
            btn.setMessage(Text.literal("Saved as " + cfg.selectedProfile));
        }).dimensions(cx - 100, y, 200, BTN_H).build());

        y += SPACING + 6;

        int lx = cx - BTN_W - 5;
        int rx = cx + 5;

        // Row 1
        addDrawableChild(toggleBtn(lx, y, "Entity Culling",
                () -> cfg.entityCulling.enabled,
                v -> cfg.entityCulling.enabled = v));
        addDrawableChild(toggleBtn(rx, y, "Block Entity Culling",
                () -> cfg.blockEntityCulling.enabled,
                v -> cfg.blockEntityCulling.enabled = v));
        y += SPACING;

        // Row 2
        addDrawableChild(toggleBtn(lx, y, "Occlusion Culling",
                () -> cfg.entityCulling.occlusionCulling,
                v -> cfg.entityCulling.occlusionCulling = v));
        addDrawableChild(toggleBtn(rx, y, "Async Occlusion",
                () -> cfg.entityCulling.asyncOcclusion,
                v -> cfg.entityCulling.asyncOcclusion = v));
        y += SPACING;

        // Row 3
        addDrawableChild(toggleBtn(lx, y, "Particle Optimizer",
                () -> cfg.particleOptimization.enabled,
                v -> cfg.particleOptimization.enabled = v));
        addDrawableChild(toggleBtn(rx, y, "GUI Optimizer",
                () -> cfg.guiOptimization.enabled,
                v -> cfg.guiOptimization.enabled = v));
        y += SPACING;

        // Row 4
        addDrawableChild(toggleBtn(lx, y, "Update Checker",
                () -> cfg.updateChecker.enabled,
                v -> cfg.updateChecker.enabled = v));
        addDrawableChild(toggleBtn(rx, y, "Join Optimizer",
                () -> cfg.worldJoinOptimizer.enabled,
                v -> cfg.worldJoinOptimizer.enabled = v));
        y += SPACING;

        // Row 5
        addDrawableChild(toggleBtn(lx, y, "Entity LOD",
                () -> cfg.entityLOD.enabled,
                v -> cfg.entityLOD.enabled = v));
        y += SPACING;

        // Row 6
        addDrawableChild(createSlider(lx, y, "Medium LOD distance",
                () -> cfg.entityLOD.mediumLODDistance,
                v -> cfg.entityLOD.mediumLODDistance = v,
                8, 128));
        addDrawableChild(createSlider(rx, y, "Far LOD distance",
                () -> cfg.entityLOD.farLODDistance,
                v -> cfg.entityLOD.farLODDistance = v,
                16, 256));
        y += SPACING;

        // Row 7
        addDrawableChild(toggleBtn(lx, y, "Map Frame Throttle",
                () -> cfg.itemFrame.enabled,
                v -> cfg.itemFrame.enabled = v));

        // Done
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> close())
                .dimensions(cx - 75, height - 30, 150, BTN_H).build());
    }

    private ButtonWidget toggleBtn(int x, int y, String label,
                                   BoolSupplier getter, BoolConsumer setter) {
        return ButtonWidget.builder(toggleText(label, getter.get()), btn -> {
            boolean next = !getter.get();
            setter.accept(next);
            btn.setMessage(toggleText(label, next));
            ConfigManager.getInstance().save();
        }).dimensions(x, y, BTN_W, BTN_H).build();
    }

    private static Text toggleText(String label, boolean on) {
        return Text.literal(label + ": " + (on ? "ON" : "OFF"));
    }

    private Text profileText() {
        String selected = cfg.selectedProfile != null ? cfg.selectedProfile : PerformanceProfile.BALANCED.name();
        return Text.literal("Profile: " + selected);
    }

    private void cycleProfile() {
        List<String> profileNames = new ArrayList<>();
        for (PerformanceProfile profile : PerformanceProfile.values()) {
            profileNames.add(profile.name());
        }
        profileNames.addAll(cfg.customProfiles.keySet());

        if (profileNames.isEmpty()) {
            cfg.selectedProfile = PerformanceProfile.BALANCED.name();
            PerformanceProfile.BALANCED.apply(cfg);
            ConfigManager.getInstance().save();
            clearAndInit();
            return;
        }

        String current = cfg.selectedProfile != null ? cfg.selectedProfile : profileNames.get(0);
        int index = profileNames.indexOf(current);
        if (index < 0) index = 0;
        int next = (index + 1) % profileNames.size();
        String nextName = profileNames.get(next);
        cfg.selectedProfile = nextName;

        if (isBuiltInProfile(nextName)) {
            PerformanceProfile.valueOf(nextName).apply(cfg);
            cfg.profile = PerformanceProfile.valueOf(nextName);
        } else {
            FPSFlowConfig.CustomProfile custom = cfg.customProfiles.get(nextName);
            if (custom != null) {
                custom.applyTo(cfg);
            } else {
                PerformanceProfile.BALANCED.apply(cfg);
                cfg.profile = PerformanceProfile.BALANCED;
                cfg.selectedProfile = PerformanceProfile.BALANCED.name();
            }
        }

        ConfigManager.getInstance().save();
        clearAndInit();
    }

    private void saveCurrentAsCustomProfile() {
        String baseName = "Custom";
        int suffix = 1;
        String candidate = baseName + " " + suffix;
        while (cfg.customProfiles.containsKey(candidate)) {
            suffix++;
            candidate = baseName + " " + suffix;
        }
        cfg.customProfiles.put(candidate, new FPSFlowConfig.CustomProfile(cfg));
        cfg.selectedProfile = candidate;
        ConfigManager.getInstance().save();
    }

    private boolean isBuiltInProfile(String profileName) {
        try {
            PerformanceProfile.valueOf(profileName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private SliderWidget createSlider(int x, int y, String label,
                                        IntSupplier getter, IntConsumer setter,
                                        int min, int max) {
        double initialValue = (double)(getter.getAsInt() - min) / (max - min);
        SliderWidget slider = new SliderWidget(x, y, BTN_W, BTN_H,
                Text.literal(label + ": " + getter.getAsInt()), initialValue) {
            @Override
            protected void updateMessage() {
                int value = min + (int) Math.round(this.value * (max - min));
                setMessage(Text.literal(label + ": " + value));
            }

            @Override
            protected void applyValue() {
                int value = min + (int) Math.round(this.value * (max - min));
                if (value < min) value = min;
                if (value > max) value = max;
                setter.accept(value);
                ConfigManager.getInstance().save();
            }
        };
        return slider;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xC0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        ConfigManager.getInstance().save();
        assert client != null;
        client.setScreen(parent);
    }

    @FunctionalInterface
    private interface BoolSupplier {
        boolean get();
    }

    @FunctionalInterface
    private interface BoolConsumer {
        void accept(boolean value);
    }
}
