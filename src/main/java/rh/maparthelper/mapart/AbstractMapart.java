package rh.maparthelper.mapart;

public abstract class AbstractMapart {
//    protected MapSegment[] mapSegments;
    protected ColorsCounter colorsCounter = new ColorsCounter();

    public String mapartName = "New mapart";
    protected int width = 1;
    protected int height = 1;

    protected final CroppingFrame croppingFrame = new CroppingFrame();
    protected int insertionX = 0;
    protected int insertionY = 0;

    public void clearColorCounters() {
//        if (mapSegments == null) return;
//        for (MapSegment mapSegment : mapSegments) {
//            mapSegment.getColorsCounter().clear();
//        }
        colorsCounter.clear();
    }

    public ColorsCounter getColorsCounter() {
//        if (mapSegments == null) return new ColorsCounter();
//        ColorsCounter[] counters = Arrays.stream(mapSegments)
//                .map(MapSegment::getColorsCounter)
//                .toArray(ColorsCounter[]::new);
//        return ColorsCounter.sum(counters);
        return colorsCounter;
    }

    public abstract int getOriginalWidth();

    public abstract int getOriginalHeight();

    public CroppingFrame getCroppingFrame() {
        return croppingFrame;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getInsertionX() {
        return insertionX;
    }

    public int getInsertionY() {
        return insertionY;
    }


    public class CroppingFrame {
        private int x = 0;
        private int y = 0;
        private int width = 1;
        private int height = 1;

        public int getX() {
            return x;
        }

        protected void setX(int x) {
            if (x < 0 || x > getOriginalWidth()) return;
            this.x = x;
        }

        public int getY() {
            return y;
        }

        protected void setY(int y) {
            if (y < 0 || y > getOriginalHeight()) return;
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        protected void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        protected void setHeight(int height) {
            this.height = height;
        }
    }
}
