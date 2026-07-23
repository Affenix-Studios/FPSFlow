# Compatibility

FPSFlow is designed to work alongside other popular optimization and rendering mods. It automatically detects certain mods and adjusts its behavior to avoid conflicts.

## Automatic Compatibility

| Mod | Behavior |
|-----|----------|
| **EntityCulling** | FPSFlow disables its own entity occlusion culling to avoid double-work |
| **Sodium** | No conflicts; works alongside Sodium's chunk/block culling |
| **ImmediatelyFast** | FPSFlow disables HUD caching to avoid redundant work |
| **Lithium** | Detected for compatibility; FPSFlow does not bundle Lithium |
| **FerriteCore** | No conflicts |
| **ModernFix** | No conflicts |

## Manual Compatibility Notes

### Sodium

- Let Sodium handle chunk and block culling
- Let FPSFlow handle entity culling and LOD
- Do not enable both mods' entity culling features simultaneously

### EntityCulling

- If EntityCulling is installed, FPSFlow automatically disables its own occlusion culling
- FPSFlow's entity LOD and nameplate culling remain active
- No configuration changes needed

### ImmediatelyFast

- FPSFlow detects ImmediatelyFast and disables HUD caching
- Hotbar and HUD optimizations are skipped to avoid conflicts
- All other FPSFlow features remain active

### Lithium

- FPSFlow detects Lithium for compatibility reporting
- FPSFlow does not bundle or depend on Lithium
- Both mods can be installed together
- FPSFlow's Lithium-inspired utilities are independent of the Lithium mod

### OptiFine

- FPSFlow is not tested with OptiFine
- Use Sodium + FPSFlow instead for best results
- Some features may not work correctly with OptiFine

## Known Issues

### Shader Packs

- Entity culling may cause entities to disappear behind transparent shader geometry
- Disable `occlusionCulling` if using complex shader packs
- Entity LOD and nameplate culling work correctly with most shaders

### Server-Side Mods

- FPSFlow is client-side only
- Server mods that force entity visibility (`isCustomNameVisible`) are respected
- Nameplate culling backs off for server-forced visible entities

### Multiplayer

- All features work in multiplayer
- World Join Optimizer activates on both singleplayer and multiplayer joins
- Async occlusion adapts to server tick rates automatically

## Testing Matrix

| Minecraft Version | Fabric Loader | Fabric API | Java | Status |
|-------------------|---------------|------------|------|--------|
| 1.21.11 | 0.19.3+ | 0.141.4+ | 21 | Supported |
| 26.2 | 0.19.3+ | 0.155.2+ | 25 | Supported |

## Reporting Compatibility Issues

If you encounter a conflict:

1. Check the [Troubleshooting](Troubleshooting.md) page first
2. Disable FPSFlow features one by one to isolate the conflict
3. Report the issue on [GitHub](https://github.com/Affenix-Studios/FPSFlow/issues) with:
   - Minecraft version
   - List of installed mods
   - FPSFlow config (`config/fpsflow.json`)
   - Game log (`latest.log`)

## License

FPSFlow is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.

- You are free to use, modify, and distribute FPSFlow
- If you run a modified version on a server, you must provide the source code to users
- See [LICENSE](https://github.com/Affenix-Studios/FPSFlow/blob/main/LICENSE) for full terms