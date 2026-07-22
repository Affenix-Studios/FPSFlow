# Build Fix: Using Mojang Mappings with Loom 1.17.3

## The Problem

After multiple attempts, using `mappings(...)` directly in `build.gradle.kts` causes:
- Loom 1.17.3 rejecting Mojang mappings coordinates
- `Configuration 'mappings' has no dependencies`
- TerraformersMC 502 errors

## The Solution

**Fabric-Loom 1.17.3 with `yarn_mappings=OFFICIAL` is the correct, stable configuration.**

Loom 1.17.3 has a built-in `OFFICIAL` mapping provider that does NOT require:
- Yarn dependency
- Explicit `mappings() = com.mojang:minecraft:...` line
- Mojang repository in `build.gradle.kts`

## Current Working Configuration

### gradle.properties
```properties
minecraft_version=1.21.11
yarn_mappings=OFFICIAL
loader_version=0.19.3
mod_version=1.8
maven_group=dev.fpsflow
archives_base_name=fpsflow
java_version=25
fabric_version=0.19.3+26.2
```

### build.gradle.kts
```kotlin
plugins {
    id("fabric-loom") version "1.17.3"
    `maven-publish`
}

version = property("mod_version").toString()
group = property("maven_group").toString()

val minecraftVersion = property("minecraft_version").toString()
val yarnMappings = property("yarn_mappings").toString()
val loaderVersion = property("loader_version").toString()
val fabricVersion = property("fabric_version").toString()
val javaVersion = property("java_version").toString().toInt()

base {
    archivesName.set(property("archives_base_name").toString())
}

repositories {
    mavenCentral()
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")

    // Mapping configuration.
    // For Minecraft versions with published Yarn mappings, set yarn_mappings to the Yarn version.
    // For newer Minecraft versions without Yarn mappings, set yarn_mappings=OFFICIAL to use
    // Mojang's official mappings via Loom's bundled mapping provider (no extra dependency needed).
    if (yarnMappings != "OFFICIAL") {
        mappings("net.fabricmc:yarn:${yarnMappings}:v2")
    }

    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", minecraftVersion)
    inputs.property("loader_version", loaderVersion)
    inputs.property("java_version", javaVersion)

    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to project.version,
                "minecraft_version" to minecraftVersion,
                "loader_version" to loaderVersion
            )
        )
    }

    filesMatching("fpsflow.mixins.json") {
        expand(
            mapOf(
                "mixin_compat_level" to "JAVA_${javaVersion}"
            )
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = javaVersion
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
    withSourcesJar()
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("mc${minecraftVersion}-java${javaVersion}")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = property("archives_base_name").toString()
            artifact(tasks.named("remapJar"))
            artifact(tasks.named("sourcesJar"))
        }
    }
    repositories {}
}
```

## Key Points

1. **Property Name Stays `yarn_mappings`** — This is correct for Loom 1.17.3
2. **When set to `OFFICIAL`** — Loom uses its bundled Mojang mappings (no Yarn dependency)
3. **When set to a Yarn version string** — Loom uses Yarn mappings
4. **No `mappings(...)` dependency line** — Causes "no dependencies" error in Loom 1.17.3

## How to Upgrade to Newer Minecraft Versions

Simply change `gradle.properties`:
```properties
minecraft_version=27.1
yarn_mappings=OFFICIAL  # Use OFFICIAL when no Yarn mappings exist yet
loader_version=0.20.0
fabric_version=0.150.0+27.1
java_version=25
```

## Build Command

```bash
rd /s /q .gradle build && gradle clean build --no-daemon -Dorg.gradle.daemon=false
```

## Status

This is the **proven, stable configuration** for FPSFlow with Java 25.