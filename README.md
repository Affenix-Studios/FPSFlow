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

### Painting Back-Face Culling *(new in 1.6.0)*
- The back face of a painting is solid and never visible — FPSFlow skips it at render time
- Dot-product check against the painting's facing normal; zero false positives
- Toggle via `entityCulling.paintingBackfaceCulling` (default: `true`)
- Accessible in the ModMenu config screen *(new in 1.6.1)*

### Entity LOD *(new in 1.3.0)*
- **Medium LOD** – entities beyond the configured medium distance render every 2nd tick
- **Far LOD** – entities beyond the far distance render every 3rd tick
- XOR-based distribution staggers throttling across entities to avoid synchronized "freeze frames"
- **FPS-adaptive tightening** *(new in 1.6.0)* – when FPS drops below 30/15, LOD thresholds shrink automatically (0.7×/0.5×) without requiring a manual profile change
- Fully profile-aware; toggle and adjust distances in-game

### Nameplate Culling *(new in 1.3.0)*
- Entity name tags beyond a configurable distance are hidden entirely
- Works for players, mobs, armor stands — anything with a rendered label
- **Server NPC awareness** *(new in 1.6.0)* – entities whose nametag is set to always-visible by the server (`isCustomNameVisible`) are never culled, preventing flickering on plugin NPC servers

### Map Item Frame Throttle *(new in 1.3.0)*
- Item frames holding maps update their render state every N ticks instead of every frame
- First render always runs in full; throttle activates after initial map texture load

### Block Entity Culling *(new in 1.1.0)*
- **Distance culling** – chests, furnaces, signs, banners, item frames, and armor stands beyond the configured distance are not rendered
- Fully configurable max distance, adjusts with each performance profile

### Particle Optimization
- **Count cap** – no new particles spawn once the configured limit is reached
- **Tiered density LOD** *(new in 1.6.0)* – three distance zones: near (< `midDistance`): 100 %; mid zone: ~50 % via stable position hash; beyond `maxDistance`: 0 %
- **Adaptive reduction** – when FPS drops, the effective spawn radius shrinks automatically

### Background FPS Limiter *(new in 1.6.0)*
- Caps the frame rate when the Minecraft window is unfocused or minimised
- Configurable per state: unfocused (default 60 FPS) and minimised (default 30 FPS)
- Post-frame sleep on the render thread; MC's tick catch-up logic handles any missed ticks
- Profile-aware caps (Quality: 30/10, Balanced: 60/30, Performance: 10/3, Ultra Performance: 5/2)
- Toggle and adjust FPS caps in the ModMenu config screen *(new in 1.6.1)*

### GUI & HUD Optimization
- **Hotbar slot caching** – dirty-flag tracking per slot avoids redundant icon processing
- **HUD update throttling** – non-critical stat updates are gated to every-other-tick; forces immediate update when stats actually change
- **ImmediatelyFast awareness** – HUD caching is automatically disabled when ImmediatelyFast is present

### Adaptive Rendering
- **Smoothed FPS monitoring** – exponential moving average keeps FPS estimates stable
- **Dynamic culling levels** – culling aggressiveness increases automatically below 30 FPS and again below 15 FPS

### World Join Optimizer *(new in 1.2.0)*
- **Grace period** – on joining a world, culling distances start at 35% of normal and ease back to 100% over ~10 seconds
- Prevents the initial entity and chunk flood from tanking FPS during the first seconds after load
- Async occlusion batch triples during the grace period to clear the raycast backlog faster
- Configurable grace period length; toggleable via the in-game config screen

### In-game Config Screen *(new in 1.1.0)*
- Install [ModMenu](https://modrinth.com/mod/modmenu) to access the config screen directly in-game
- Switch profiles, toggle every feature, and adjust FPS caps and LOD distances — no JSON editing required
- **Fully updated in 1.6.1** – Background FPS Limit, Unfocused/Minimized FPS cap sliders, Painting Backface Culling, and Nameplate Culling toggles added

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

| Profile | Entity Culling | BE Culling | LOD (med/far) | Nameplate | Particle (mid/max) | Map Throttle | BG FPS (unfoc/min) |
|---------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| Quality | ✓ (128 b) | ✗ | ✓ (48/96 b) | ✗ | Off | ✗ | 30/10 |
| Balanced | ✓ (64 b) | ✓ (64 b) | ✓ (40/80 b) | ✓ 32 b | 32/64 b | ✓ /3 t | 60/30 |
| Performance | ✓ (48 b) | ✓ (48 b) | ✓ (24/48 b) | ✓ 24 b | 16/32 b | ✓ /4 t | 10/3 |
| Ultra Performance | ✓ (32 b) | ✓ (32 b) | ✓ (16/32 b) | ✓ 16 b | 8/16 b | ✓ /5 t | 5/2 |

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
  "backgroundFps": {
    "enabled": true,
    "unfocusedFpsCap": 60,
    "minimizedFpsCap": 30
  },
  "entityCulling": {
    "enabled": true,
    "occlusionCulling": true,
    "asyncOcclusion": true,
    "maxDistance": 64,
    "cacheUpdateIntervalTicks": 10,
    "paintingBackfaceCulling": true,
    "entityTypeOverrides": {}
  },
  "blockEntityCulling": {
    "enabled": true,
    "maxDistance": 64
  },
  "particleOptimization": {
    "enabled": true,
    "maxParticles": 4096,
    "midDistance": 32,
    "maxDistance": 64
  },
  "guiOptimization": {
    "enabled": true,
    "hotbarCaching": true,
    "hudUpdateThrottling": true
  },
  "renderCaching": { "enabled": true },
  "entityLOD": {
    "enabled": true,
    "mediumLODDistance": 40,
    "farLODDistance": 80
  },
  "nameplateCulling": {
    "enabled": true,
    "maxDistance": 32,
    "checkIntervalTicks": 10
  },
  "itemFrame": {
    "enabled": true,
    "mapUpdateIntervalTicks": 3
  }
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
