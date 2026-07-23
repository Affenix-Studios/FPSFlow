val modern = extensions.create("modernVariant", mapOf<String, Any?>()::class.java)

modern.apply {
    val target = "modern"
    val minecraftVersion = "26.2"
    val yarnMappings = "26.2+build.1"
    val loaderVersion = "0.19.3"
    val fabricVersion = "0.155.2+26.2"
    val javaVersion = 25

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
