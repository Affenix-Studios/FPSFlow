package dev.fpsflow.mixin.entity;

import dev.fpsflow.entities.NameplateCullingManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks EntityRenderer.hasLabel() to apply distance-based nameplate culling
 * while respecting server-forced visibility overrides.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityLabelMixin {

    @Inject(method = "hasLabel", at = @At("RETURN"), cancellable = true)
    private void fpsflow$cullNameplate(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        // If the original method says no label, keep it that way
        if (!cir.getReturnValueZ()) return;

        // Get camera position and compute squared distance
        net.minecraft.client.render.Camera camera = net.minecraft.client.MinecraftClient.getInstance().gameRenderer.getCamera();
        if (camera == null) return;

        net.minecraft.util.math.Vec3d camPos = camera.getCameraPos();
        double dx = entity.getX() - camPos.x;
        double dy = entity.getY() - camPos.y;
        double dz = entity.getZ() - camPos.z;
        double squaredDist = dx * dx + dy * dy + dz * dz;

        // Apply nameplate culling
        if (!NameplateCullingManager.getInstance().shouldShowNameplate(entity, squaredDist)) {
            cir.setReturnValue(false);
        }
    }
}
