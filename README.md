# FPSFlow

> **Improve FPS with smart rendering, entity culling, adaptive optimizations, and configurable performance profiles.**

[![Modrinth](https://img.shields.io/badge/Modrinth-fpsflow-1bd96a?logo=modrinth)](https://modrinth.com/mod/fpsflow)
[![License: MPL-2.0](https://img.shields.io/badge/License-MPL--2.0-blue.svg)](https://opensource.org/licenses/MPL-2.0)
[![Fabric](https://img.shields.io/badge/Mod%20Loader-Fabric-747bff)](https://fabricmc.net/)
[![Java 21](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net/)

---

## Features

### Entity Culling
- **Frustum culling** – entities outside the camera view are never rendered
- **Occlusion culling** – entities behind solid blocks are skipped with a cached raycast
- **Async occlusion** – raycasts are spread across multiple ticks to eliminate frame spikes
- **Distance culling** – configurable maximum render distance per entity
- **Per-entity-type overrides** – exempt specific entity types from culling via config

### Entity LOD *(new in 1.3.0)*
- **Medium LOD** – entities beyond the configured medium distance render every 2nd tick
- **Far LOD** – entities beyond the far distance render every 3rd tick
- XOR-based distribution staggers throttling across entities to avoid synchronized "freeze frames"
- Fully profile-aware; toggle in-game

### Nameplate Culling *(new in 1.3.0)*
- Entity name tags beyond a configurable distance are hidden entirely
- Works for players, mobs, armor stands — anything with a rendered label

### Map Item Frame Throttle *(new in 1.3.0)*
- Item frames holding maps update their render state every N ticks instead of every frame
- First render always runs in full; throttle activates after initial map texture load

### Block Entity Culling
- **Distance culling** – chests, furnaces, signs, banners, item frames, and armor stands beyond the configured distance are not rendered
- Fully configurable max distance, adjusts with each performance profile

### Particle Optimization
- **Count cap** – no new particles spawn once the configured limit is reached
- **Distance limit** – particles beyond a configurable radius from the player are discarded before they are allocated
- **Adaptive reduction** – when FPS drops, the effective spawn radius shrinks automatically

### GUI & HUD Optimization
- **Hotbar slot caching** – dirty-flag tracking per slot avoids redundant icon processing
- **HUD update throttling** – non-critical stat updates are gated to every-other-tick; forces immediate update when stats actually change *(improved in 1.3.0)*
- **ImmediatelyFast awareness** – HUD caching is automatically disabled when ImmediatelyFast is present

### Adaptive Rendering
- **Smoothed FPS monitoring** – exponential moving average keeps FPS estimates stable
- **Dynamic culling levels** – culling aggressiveness increases automatically below 30 FPS and again below 15 FPS

### World Join Optimizer
- **Grace period** – on joining a world, culling distances start at 35% of normal and ease back to 100% over ~10 seconds
- Prevents the initial entity and chunk flood from tanking FPS during the first seconds after load
- Async occlusion batch triples during the grace period to clear the raycast backlog faster
- Configurable grace period length; toggleable via the in-game config screen

### In-game Config Screen
- Install [ModMenu](https://modrinth.com/mod/modmenu) to access the config screen directly in-game
- Switch profiles and toggle features with one click — no JSON editing required

### Compatibility Detection
Automatically detects and gracefully co-exists with:

| Mod | Behaviour |
|-----|-----------|
| **EntityCulling** (tr7zw) | FPSFlow entity culling disabled – no duplicate raycasts |
| **Sodium** | Fully compatible; FPSFlow adds entity/GUI layer on top |
| **ImmediatelyFast** | HUD caching deferred to ImmediatelyFast |
| **Lithium** | No overlap; both mods run independently |
| **FerriteCore** | No overlap; both mods run independently |
| **ModernFix** | No overlap; both mods run independently |

---

## Performance Profiles

| Profile | Entity Culling | BE Culling | Entity LOD (med/far) | Nameplate | Map Throttle |
|---------|:---:|:---:|:---:|:---:|:---:|
| Quality | ✓ (128 b) | ✗ | ✓ (48/96 b) | ✗ | ✗ |
| Balanced | ✓ (64 b) | ✓ (64 b) | ✓ (40/80 b) | ✓ 32 b | ✓ /3 t |
| Performance | ✓ (48 b) | ✓ (48 b) | ✓ (40/80 b)* | ✓ 24 b | ✓ /4 t |
| Ultra Performance | ✓ (32 b) | ✓ (32 b) | ✓ (16/32 b) | ✓ 16 b | ✓ /5 t |

*Default Balanced Medium/Far distances were widened in 1.5.1 for better visibility and smoother distant entity behavior.

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.11
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop the FPSFlow `.jar` into your `mods/` folder
4. *(Optional)* Install [ModMenu](https://modrinth.com/mod/modmenu) for the in-game config screen
5. Launch the game – the default configuration is written to `config/fpsflow.json`

### Recommended Companion Mods

- [Sodium](https://modrinth.com/mod/sodium)
- [Lithium](https://modrinth.com/mod/lithium)
- [FerriteCore](https://modrinth.com/mod/ferrite-core)
- [ImmediatelyFast](https://modrinth.com/mod/immediatelyfast)
- [ModMenu](https://modrinth.com/mod/modmenu) *(for in-game config screen)*

---

## Configuration

Config file: `.minecraft/config/fpsflow.json`

```json
{
  "profile": "BALANCED",
  "updateChecker": { "enabled": true },
  "entityCulling": {
    "enabled": true,
    "occlusionCulling": true,
    "asyncOcclusion": true,
    "maxDistance": 64,
    "cacheUpdateIntervalTicks": 10,
    "entityTypeOverrides": {}
  },
  "blockEntityCulling": {
    "enabled": true,
    "maxDistance": 64
  },
  "particleOptimization": {
    "enabled": true,
    "maxParticles": 4096,
    "maxDistance": 64
  },
  "guiOptimization": {
    "enabled": true,
    "hotbarCaching": true,
    "hudUpdateThrottling": true
  },
  "renderCaching": { "enabled": true }
}
```

**Per-entity-type override example** — never cull armor stands:
```json
"entityTypeOverrides": {
  "minecraft:armor_stand": false
}
```

---

## License

Mozilla Public License 2.0 – see [LICENSE](LICENSE).
