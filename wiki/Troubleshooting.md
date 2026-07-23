# Troubleshooting

Common issues and their solutions.

## Installation Issues

### Game crashes on launch

**Symptoms**: Minecraft crashes immediately when launching with FPSFlow installed.

**Solutions**:
1. Verify you have the correct Java version:
   - MC 1.21.11 requires Java 21
   - MC 26.2 requires Java 25
2. Check that Fabric API is installed and matches your Minecraft version
3. Remove old FPSFlow versions from your `mods` folder
4. Check `latest.log` for specific error messages

### FPSFlow does not appear in ModMenu

**Symptoms**: No FPSFlow entry in the Mods list.

**Solutions**:
- ModMenu is optional; FPSFlow works without it
- Edit `config/fpsflow.json` manually
- Ensure you have ModMenu 20.0.1 or newer

### "Duplicate mod" errors

**Symptoms**: Game log shows duplicate mod ID warnings.

**Solutions**:
- Remove all old FPSFlow JARs from `mods`
- Keep only the latest version

---

## Performance Issues

### Low FPS with FPSFlow enabled

**Symptoms**: FPS is lower with FPSFlow than without it.

**Solutions**:
1. Check your profile:
   - Use Quality profile for high-end PCs
   - Use Performance or Ultra Performance for low-end PCs
2. Disable `occlusionCulling` if async raycasts cause hitches
3. Lower `entityCulling.maxDistance` and `entityLOD.farLODDistance`
4. Check for conflicts with other mods (see [Compatibility](Compatibility.md))

### Stutter when joining a world

**Symptoms**: Frame drops during world load.

**Solutions**:
- This is expected behavior; the World Join Optimizer tightens culling during chunk loading
- Increase `worldJoinOptimizer.gracePeriodTicks` to 300 (15 seconds)
- Enable `adaptiveViewDistance.enabled` to dynamically reduce render distance

### Entities disappear behind walls

**Symptoms**: Entities vanish when you move behind a wall.

**Solutions**:
1. Disable `occlusionCulling` in `entityCulling`
2. The raycast may be hitting invisible blocks like barriers or structure void
3. Reduce `entityCulling.maxDistance` to limit the culling range

### Name tags flicker at distance

**Symptoms**: Name tags rapidly appear and disappear.

**Solutions**:
1. Increase `nameplateCulling.maxDistance`
2. Disable nameplate culling entirely if flicker persists
3. The hysteresis system should prevent rapid toggling; report the issue if it does not

### Particles look sparse

**Symptoms**: Fewer particles than expected.

**Solutions**:
1. Increase `particleOptimization.maxParticles` to 8192
2. Raise `particleOptimization.midDistance` and `maxDistance`
3. Disable `particleOptimization.enabled` to test if FPSFlow is the cause

---

## Visual Issues

### Block entities pop in at distance

**Symptoms**: Chests, furnaces, etc. appear suddenly when approaching.

**Solutions**:
- Increase `blockEntityCulling.maxDistance`
- This is expected behavior; increase the distance to match your render distance

### Entities LOD causes flicker

**Symptoms**: Distant entities flicker on and off.

**Solutions**:
1. Increase `entityLOD.farLODDistance`
2. Disable `entityLOD.enabled` if flicker is unacceptable
3. Ensure you are using FPSFlow 1.7.0 or later; earlier versions had LOD flicker issues

### Map item frames flicker

**Symptoms**: Map frames show stale or flickering textures.

**Solutions**:
- This was fixed in FPSFlow 1.7.11
- Ensure you are on the latest version
- The throttle now skips updates during resource pack reloads

---

## Compatibility Issues

### Conflict with EntityCulling

**Symptoms**: Both mods' entity culling features seem active.

**Solutions**:
- FPSFlow automatically disables its own occlusion culling when EntityCulling is detected
- Check the game log for compatibility messages
- No manual configuration needed

### Conflict with ImmediatelyFast

**Symptoms**: HUD rendering issues or duplicate optimizations.

**Solutions**:
- FPSFlow automatically disables HUD caching when ImmediatelyFast is detected
- All other FPSFlow features remain active
- No manual configuration needed

### Shader pack issues

**Symptoms**: Entities disappear behind transparent shader geometry.

**Solutions**:
1. Disable `occlusionCulling` in `entityCulling`
2. Entity LOD and nameplate culling work correctly with most shaders
3. Report shader-specific issues on GitHub

---

## Debugging

### Enable debug logging

Add to `config/fpsflow.json`:
```json
{
  "debug": true
}
```

Or check the game log for FPSFlow initialization messages.

### Check module status

Look for these messages in `latest.log`:
```
[FPSFlow] Module 'entity-culling' initialized
[FPSFlow] Module 'particle-optimizer' initialized
[FPSFlow] Compact sine table initialized (64 KB LUT)
```

If a module fails to initialize, you will see:
```
[FPSFlow] Module 'X' could not initialize because a Minecraft class is missing or incompatible
```

### Verify config is loaded

Check that your settings are applied:
1. Make a change in `config/fpsflow.json`
2. Restart Minecraft
3. Check the log for the active profile name:
   ```
   [FPSFlow] FPSFlow initialized – profile: Balanced
   ```

---

## Getting Help

If you cannot resolve your issue:

1. Check the [Performance Tips](Performance-Tips.md) page
2. Review the [Configuration](Configuration.md) reference
3. Search [existing issues](https://github.com/Affenix-Studios/FPSFlow/issues) on GitHub
4. Create a new issue with:
   - Minecraft version and mod loader
   - List of all installed mods
   - FPSFlow config (`config/fpsflow.json`)
   - Game log (`latest.log`)
   - Screenshots or video if applicable

## License

FPSFlow is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.

- You are free to use, modify, and distribute FPSFlow
- If you run a modified version on a server, you must provide the source code to users
- See [LICENSE](https://github.com/Affenix-Studios/FPSFlow/blob/main/LICENSE) for full terms