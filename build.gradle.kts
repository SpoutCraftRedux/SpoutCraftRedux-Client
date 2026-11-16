plugins {
    id("net.fabricmc.fabric-loom")
}

val minecraftVersion = providers.gradleProperty("minecraft_version").get()
version = "${providers.gradleProperty("version").get()}+$minecraftVersion"
base.archivesName = "spoutcraftredux-fabric"

sourceSets.main {
    // Protocol submodule, compiled straight into the mod
    java.srcDir("protocol/src/main/java")
}

loom {
    runs {
        // ./gradlew runJoinLocal: starts the client and joins a server on localhost:25565
        register("joinLocal") {
            client()
            configName = "Client (join localhost)"
            programArgs("--quickPlayMultiplayer", "localhost:25565")
        }
    }
    mods {
        register("spoutcraftredux") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
    implementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

tasks.processResources {
    val props = mapOf("version" to project.version, "minecraft_version" to minecraftVersion)
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

tasks.jar {
    from("LICENSE", "COPYING")
}
