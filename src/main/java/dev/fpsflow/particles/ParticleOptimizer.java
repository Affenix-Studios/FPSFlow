package dev.fpsflow.particles;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.join.WorldJoinOptimizer;
import dev.fpsflow.optimization.OptimizationModule;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.atomic.AtomicInteger;

public final class ParticleOptimizer implements OptimizationModule {

    private static final ParticleOptimizer INSTANCE = new ParticleOptimizer();

    private final AtomicInteger activeParticleCount = new AtomicInteger(0);

    private ParticleOptimizer() {}

    public static ParticleOptimizer getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId() {
        return "particle-optimizer";
    }

    @Override
    public void initialize() {
        FPSFlow.LOGGER.debug("[FPSFlow] ParticleOptimizer ready");
    }

    @Override
    public void shutdown() {
        activeParticleCount.set(0);
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().getConfig().particleOptimization.enabled;
    }

    /**
     * Called from the ParticleManagerMixin before a new particle is added.
     * Returns true if the particle should be blocked.
     *
     * @param x spawn X
     * @param y spawn Y
     * @param z spawn Z
     */
    public boolean shouldBlockParticle(double x, double y, double z) {
        if (!isEnabled()) return false;

        FPSFlowConfig.ParticleConfig cfg = ConfigManager.getInstance().getConfig().particleOptimization;

        // Check total count cap
        if (activeParticleCount.get() >= cfg.maxParticles) {
            return true;
        }

        // Check spawn distance
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;

        double distSq = mc.player.squaredDistanceTo(x, y, z);
        double maxDist = cfg.maxDistance;
        return distSq > maxDist * maxDist;
    }

    /**
     * Like {@link #shouldBlockParticle} but applies an additional distance multiplier.
     * Uses three density zones:
     *   near  (< midDistance)  → 100 % of particles allowed
     *   mid   (midDistance … maxDistance) → ~50 % allowed (position-hash thinning)
     *   far   (> maxDistance)  → 0 % (hard block)
     */
    public boolean shouldBlockParticleWithMultiplier(double x, double y, double z, double distMultiplier) {
        if (!isEnabled()) return false;

        FPSFlowConfig.ParticleConfig cfg = ConfigManager.getInstance().getConfig().particleOptimization;
        float joinFraction = WorldJoinOptimizer.getInstance().getDistanceFraction();

        int effectiveCap = Math.max(64, (int)(cfg.maxParticles * joinFraction));
        if (activeParticleCount.get() >= effectiveCap) {
            return true;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;

        double distSq = mc.player.squaredDistanceTo(x, y, z);
        double scale = distMultiplier * joinFraction;
        double maxDist = cfg.maxDistance * scale;
        if (distSq > maxDist * maxDist) return true;

        // Mid zone: thin to ~50 % using a stable position-based hash so the
        // reduction looks spatially uniform rather than flickering per tick.
        double midDist = Math.min(cfg.midDistance * scale, maxDist * 0.95);
        if (distSq > midDist * midDist) {
            int xi = (int) Math.floor(x);
            int zi = (int) Math.floor(z);
            int hash = xi * 374761393 ^ zi * 668265263;
            return (hash & 1) == 0;
        }

        return false;
    }

    /** Called from the mixin each time a particle is about to be added (and not blocked). */
    public void incrementCount() {
        activeParticleCount.incrementAndGet();
    }

    /** Called at the start of each ParticleManager tick to reset the per-tick counter. */
    public void resetTickCount() {
        activeParticleCount.set(0);
    }

    public void setActiveParticleCount(int count) {
        activeParticleCount.set(count);
    }

    public int getActiveParticleCount() {
        return activeParticleCount.get();
    }
}
