package dev.fpsflow.mixin.entity;

import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.WeakHashMap;

@Mixin(EntityRenderer.class)
public abstract class EntityLabelMixin {

    // Both maps are render-thread-only; no synchronisation needed.
    private static final WeakHashMap<Entity, Boolean> VISIBILITY_CACHE = new WeakHashMap<>();
    private static final WeakHashMap<Entity, Long>    LAST_CHECK_TICK  = new WeakHashMap<>();

    @Inject(method = "hasLabel", at = @At("RETURN"), cancellable = true)
    private void fpsflow$cullNameplate(Entity entity, double squaredDistanceToCam,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;

        FPSFlowConfig.NameplateCullingConfig cfg =
                ConfigManager.getInstance().getConfig().nameplateCulling;
        if (!cfg.enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        long currentTick = (client.world != null) ? client.world.getTime() : 0L;
        int interval = Math.max(1, cfg.checkIntervalTicks);

        Long lastCheck = LAST_CHECK_TICK.get(entity);
        Boolean cached = VISIBILITY_CACHE.get(entity);

        // Within the interval: return the cached decision without recalculating.
        if (cached != null && lastCheck != null && (currentTick - lastCheck) < interval) {
            if (!cached) cir.setReturnValue(false);
            return;
        }

        // Hysteresis: widen the hide-threshold and narrow the show-threshold by 15 %.
        // This creates a dead-band around maxDistance so entities that hover at the
        // boundary don't toggle every few ticks.
        double maxDist   = cfg.maxDistance;
        double buffer    = maxDist * 0.15;
        boolean wasVisible = (cached == null) || cached;

        boolean nowVisible;
        if (wasVisible) {
            // Already visible → only hide when clearly past the outer edge.
            double outer = maxDist + buffer;
            nowVisible = squaredDistanceToCam <= outer * outer;
        } else {
            // Already hidden → only show again when clearly inside the inner edge.
            double inner = maxDist - buffer;
            nowVisible = squaredDistanceToCam <= inner * inner;
        }

        VISIBILITY_CACHE.put(entity, nowVisible);
        LAST_CHECK_TICK.put(entity, currentTick);

        if (!nowVisible) cir.setReturnValue(false);
    }
}
