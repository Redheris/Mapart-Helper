import me.modmuss50.mpp.ReleaseType

plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom-remap") version "1.15-SNAPSHOT" apply false
    id("me.modmuss50.mod-publish-plugin") version "1.1.0"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
}

stonecutter active "1.21.8"

val versionTypeRaw = property("mod.version_type") as String
val versionType: ReleaseType = when {
    versionTypeRaw.lowercase() == "stable" -> ReleaseType.STABLE
    versionTypeRaw.lowercase() == "beta" -> ReleaseType.BETA
    else -> ReleaseType.ALPHA
}

publishMods {
    displayName = "${property("mod.name")} ${property("mod.version")}"
    changelog = rootProject.file("CHANGELOG.md").readText()
    version = property("mod.version") as String
    type = versionType
    dryRun = property("publish.dry_run") == "true"

    github {
        accessToken = env.GITHUB_TOKEN.value
        repository = property("publish.github_repo") as String
        commitish = "main"
        tagName = version

        allowEmptyFiles = true
    }
}

val announceVersion = property("publish.announce_for") as String

tasks.register("publishAnnouncementVersion") {
    group = "publishing"
    description = "Publishes the version accompanied by a Discord announcement if set"

    if (announceVersion != "none") {
        val publishTask = stonecutter.tasks.named("publishMods") {
            metadata.version == announceVersion
        }
        dependsOn(publishTask)
    }
}

tasks.register("publishModsAndAnnounce") {
    group = "publishing"
    description = "Publishes all versions from stonecutter and announce in Discord if set"

    dependsOn("publishMods")
    // This is a lazy collection containing all `publishMods` tasks from registered versions
    // DO NOT use `.get()` on it!
    dependsOn(stonecutter.tasks.named("publishMods") {
        metadata.version != announceVersion
    })
    dependsOn("publishAnnouncementVersion")
}


// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    swaps["mod_id"] = "\"${property("mod.id")}\";"
    swaps["mod_name"] = "\"${property("mod.name")}\";"
    constants["release"] = property("mod.id") != "template"
    dependencies["fapi"] = node.project.property("fabric_api") as String

    replacements {
        string(current.parsed > "26.0") {
            replace("GuiGraphics", "GuiGraphicsExtractor")
        }

        string(current.parsed >= "1.21.10", "widget_events") {
            replace(
                "(double mouseX, double mouseY, int button)",
                "(@NotNull MouseButtonEvent mouseEvent, boolean doubleClick)"
            )
            replace("mouseClicked(mouseX, mouseY, button)", "mouseClicked(mouseEvent, doubleClick)")
            replace("mouseX", "mouseEvent.x()")
            replace("mouseY", "mouseEvent.y()")
            replace("button", "mouseEvent.button()")
            replace("(int keyCode, int scanCode, int modifiers)", "(@NotNull KeyEvent keyEvent)")
            replace("keyCode == 257 || keyCode == 335", "keyEvent.isConfirmation()")
        }
        string(current.parsed > "26.0", "gui_rendering") {
            replace("renderWidget(", "extractWidgetRenderState(")
            replace("renderBackground(", "extractBackground(")
            replace("render(", "extractRenderState(")
            replace("context.hLine(", "context.horizontalLine(")
        }
    }
}
