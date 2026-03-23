pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9"
}

stonecutter {
    create(rootProject) {
        // See https://stonecutter.kikugie.dev/wiki/start/#choosing-minecraft-versions
        versions("1.21.8", "1.21.11").buildscript("build.fabric_remap.gradle.kts")
//        version("26.1", "26.1-rc-3").buildscript("build.fabric.gradle.kts")
        vcsVersion = "1.21.8"
    }
}

rootProject.name = "Mapart Helper"