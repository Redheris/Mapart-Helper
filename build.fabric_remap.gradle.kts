@file:Suppress("UnstableApiUsage")

import me.modmuss50.mpp.ReleaseType

plugins {
    id("net.fabricmc.fabric-loom-remap")
    id("me.modmuss50.mod-publish-plugin")
    // `maven-publish`
}

version = "${property("mod.version")}+mc${sc.current.version}"
base.archivesName = property("mod.id") as String
val versionTypeRaw = property("mod.version_type") as String

val modIcon = property("publish.mod_icon_url") as String
val modName = property("mod.name") as String
val modVersion = property("mod.version") as String

val requiredJava = when {
    // You don't want to run this buildscript for >=26.1
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}
val versionType: ReleaseType = when {
    versionTypeRaw.lowercase() == "stable" -> ReleaseType.STABLE
    versionTypeRaw.lowercase() == "beta" -> ReleaseType.BETA
    else -> ReleaseType.ALPHA
}

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")

    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }
    maven("https://maven.gegy.dev/releases/") {
        name = "Gegy"
    }
    maven("https://maven.isxander.dev/releases") {
        name = "Xander Maven"
    }
    maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${project.property("parchment")}@zip")
        if (sc.current.parsed < "1.21.11")
            mappings("dev.lambdaurora:yalmm-mojbackward:${project.property("mojbackward")}")
    })
    modImplementation("net.fabricmc:fabric-loader:${property("mod.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api")}")

    modImplementation("dev.isxander:yet-another-config-lib:${property("yacl")}")
    modApi("com.terraformersmc:modmenu:${property("modmenu")}")
}

loom {
    splitEnvironmentSourceSets()

    mods {
        create("${property("mod.id")}") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }

    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection
// For mods with access wideners
//    accessWidenerPath = rootProject.file("src/main/resources/aw/${sc.current.version}.classtweaker")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks {
    val mixinJava = "JAVA_${requiredJava.majorVersion}"
    processResources {
        // Excluding unnecessary AW files from adding to the jar
//        exclude("aw/**")
//        val awFile = loom.accessWidenerPath.asFile.orNull
//        if (awFile != null) {
//            from(awFile.parentFile) {
//                include(awFile.name)
//                rename(awFile.name, "${project.property("mod.id")}.classtweaker")
//            }
//        }

        // Add LICENSE_Mod-name file into the jar
        from(rootProject.file("LICENSE")) {
            rename(
                "LICENSE", "LICENSE_${
                    project.property("mod.name").toString().replace(" ", "-")
                }"
            )
        }

        val mcDep = (project.findProperty("mc_dep_fabric") ?: project.property("mc_dep")) as String
        inputs.property("id", project.property("mod.id"))
        inputs.property("name", project.property("mod.name"))
        inputs.property("version", project.property("mod.version"))
        inputs.property("fabric_loader", project.property("mod.fabric_loader"))
        inputs.property("minecraft", mcDep)

        val props = mapOf(
            "id" to project.property("mod.id"),
            "name" to project.property("mod.name"),
            "version" to project.property("mod.version"),
            "fabric_loader" to project.property("mod.fabric_loader"),
            "minecraft" to mcDep
        )

        filesMatching("fabric.mod.json") { expand(props) }
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }
    named<ProcessResources>("processClientResources") {
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}

// Publishes builds to Modrinth, Curseforge and GitHub with changelog from the CHANGELOG.md file
publishMods {
    file = tasks.remapJar.map { it.archiveFile.get() }
// Adds sources jar
//    additionalFiles.from(tasks.remapSourcesJar.map { it.archiveFile.get() })
    displayName = "${property("mod.name")} ${property("mod.version")} for ${property("release_title")}"
    version = property("mod.version") as String
    changelog = rootProject.file("CHANGELOG.md").readText()
    val changelogSimple = changelog.get()
        .replace("<summary>", "### ")
        .replace(Regex("(?m)^\\s*<details>\\s*"), "\n")
        .replace(Regex("(?m)^\\s*</details>\\s*"), "")
        .replace(Regex("<.*?>"), "")
    type = versionType
    modLoaders.add("fabric")

    dryRun = property("publish.dry_run") == "true"

    if (dryRun.get() || env.MODRINTH_TOKEN.orElse("") != "") {
        modrinth {
            projectId = property("publish.modrinth") as String
            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
            accessToken = env.MODRINTH_TOKEN.orElse("")
            minecraftVersions.addAll(property("mc_targets").toString().split(' '))
            requires {
                slug = "fabric-api"
                slug = "yacl"
            }
            optional {
                slug = "modmenu"
            }
            announcementTitle = "Modrinth"
        }
    }

    if (dryRun.get() || env.CURSEFORGE_TOKEN.orElse("") != "") {
        curseforge {
            changelog = changelogSimple
            projectId = property("publish.curseforge_id") as String
            projectSlug = property("publish.curseforge_slug") as String
            accessToken = env.CURSEFORGE_TOKEN.orElse("")
            minecraftVersions.addAll(property("mc_targets").toString().split(' '))
            requires {
                slug = "fabric-api"
                slug = "yacl"
            }
            optional {
                slug = "modmenu"
            }
            announcementTitle = "CurseForge"
        }
    }

    github {
        accessToken = env.GITHUB_TOKEN.orElse("")
        parent(rootProject.tasks.named("publishGithub"))
        announcementTitle = "GitHub"
    }

    if (sc.current.version == property("publish.announce_for")) {
        discord {
            webhookUrl = env.DISCORD_WEBHOOK.orElse("")
            dryRunWebhookUrl = env.DRY_RUN_DISCORD_WEBHOOK.orElse("")
            username = "Release publisher"
            avatarUrl = "https://i.imgur.com/NiTaw1Z.png"
            content = changelog.map { "# $modName v$modVersion\n$changelogSimple" }
            style {
                look = "MODERN"
                thumbnailUrl = modIcon
//				link = "BUTTON" // No buttons for non-application webhooks o7
                color = "#7419CA"
            }
        }
    }
}


/*
// Publishes builds to a maven repository under `com.example:template:0.1.0+mc`
publishing {
    repositories {
        maven("https://maven.example.com/releases") {
            name = "myMaven"
            // To authenticate, create `myMavenUsername` and `myMavenPassword` properties in your Gradle home properties.
            // See https://stonecutter.kikugie.dev/wiki/tips/properties#defining-properties
            credentials(PasswordCredentials::class.java)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${property("mod.group")}.${property("mod.id")}"
            artifactId = property("mod.id") as String
            version = project.version

            from(components["java"])
        }
    }
}
 */
