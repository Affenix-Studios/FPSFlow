val legacy = extensions.create("legacyVariant", mapOf<String, Any?>()::class.java)

legacy.apply {
    val target = "legacy"
    val minecraftVersion = "1.21.11"
    val yarnMappings = "1.21.11+build.1"
    val loaderVersion = "0.19.3"
    val fabricVersion = "0.141.4+1.21.11"
    val javaVersion = 21

    project.tasks.register("build${target.capitalize()}") {
        group = "build"
        description = "Build FPSFlow ${target} variant"
        doLast {
            println("Building ${target} variant for Minecraft ${minecraftVersion}")
        }
    }

    project.extensions.extraProperties["variantMinecraftVersion"] = minecraftVersion
    project.extensions.extraProperties["variantYarnMappings"] = yarnMappings
    project.extensions.extraProperties["variantLoaderVersion"] = loaderVersion
    project.extensions.extraProperties["variantFabricVersion"] = fabricVersion
    project.extensions.extraProperties["variantJavaVersion"] = javaVersion
}
