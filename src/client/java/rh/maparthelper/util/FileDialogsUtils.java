package rh.maparthelper.util;

import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

//? if <=26.2 {
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.lwjgl.PointerBuffer;
//?} else if >=26.3 {
/*import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.system.MemoryUtil;
*///?}

public class FileDialogsUtils {
    public static void openImageImportDialog(Consumer<String> pathConsumer) {
        //? if >=26.3 {
        /*sdl(pathConsumer);
        *///?} else
        tinyfd(pathConsumer);
    }

    //? if >=26.3 {
    /*private static void sdl(Consumer<String> pathConsumer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_DialogFileFilter.Buffer filters = SDL_DialogFileFilter.malloc(1, stack);
            filters.name(stack.UTF8("Image files"));
            filters.pattern(stack.UTF8(String.join(";", ImageIO.getReaderFileSuffixes())));

            SDLDialog.SDL_ShowOpenFileDialog(
                    (userdata, filelist, filter1) -> {
                        long ptr = MemoryUtil.memGetAddress(filelist);

                        if (ptr == MemoryUtil.NULL) {
                            return;
                        }

                        String path = MemoryUtil.memUTF8(ptr);
                        pathConsumer.accept(path);
                    },
                    MemoryUtil.NULL,
                    Minecraft.getInstance().getWindow().handle(),
                    filters,
                    (ByteBuffer) null,
                    false
            );
        }
    }
    *///?} else {
    private static void tinyfd(Consumer<String> pathConsumer) {
        new Thread(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                String[] readerFileSuffixes = ImageIO.getReaderFileSuffixes();
                PointerBuffer filters = stack.mallocPointer(readerFileSuffixes.length);
                for (String suffix : readerFileSuffixes)
                    filters.put(stack.UTF8("*." + suffix));
                filters.flip();

                String path = TinyFileDialogs.tinyfd_openFileDialog(
                        "Import Image",
                        null,
                        filters,
                        "Image files",
                        false
                );

                if (path != null) {
                    pathConsumer.accept(path);
                }
            }
        }).start();
    }
    //?}
}
