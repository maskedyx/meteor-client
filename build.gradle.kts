import org.gradle.jvm.tasks.Jar

buildscript {
    repositories {
        maven("https://maven.neoforged.net/releases")
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("net.neoforged:moddev-gradle:2.0.141")
    }
}

plugins {
    id("maven-publish")
}

apply(plugin = "net.neoforged.moddev")

base {
    archivesName = properties["archives_base_name"] as String
    group = properties["maven_group"] as String

    val suffix = providers.gradleProperty("build_number").getOrElse("local")
    version = "${libs.versions.minecraft.get()}-$suffix"
}

repositories {
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.meteordev.org/releases") {
        name = "meteor-maven"
    }
    maven("https://maven.meteordev.org/snapshots") {
        name = "meteor-maven-snapshots"
    }
    mavenCentral()
}

val embedded: Configuration by configurations.creating

configurations {
    implementation.configure {
        extendsFrom(embedded)
    }
}

dependencies {
    compileOnly(libs.baritone)

    embedded(libs.orbit)
    embedded(libs.starscript)
    embedded(libs.discord.ipc)
    embedded(libs.reflections)
    embedded(libs.netty.handler.proxy) { isTransitive = false }
    embedded(libs.netty.codec.socks) { isTransitive = false }
    embedded(libs.waybackauthlib)
}

neoForge {
    version = libs.versions.neoforge.get()
    accessTransformers.from(file("src/main/resources/META-INF/accesstransformer.cfg"))
    validateAccessTransformers = true

    runs {
        configureEach {
            gameDirectory = file("run/$name")
            systemProperty("neoforge.enabledGameTestNamespaces", properties["archives_base_name"] as String)
        }

        create("client") {
            client()
        }
    }

    mods {
        create(properties["archives_base_name"] as String) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets {
    val launcher by creating {
        java {
            srcDir("src/launcher/java")
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get().toInt()))
    }

    if (System.getenv("CI")?.toBoolean() == true) {
        withSourcesJar()
        withJavadocJar()
    }
}

tasks {
    processResources {
        val buildNumber = providers.gradleProperty("build_number").getOrElse("")
        val commit = providers.gradleProperty("commit").getOrElse("")

        val propertyMap = mapOf(
            "version" to project.version,
            "mod_id" to properties["archives_base_name"],
            "mod_name" to "Meteor Client",
            "mod_description" to "Based utility mod.",
            "mod_authors" to "MineGame159, squidoodly, seasnail",
            "homepage" to "https://meteorclient.com",
            "issue_tracker" to "https://github.com/MeteorDevelopment/meteor-client/issues",
            "license" to "GPL-3.0",
            "build_number" to buildNumber,
            "commit" to commit,
            "color" to "145,61,226",
            "jdk_version" to libs.versions.jdk.get(),
            "minecraft_version" to libs.versions.minecraft.get(),
            "loader_version" to libs.versions.neoforge.get()
        )

        inputs.properties(propertyMap)
        filesMatching(listOf("META-INF/neoforge.mods.toml", "meteor-client.properties")) {
            expand(propertyMap)
        }
    }

    named<JavaCompile>("compileLauncherJava").configure {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
        options.compilerArgs.add("-Xlint:-options")
    }

    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    }

    withType<Jar>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    jar {
        inputs.property("archivesName", project.base.archivesName.get())

        from("LICENSE") {
            rename { "${it}_${inputs.properties["archivesName"]}" }
        }

        from(sourceSets["launcher"].output)
        from({
            embedded
                .filter { it.exists() }
                .map { if (it.isDirectory) it else zipTree(it) }
        }) {
            exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        }

        manifest {
            attributes(
                "Main-Class" to "meteordevelopment.meteorclient.Main",
                "MixinConfigs" to listOf(
                    "meteor-client.mixins.json",
                    "meteor-client-baritone.mixins.json"
                ).joinToString(",")
            )
        }
    }

    javadoc {
        with(options as StandardJavadocDocletOptions) {
            addStringOption("Xdoclint:none", "-quiet")
            addStringOption("encoding", "UTF-8")
            addStringOption("charSet", "UTF-8")
        }
    }

    build {
        if (System.getenv("CI")?.toBoolean() == true) {
            dependsOn("javadocJar")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "meteor-client"

            version = "${libs.versions.minecraft.get()}-SNAPSHOT"
        }
    }

    repositories {
        maven("https://maven.meteordev.org/snapshots") {
            name = "meteor-maven"

            credentials {
                username = System.getenv("MAVEN_METEOR_ALIAS")
                password = System.getenv("MAVEN_METEOR_TOKEN")
            }

            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
