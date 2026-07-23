# Performance Tips

Get the most out of FPSFlow with these tuning recommendations.

## Start with the Right Profile

- **Quality**: Use only if you have a powerful PC and want maximum visuals
- **Balanced**: Best for most players; good FPS with minimal visual compromise
- **Performance**: Ideal for mid-range PCs or busy servers
- **Ultra Performance**: For low-end systems or extreme entity counts

## Tuning for Your Play Style

### PvP and Competitive Play

- Disable `occlusionCulling` to avoid entities popping out behind walls
- Keep `entityLOD` enabled but increase `farLODDistance` to 128
- Disable `nameplateCulling` or set `maxDistance` to 64
- Enable `singleplayerOpt` if playing on local servers

### Survival and Exploration

- Keep all features enabled at default values
- Increase `particleOptimization.maxDistance` to 80 for better visuals
- Enable `worldJoinOptimizer` to smooth out chunk loading

### Skyblock and Mob Farms

- Increase `entityCulling.maxDistance` to 96 or higher
- Disable `entityLOD` if you need all entities visible
- Lower `particleOptimization.maxParticles` to 2048 if farm particles cause lag
- Use `entityTypeOverrides` to always render specific entities

### Texture Pack Servers

- Increase `particleOptimization.maxParticles` to 8192
- Raise `midDistance` and `maxDistance` for particles
- Enable `backgroundFps.enabled` to reduce GPU load on static menus

## Advanced Tweaks

### Reduce Micro-Stutters

- Enable `chunkRebuildBudget.enabled` to limit chunk rebuilds per frame
- Set `maxRebuildsPerFrame` to 2 or 3
- Enable `gcPressure.enabled` to hint GC during loading screens

### Improve Join Times

- Increase `worldJoinOptimizer.gracePeriodTicks` to 300 (15 seconds)
- Enable `adaptiveViewDistance.enabled` to dynamically reduce render distance

### Maximize FPS

- Set all `maxDistance` values to their minimums
- Disable `occlusionCulling` if async raycasts cause hitches
- Enable `entityTickOptimizer.enabled` to skip AI for distant entities
- Use Performance or Ultra Performance profile

### Balance FPS and Visuals

- Start with Balanced profile
- Adjust one setting at a time and measure FPS impact
- Use the in-game debug screen (F3) to monitor FPS and entity counts
- Save your tuned settings as a Custom profile

## Monitoring Performance

### Debug Screen

Press F3 to see:
- **FPS**: Current frame rate
- **Entity Count**: Total entities in range
- **Chunk Count**: Loaded chunks
- **Memory**: Heap usage

### Log Analysis

Check `latest.log` for FPSFlow initialization messages:
```
[FPSFlow] Module 'entity-culling' initialized
[FPSFlow] Module 'particle-optimizer' initialized
```

If modules fail to initialize, check for compatibility issues.

## Common Mistakes

1. **Setting distances too low**: You will notice pop-in. Start high and reduce gradually.
2. **Disabling all features**: You lose the benefit of adaptive tuning. Keep the Adaptive Renderer enabled.
3. **Ignoring profiles**: Custom profiles are powerful but can be inconsistent. Start with built-in profiles.
4. **Mixing Sodium settings**: Sodium has its own culling and entity settings. Let FPSFlow manage entity culling; let Sodium manage chunk/block culling.

## Recommended Starting Points

### High-End PC (RTX 3080+, 32 GB RAM)

```json
"entityCulling": { "maxDistance": 96 },
"entityLOD": { "farLODDistance": 128 },
"nameplateCulling": { "maxDistance": 48 },
"blockEntityCulling": { "maxDistance": 64 }
```

### Mid-Range PC (GTX 1660, 16 GB RAM)

```json
"entityCulling": { "maxDistance": 64 },
"entityLOD": { "farLODDistance": 96 },
"nameplateCulling": { "maxDistance": 32 },
"blockEntityCulling": { "maxDistance": 48 }
```

### Low-End PC (Integrated Graphics, 8 GB RAM)

```json
"entityCulling": { "maxDistance": 48, "occlusionCulling": false },
"entityLOD": { "farLODDistance": 64 },
"nameplateCulling": { "maxDistance": 16, "enabled": false },
"blockEntityCulling": { "maxDistance": 32 },
"particleOptimization": { "maxParticles": 2048, "maxDistance": 32 }
```

## Getting Help

If you cannot resolve your issue:

1. Check the [Features](Features.md) page
2. Review the [Configuration](Configuration.md) reference
3. Search [existing issues](https://github.com/Affenix-Studios/FPSFlow/issues) on GitHub
4. Create a new issue with:
   - Minecraft version and mod loader
   - List of all installed mods
   - FPSFlow config (`config/fpsflow.json`)
   - Game log (`latest.log`)