plugins {
    id("fabric-loom") version "1.17.3"
    `maven-publish`
}

version = property("mod_version").toString()
group = property("maven_group").toString()

val minecraftVersion = property("minecraft_version").toString()
val yarnMappings = project.findProperty("yarn_mappings")?.toString()
    ?: project.findProperty("mappings_version")?.toString()
    ?: "OFFICIAL"
val loaderVersion = property("loader_version").toString()
val fabricVersion = property("fabric_version").toString()
val javaVersion = (project.findProperty("java_version")?.toString() ?: property("java_version").toString()).toInt()

buildDir = file("build/java${javaVersion}")

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

// =====================================================================
// Multi-Release Build: buildJava21, buildJava25, buildAll
// =====================================================================
// These tasks run a clean build for each Java version via a subprocess.
// They use Gradle properties instead of editing gradle.properties directly.
// Usage:
//   gradle buildJava21       → builds only Java 21 JAR
//   gradle buildJava25       → builds only Java 25 JAR
//   gradle buildAll          → builds BOTH Java 21 + Java 25 JARs
//   gradle clean build       → builds both variants automatically
// =====================================================================

val gradleCmd = if (System.getProperty("os.name").lowercase().contains("win")) "gradle.bat" else "gradle"
val projectDirRoot = rootProject.projectDir.absolutePath
val runMatrixBuilds = project.findProperty("skipSubBuilds")?.toString()?.equals("true", ignoreCase = true) != true

fun buildJarDir(javaVer: Int): File = File(projectDirRoot, "build/java${javaVer}/libs")

fun registerJavaBuild(name: String, javaVer: Int): TaskProvider<Task> {
    return tasks.register(name) {
        group = "build"
        description = "Build FPSFlow for Java ${javaVer}"

        doLast {
            println()
            println("╔══════════════════════════════════════════════╗")
            println("║  Building for Java ${javaVer}...                ║")
            println("╚══════════════════════════════════════════════╝")
            println()

            val proc = ProcessBuilder(
                gradleCmd,
                "clean",
                "build",
                "--no-daemon",
                "-Dorg.gradle.daemon=false",
                "-Pjava_version=${javaVer}",
                "-PskipSubBuilds=true"
            )
                .directory(rootProject.projectDir)
                .inheritIO()
                .start()
            val exitCode = proc.waitFor()

            if (exitCode != 0) {
                throw GradleException("Java ${javaVer} build failed (exit code ${exitCode})")
            }

            val jarDir = buildJarDir(javaVer)
            val javaJars = jarDir.listFiles { f -> f.name.contains("java${javaVer}") }
            println()
            println("✅ Java ${javaVer} Build successful!")
            javaJars?.forEach { jar ->
                val sizeKb = String.format("%.1f", jar.length() / 1024.0)
                println("   📦 ${jar.name} (${sizeKb} KB)")
            }
        }
    }
}

val buildJava21 = registerJavaBuild("buildJava21", 21)
val buildJava25 = registerJavaBuild("buildJava25", 25)

fun copyBuiltJarsToBuildLibs(vararg srcDirs: File) {
    val destDir = File(projectDirRoot, "build/libs")
    destDir.mkdirs()
    delete(fileTree(destDir).matching { include("fpsflow-*.jar") })
    val jars = srcDirs.flatMap { dir -> dir.listFiles { f -> f.name.endsWith(".jar") }?.toList() ?: emptyList() }
    copy {
        from(jars)
        into(destDir)
    }
}

tasks.register("buildAll") {
    group = "build"
    description = "Build FPSFlow for both Java 21 and Java 25"
    dependsOn(buildJava21, buildJava25)

    doLast {
        println()
        println("╔══════════════════════════════════════════════╗")
        println("║  All builds completed successfully!         ║")
        println("╚══════════════════════════════════════════════╝")
        println()

        val java21Dir = buildJarDir(21)
        val java25Dir = buildJarDir(25)
        copyBuiltJarsToBuildLibs(java21Dir, java25Dir)

        listOf(java21Dir, java25Dir).forEach { jarDir ->
            jarDir.listFiles { f -> f.name.endsWith(".jar") }?.sorted()?.forEach { jar ->
                val sizeKb = String.format("%.1f", jar.length() / 1024.0)
                println("   📦 ${jar.name} (${sizeKb} KB)")
            }
        }
    }
}

if (runMatrixBuilds) {
    tasks.named("build") {
        dependsOn(buildJava21, buildJava25)
        doLast {
            val java25Dir = buildJarDir(25)
            copyBuiltJarsToBuildLibs(java25Dir)
        }
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