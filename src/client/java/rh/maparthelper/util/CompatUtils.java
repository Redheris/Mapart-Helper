package rh.maparthelper.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CompatUtils {
    public static void sendMessage(Player player, Component message, boolean overlay) {
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
}
