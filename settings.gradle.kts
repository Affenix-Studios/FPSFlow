pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "fabric-loom" || requested.id.id == "net.fabricmc.fabric-loom") {
                val mcVersion = providers.gradleProperty("minecraft_version").orNull ?: "1.21.11"
                if (mcVersion.startsWith("1.21")) {
                    useModule("net.fabricmc:fabric-loom:1.17.3")
                } else {
                    useModule("net.fabricmc:fabric-loom:1.19.2")
                }
            }
        }
    }
}

rootProject.name = "fpsflow"
