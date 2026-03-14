package rh.maparthelper.gui.input;

import java.util.function.Predicate;

public class TextFieldPredicates {
    public static Predicate<String> positiveInt() {
        return integer(1, null);
    }

    public static Predicate<String> rangeInt(int min, int max) {
        return integer(min, max);
    }

    private static Predicate<String> integer(Integer min, Integer max) {
        return s -> {
            if (s.isEmpty()) return true;
            if (s.equals("-")) return min == null || min < 0;
            try {
                int i = Integer.parseInt(s);
                return (min == null || i >= min) && (max == null || i <= max);
            } catch (NumberFormatException e) {
                return false;
            }
        };
    }

    public static Predicate<String> validPathName() {
        return s -> !s.matches(".*[<>:\"/|?*\\\\].*");
    }
}
