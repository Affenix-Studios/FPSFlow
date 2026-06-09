package dev.fpsflow.entities;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.compatibility.CompatibilityChecker;
import dev.fpsflow.join.WorldJoinOptimizer;
import dev.fpsflow.optimization.OptimizationModule;
import dev.fpsflow.rendering.AdaptiveRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class EntityCullingManager implements OptimizationModule {

    private static final EntityCullingManager INSTANCE = new EntityCullingManager();
    private static final int MAX_ASYNC_RAYCASTS_PER_TICK = 8;

    private final Map<Integer, CullingEntry> cache = new ConcurrentHashMap<>();
    private final Queue<PendingCheck> pendingChecks = new ConcurrentLinkedQueue<>();
    private final Set<Integer> pendingIds = ConcurrentHashMap.newKeySet();
    private final Map<EntityType<?>, Optional<Boolean>> typeOverrideCache = new ConcurrentHashMap<>();
    private int currentTick = 0;

    private EntityCullingManager() {}

    public static EntityCullingManager getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId() {
        return "entity-culling";
    }

    @Override
    public void initialize() {
        FPSFlow.LOGGER.debug("[FPSFlow] EntityCullingManager ready");
    }

    @Override
    public void shutdown() {
        cache.clear();
        pendingChecks.clear();
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().getConfig().entityCulling.enabled
                && !CompatibilityChecker.getInstance().isEntityCullingModPresent();
    }

    @Override
    public void onTick() {
        currentTick++;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null && mc.player != null) {
            int batchLimit = MAX_ASYNC_RAYCASTS_PER_TICK * WorldJoinOptimizer.getInstance().getAsyncBatchMultiplier();
            int processed = 0;
            while (processed < batchLimit) {
                PendingCheck pending = pendingChecks.poll();
                if (pending == null) break;
                pendingIds.remove(pending.entityId());
                boolean occluded = rayCast(pending.camPos(), pending.samplePoint(), mc);
                cache.put(pending.entityId(), new CullingEntry(occluded, currentTick));
                processed++;
            }
        }

        if (currentTick % 100 == 0) {
            int threshold = currentTick - 200;
            Iterator<Map.Entry<Integer, CullingEntry>> it = cache.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue().lastCheckedTick() < threshold) {
                    it.remove();
                }
            }
        }
    }

    public boolean shouldCullEntity(Entity entity, Camera camera) {
        if (!isEnabled()) return false;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return false;

        if (entity == mc.player) return false;
        if (mc.player.getVehicle() == entity) return false;
        if (entity instanceof PlayerEntity) return false;

        // Entities whose nameplate is forced always-visible by the server are cosmetically
        // important server objects (NPCs, display minecarts, cosmetic entities).
        // Culling them races against server-side metadata updates and causes persistent flicker,
        // exactly as it did for nameplate visibility before 1.6.0.
        if (entity.isCustomNameVisible()) return false;

        FPSFlowConfig.EntityCullingConfig cfg = ConfigManager.getInstance().getConfig().entityCulling;

        Boolean typeOverride = getTypeOverride(entity.getType(), cfg);
        if (typeOverride != null && !typeOverride) return false;

        Vec3d camPos = camera.getCameraPos();
        double distSq = entity.squaredDistanceTo(camPos.x, camPos.y, camPos.z);
        double maxDist = cfg.maxDistance * WorldJoinOptimizer.getInstance().getDistanceFraction();
        maxDist *= AdaptiveRenderer.getInstance().getEntityDistanceMultiplier();
        if (distSq > maxDist * maxDist) return true;
        if (distSq < 16.0) return false;  // within 4 blocks → never cull

        if (!cfg.occlusionCulling) return false;

        int id = entity.getId();
        CullingEntry entry = cache.get(id);
        boolean cacheValid = entry != null && (currentTick - entry.lastCheckedTick()) < cfg.cacheUpdateIntervalTicks;

        if (cacheValid) return entry.occluded();

        // Use the entity's eye position rather than the AABB centre.
        // Eye pos is naturally higher than centre (avoids floor-level false positives
        // for small entities) while still staying safely below the top of the AABB,
        // so it does not poke above walls and cause false negatives.
        Vec3d samplePoint = entity.getEyePos();

        if (cfg.asyncOcclusion) {
            if (pendingIds.size() < 256 && pendingIds.add(id)) {
                pendingChecks.offer(new PendingCheck(id, camPos, samplePoint));
            }
            return entry != null && entry.occluded();
        }

        boolean occluded = rayCast(camPos, samplePoint, mc);
        cache.put(id, new CullingEntry(occluded, currentTick));
        return occluded;
    }

    private Boolean getTypeOverride(EntityType<?> type, FPSFlowConfig.EntityCullingConfig cfg) {
        return typeOverrideCache.computeIfAbsent(type, key -> {
            Identifier id = Registries.ENTITY_TYPE.getId(key);
            return id == null ? Optional.empty() : Optional.ofNullable(cfg.entityTypeOverrides.get(id.toString()));
        }).orElse(null);
    }

    private boolean rayCast(Vec3d from, Vec3d to, MinecraftClient mc) {
        RaycastContext ctx = new RaycastContext(
                from, to,
                RaycastContext.ShapeType.VISUAL,
                RaycastContext.FluidHandling.NONE,
                mc.player
        );
        BlockHitResult result = mc.world.raycast(ctx);
        return result.getType() != HitResult.Type.MISS;
    }

    public void invalidate(int entityId) {
        cache.remove(entityId);
    }

    private record CullingEntry(boolean occluded, int lastCheckedTick) {}
    private record PendingCheck(int entityId, Vec3d camPos, Vec3d samplePoint) {}
}
