package rh.maparthelper.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.player.Player;

//? if >=1.21.5
//import java.net.URI;

public class CompatUtils {
    public static void sendMessage(Player player, Component message, boolean overlay) {
        Minecraft.getInstance().execute(() -> {
            player.displayClientMessage(message, overlay);
        });
    }

    public static ClickEvent createClickEvent(ClickEvent.Action action, String value) {
        //? if >=1.21.5 {
        /*return switch (action) {
            case OPEN_FILE -> new ClickEvent.OpenFile(value);
            case OPEN_URL -> new ClickEvent.OpenUrl(URI.create(value));
            case COPY_TO_CLIPBOARD -> new ClickEvent.CopyToClipboard(value);
            case null, default -> throw new UnsupportedOperationException();
        };
        *///?} else
        return new ClickEvent(action, value);
    }

    public static HoverEvent createShowTextHoverEvent(Component text) {
        //? if >=1.21.5 {
        /*return new HoverEvent.ShowText(text);
        *///?} else
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
    }
}
