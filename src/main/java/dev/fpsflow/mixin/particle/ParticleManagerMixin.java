package dev.fpsflow.mixin.particle;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.particles.ParticleOptimizer;
import dev.fpsflow.rendering.AdaptiveRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    private static volatile long fpsflow$lastParticleErrorMs = 0L;

    @Inject(
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
        at = @At("HEAD"),
        cancellable = true
    )
    private <T extends ParticleEffect> void fpsflow$limitParticles(
            T parameters,
            double x, double y, double z,
            double vx, double vy, double vz,
            CallbackInfoReturnable<Particle> cir) {

        ParticleOptimizer optimizer = ParticleOptimizer.getInstance();
        if (!optimizer.isEnabled()) return;

        double distMult = AdaptiveRenderer.getInstance().getParticleDistanceMultiplier();

        if (optimizer.shouldBlockParticleWithMultiplier(x, y, z, distMult)) {
            cir.setReturnValue(null);
            return;
        }

        optimizer.incrementCount();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void fpsflow$onTickStart(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        ParticleOptimizer.getInstance().resetTickCount();
    }

    // particles field in ParticleManager is declared as Map<> (interface), so the bytecode
    // emits invokeinterface Map.forEach – not invokevirtual IdentityHashMap.forEach.
    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"
        )
    )
    private <K, V> void fpsflow$safeParticleForEach(Map<K, V> map, BiConsumer<K, V> consumer) {
        map.forEach((k, v) -> {
            try {
                consumer.accept(k, v);
            } catch (NullPointerException e) {
                long now = System.currentTimeMillis();
                if (now - fpsflow$lastParticleErrorMs > 5000L) {
                    fpsflow$lastParticleErrorMs = now;
                    FPSFlow.LOGGER.warn("[FPSFlow] Particle tick NPE caught (stale entity reference) – crash prevented: {}", e.getMessage());
                }
            }
        });
    }
}
