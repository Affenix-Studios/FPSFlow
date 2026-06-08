package dev.fpsflow.mixin.entity;

import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.entities.EntityLODManager;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntityRenderer.class)
public abstract class ItemFrameEntityRendererMixin {

    @Inject(method = "updateRenderState", at = @At("HEAD"), cancellable = true)
    private void fpsflow$throttleMapFrameUpdate(
            ItemFrameEntity entity, ItemFrameEntityRenderState state, float tickDelta,
            CallbackInfo ci) {
        FPSFlowConfig.ItemFrameConfig cfg = ConfigManager.getInstance().getConfig().itemFrame;
        if (!cfg.enabled) return;
        // mapId is null until the first update runs — let that initial update through
        if (state.mapId == null) return;
        int interval = Math.max(2, cfg.mapUpdateIntervalTicks);
        if ((entity.getId() ^ EntityLODManager.getInstance().getCurrentTick()) % interval != 0) {
            ci.cancel();
        }
    }
}
