package rh.maparthelper.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;

//? if >=26.3
//import com.mojang.blaze3d.Blaze3D;

public class CompatUtils {
    public static final boolean ON_OSX = Util.getPlatform() == Util.OS.OSX;

    public static void sendMessage(@NotNull Player player, Component message, boolean overlay) {
        Minecraft.getInstance().execute(() -> {
            //? if >=26.1 {
            /*if (overlay)
                player.sendOverlayMessage(message);
            else
                player.sendSystemMessage(message);
            *///?} else
            player.displayClientMessage(message, overlay);
        });
    }

    public static boolean hasControlDown() {
        return ON_OSX ?
                //? if >= 26.3 {
                /*isKeyDown(InputConstants.KEY_LGUI) || isKeyDown(InputConstants.KEY_RGUI)
                *///?} else if >=1.21.11 {
                /*isKeyDown(InputConstants.KEY_LSUPER) || isKeyDown(InputConstants.KEY_RSUPER)
                *///?} else
                isKeyDown(InputConstants.KEY_LWIN) || isKeyDown(InputConstants.KEY_RWIN)
                : isKeyDown(InputConstants.KEY_LCONTROL) || isKeyDown(InputConstants.KEY_RCONTROL);
    }

    public static boolean hasShiftDown() {
        return isKeyDown(InputConstants.KEY_LSHIFT) || isKeyDown(InputConstants.KEY_RSHIFT);
    }

    public static boolean hasAltDown() {
        return isKeyDown(InputConstants.KEY_LALT) || isKeyDown(InputConstants.KEY_RALT);
    }

    public static boolean isKeyDown(int key) {
        //? if >=26.3 {
        /*return InputConstants.isKeyDown(key);
         *///?} else {
        //~ if >=1.21.10 'getWindow().getWindow()' -> 'getWindow()'
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
        //?}
    }

    public static void openPath(@NotNull Path path) {
        //~ if >=26.3 'Util.getPlatform()' -> 'Blaze3D'
        Util.getPlatform().openPath(path);
    }

    public static void openFile(@NotNull File file) {
        openPath(file.toPath());
    }

}
