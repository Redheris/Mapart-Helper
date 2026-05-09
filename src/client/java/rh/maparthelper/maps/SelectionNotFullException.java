package rh.maparthelper.maps;

public class SelectionNotFullException extends RuntimeException {
    public SelectionNotFullException() {
        super("Some item frames in the selection are absent or without a filled map");
    }
}
