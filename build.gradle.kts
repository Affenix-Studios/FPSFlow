plugins {
    id("fabric-loom") version "1.17.17"
    `maven-publish`
}

version = property("mod_version").toString()
group = property("maven_group").toString()

val buildTarget = (project.findProperty("build_target")?.toString()
    ?: property("build_target").toString()).lowercase()
val targetConfig = when (buildTarget) {
    "modern" -> mapOf(
        "name" to "modern",
        "minecraft" to "1.21.11",
        "yarn" to "1.21.11+build.1",
        "loader" to "0.19.3",
        "fabric" to "0.141.4+1.21.11",
        "java" to 25
    )
    else -> mapOf(
        "name" to "legacy",
        "minecraft" to "1.21.11",
        "yarn" to "1.21.11+build.1",
        "loader" to "0.19.3",
        "fabric" to "0.141.4+1.21.11",
        "java" to 21
    )
}

val minecraftVersion = targetConfig["minecraft"].toString()
val yarnMappings = targetConfig["yarn"].toString()
val loaderVersion = targetConfig["loader"].toString()
val fabricVersion = targetConfig["fabric"].toString()
val javaVersion = targetConfig["java"].toString().toInt()

buildDir = file("build/${buildTarget}")

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

    if (yarnMappings == "OFFICIAL") {
        mappings(loom.officialMojangMappings())
    } else {
        mappings("net.fabricmc:yarn:${yarnMappings}:v2")
    }

    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", minecraftVersion)
    inputs.property("loader_version", loaderVersion)
    inputs.property("fabric_version", fabricVersion)
    inputs.property("java_version", javaVersion)

    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to project.version,
                "minecraft_version" to minecraftVersion,
                "loader_version" to loaderVersion,
                "fabric_version" to fabricVersion,
                "java_version" to javaVersion
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

// =====================================================================
// Multi-Target Build: legacy and modern
// =====================================================================
// The same source tree now builds either target by switching the
// build_target property. No source duplication is required.
// =====================================================================

val gradleCmd = if (System.getProperty("os.name").lowercase().contains("win")) "gradle.bat" else "gradle"

fun runVariantBuild(target: String) {
    val proc = ProcessBuilder(
        gradleCmd,
        "clean",
        "build",
        "--no-daemon",
        "-Dorg.gradle.daemon=false",
        "-Pbuild_target=${target}"
    )
        .directory(rootProject.projectDir)
        .inheritIO()
        .start()
    val exitCode = proc.waitFor()
    if (exitCode != 0) {
        throw GradleException("Variant ${target} build failed (exit code ${exitCode})")
    }
}

tasks.register("buildLegacy") {
    group = "build"
    description = "Build FPSFlow for Minecraft 1.21.11"
    doLast { runVariantBuild("legacy") }
}

tasks.register("buildModern") {
    group = "build"
    description = "Build FPSFlow for Minecraft 26.2"
    doLast { runVariantBuild("modern") }
}

tasks.register("buildAllVariants") {
    group = "build"
    description = "Build both FPSFlow targets"
    dependsOn("buildLegacy", "buildModern")
}

tasks.named("build") {
    if (project.findProperty("build_target") == null) {
        dependsOn("buildAllVariants")
    }
}

tasks.register("cleanWorkspace") {
    group = "build"
    description = "Remove temporary files, backups, and generated build directories"

    doLast {
        delete(
            fileTree(rootProject.projectDir) {
                include("**/*.bak")
                include("**/*~")
                include("**/*.swp")
            }
        )
        delete(file("${rootProject.projectDir}/build"))
        delete(file("${rootProject.projectDir}/.gradle"))
    }
}