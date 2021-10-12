#version 150
in vec2 texCoord;

uniform sampler2D mosaic;

out vec4 outColor; // output from the fragment shader

void main() {
//    outColor = vec4(1.0, 0.0, 0.0, 1.0);

    outColor = vec4(texture(mosaic, texCoord).rgb, 1.0);
}
