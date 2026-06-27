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
        url = uri("https://terraformersmc.com")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")

    val mappingsVersion = property("yarn_mappings").toString()
    if (mappingsVersion == "OFFICIAL") {
        mappings(loom.officialMojangMappings())
    } else {
        mappings("net.fabricmc:yarn:${mappingsVersion}:v2")
    }

    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")
    // ModMenu is optional. Some modpack build setups may not have the requested ModMenu version
    // available in the configured repositories; failing here would break the whole build.
    // Keeping it as optional classpath avoids hard dependency resolution errors.
    // Make ModMenu truly optional at build time.
    // If the specified version cannot be resolved from the configured repositories,
    // the build would previously fail during dependency resolution.
    // We guard it with a best-effort resolution and fall back to skipping ModMenu.
    val modmenuVersion = property("modmenu_version").toString()

    // ModMenu wirklich optional halten.
    // Wichtig: Kotlin DSL in deinem Setup hat keine stabile Klasse/Import-Auflösung für java.net/java.io.
    // Deshalb lassen wir den Build nicht von einer Netzwerk-Prüfung abhängig machen, sondern vermeiden die Hard-Fail Auflösung.
    // Lösung: ModMenu nur hinzufügen, wenn es explizit als Projekt-Property aktiviert ist.
    // (Setz: -Penable_modmenu=true)
    if ((findProperty("enable_modmenu") as String?)?.toBoolean() == true) {
        modCompileOnly("com.terraformersmc:modmenu:${modmenuVersion}")
    } else {
        logger.lifecycle("[FPSFlow] ModMenu disabled for build (pass -Penable_modmenu=true to enable). Requested version: $modmenuVersion")
    }


}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", minecraftVersion)
    inputs.property("loader_version", loaderVersion)

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
}

val targetJavaVersion = if (project.hasProperty("targetJava")) {
    project.property("targetJava").toString().toInt()
} else if (minecraftVersion.startsWith("1.21")) {
    21
} else {
    25
}

println("[FPSFlow] Building for Minecraft $minecraftVersion with Java $targetJavaVersion")

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = targetJavaVersion
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
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
    archiveClassifier.set("mc$minecraftVersion-java$targetJavaVersion")
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

// Helper: reuse the same Gradle invocation (no nested gradle call) to avoid Loom/Gradle cache locks.
fun configureMatrixForJava21(project: Project) {
    project.extensions.extraProperties["targetJava"] = 21
    project.extensions.extraProperties["minecraft_version"] = "1.21.11"
    project.extensions.extraProperties["yarn_mappings"] = "1.21.11+build.6"
    project.extensions.extraProperties["loader_version"] = "0.19.3"
    project.extensions.extraProperties["fabric_version"] = "0.141.4+1.21.11"
    project.extensions.extraProperties["modmenu_version"] = "17.0.0"
}

fun configureMatrixForJava25(project: Project) {
    project.extensions.extraProperties["targetJava"] = 25
    project.extensions.extraProperties["minecraft_version"] = "26.1.2"
    project.extensions.extraProperties["yarn_mappings"] = "OFFICIAL"
    project.extensions.extraProperties["loader_version"] = "0.19.3"
    project.extensions.extraProperties["fabric_version"] = "0.152.1+26.1.2"
    project.extensions.extraProperties["modmenu_version"] = "18.0.0-beta.1"
}

tasks.register("buildJava21") {
    group = "build"
    description = "Build artifact for Minecraft 1.21.11 (Java 21) using Yarn mappings."
    dependsOn("build")
    doFirst {
        configureMatrixForJava21(project)
    }
}

tasks.register("buildJava25") {
    group = "build"
    description = "Build artifact for Minecraft 26.1.2+ (Java 25) using official Mojang mappings."
    dependsOn("build")
    doFirst {
        configureMatrixForJava25(project)
    }
}

