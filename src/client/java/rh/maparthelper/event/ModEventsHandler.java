package rh.maparthelper.event;

public class ModEventsHandler {

    public static void registerAll() {
        MapartSelectionHandler.init();
        ClientTickHandler.init();
    }
}
