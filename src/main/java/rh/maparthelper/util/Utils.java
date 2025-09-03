package rh.maparthelper.util;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class Utils {


    public static @NotNull String makeUniqueName(@NotNull Predicate<String> existingTest, String filename, String ext, String nameFormat) {
        String suf = ext == null ? "" : "." + ext;
        if (existingTest.test(filename + suf)) {
            int suffix = 1;
            while (existingTest.test(String.format(nameFormat, filename, suffix) + suf))
                suffix++;
            return String.format(nameFormat, filename, suffix) + suf;
        }
        return filename + suf;
    }

    public static @NotNull String makeUniqueFilename(Path dir, String filename, String ext, String nameFormat) {
        return makeUniqueName(f -> Files.exists(dir.resolve(f)), filename, ext, nameFormat);
    }

    public static @NotNull String makeUniqueFilename(Path dir, String filename, String ext) {
        return makeUniqueFilename(dir, filename, ext, "%s (%d)");
    }
}
