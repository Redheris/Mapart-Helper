import me.modmuss50.mpp.ReleaseType

plugins {
    id("dev.kikugie.stonecutter")
    id("me.modmuss50.mod-publish-plugin") version "2.0.0"
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
    dryRun = property("publish.dry_run") == "true" || env.GITHUB_TOKEN.orElse("") == ""

    github {
        accessToken = env.GITHUB_TOKEN.orElse("")
        repository = property("publish.github_repo") as String
        commitish = "main"
        tagName = version

        allowEmptyFiles = true
    }
}

stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
    order("publishGithub")
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
        string(current.parsed >= "26.1") {
            replace("world.WorldRenderEvents", "level.LevelRenderEvents")
            replace("WorldRenderEvents.AFTER_ENTITIES", "LevelRenderEvents.COLLECT_SUBMITS")
            replace("WorldRenderEvents", "LevelRenderEvents")

            replace("keybinding.v1.KeyBindingHelper", "keymapping.v1.KeyMappingHelper")
            replace("KeyBindingHelper.registerKeyBinding", "KeyMappingHelper.registerKeyMapping")

            replace("GuiGraphics", "GuiGraphicsExtractor")

            replace("net.minecraft.client.gui.render.state.", "net.minecraft.client.renderer.state.gui.")
            replace(".command.v2.ClientCommandManager", ".command.v2.ClientCommands")
            replace("SpecialGuiElementRegistry", "PictureInPictureRendererRegistry")
        }

        string(current.parsed >= "1.21.10", "widget_events") {
            replace(
                "mouseReleased(double mouseX, double mouseY, int button)",
                "mouseReleased(@NotNull MouseButtonEvent mouseEvent)"
            )
            replace(
                "onRelease(double mouseX, double mouseY)",
                "onRelease(@NotNull MouseButtonEvent mouseEvent)"
            )
            replace(
                "mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)",
                "mouseDragged(@NotNull MouseButtonEvent mouseEvent, double dragX, double dragY)"
            )
            replace(
                "onClick(double mouseX, double mouseY)",
                "onClick(@NotNull MouseButtonEvent mouseEvent, boolean isDoubleClick)"
            )
            replace(
                "updateScrolling(double mouseX, double mouseY, int button)",
                "updateScrolling(@NotNull MouseButtonEvent mouseEvent)"
            )
            replace(
                "(double mouseX, double mouseY, int button)",
                "(@NotNull MouseButtonEvent mouseEvent, boolean isDoubleClick)"
            )
            replace("mouseClicked(mouseX, mouseY, button)", "mouseClicked(mouseEvent, isDoubleClick)")
            replace("mouseReleased(mouseX, mouseY, button)", "mouseReleased(mouseEvent)")
            replace("mouseDragged(mouseX, mouseY, button, dragX, dragY)", "mouseDragged(mouseEvent, dragX, dragY)")
            replace("mouseX", "mouseEvent.x()")
            replace("mouseY", "mouseEvent.y()")
            replace("isValidClickButton(button)", "isValidClickButton(mouseEvent.buttonInfo())")
            replace("button", "mouseEvent.button()")
            replace("(int keyCode, int scanCode, int modifiers)", "(@NotNull KeyEvent keyEvent)")
            replace("(keyCode, scanCode, modifiers)", "(keyEvent)")
            replace("keyCode == 257 || keyCode == 335", "keyEvent.isConfirmation()")
            replace("keyCode", "keyEvent.key()")
        }

        // gui_rendering

        string(current.parsed >= "26.1", "gui_rendering") {
            replace("renderWidget(", "extractWidgetRenderState(")
            replace("renderBackground(", "extractBackground(")
            replace("renderBlurredBackground(", "extractBlurredBackground(")
            replace("renderMenuBackground(", "extractMenuBackground(")
            replace("renderContents(", "extractContents(")
            replace("renderDefaultLabel(", "extractDefaultLabel(")
            replace("renderScrollbar(", "extractScrollbar(")

            replace("render(", "extractRenderState(")

            replace("graphics.hLine(", "graphics.horizontalLine(")
            replace("graphics.vLine(", "graphics.verticalLine(")
            replace("scrollbarVisible()", "scrollable()")
        }
    }
}