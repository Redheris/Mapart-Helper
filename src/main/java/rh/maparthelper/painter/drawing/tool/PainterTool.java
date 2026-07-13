package rh.maparthelper.painter.drawing.tool;

import rh.maparthelper.painter.drawing.DrawingContext;
import rh.maparthelper.painter.history.action.HistoryAction;

public interface PainterTool {

    /**
     * Starts drawing process. Used to initialize tool's state and start collecting data to save to the action history
     *
     * @param drawingContext Context containing data for drawing, such as visited pixels
     * @param x              Surface's {@code x} coordinate
     * @param y              Surface's {@code y} coordinate
     * @param lineX          Closest vertical line (for even shapes)
     * @param lineY          Closest horizontal line (for even shapes)
     * @param firstColor     Main ARGB color
     * @param secondColor    Secondary ARGB color
     */
    void start(DrawingContext drawingContext, int x, int y, int lineX, int lineY, int firstColor, int secondColor);

    /**
     * Processes drawing. If isDrawing(), makes changes to the surface and collects data to save to the action history if needed
     *
     * @param drawingContext Context containing data for drawing, such as visited pixels
     * @param x              Surface's {@code x} coordinate
     * @param y              Surface's {@code y} coordinate
     * @param lineX          Closest vertical line (for even shapes)
     * @param lineY          Closest horizontal line (for even shapes)
     * @param firstColor     Main ARGB color
     * @param secondColor    Secondary ARGB color
     */
    void process(DrawingContext drawingContext, int x, int y, int lineX, int lineY, int firstColor, int secondColor);

    /**
     * Finishes drawing process with further saving to the action history
     *
     * @return Specific implementation of the {@link HistoryAction}
     */
    HistoryAction submit();

    /**
     * Cancels drawing process without saving to the action history
     */
    void cancel();

    /**
     * @return Whether the tool is in drawing state, i.e. the tool has been started, and has not yet been submitted or canceled
     */
    boolean isDrawing();
}
