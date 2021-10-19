#version 150
in vec2 inPosition; // input from the vertex buffer

uniform mat4 view;
uniform mat4 projection;

uniform int solid;
uniform vec3 lightPosition;

out vec2 texCoord;
out vec3 normal;
out vec3 light;

const float PI = 3.14159;

vec3 getSphere(vec2 pos) {
    float az = pos.x * PI; // souřadnice X je <-1; 1> a chceme v rozsahu <-PI; PI>
    float ze = pos.y * PI / 2; // souřadnice Y je <-1; 1> a chceme v rozsahu <-PI/2; PI/2>
    float r = 1;

    float x = r * cos(az) * cos(ze);
    float y = 2 * r * sin(az) * cos(ze);
    float z = 0.5 * r * sin(ze);

    return vec3(x, y, z);
}

vec3 getSphereNormalWithDerivation(vec2 pos) {
    // x = cos(x * PI) * cos(y * PI / 2)
    // y = 2 * sin(x * PI) * cos(y * PI / 2)
    // z = 0.5 * sin(y * PI / 2)
    float az = pos.x * PI;
    float ze = pos.y * PI / 2;

    vec3 u = vec3(-sin(az) * cos(ze) * PI, cos(az) * cos(ze) * PI, 0);
    vec3 v = vec3(cos(az) * -sin(ze) * PI / 2, sin(az) * -sin(ze) * PI / 2, cos(ze) * PI / 2);
    return cross(u, v);
}

vec3 getSphereNormal(vec2 pos) {
    vec3 u = getSphere(pos + vec2(0.001, 0)) - getSphere(pos - vec2(0.001, 0));
    vec3 v = getSphere(pos + vec2(0, 0.001)) - getSphere(pos - vec2(0, 0.001));
    return cross(u, v);
}

vec3 getPlane(vec2 pos) {
    return vec3(pos * 3.0, -1.0);
}

vec3 getPlaneNormal(vec2 pos) {
    vec3 u = getPlane(pos + vec2(0.001, 0)) - getPlane(pos - vec2(0.001, 0));
    vec3 v = getPlane(pos + vec2(0, 0.001)) - getPlane(pos - vec2(0, 0.001));
    return cross(u, v);
}

float getZ(vec2 pos) {
    return sin(pos.x * 5);
}

void main() {
    vec2 position = inPosition * 2 - 1;// inPosition <0; 1> | position <-1; 1>
    texCoord = inPosition;

    vec3 pos3;
    if (solid == 1) {
        pos3 = getSphere(position);
        normal = getSphereNormalWithDerivation(position);
    } else if (solid == 2) {
        pos3 = getPlane(position);
        normal = getPlaneNormal(position);
    }

    gl_Position = projection * view * vec4(pos3, 1.0);

    light = lightPosition - pos3;
} 
