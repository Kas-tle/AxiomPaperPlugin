plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("xyz.jpenilla.run-paper") version "2.2.4" // Adds runServer and runMojangMappedServer tasks for testing

    // Shades and relocates dependencies into our plugin jar. See https://imperceptiblethoughts.com/shadow/introduction/
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "com.moulberry.axiom"
version = "1.5.11"
description = "Serverside component for Axiom on Paper"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.viaversion.com")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
    implementation("xyz.jpenilla:reflection-remapper:0.1.0-SNAPSHOT")
    implementation("org.incendo:cloud-paper:2.0.0-SNAPSHOT")

    // Zstd Compression Library
    implementation("com.github.luben:zstd-jni:1.5.5-4")

    // ViaVersion support
    compileOnly("com.viaversion:viaversion-api:4.10.1-SNAPSHOT")

    // WorldGuard support
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")

    // PlotSquared support
    implementation(platform("com.intellectualsites.bom:bom-newest:1.37"))
    compileOnly("com.intellectualsites.plotsquared:plotsquared-core")
    compileOnly("com.intellectualsites.plotsquared:plotsquared-bukkit") { isTransitive = false }
}

tasks {
    // Configure reobfJar to run when invoking the build task
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(21)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        val props = mapOf(
                "name" to project.name,
                "version" to project.version,
                "description" to project.description,
                "apiVersion" to "1.20"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        // helper function to relocate a package into our package
        fun reloc(pkg: String) = relocate(pkg, "com.moulberry.axiom.dependency.$pkg")
        reloc("xyz.jpenilla:reflection-remapper")
    }
}
