package dev.fpsflow.screen;

import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.config.PerformanceProfile;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class FPSFlowConfigScreen extends Screen {

    private static final int BTN_W = 150;
    private static final int BTN_H = 20;
    private static final int SPACING = 26;

    private enum Tab {
        GENERAL("fpsflow.config.tab.general"),
        CULLING("fpsflow.config.tab.culling"),
        LOD("fpsflow.config.tab.lod_labels"),
        BACKGROUND_FPS("fpsflow.config.tab.background_fps");

        final String labelKey;

        Tab(String labelKey) {
            this.labelKey = labelKey;
        }
    }

    private final Screen parent;
    private FPSFlowConfig cfg;
    private Tab currentTab = Tab.GENERAL;

    public FPSFlowConfigScreen(Screen parent) {
        super(Component.translatable("fpsflow.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        cfg = ConfigManager.getInstance().getConfig();

        Tab[] tabs = Tab.values();
        int tabW = (width - 20) / tabs.length;
        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            Component label = Component.translatable(tab.labelKey);
            Component display = tab == currentTab
                    ? Component.literal("[ ").append(label).append(Component.literal(" ]"))
                    : label;
            int tx = 10 + i * tabW;
            addRenderableWidget(Button.builder(display, btn -> {
                currentTab = tab;
                rebuild();
            }).bounds(tx, 8, tabW - 2, BTN_H).build());
        }

        int cx = width / 2;
        int y = 40;
        int lx = cx - BTN_W - 5;
        int rx = cx + 5;

        switch (currentTab) {
            case GENERAL -> initGeneralTab(cx, lx, rx, y);
            case CULLING -> initCullingTab(lx, rx, y);
            case LOD -> initLODTab(lx, rx, y);
            case BACKGROUND_FPS -> initBackgroundFpsTab(cx, lx, rx, y);
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> onClose())
                .bounds(cx - 75, height - 30, 150, BTN_H).build());
    }

    private void rebuild() {
        clearWidgets();
        init();
    }

    private void initGeneralTab(int cx, int lx, int rx, int y) {
        addRenderableWidget(Button.builder(profileText(), btn -> {
            cycleProfile();
            btn.setMessage(profileText());
        }).bounds(cx - 100, y, 200, BTN_H).build());
        y += SPACING + 6;

        addRenderableWidget(Button.builder(Component.translatable("fpsflow.config.general.save_custom_profile"), btn -> {
            saveCurrentAsCustomProfile();
            btn.setMessage(Component.translatable("fpsflow.config.general.saved_as", cfg.selectedProfile));
        }).bounds(cx - 100, y, 200, BTN_H).build());
        y += SPACING + 6;

        addRenderableWidget(toggleBtn(lx, y,
                Component.translatable("fpsflow.config.general.update_checker"),
                () -> cfg.updateChecker.enabled,
                v -> cfg.updateChecker.enabled = v));
        addRenderableWidget(toggleBtn(rx, y,
                Component.translatable("fpsflow.config.general.world_join_optimizer"),
                () -> cfg.worldJoinOptimizer.enabled,
                v -> cfg.worldJoinOptimizer.enabled = v));
        y += SPACING;

        addRenderableWidget(toggleBtn(lx, y,
                Component.translatable("fpsflow.config.general.gui_optimizer"),
                () -> cfg.guiOptimization.enabled,
                v -> cfg.guiOptimization.enabled = v));
        addRenderableWidget(toggleBtn(rx, y,
                Component.translatable("fpsflow.config.general.particle_optimizer"),
                () -> cfg.particleOptimization.enabled,
                v -> cfg.particleOptimization.enabled = v));
        y += SPACING;

        addRenderableWidget(toggleBtn(lx, y,
                Component.translatable("fpsflow.config.general.singleplayer_boost"),
                () -> cfg.singleplayerOpt.enabled,
                v -> cfg.singleplayerOpt.enabled = v));
    }

    private void initCullingTab(int lx, int rx, int y) {
        addRenderableWidget(toggleBtn(lx, y,
                Component.translatable("fpsflow.config.entity_culling"),
                () -> cfg.entityCulling.enabled,
                v -> cfg.entityCulling.enabled = v));
        addRenderableWidget(toggleBtn(rx, y,
                Component.translatable("fpsflow.config.block_entity_culling"),
                () -> cfg.blockEntityCulling.enabled,
                v -> cfg.blockEntityCulling.enabled = v));
        y += SPACING;

        addRenderableWidget(toggleBtn(lx, y,
                Component.translatable("fpsflow.config.entity_culling.occlusion"),
                () -> cfg.entityCulling.occlusionCulling,
                v -> cfg.entityCulling.occlusionCulling = v));
        addRenderableWidget(toggleBtn(rx, y,
                Component.translatable("fpsflow.config.entity_culling.async_occlusion"),
                () -> cfg.entityCulling.asyncOcclusion,
                v -> cfg.entityCulling.asyncOcclusion = v));
        y += SPACING;

        addRenderableWidget(toggleBtn(lx, y,
                Component.translatable("fpsflow.config.entity_culling.painting_backface_culling"),
                () -> cfg.entityCulling.paintingBackfaceCulling,
                v -> cfg.entityCulling.paintingBackfaceCulling = v));
    }

    private void initLODTab(int lx, int rx, int y) {
        addRenderableWidget(toggleBtn(lx, y,
                Component.translatable("fpsflow.config.entity_lod"),
                () -> cfg.entityLOD.enabled,
                v -> cfg.entityLOD.enabled = v));
        addRenderableWidget(toggleBtn(rx, y,
                Component.translatable("fpsflow.config.nameplate_culling"),
                () -> cfg.nameplateCulling.enabled,
                v -> cfg.nameplateCulling.enabled = v));
        y += SPACING;

        addRenderableWidget(createSlider(lx, y,
                Component.translatable("fpsflow.config.entity_lod.medium_distance"),
                () -> cfg.entityLOD.farLODDistance,
                v -> cfg.entityLOD.farLODDistance = v,
                16, 320));
        y += SPACING;

        addRenderableWidget(createSlider(lx, y,
                Component.translatable("fpsflow.config.nameplate_culling.max_distance"),
                () -> cfg.nameplateCulling.maxDistance,
                v -> cfg.nameplateCulling.maxDistance = v,
                8, 128));
        addRenderableWidget(toggleBtn(rx, y,
                Component.translatable("fpsflow.config.item_frame"),
                () -> cfg.itemFrame.enabled,
                v -> cfg.itemFrame.enabled = v));
    }

    private void initBackgroundFpsTab(int cx, int lx, int rx, int y) {
        addRenderableWidget(toggleBtn(cx - BTN_W / 2, y,
                Component.translatable("fpsflow.config.background_fps"),
                () -> cfg.backgroundFps.enabled,
                v -> cfg.backgroundFps.enabled = v));
        y += SPACING + 6;

        addRenderableWidget(createFpsCapSlider(lx, y,
                Component.translatable("fpsflow.config.background_fps.unfocused_cap"),
                () -> cfg.backgroundFps.unfocusedFpsCap,
                v -> cfg.backgroundFps.unfocusedFpsCap = v));
        addRenderableWidget(createFpsCapSlider(rx, y,
                Component.translatable("fpsflow.config.background_fps.minimized_cap"),
                () -> cfg.backgroundFps.minimizedFpsCap,
                v -> cfg.backgroundFps.minimizedFpsCap = v));
        y += SPACING;

        addRenderableWidget(createFpsCapSlider(lx, y,
                Component.translatable("fpsflow.config.background_fps.menu_load_cap"),
                () -> cfg.backgroundFps.titleScreenFpsCap,
                v -> cfg.backgroundFps.titleScreenFpsCap = v));
    }

    private Button toggleBtn(int x, int y, Component label, BooleanSupplier getter, Consumer<Boolean> setter) {
        return Button.builder(toggleText(label, getter.getAsBoolean()), btn -> {
            boolean next = !getter.getAsBoolean();
            setter.accept(next);
            btn.setMessage(toggleText(label, next));
            ConfigManager.getInstance().save();
        }).bounds(x, y, BTN_W, BTN_H).build();
    }

    private static Component toggleText(Component label, boolean on) {
        return Component.literal(label.getString() + ": " + (on ? "ON" : "OFF"));
    }

    private Component profileText() {
        String selected = cfg.selectedProfile != null ? cfg.selectedProfile : PerformanceProfile.BALANCED.name();
        return Component.translatable("fpsflow.config.general.profile", selected);
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
            rebuild();
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
        rebuild();
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

    private AbstractSliderButton createSlider(int x, int y, Component label,
                                              IntSupplier getter, IntConsumer setter,
                                              int min, int max) {
        double initialValue = (double) (getter.getAsInt() - min) / (max - min);
        return new AbstractSliderButton(x, y, BTN_W, BTN_H,
                Component.literal(label.getString() + ": " + getter.getAsInt()), initialValue) {
            @Override
            protected void updateMessage() {
                int value = min + (int) Math.round(this.value * (max - min));
                setMessage(Component.literal(label.getString() + ": " + value));
            }

            @Override
            protected void applyValue() {
                int value = min + (int) Math.round(this.value * (max - min));
                setter.accept(Math.max(min, Math.min(max, value)));
                ConfigManager.getInstance().save();
            }
        };
    }

    private AbstractSliderButton createFpsCapSlider(int x, int y, Component label,
                                                    IntSupplier getter, IntConsumer setter) {
        final int MAX_FPS = 480;
        double initial = (double) Math.max(0, getter.getAsInt()) / MAX_FPS;
        return new AbstractSliderButton(x, y, BTN_W, BTN_H,
                Component.literal(fpsCapLabel(label.getString(), getter.getAsInt())), initial) {
            @Override
            protected void updateMessage() {
                int v = (int) Math.round(this.value * MAX_FPS);
                setMessage(Component.literal(fpsCapLabel(label.getString(), v)));
            }

            @Override
            protected void applyValue() {
                int v = (int) Math.round(this.value * MAX_FPS);
                setter.accept(v);
                ConfigManager.getInstance().save();
            }
        };
    }

    private static String fpsCapLabel(String label, int fps) {
        return label + ": " + (fps <= 0 ? "Unlimited" : fps);
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
    }
}
