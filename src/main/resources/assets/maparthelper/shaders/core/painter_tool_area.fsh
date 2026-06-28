#version 150

#moj_import <minecraft:globals.glsl>

layout(std140) uniform PainterToolArea {
    vec2 ScaledMaskSize;
    bool MarchingAnts;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec4 pixelColor = texture(Sampler0, texCoord0);
    vec2 texel = ScaledMaskSize / float(textureSize(Sampler0, 0));
    float tx = 1 / ScaledMaskSize.x;
    float ty = 1 / ScaledMaskSize.y;

    float center = pixelColor.r;
    if (center < 0.5) {
        discard;
    }

    ivec2 pixel = ivec2(texCoord0 * ScaledMaskSize);
    float thickness = 1.5;


    bool left  = pixel.x < 1                                  || texture(Sampler0, texCoord0 + vec2(-tx, 0.0)).r < 0.5;
    bool right = pixel.x >= ScaledMaskSize.x - 1              || texture(Sampler0, texCoord0 + vec2( tx, 0.0)).r < 0.5;
    bool up    = pixel.y < 1                                  || texture(Sampler0, texCoord0 + vec2(0.0, -ty)).r < 0.5;
    bool down  = pixel.y >= ScaledMaskSize.y - 1              || texture(Sampler0, texCoord0 + vec2(0.0,  ty)).r < 0.5;

    if (left || right || up || down) {
        float coord;
        if (left)
            coord = pixel.y;
        else if (right)
            coord = -pixel.y;
        else if (down)
            coord = pixel.x;
        else
            coord = -pixel.x;

        if (MarchingAnts) {
            float t = floor(GameTime * 20000);
            float pattern = mod(floor(coord + t), 8);
            if (pattern < 4.0) {
                fragColor = vec4(0.0, 0.0, 0.0, 1.0);
            } else {
                fragColor = vec4(1.0, 1.0, 1.0, 1.0);
            }
        } else {
            if (mod(gl_FragCoord.x + gl_FragCoord.y, 4) < 2) {
                fragColor = vec4(0.0, 0.0, 0.0, 1.0);
            } else {
                fragColor = vec4(1.0, 1.0, 1.0, 1.0);
            }
        }
        return;
    }
    discard;
}