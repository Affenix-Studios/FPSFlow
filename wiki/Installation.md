# Installation

## Requirements

- **Minecraft**: 1.21.11 or 26.2
- **Fabric Loader**: 0.19.3 or newer
- **Fabric API**: 0.141.4 or newer
- **Java**: 21 for MC 1.21.11, 25 for MC 26.2

## Step-by-Step

1. **Install Fabric Loader**
   - Download from [fabricmc.net](https://fabricmc.net/use/server/)
   - Run the installer and select your Minecraft version
   - Complete the installation

2. **Download FPSFlow**
   - Get the latest release from [Modrinth](https://modrinth.com/mod/fpsflow)
   - Choose the file matching your Minecraft version and Java version:
     - `fpsflow-1.8.1-mc1.21.11-java21.jar`
     - `fpsflow-1.8.1-mc26.2-java25.jar`

3. **Install Fabric API**
   - Download from [Modrinth](https://modrinth.com/mod/fabric-api)
   - Place it in your `mods` folder

4. **Install FPSFlow**
   - Place the downloaded FPSFlow JAR in your `mods` folder
   - Do **not** rename the file

5. **Launch Minecraft**
   - Select the Fabric profile in the Minecraft launcher
   - Click Play

## Verification

After launching, check the game log (`latest.log`) for:
```
[FPSFlow] Initializing FPSFlow
[FPSFlow] Minecraft runtime: 1.21.11 (supported: true)
[FPSFlow] Module 'entity-culling' initialized
[FPSFlow] Module 'particle-optimizer' initialized
...
[FPSFlow] Client subsystems ready
```

If you see these messages, FPSFlow is working correctly.

## ModMenu Integration (Optional)

If you have [ModMenu](https://modrinth.com/mod/modmenu) installed:
- FPSFlow appears in the Mods list
- Click the gear icon to open the config screen
- Toggle features and switch profiles without editing JSON

## Troubleshooting Installation

**Q: Game crashes on launch**
A: Ensure you have the correct Java version for your Minecraft version. MC 1.21.11 requires Java 21; MC 26.2 requires Java 25.

**Q: FPSFlow does not appear in ModMenu**
A: ModMenu is optional. FPSFlow works without it. Edit `config/fpsflow.json` manually if needed.

**Q: "Duplicate mod" errors**
A: Remove any old FPSFlow versions from your `mods` folder before installing the new one.