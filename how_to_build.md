# How to Build FPSFlow

## Quick Start

```bash
gradle clean build
```

The compiled JAR is placed in `build/libs/`:
```
build/libs/fpsflow-<version>-mc<minecraft_version>-java<java_version>.jar
```

## Prerequisites

- **Java 21 or 25** (JDK) — configured via `java_version` in `gradle.properties`
- **Gradle** (system-wide installation) — the `gradlew` wrapper is not included

## Current Configuration

| Property | Value | Description |
|----------|-------|-------------|
| `minecraft_version` | 1.21.11 | Target Minecraft version |
| `yarn_mappings` | 1.21.11+build.1 | Yarn mappings for this MC version |
| `loader_version` | 0.19.3 | Fabric Loader version |
| `fabric_version` | 0.141.4+1.21.11 | Fabric API version |
| `java_version` | 25 | Java target (21 or 25) |
| `mod_version` | 1.8 | Mod version |

## Upgrading to a Newer Minecraft Version

### Step 1: Update `gradle.properties`

```properties
minecraft_version=<new_version>        # e.g. 1.22.0
yarn_mappings=<yarn_version>           # e.g. 1.22.0+build.1
loader_version=<loader_version>        # e.g. 0.20.0
fabric_version=<fabric_version>        # e.g. 0.150.0+1.22.0
java_version=25                        # Keep or adjust
```

### Step 2: Find the correct Yarn version

Check available Yarn versions at:
- https://maven.fabricmc.net/net/fabricmc/yarn/
- https://github.com/FabricMC/yarn/releases

### Step 3: Build and test

```bash
gradle clean build
```

## Version Independence Strategy

**Short-term:** Yarn mappings (current, working)
**Long-term:** Mojang mappings (once Loom > 1.17.3 is released)

See `VERSION_MIGRATION.md` for detailed migration instructions.

## Clean

```bash
gradle clean
```

## IDE Setup

Import the project as a Gradle project in your IDE (VS Code, IntelliJ IDEA, or Eclipse).

## Publishing (Maven)

```bash
gradle publish
```

## Troubleshooting

| Error | Solution |
|-------|----------|
| `Configuration 'mappings' has no dependencies` | Loom 1.17.3 requires Yarn mappings. Set `yarn_mappings` to a valid version. |
| `Could not find net.fabricmc:yarn:<version>` | The Yarn version doesn't exist. Check https://maven.fabricmc.net/net/fabricmc/yarn/ |
| 502 Bad Gateway | TerraformersMC Maven is temporarily offline. Retry later. |
| Java version mismatch | Set `java_version` to match your JDK (21 or 25). |

---

*See `VERSION_MIGRATION.md` for the complete migration guide and future plans.*