package rh.maparthelper.maps;

public class SelectionIsEmptyException extends RuntimeException {
    public SelectionIsEmptyException() {
        super("Selection is empty");
    }
}
