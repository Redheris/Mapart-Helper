package rh.maparthelper.event;

public class ModEventsHandler {

    public static void registerAll() {
        AttackBlockHandler.init();
        AttackEntityHandler.init();
        ClientTickHandler.init();
    }
}
