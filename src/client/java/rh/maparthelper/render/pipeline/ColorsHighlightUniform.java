package rh.maparthelper.render.pipeline;

public class ColorsHighlightUniform {
//    public static final int SIZE = new Std140SizeCalculator()
//            .putVec4()
//            .putVec4()
//            .putVec4()
//            .putVec4()
//            .get();
//    public static final GpuBuffer BUFFER = RenderSystem.getDevice().createBuffer(() -> "ColorsHighlight", 136, SIZE);
//
//    public static void set(int color1, int color2, int color3, int colorHighlight) {
//        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
//            ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, SIZE)
//                    .putVec4(intToVec4(color1))
//                    .putVec4(intToVec4(color2))
//                    .putVec4(intToVec4(color3))
//                    .putVec4(intToVec4(colorHighlight))
//                    .get();
//            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(BUFFER.slice(), byteBuffer);
//        }
//    }
//
//    private static Vector4f intToVec4(int colorARGB) {
//        int a = (colorARGB >> 24) & 0xFF;
//        int r = (colorARGB >> 16) & 0xFF;
//        int g = (colorARGB >> 8) & 0xFF;
//        int b = (colorARGB) & 0xFF;
//        return new Vector4f(r / 255f, g / 255f, b / 255f, a / 255f);
//    }
}
