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

    float left  = texture(Sampler0, texCoord0 + vec2(-texel.x, 0.0)).r;
    float right = texture(Sampler0, texCoord0 + vec2( texel.x, 0.0)).r;
    float up    = texture(Sampler0, texCoord0 + vec2(0.0, -texel.y)).r;
    float down  = texture(Sampler0, texCoord0 + vec2(0.0,  texel.y)).r;

    if (left < 0.5 || right < 0.5 || up < 0.5 || down < 0.5) {
        float t = floor(GameTime * 20000);
        vec2 p = texCoord0 / texel;

        float coord;
        if (left < 0.5)
            coord = p.y;
        else if (right < 0.5)
            coord = -p.y;
        else if (down < 0.5)
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