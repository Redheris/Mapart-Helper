#version 150

layout(std140) uniform ColorsHighlight {
    vec4 color1;
    vec4 color2;
    vec4 color3;
    vec4 highlight;
};

uniform sampler2D Sampler0;

in vec2 texCoord0;
out vec4 fragColor;

const float tolerance = 0.001;

bool colorEquals(vec4 a, vec4 b, float tol) {
    return abs(a.r - b.r) < tol &&
    abs(a.g - b.g) < tol &&
    abs(a.b - b.b) < tol;
}

void main() {
    vec4 c = texture(Sampler0, texCoord0);

    if (colorEquals(c, color1, tolerance)
        || colorEquals(c, color2, tolerance)
        || colorEquals(c, color3, tolerance)) {
        fragColor = highlight;
    } else {
        fragColor = c;
    }
}
