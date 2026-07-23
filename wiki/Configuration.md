# Configuration

FPSFlow creates `config/fpsflow.json` on first launch. You can edit it manually or use the ModMenu config screen.

## Profiles

FPSFlow includes four built-in profiles and supports custom profiles:

- **Quality**: maximum visual fidelity, minimal culling
- **Balanced**: default; good mix of performance and visuals
- **Performance**: aggressive culling for smoother gameplay
- **Ultra Performance**: maximum FPS at the cost of visual range
- **Custom**: save your own preset via ModMenu

## Configuration Reference

### Entity Culling

```json
"entityCulling": {
  "enabled": true,
  "occlusionCulling": true,
  "asyncOcclusion": true,
  "maxDistance": 64,
  "cacheUpdateIntervalTicks": 10,
  "paintingBackfaceCulling": true,
  "entityTypeOverrides": {}
}
```

| Field | Description | Default |
|-------|-------------|---------|
| `enabled` | Master toggle for entity culling | `true` |
| `occlusionCulling` | Enable raycast-based occlusion culling | `true` |
| `asyncOcclusion` | Spread raycasts across multiple ticks | `true` |
| `maxDistance` | Maximum distance to render entities (blocks) | `64` |
| `cacheUpdateIntervalTicks` | How often to refresh occlusion cache | `10` |
| `paintingBackfaceCulling` | Skip paintings when camera is behind them | `true` |
| `entityTypeOverrides` | Per-entity-type culling overrides | `{}` |

### Entity LOD

```json
"entityLOD": {
  "enabled": true,
  "farLODDistance": 96
}
```

| Field | Description | Default |
|-------|-------------|---------|
| `enabled` | Enable entity level-of-detail | `true` |
| `farLODDistance` | Distance at which entities render every 2nd tick (blocks) | `96` |

### Nameplate Culling

```json
"nameplateCulling": {
  "enabled": true,
  "maxDistance": 32,
  "checkIntervalTicks": 5
}
```

| Field | Description | Default |
|-------|-------------|---------|
| `enabled` | Hide distant name tags | `true` |
| `maxDistance` | Maximum nameplate render distance (blocks) | `32` |
| `checkIntervalTicks` | Re-evaluate visibility every N ticks | `5` |

### Block Entity Culling

```json
"blockEntityCulling": {
  "enabled": true,
  "maxDistance": 48
}
```

| Field | Description | Default |
|-------|-------------|---------|
| `enabled` | Enable block entity distance culling | `true` |
| `maxDistance` | Maximum distance to render block entities (blocks) | `48` |

### Particle Optimization

```json
"particleOptimization": {
  "enabled": true,
  "maxParticles": 4096,
  "midDistance": 32,
  "maxDistance": 64
}
```

| Field | Description | Default |
|-------|-------------|---------|
| `enabled` | Enable particle capping and LOD | `true` |
| `maxParticles` | Hard cap on total live particles | `4096` |
| `midDistance` | Beyond this distance, spawn at ~50% density (blocks) | `32` |
| `maxDistance` | Beyond this distance, spawn no particles (blocks) | `64` |

### Background FPS

```json
"backgroundFps": {
  "enabled": true,
  "unfocusedFpsCap": 60,
  "minimizedFpsCap": 30,
  "titleScreenFpsCap": 120
}
```

| Field | Description | Default |
|-------|-------------|---------|
| `enabled` | Enable background FPS limiting | `true` |
| `unfocusedFpsCap` | FPS cap when window is unfocused (0 = unlimited) | `60` |
| `minimizedFpsCap` | FPS cap when window is minimized (0 = unlimited) | `30` |
| `titleScreenFpsCap` | FPS cap on title/loading screens (0 = unlimited) | `120` |

### World Join Optimizer

```json
"worldJoinOptimizer": {
  "enabled": true,
  "gracePeriodTicks": 200
}
```

| Field | Description | Default |
|-------|-------------|---------|
| `enabled` | Tighten culling after world join | `true` |
| `gracePeriodTicks` | Duration of join grace period (200 = 10 seconds) | `200` |

### GUI Optimizer

```json
"guiOptimization": {
  "enabled": true,
  "hotbarCaching": true,
  "hudUpdateThrottling": true
}
```

| Field | Description | Default |
|-------|-------------|---------|
| `enabled` | Enable GUI optimizations | `true` |
| `hotbarCaching` | Track hotbar slot changes to avoid redundant renders | `true` |
| `hudUpdateThrottling` | Throttle HUD updates for health, food, armor, XP | `true` |

### Item Frame

```json
"itemFrame": {
  "enabled": true,
  "mapUpdateIntervalTicks": 4
}
```

| Field | Description | Default |
|-------|-------------|---------|
| `enabled` | Enable map item frame throttle | `true` |
| `mapUpdateIntervalTicks` | Update map frames every N ticks | `4` |

### Singleplayer Boost

```json
"singleplayerOpt": {
  "enabled": true
}
```

| Field | Description | Default |
|-------|-------------|---------|
| `enabled` | Use more aggressive culling in singleplayer | `true` |

### Advanced Config (v1.8+)

```json
"entityTickOptimizer": {
  "enabled": true,
  "farSkipDistance": 32
},
"adaptiveViewDistance": {
  "enabled": true,
  "lowFpsThreshold": 30,
  "minViewDistance": 4,
  "reductionStep": 2,
  "restoreDelaySec": 5
},
"chunkRebuildBudget": {
  "enabled": true,
  "maxRebuildsPerFrame": 2
},
"frameTiming": {
  "enabled": true,
  "targetGpuLoadFraction": 0.75,
  "minDistanceFraction": 0.5
},
"lightCache": {
  "enabled": true,
  "cacheSize": 256
},
"chunkUpdates": {
  "enabled": true,
  "maxDelayMs": 50,
  "maxRebuildsPerFrame": 4
},
"gcPressure": {
  "enabled": true,
  "heapUsageThreshold": 75,
  "gcOnLoadScreen": true
}
```

## Custom Profiles

Create custom profiles via ModMenu or by editing `config/fpsflow.json`:

```json
"customProfiles": {
  "My Custom Profile": {
    "entityCulling": { ... },
    "entityLOD": { ... },
    ...
  }
}
```

Switch profiles using the in-game config screen or by setting:
```json
"selectedProfile": "My Custom Profile"
```

---

## License

FPSFlow is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.

- You are free to use, modify, and distribute FPSFlow
- If you run a modified version on a server, you must provide the source code to users
- See [LICENSE](https://github.com/Affenix-Studios/FPSFlow/blob/main/LICENSE) for full terms