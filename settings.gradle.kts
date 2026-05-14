pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9"
    id("dev.kikugie.loom-back-compat") version "0.2"
}

stonecutter {
    create(rootProject) {
        // See https://stonecutter.kikugie.dev/wiki/start/#choosing-minecraft-versions
        fun fabric(vararg versions: String) {
            versions(versions.toList()).buildscript("build.fabric.gradle.kts")
        }
        fabric("1.21.4", "1.21.5")
        vcsVersion = "1.21.4"
    }
}

rootProject.name = "Mapart Helper"