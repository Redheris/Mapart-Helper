package rh.maparthelper.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class Utils {

    public static String makeUniqueFilename(Predicate<String> existingTest, String filename, String ext, String nameFormat) {
        if (existingTest.test(filename + "." + ext)) {
            int suffix = 1;
            while (existingTest.test(String.format(nameFormat, filename, suffix) + "." + ext))
                suffix++;
            return String.format(nameFormat, filename, suffix) + "." + ext;
        }
        return filename + "." + ext;
    }

    public static String makeUniqueFilename(Path dir, String filename, String ext, String nameFormat) {
        return makeUniqueFilename(f -> Files.exists(dir.resolve(f)), filename, ext, nameFormat);
    }

    public static String makeUniqueFilename(Path dir, String filename, String ext) {
        return makeUniqueFilename(dir, filename, ext, "%s (%d)");
    }
}
