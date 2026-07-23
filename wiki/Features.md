# Features

## Entity Culling

Entity Culling skips rendering entities that are not visible to the player, reducing GPU load significantly in entity-dense areas.

### How It Works

1. **Frustum Culling**: Entities outside the camera view are never sent to the GPU
2. **Occlusion Culling**: A raycast checks if solid blocks hide the entity
3. **Distance Culling**: Entities beyond a configurable distance are not rendered
4. **Async Occlusion**: Raycasts are spread across multiple ticks to prevent frame spikes

### Configuration

See [Configuration](Configuration.md#entity-culling) for full options.

### Performance Impact

- **Low**: Frustum culling alone saves 10-30% entity render time
- **Medium**: Adding occlusion culling saves 20-50% in complex scenes
- **High**: Distance culling + async occlusion provides the best experience

---

## Entity LOD (Level of Detail)

Distant entities are render-throttled to reduce GPU load without removing them entirely.

### How It Works

- Entities within the near distance render every tick
- Entities beyond the far distance render every 2nd or 3rd tick
- XOR-based distribution ensures throttling is staggered across entities

### Configuration

See [Configuration](Configuration.md#entity-lod) for full options.

### Performance Impact

- Reduces entity render time by 30-50% for distant entities
- No visual impact at typical viewing distances

---

## Nameplate Culling

Hides entity name tags beyond a configurable distance with hysteresis to prevent flickering.

### How It Works

- Name tags beyond `maxDistance` are hidden
- A hysteresis dead-band prevents rapid toggling at the threshold
- Visibility is re-evaluated every `checkIntervalTicks` to reduce overhead

### Configuration

See [Configuration](Configuration.md#nameplate-culling) for full options.

---

## Block Entity Culling

Skips rendering distant block entities such as chests, furnaces, signs, banners, item frames, and armor stands.

### How It Works

- Distance-based culling only (no occlusion raycast)
- Configurable maximum distance per profile

### Configuration

See [Configuration](Configuration.md#block-entity-culling) for full options.

---

## Particle Optimization

Controls particle spawning and lifetime to reduce CPU and GPU overhead.

### How It Works

- Hard cap on total live particles
- Distance-based density LOD:
  - Near (< `midDistance`): 100% spawn rate
  - Mid (`midDistance` to `maxDistance`): ~50% spawn rate via position-based hash
  - Far (> `maxDistance`): 0% spawn rate
- Adaptive distance multiplier based on current FPS

### Configuration

See [Configuration](Configuration.md#particle-optimization) for full options.

---

## Background FPS Limiter

Caps frame rate when the Minecraft window is unfocused or minimized to reduce GPU thermals and power usage.

### How It Works

- Detects window focus state via Fabric API
- Applies a configurable FPS cap using post-frame sleep
- Separate caps for unfocused and minimized states

### Configuration

See [Configuration](Configuration.md#background-fps) for full options.

---

## World Join Optimizer

Temporarily tightens all culling distances after joining a world to prevent frame spikes during chunk loading.

### How It Works

- Detects world join via Fabric client events
- Starts culling distances at 35% of normal values
- Eases linearly back to 100% over the grace period
- Async occlusion batch increases 3x during grace period

### Configuration

See [Configuration](Configuration.md#world-join-optimizer) for full options.

---

## GUI Optimizer

Reduces HUD rendering overhead through dirty-flag tracking and throttling.

### How It Works

- **Hotbar Caching**: Tracks item slot changes and only re-renders changed slots
- **HUD Throttling**: Limits health/food/armor/XP/air updates to reduce draw calls

### Configuration

See [Configuration](Configuration.md#gui-optimizer) for full options.

---

## Adaptive Renderer

Automatically adjusts culling distances based on current FPS to maintain smooth gameplay.

### How It Works

- Monitors smoothed FPS using exponential moving average
- Reduces culling distances when FPS drops below thresholds
- Restores distances after a configurable delay
- Singleplayer mode uses more aggressive multipliers

### Configuration

See [Configuration](Configuration.md) for adaptive settings.

---

## Lithium-Inspired Utilities

FPSFlow 1.8.1 includes reimplemented optimization primitives inspired by CaffeineMC's Lithium mod. These are not mixins into Minecraft code; they are internal utilities that reduce allocation overhead and improve cache efficiency.

### Compact Sine Table

- Reduces sine lookup table from 256 KB to 64 KB
- Uses trigonometric identities to eliminate redundant entries
- Maintains bit-for-bit compatibility with Minecraft's MathHelper

### Cached Direction Constants

- Prevents repeated defensive array copies from `Direction.values()`
- Every call to `Enum.values()` allocates a new array
- Cached references eliminate this overhead in hot code paths

### Fast BlockPos Utilities

- Thread-local mutable BlockPos for iteration contexts
- Efficient long-packing for hash-based collections
- Reduces GC pressure in chunk scanning and neighbor-iteration loops

### Collection Helpers

- Pre-sized HashMap/HashSet factories with optimal initial capacities
- Identity-based sets for entity classification
- Utility methods that avoid common allocation pitfalls

---

## Feature Comparison

| Feature | FPSFlow | EntityCulling | Sodium | Lithium |
|---------|---------|---------------|--------|---------|
| Entity Culling | Yes | Yes | No | No |
| Entity LOD | Yes | No | No | No |
| Nameplate Culling | Yes | No | No | No |
| Block Entity Culling | Yes | No | No | No |
| Particle Optimization | Yes | No | No | Yes |
| Background FPS Limiter | Yes | No | No | No |
| World Join Optimizer | Yes | No | No | No |
| GUI Optimizer | Yes | No | No | No |
| Adaptive Renderer | Yes | No | No | No |
| Compact Sine Table | Yes | No | No | Yes |
| Direction Caching | Yes | No | No | Yes |

---

## License

FPSFlow is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.

- You are free to use, modify, and distribute FPSFlow
- If you run a modified version on a server, you must provide the source code to users
- See [LICENSE](https://github.com/Affenix-Studios/FPSFlow/blob/main/LICENSE) for full terms
