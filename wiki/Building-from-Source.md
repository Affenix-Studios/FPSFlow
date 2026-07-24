# Building from Source

Build FPSFlow from source for development or to get the latest unreleased changes.

## Prerequisites

- **Git** — for cloning the repository
- **Java 21** — for building MC 1.21.11 target
- **Java 25** — for building MC 26.2 target
- **Gradle** — system-wide Gradle 9.x (wrapper is not included)

## Clone the Repository

```bash
git clone https://github.com/Affenix-Studios/FPSFlow.git
cd FPSFlow
```

## Build Targets

FPSFlow supports two build targets:

### Legacy Target (MC 1.21.11)

```bash
gradle build --no-daemon -Pbuild_target=legacy
```

Output:
- `build/legacy/libs/fpsflow-1.8.1-mc1.21.11-java21.jar`
- `build/legacy/libs/fpsflow-1.8.1-mc1.21.11-java21-sources.jar`

### Modern Target (MC 26.2)

```bash
gradle build --no-daemon -Pbuild_target=modern
```

Output:
- `build/modern/libs/fpsflow-1.8.1-mc26.2-java25.jar`
- `build/modern/libs/fpsflow-1.8.1-mc26.2-java25-sources.jar`

### Build Both Targets

```bash
gradle build --no-daemon
```

This builds both legacy and modern targets sequentially.

## Build Options

### Skip Tests

```bash
gradle build --no-daemon -x test
```

### Clean Build

```bash
gradle clean build --no-daemon -Pbuild_target=legacy
```

### Verbose Output

```bash
gradle build --no-daemon --info
```

## Project Structure

```
FPSFlow/
├── build.gradle.kts          # Build configuration
├── gradle.properties         # Version and dependency properties
├── settings.gradle.kts       # Gradle settings
├── src/main/java/            # Source code
│   └── dev/fpsflow/
│       ├── FPSFlow.java      # Main mod entry point
│       ├── FPSFlowClient.java # Client-side entry point
│       ├── config/           # Configuration system
│       ├── entities/         # Entity culling and LOD
│       ├── blockentity/      # Block entity culling
│       ├── particles/        # Particle optimization
│       ├── gui/              # GUI optimizer
│       ├── rendering/        # Adaptive renderer, background FPS
│       ├── mixin/            # Mixin implementations
│       └── util/             # Utilities (CompactSineTable, FastBlockPos, etc.)
├── src/main/resources/       # Resources (mixins.json, assets)
└── build/                    # Build outputs
    ├── legacy/               # MC 1.21.11 builds
    └── modern/               # MC 26.2 builds
```

## Development Tips

### IDE Setup

**IntelliJ IDEA**:
1. Open the project folder
2. Import as Gradle project
3. Wait for sync to complete
4. Run `build` Gradle task to verify setup

**Eclipse**:
1. Run `gradle eclipse` or use Buildship plugin
2. Import as existing Gradle project

### Making Changes

1. Edit source files in `src/main/java/`
2. Edit mixins in `src/main/java/dev/fpsflow/mixin/`
3. Register new mixins in `src/main/resources/fpsflow.mixins.json`
4. Build and test in-game

### Testing Changes

1. Build the mod: `gradle build --no-daemon -Pbuild_target=legacy`
2. Copy the JAR from `build/legacy/libs/` to your `mods` folder
3. Launch Minecraft and check the log for errors
4. Test the specific feature you modified

### Debugging

Enable debug logging in `config/fpsflow.json`:
```json
{
  "debug": true
}
```

Check `latest.log` for FPSFlow initialization messages and module status.

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Make your changes
4. Test thoroughly
5. Commit with a descriptive message
6. Push to your fork
7. Open a Pull Request on GitHub

## Code Style

- Follow existing code style in the project
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Attribute Lithium-inspired code with `@see` references
- Keep mixins minimal and well-commented

## Common Build Issues

### "gradlew.bat not found"

FPSFlow uses system-wide Gradle, not the wrapper. Install Gradle 9.x and use the `gradle` command.

### "Java version mismatch"

Ensure you are using Java 21 for legacy builds and Java 25 for modern builds. Set `JAVA_HOME` or use the full path to the correct Java version.

### "Could not resolve dependencies"

Check your internet connection and ensure the Fabric Maven and TerraformersMC repositories are accessible.

### Mixin target not found

This is usually harmless (`defaultRequire=0` in mixin config). It means the mixin target class was not found in the current Minecraft version mappings. The mod will still work.

## License

FPSFlow is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.

- You are free to use, modify, and distribute FPSFlow
- If you run a modified version on a server, you must provide the source code to users
- See [LICENSE](https://github.com/Affenix-Studios/FPSFlow/blob/main/LICENSE) for full terms