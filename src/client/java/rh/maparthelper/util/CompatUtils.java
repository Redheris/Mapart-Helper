package rh.maparthelper.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

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

    //~ if >=1.21.10 'getWindow().getWindow()' -> 'getWindow()' {
    public static boolean hasControlDown() {
        return ON_OSX
                ? InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 343)
                  || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 347)
                : InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 341)
                  || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 345);
    }

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
    }

    public static boolean hasAltDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 342)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 346);
    }

    public static boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
    }
    //~}
}
