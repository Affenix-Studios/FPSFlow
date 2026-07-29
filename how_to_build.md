# How to Build FPSFlow

## Quick Start

The project uses one shared source tree and switches between targets with the `build_target` property.

### Build both variants

```bash
gradle buildAllVariants
```

This runs both target builds back to back and is the easiest way to produce the full set of jars.

### Build with Java 21 only

```bash
gradle clean build -Pbuild_target=legacy
```

This builds only the legacy target with Java 21.

### Build with Java 25 only

```bash
gradle clean build -Pbuild_target=modern
```

This builds only the modern target with Java 25, but it is currently intended as a build/test target for newer Minecraft compatibility work rather than a fully verified runtime target.

### Convenience tasks

```bash
gradle buildLegacy
gradle buildModern
gradle buildAllVariants
```

The compiled jars are placed under the target-specific build folders:

```text
build/legacy/
build/modern/
```

If you run `gradle clean build` without `-Pbuild_target`, the build script automatically depends on `buildAllVariants` and produces both targets.

## Prerequisites

- Java 21 or Java 25 (JDK)
- Gradle installed system-wide
- The project does not include a Gradle wrapper (`gradlew`)

## Current Targets

| Target | Minecraft | Mappings | Java | Output note |
|--------|-----------|----------|------|-------------|
| `legacy` | 1.21.11 | 1.21.11+build.1 | 21 | Verified build and runtime target |
| `modern` | 26.2 | OFFICIAL | 25 | Build target for newer compatibility work; runtime compatibility is still incomplete |

## Jar naming

The output jar name includes the target and Java version, for example:

```text
fpsflow-1.8.3-mc1.21.11-java21.jar
fpsflow-1.8.3-mc26.2-java25.jar
```

So you can keep both builds apart clearly.

## Switching Targets

You can switch targets either by editing `gradle.properties`:

```properties
build_target=legacy
# or
build_target=modern
```

or by passing the property on the command line:

```bash
gradle clean build -Pbuild_target=modern
```

## Common Commands

```bash
gradle clean
gradle buildLegacy
gradle buildModern
gradle buildAllVariants
gradle clean build
gradle clean build -Pbuild_target=legacy
gradle clean build -Pbuild_target=modern
```

## Troubleshooting

- If Gradle cannot resolve mappings, verify the selected target and the values in `build.gradle.kts`.
- If the modern target fails, confirm that the chosen Minecraft/Fabric API versions are available from the configured repositories.
- If the modern target starts but crashes at runtime, the issue is usually caused by missing or outdated mixin targets and Minecraft class mappings rather than the Java version itself.
- If you use a different Java version, make sure it matches the selected target configuration.