package dev.fpsflow.mixin.entity;

import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityLabelMixin {

    @Inject(method = "hasLabel", at = @At("RETURN"), cancellable = true)
    private void fpsflow$cullNameplate(Entity entity, double squaredDistanceToCam,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        FPSFlowConfig.NameplateCullingConfig cfg =
                ConfigManager.getInstance().getConfig().nameplateCulling;
        if (!cfg.enabled) return;
        double maxDist = cfg.maxDistance;
        if (squaredDistanceToCam > maxDist * maxDist) {
            cir.setReturnValue(false);
        }
    }
}
