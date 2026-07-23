package dev.fpsflow.mixin.entity;

import dev.fpsflow.compatibility.MinecraftVersionCompat;
import dev.fpsflow.entities.NameplateCullingManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityLabelMixin {

    @Inject(method = "hasLabel", at = @At("RETURN"), cancellable = true, require = 0)
    private void fpsflow$cullNameplate(Entity entity, double squaredDistance,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (!MinecraftVersionCompat.isSupportedRuntime()) return;
        if (!cir.getReturnValueZ()) return;
        if (!NameplateCullingManager.getInstance().shouldShowNameplate(entity, squaredDistance)) {
            cir.setReturnValue(false);
        }
    }
}
