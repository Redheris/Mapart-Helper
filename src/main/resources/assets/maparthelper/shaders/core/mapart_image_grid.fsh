#version 150

layout(std140) uniform MapartImageGrid {
    ivec2 ScreenSize;
    ivec2 ScaledSize;
    ivec2 StartPos;
    vec4 ColorPixel1;
    vec4 ColorPixel2;
    vec4 ColorMap;
    bool PixelsGrid;
    bool MapsGrid;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec4 originalColor = texture(Sampler0, texCoord0);
    ivec2 originalSize = textureSize(Sampler0, 0);

    float texelW = ScaledSize.x / float(originalSize.x);
    float texelH = ScaledSize.y / float(originalSize.y);

    float localX = gl_FragCoord.x - StartPos.x;
    float localY = ScreenSize.y - gl_FragCoord.y - StartPos.y;

    if (MapsGrid) {
        float mapWidth = 128 * ScaledSize.x / originalSize.x;
        float mapHeight = 128 * ScaledSize.y / originalSize.y;

        bool verticalMapLine = fract(localX / mapWidth) * mapWidth < 2;
        bool horizontalMapLine = fract(localY / mapHeight) * mapHeight < 2;

        bool nonBorderVertical = verticalMapLine && (localX > texelW && localX < ScaledSize.x - texelW);
        bool nonBorderHorizontal = horizontalMapLine && (localY > texelH && localY < ScaledSize.y - texelH);

        if (nonBorderVertical || nonBorderHorizontal) {
            fragColor = mix(originalColor, ColorMap, ColorMap.a);
            return;
        }
    }

    if (PixelsGrid) {
        // I didn't manage to fix rare 2px lines with math
        bool raceCheckX = fract((localX - 1) / texelW) * texelW < 1;
        bool raceCheckY = fract((localY - 1) / texelH) * texelH < 1;

        float distToLineX = fract(localX / texelW) * texelW;
        float distToLineY = fract(localY / texelH) * texelH;

        if ((!raceCheckX && distToLineX < 1 || !raceCheckY && distToLineY < 1)) {
            if (mod(localX + localY, 2) < 1) {
                fragColor = mix(originalColor, ColorPixel1, ColorPixel1.a);
            } else {
                fragColor = mix(originalColor, ColorPixel2, ColorPixel2.a);
            }
            fragColor.a = 1;
            return;
        }
    }

    fragColor = originalColor;
}