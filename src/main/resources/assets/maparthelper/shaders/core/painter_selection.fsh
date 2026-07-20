#version 150

#moj_import <minecraft:globals.glsl>

layout(std140) uniform PainterSelection {
    ivec2 ScaledSize;
    vec4 SelectionFillColor;
    bool FillSelection;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec2 texel = 1.0 / ScaledSize;

    float center = texture(Sampler0, texCoord0).r;
    if (center < 0.5) {
        discard;
    }

    ivec2 pixel = ivec2(texCoord0 * ScaledSize);

    bool left  = pixel.x == 0                || texture(Sampler0, texCoord0 + vec2(-texel.x, 0.0)).r < 0.5;
    bool right = pixel.x == ScaledSize.x - 1 || texture(Sampler0, texCoord0 + vec2( texel.x, 0.0)).r < 0.5;
    bool up    = pixel.y == 0                || texture(Sampler0, texCoord0 + vec2(0.0, -texel.y)).r < 0.5;
    bool down  = pixel.y == ScaledSize.y - 1 || texture(Sampler0, texCoord0 + vec2(0.0,  texel.y)).r < 0.5;

    if (left || right || up || down) {
        float t = floor(GameTime * 20000);
        vec2 p = texCoord0 / texel;

        float coord;
        if (left)
            coord = p.y;
        else if (right)
            coord = -p.y;
        else if (down)
            coord = p.x;
        else
            coord = -p.x;
        float pattern = mod(floor(coord + t), 8);

        if (pattern < 4.0) {
            fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        } else {
            fragColor = vec4(1.0, 1.0, 1.0, 1.0);
        }
        return;
    }

    if (FillSelection) {
        fragColor = SelectionFillColor;
        return;
    }

    discard;
}