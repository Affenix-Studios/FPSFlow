# FPSFlow

**FPSFlow** is a Fabric client-side optimization mod for Minecraft that significantly improves frame rates and reduces micro-stutters through intelligent culling, level-of-detail rendering, and adaptive performance tuning.

- **Java**: 21+ (25 for MC 26.2)
- [Download on Modrinth](https://modrinth.com/mod/fpsflow)
- [Source Code](https://github.com/Affenix-Studios/FPSFlow)

---


## Core Features

- **Entity Culling** — frustum, occlusion, and distance culling with async raycasts
- **Entity LOD** — render-throttles distant entities without removing them
- **Nameplate Culling** — hides distant name tags with hysteresis
- **Block Entity Culling** — skips distant block entities
- **Particle Optimization** — cap + distance-based density LOD
- **Background FPS Limiter** — caps FPS when unfocused/minimized
- **World Join Optimizer** — tightens culling during chunk loading
- **GUI Optimizer** — hotbar dirty-flag tracking and HUD throttling
- **Adaptive Renderer** — FPS-aware dynamic culling adjustment
- **Lithium-Inspired Utilities** — compact sine LUT, cached directions, fast BlockPos helpers