#version 150

layout(std140) uniform DottedGrid {
    ivec2 ScreenSize;
    ivec2 ScaledSize;
    ivec2 StartPos;
    vec4 Color1;
    vec4 Color2;
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

    // I didn't manage to fix rare 2px lines with math
    bool raceCheckX = fract((localX - 1) / texelW) * texelW < 1;
    bool raceCheckY = fract((localY - 1) / texelH) * texelH < 1;

    if (!raceCheckX && fract(localX / texelW) * texelW < 1 || !raceCheckY && fract(localY / texelH) * texelH < 1) {
        if (mod(localX + localY, 2) < 1) {
            fragColor = mix(originalColor, Color1, Color1.a);
        } else {
            fragColor = mix(originalColor, Color2, Color2.a);
        }
        fragColor.a = 1;
    } else {
        fragColor = originalColor;
    }
}