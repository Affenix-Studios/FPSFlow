package dev.fpsflow.mixin.entity;

import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.entities.EntityCullingManager;
import dev.fpsflow.entities.EntityLODManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderManager.class)
public abstract class EntityRenderManagerMixin {

    @Inject(
        method = "shouldRender",
        at = @At("RETURN"),
        cancellable = true
    )
    private <E extends Entity> void fpsflow$cullEntity(
            E entity,
            Frustum frustum,
            double cameraX, double cameraY, double cameraZ,
            CallbackInfoReturnable<Boolean> cir) {

        if (!cir.getReturnValueZ()) return;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        if (camera == null) return;

        if (EntityCullingManager.getInstance().shouldCullEntity(entity, camera)) {
            cir.setReturnValue(false);
            return;
        }

        if (entity instanceof PaintingEntity painting
                && ConfigManager.getInstance().getConfig().entityCulling.paintingBackfaceCulling) {
            Direction facing = painting.getHorizontalFacing();
            double pdx = cameraX - painting.getX();
            double pdy = cameraY - painting.getY();
            double pdz = cameraZ - painting.getZ();
            if (pdx * facing.getOffsetX() + pdy * facing.getOffsetY() + pdz * facing.getOffsetZ() < 0.0) {
                cir.setReturnValue(false);
                return;
            }
        }

        if (entity instanceof PlayerEntity) return;

        double dx = entity.getX() - cameraX;
        double dy = entity.getY() - cameraY;
        double dz = entity.getZ() - cameraZ;
        if (EntityLODManager.getInstance().shouldThrottleRender(entity.getId(), dx * dx + dy * dy + dz * dz)) {
            cir.setReturnValue(false);
        }
    }
}
