# FPSFlow

**Real FPS improvements — no placebo, no bloat.**

FPSFlow is a client-side Fabric optimization mod that targets the biggest rendering bottlenecks in vanilla Minecraft: entities, block entities, particles, and the HUD. Every feature is measurably effective, individually toggleable, and automatically adjusts to your hardware via four built-in performance profiles.

## Latest update — 1.6.0
- **Background FPS limiter** — window unfocused or minimised? FPSFlow now caps the frame rate automatically (60 FPS / 30 FPS by default) so your GPU isn't burning cycles for nothing. Inspired by Dynamic FPS.
- **Painting back-face culling** — the back of a painting is never visible; FPSFlow now skips it at render time using a simple normal dot-product check.
- **NPC nameplate flickering fixed** — plugin NPCs with server-forced nametags (`isCustomNameVisible`) no longer fight the mod's distance culling. Zero flickering when changing view distance on minigame servers.
- **Tiered particle density** — three distance zones (100 % → ~50 % → 0 %) for a smooth visual falloff instead of a hard cutoff.
- **FPS-adaptive LOD tightening** — Entity LOD thresholds shrink automatically when FPS drops below 30 or 15, no manual profile change needed.

---

## ✦ Features

### Background FPS Limiter *(new in 1.6.0)*
Inspired by [Dynamic FPS](https://modrinth.com/mod/dynamic-fps): when the Minecraft window loses focus or is minimised, FPSFlow inserts a post-frame sleep to cap the effective frame rate.

- **Unfocused cap** — default 60 FPS; the game stays responsive but the GPU is no longer running at full speed for a window you can't see
- **Minimised cap** — default 30 FPS; significantly reduced CPU/GPU usage while the window is in the taskbar
- Implemented as a sleep on the render thread after each frame — MC's built-in tick catch-up loop handles any missed ticks normally
- Profile-aware caps: Quality 30/10 FPS, Balanced 15/5, Performance 10/3, Ultra Performance 5/2
- Toggle via `backgroundFps.enabled` in `fpsflow.json`

### Painting Back-Face Culling *(new in 1.6.0)*
Inspired by [MoreCulling](https://modrinth.com/mod/moreculling): the back face of a painting is solid and never visible to the player — there is no reason to render it.

- A dot-product check against the painting's facing normal decides in microseconds whether to skip the draw call
- Zero false positives — only skips the painting when the camera is definitively behind it
- Toggle via `entityCulling.paintingBackfaceCulling` in `fpsflow.json` (default: enabled)

### Entity Culling
The single biggest FPS win in entity-heavy scenes.

- **Frustum culling** — entities outside the camera frustum are never submitted to the renderer
- **Occlusion culling** — a cached raycast check skips entities hidden behind solid blocks; the result is reused for several ticks so the raycast cost is amortized
- **Async occlusion** — raycasts are spread across multiple game ticks (up to 8/tick) to eliminate frame spikes when many entities need rechecking simultaneously
- **Distance culling** — a configurable maximum render distance cuts entities beyond that range entirely
- **Per-entity-type overrides** — configure specific entity types to always or never be culled (e.g. exempt armor stands)

### Entity LOD *(new in 1.3.0)*
Entities that are too far away to matter still get submitted to the renderer every frame. Entity LOD fixes this:

- **Medium LOD** (>40 blocks on Balanced): entity renders every 2nd tick instead of every frame
- **Far LOD** (>80 blocks on Balanced): entity renders every 3rd tick
- XOR-based distribution staggers throttling across entities, so the skip isn't synchronized across the entire scene
- **FPS-adaptive tightening** *(new in 1.6.0)* — when FPS drops below 30/15, thresholds shrink automatically (0.7×/0.5×) so throttling kicks in earlier without a manual profile switch
- Fully profile-aware — distances adjust automatically; toggle in the config screen

### Nameplate Culling *(new in 1.3.0)*
Name tags float above every entity, even ones you can barely see. Nameplate Culling hides them beyond a configurable distance:

- Works for players, mobs, armor stands — any entity with a rendered label
- Default: 32 blocks on Balanced (adjusts with each profile)
- Toggle in the config screen
- **Fixed in 1.6.0:** plugin NPCs whose nametag is set to always-visible by the server are never culled — no more flickering when changing view distance on minigame servers

### Map Item Frame Throttle *(new in 1.3.0)*
Maps in item frames re-evaluate their render state every single frame — even when nothing on the map has changed.

- Throttles render state updates to every 3–5 ticks (profile-dependent)
- First render always runs in full; throttle activates after the initial map texture is loaded
- Only affects item frames with maps; empty frames are unaffected

### Block Entity Culling *(new in 1.1.0)*
Chests, furnaces, signs, banners, item frames, and armor stands are common sources of unexpected draw calls.

- **Distance culling** — block entities beyond a configurable distance are not rendered
- Fully profile-aware — distance adjusts automatically with each performance profile

### Particle Optimization
Uncontrolled particle explosions can tank FPS instantly.

- **Count cap** — once the configured particle limit is reached, no new particles are allocated
- **Tiered density LOD** *(new in 1.6.0)* — three zones: near (< `midDistance`): full density; mid zone: ~50 % via stable position hash; beyond `maxDistance`: none. Smooth visual falloff, no hard pop-in
- **FPS-aware radius** — when FPS is critically low, the allowed radius tightens further automatically

### GUI & HUD Optimization
The HUD renders every single frame. Small savings compound fast.

- **Hotbar slot hashing** — unchanged slots skip redundant processing
- **HUD update throttling** — non-critical stat elements are gated to every other tick; forces an immediate update when health, food, XP, or armor actually changes *(improved in 1.3.0)*
- **ImmediatelyFast awareness** — FPSFlow's HUD layer steps aside completely when ImmediatelyFast is installed

### World Join Optimizer *(new in 1.2.0)*
That FPS drop right after loading into a world — gone.

- **Grace period** — on joining, culling distances start at 35% of normal and ease back smoothly over ~10 seconds
- **Async burst** — occlusion raycast processing triples during the grace period to drain the backlog that builds up while chunks flood in
- **Particle suppression** — particle cap and spawn radius are also tightened during the grace period
- Works for both singleplayer worlds and multiplayer servers
- Configurable grace period per profile (120–200 ticks); toggle in the in-game config screen

### In-game Config Screen *(new in 1.1.0)*
Install [ModMenu](https://modrinth.com/mod/modmenu) to get a settings button directly in the mod list. Toggle any feature on or off and switch performance profiles with a single click — no JSON editing required. ModMenu is optional; FPSFlow works fine without it.

---

## ✦ Performance Profiles

One setting, four presets. Changing the profile rewrites all other settings automatically.

| Profile | Entity Dist. | BE Dist. | LOD (med/far) | Nameplate | Particle (mid/max) | Map Throttle | BG FPS (unfoc/min) |
|---------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **Quality** | 128 b | Off | 48/96 b | Off | Off | Off | 30/10 |
| **Balanced** *(default)* | 64 b | 64 b | 40/80 b | 32 b | 32/64 b | /3 t | 60/30 |
| **Performance** | 48 b | 48 b | 24/48 b | 24 b | 16/32 b | /4 t | 10/3 |
| **Ultra Performance** | 32 b | 32 b | 16/32 b | 16 b | 8/16 b | /5 t | 5/2 |

> Set `"profile": null` in the config to use fully custom values.

---

## ✦ Compatibility

FPSFlow detects installed mods at startup and automatically disables any overlapping features.

| Mod | Status |
|-----|--------|
| 🟢 **Sodium** | Fully compatible — FPSFlow adds entity/GUI optimizations on top |
| 🟢 **Lithium** | No overlap — both run independently |
| 🟢 **FerriteCore** | No overlap — both run independently |
| 🟢 **ModernFix** | No overlap — both run independently |
| 🟢 **ModMenu** | Optional — enables in-game config screen |
| ⚡ **EntityCulling** *(tr7zw)* | FPSFlow's built-in entity culling disables automatically |
| ⚡ **ImmediatelyFast** | FPSFlow's HUD caching defers automatically |

**Recommended stack:** Sodium + Lithium + FerriteCore + ImmediatelyFast + ModMenu + **FPSFlow**

---

## ✦ Configuration

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
  "renderCaching": { "enabled": true }
}
```

**Per-entity-type override** — never cull armor stands:
```json
"entityTypeOverrides": {
  "minecraft:armor_stand": false
}
```

---

## ✦ Installation

1. Install [**Fabric Loader**](https://fabricmc.net/use/installer/) for Minecraft 1.21.11
2. Download and install [**Fabric API**](https://modrinth.com/mod/fabric-api)
3. Drop the FPSFlow `.jar` into your `mods/` folder
4. *(Optional)* Install [**ModMenu**](https://modrinth.com/mod/modmenu) for the in-game settings screen
5. Launch — the config file is written automatically on first run

---

## ✦ FAQ

**Does FPSFlow conflict with Sodium, Lithium, or FerriteCore?**
No. FPSFlow is designed to complement these mods. Install all of them together for best results.

**Can entities "pop in" with occlusion culling?**
Rarely. The occlusion check is cached per entity and refreshed every 10 ticks by default. Lower `cacheUpdateIntervalTicks` for more accuracy at a small CPU cost. Async occlusion means the refresh is deferred rather than causing a frame spike.

**Can I make specific entities never get culled?**
Yes. Add them to `entityTypeOverrides` in `fpsflow.json`, e.g. `{"minecraft:item_frame": false}`.

**Is there an in-game config screen?**
Yes — install ModMenu and a settings button appears on the FPSFlow entry in the mod list.

**Does this work on servers?**
FPSFlow is purely client-side. It works with any server and requires no server-side installation.

**Is it compatible with Forge / NeoForge?**
No — Fabric only.

---

## ✦ Source & License

[**GitHub — fpsflow/fpsflow**](https://github.com/McAffe13/FPSFlow)

Licensed under the **Mozilla Public License 2.0**.
You are free to use, modify, and redistribute FPSFlow. Modifications to covered files must remain MPL-2.0.
