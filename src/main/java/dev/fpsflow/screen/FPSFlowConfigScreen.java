package dev.fpsflow.screen;

import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.config.PerformanceProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
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

    private enum Tab {
        GENERAL("fpsflow.config.tab.general"),
        CULLING("fpsflow.config.tab.culling"),
        LOD("fpsflow.config.tab.lod_labels"),
        BACKGROUND_FPS("fpsflow.config.tab.background_fps");

        final String labelKey;
        Tab(String labelKey) { this.labelKey = labelKey; }
    }

    private final Screen parent;
    private FPSFlowConfig cfg;
    private Tab currentTab = Tab.GENERAL;

    public FPSFlowConfigScreen(Screen parent) {
        super(Text.translatable("fpsflow.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        cfg = ConfigManager.getInstance().getConfig();

        Tab[] tabs = Tab.values();
        int tabW = (width - 20) / tabs.length;
        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            Text label = Text.translatable(tab.labelKey);
            Text display = tab == currentTab
                    ? Text.literal("[ ").append(label).append(Text.literal(" ]"))
                    : label;
            int tx = 10 + i * tabW;
            addDrawableChild(ButtonWidget.builder(display, btn -> {
                currentTab = tab;
                clearAndInit();
            }).dimensions(tx, 8, tabW - 2, BTN_H).build());
        }

        int cx = width / 2;
        int y = 40;
        int lx = cx - BTN_W - 5;
        int rx = cx + 5;

        switch (currentTab) {
            case GENERAL        -> initGeneralTab(cx, lx, rx, y);
            case CULLING        -> initCullingTab(lx, rx, y);
            case LOD            -> initLODTab(lx, rx, y);
            case BACKGROUND_FPS -> initBackgroundFpsTab(cx, lx, rx, y);
        }

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), btn -> close())
                .dimensions(cx - 75, height - 30, 150, BTN_H).build());
    }

    // ── tab content ───────────────────────────────────────────────────────────

    private void initGeneralTab(int cx, int lx, int rx, int y) {
        addDrawableChild(ButtonWidget.builder(profileText(), btn -> {
            cycleProfile();
            btn.setMessage(profileText());
        }).dimensions(cx - 100, y, 200, BTN_H)
          .tooltip(Tooltip.of(Text.translatable("fpsflow.config.general.profile_cycle.tooltip")))
          .build());
        y += SPACING + 6;

        addDrawableChild(ButtonWidget.builder(Text.translatable("fpsflow.config.general.save_custom_profile"), btn -> {
            saveCurrentAsCustomProfile();
            btn.setMessage(Text.translatable("fpsflow.config.general.saved_as", cfg.selectedProfile));
        }).dimensions(cx - 100, y, 200, BTN_H)
          .tooltip(Tooltip.of(Text.translatable("fpsflow.config.general.save_custom_profile.tooltip")))
          .build());
        y += SPACING + 6;

        addDrawableChild(toggleBtn(lx, y,
                Text.translatable("fpsflow.config.general.update_checker"),
                Text.translatable("fpsflow.config.general.update_checker.tooltip"),
                () -> cfg.updateChecker.enabled,
                v -> cfg.updateChecker.enabled = v));
        addDrawableChild(toggleBtn(rx, y,
                Text.translatable("fpsflow.config.general.world_join_optimizer"),
                Text.translatable("fpsflow.config.general.world_join_optimizer.tooltip"),
                () -> cfg.worldJoinOptimizer.enabled,
                v -> cfg.worldJoinOptimizer.enabled = v));
        y += SPACING;

        addDrawableChild(toggleBtn(lx, y,
                Text.translatable("fpsflow.config.general.gui_optimizer"),
                Text.translatable("fpsflow.config.general.gui_optimizer.tooltip"),
                () -> cfg.guiOptimization.enabled,
                v -> cfg.guiOptimization.enabled = v));
        addDrawableChild(toggleBtn(rx, y,
                Text.translatable("fpsflow.config.general.particle_optimizer"),
                Text.translatable("fpsflow.config.general.particle_optimizer.tooltip"),
                () -> cfg.particleOptimization.enabled,
                v -> cfg.particleOptimization.enabled = v));
        y += SPACING;

        addDrawableChild(toggleBtn(lx, y,
                Text.translatable("fpsflow.config.general.singleplayer_boost"),
                Text.translatable("fpsflow.config.general.singleplayer_boost.tooltip"),
                () -> cfg.singleplayerOpt.enabled,
                v -> cfg.singleplayerOpt.enabled = v));
    }


    private void initCullingTab(int lx, int rx, int y) {
        addDrawableChild(toggleBtn(lx, y,
                Text.translatable("fpsflow.config.entity_culling"),
                Text.translatable("fpsflow.config.entity_culling.enabled.tooltip"),
                () -> cfg.entityCulling.enabled,
                v -> cfg.entityCulling.enabled = v));
        addDrawableChild(toggleBtn(rx, y,
                Text.translatable("fpsflow.config.block_entity_culling"),
                Text.translatable("fpsflow.config.block_entity_culling.enabled.tooltip"),
                () -> cfg.blockEntityCulling.enabled,
                v -> cfg.blockEntityCulling.enabled = v));
        y += SPACING;

        addDrawableChild(toggleBtn(lx, y,
                Text.translatable("fpsflow.config.entity_culling.occlusion"),
                Text.translatable("fpsflow.config.entity_culling.occlusion.tooltip"),
                () -> cfg.entityCulling.occlusionCulling,
                v -> cfg.entityCulling.occlusionCulling = v));
        addDrawableChild(toggleBtn(rx, y,
                Text.translatable("fpsflow.config.entity_culling.async_occlusion"),
                Text.translatable("fpsflow.config.entity_culling.async_occlusion.tooltip"),
                () -> cfg.entityCulling.asyncOcclusion,
                v -> cfg.entityCulling.asyncOcclusion = v));
        y += SPACING;

        addDrawableChild(toggleBtn(lx, y,
                Text.translatable("fpsflow.config.entity_culling.painting_backface_culling"),
                Text.translatable("fpsflow.config.entity_culling.painting_backface_culling.tooltip"),
                () -> cfg.entityCulling.paintingBackfaceCulling,
                v -> cfg.entityCulling.paintingBackfaceCulling = v));
    }

    private void initLODTab(int lx, int rx, int y) {
        addDrawableChild(toggleBtn(lx, y,
                Text.translatable("fpsflow.config.entity_lod"),
                Text.translatable("fpsflow.config.entity_lod.enabled.tooltip"),
                () -> cfg.entityLOD.enabled,
                v -> cfg.entityLOD.enabled = v));
        addDrawableChild(toggleBtn(rx, y,
                Text.translatable("fpsflow.config.nameplate_culling"),
                Text.translatable("fpsflow.config.nameplate_culling.enabled.tooltip"),
                () -> cfg.nameplateCulling.enabled,
                v -> cfg.nameplateCulling.enabled = v));
        y += SPACING;


        addDrawableChild(createSlider(lx, y,
                Text.translatable("fpsflow.config.entity_lod.medium_distance"),
                Text.translatable("fpsflow.config.entity_lod.medium_distance.tooltip"),
                () -> cfg.entityLOD.farLODDistance,
                v -> cfg.entityLOD.farLODDistance = v,
                16, 320));
        y += SPACING;

        addDrawableChild(createSlider(lx, y,
                Text.translatable("fpsflow.config.nameplate_culling.max_distance"),
                Text.translatable("fpsflow.config.nameplate_culling.max_distance.tooltip"),
                () -> cfg.nameplateCulling.maxDistance,
                v -> cfg.nameplateCulling.maxDistance = v,
                8, 128));
        addDrawableChild(toggleBtn(rx, y,
                Text.translatable("fpsflow.config.item_frame"),
                Text.translatable("fpsflow.config.item_frame.enabled.tooltip"),
                () -> cfg.itemFrame.enabled,
                v -> cfg.itemFrame.enabled = v));
    }

    private void initBackgroundFpsTab(int cx, int lx, int rx, int y) {
        addDrawableChild(toggleBtn(cx - BTN_W / 2, y,
                Text.translatable("fpsflow.config.background_fps"),
                Text.translatable("fpsflow.config.background_fps.enabled.tooltip"),
                () -> cfg.backgroundFps.enabled,
                v -> cfg.backgroundFps.enabled = v));
        y += SPACING + 6;


        addDrawableChild(createFpsCapSlider(lx, y,
                Text.translatable("fpsflow.config.background_fps.unfocused_cap"),
                Text.translatable("fpsflow.config.background_fps.unfocused_cap.tooltip"),
                () -> cfg.backgroundFps.unfocusedFpsCap,
                v -> cfg.backgroundFps.unfocusedFpsCap = v));
        addDrawableChild(createFpsCapSlider(rx, y,
                Text.translatable("fpsflow.config.background_fps.minimized_cap"),
                Text.translatable("fpsflow.config.background_fps.minimized_cap.tooltip"),
                () -> cfg.backgroundFps.minimizedFpsCap,
                v -> cfg.backgroundFps.minimizedFpsCap = v));
        y += SPACING;

        addDrawableChild(createFpsCapSlider(lx, y,
                Text.translatable("fpsflow.config.background_fps.menu_load_cap"),
                Text.translatable("fpsflow.config.background_fps.menu_load_cap.tooltip"),
                () -> cfg.backgroundFps.titleScreenFpsCap,
                v -> cfg.backgroundFps.titleScreenFpsCap = v));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private ButtonWidget toggleBtn(int x, int y, Text label, Text description,
                                   BoolSupplier getter, BoolConsumer setter) {
        return ButtonWidget.builder(toggleText(label, getter.get()), btn -> {
            boolean next = !getter.get();
            setter.accept(next);
            btn.setMessage(toggleText(label, next));
            ConfigManager.getInstance().save();
        }).dimensions(x, y, BTN_W, BTN_H)
          .tooltip(Tooltip.of(description))
          .build();
    }

    private static Text toggleText(Text label, boolean on) {
        // Use ON/OFF as plain literals (simple, fast, and already consistent previously).
        // This keeps behavior close to the previous UI.
        return Text.literal(label.copy().append(": ").append(Text.literal(on ? "ON" : "OFF")).getString());
    }


    private Text profileText() {
        String selected = cfg.selectedProfile != null ? cfg.selectedProfile : PerformanceProfile.BALANCED.name();
        return Text.translatable("fpsflow.config.general.profile", selected);
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

    private SliderWidget createSlider(int x, int y, Text label, Text description,
                                      IntSupplier getter, IntConsumer setter,
                                      int min, int max) {
        double initialValue = (double)(getter.getAsInt() - min) / (max - min);
        SliderWidget slider = new SliderWidget(x, y, BTN_W, BTN_H,
                Text.literal(label.getString() + ": " + getter.getAsInt()), initialValue) {
            @Override
            protected void updateMessage() {
                int value = min + (int) Math.round(this.value * (max - min));
                setMessage(Text.literal(label.getString() + ": " + value));
            }
            @Override
            protected void applyValue() {
                int value = min + (int) Math.round(this.value * (max - min));
                setter.accept(Math.max(min, Math.min(max, value)));
                ConfigManager.getInstance().save();
            }
        };
        slider.setTooltip(Tooltip.of(description));
        return slider;
    }


    private SliderWidget createFpsCapSlider(int x, int y, Text label, Text description,
                                            IntSupplier getter, IntConsumer setter) {
        final int MAX_FPS = 480;
        double initial = (double) Math.max(0, getter.getAsInt()) / MAX_FPS;
        SliderWidget slider = new SliderWidget(x, y, BTN_W, BTN_H,
                Text.literal(fpsCapLabel(label.getString(), getter.getAsInt())), initial) {
            @Override
            protected void updateMessage() {
                int v = (int) Math.round(this.value * MAX_FPS);
                setMessage(Text.literal(fpsCapLabel(label.getString(), v)));
            }
            @Override
            protected void applyValue() {
                int v = (int) Math.round(this.value * MAX_FPS);
                setter.accept(v);
                ConfigManager.getInstance().save();
            }
        };
        slider.setTooltip(Tooltip.of(description));
        return slider;
    }

    private static String fpsCapLabel(String label, int fps) {
        return label + ": " + (fps <= 0 ? "Unlimited" : fps);
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xC0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 32, 0xFFFFFF);
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
