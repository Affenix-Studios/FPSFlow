package dev.fpsflow.mixin.render;

import dev.fpsflow.rendering.BackgroundFpsLimiter;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void fpsflow$onFrameEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        BackgroundFpsLimiter.getInstance().onFrameRendered();
    }
}
