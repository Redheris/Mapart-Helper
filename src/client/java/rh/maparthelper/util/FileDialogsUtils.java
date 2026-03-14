package rh.maparthelper.util;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.imageio.ImageIO;
import java.util.function.Consumer;

public class FileDialogsUtils {
    public static void openImageImportDialog(Consumer<String> pathConsumer) {
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
}
