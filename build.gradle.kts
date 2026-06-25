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
        url = uri("https://maven.terraformersmc.com/releases")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings("net.fabricmc:yarn:${yarnMappings}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")
    modCompileOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")
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

// Dynamische Java-Version basierend auf Minecraft-Version
// Minecraft 1.21.11 → Java 21, Minecraft 26.1+ → Java 25
val targetJavaVersion = if (minecraftVersion.startsWith("26") || minecraftVersion.startsWith("27")) {
    25
} else {
    21
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

// Reproducible builds: same source → same bytes → same SHA512 hash every time.
// Without this, Gradle embeds different timestamps on each build, so Modrinth's
// hash lookup can't match locally-built JARs to the uploaded version.
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

tasks.named<AbstractArchiveTask>("remapJar") {
    archiveClassifier.set("")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = property("archives_base_name").toString()
            artifact(tasks.named("remapJar"))
            artifact(tasks.named("sourcesJar"))
        }
    }

    repositories {
        // configure publish target here if needed
    }
}


