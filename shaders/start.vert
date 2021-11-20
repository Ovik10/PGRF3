#version 150
in vec2 inPosition;
out float intensity;
out vec2 posIO;
out vec3 normalIO;
out vec3 lightDirection;
out vec3 viewDirection;
out vec3 vertColor;
out vec4 objPos;
uniform vec3 lightPos;
uniform vec3 viewPos;
uniform mat4 model;
uniform mat4 view;
uniform mat4 proj;
uniform int objectType;
uniform int lightModelType;
uniform vec3 eyePosition;
uniform float time;
out vec4 depthTextureCoord;
const float PI = 3.1415926;



// kuzel
vec3 getCone(vec2 vec) {
float x, y, z;
if (time < 30) {
    float t = sqrt(vec.x * 1)* time/30;
    float s = vec.y * 2 * PI* time/30;

    float x = t*cos(s) + time/10;
    float y = t*sin(s);
    float z = t;

    return vec3(x, y, z);
    } else {
    float t = sqrt(vec.x * 1);
        float s = vec.y * 2 * PI;

        float x = t*cos(s)+time/10;
        float y = t*sin(s);
        float z = t;

        return vec3(x, y, z);
    }
}

// sloni hlava
vec3 getElephantHead(vec2 vec) {
float x, y, z;
 if (time < 30) {
    float azimut = vec.x * 2 * PI * time/30;
    float zenit = vec.y * PI * time/30;
    float r = 3+cos(4*azimut);

    float x = r*cos(azimut)*cos(zenit);
    float y = r*sin(azimut)*cos(zenit);
    float z = r*sin(zenit);
        return vec3(x, y, z);

   } else {
    float azimut = vec.x * 2 * PI;
        float zenit = vec.y * PI;
        float r = 3+cos(4*azimut);

        float x = r*cos(azimut)*cos(zenit);
        float y = r*sin(azimut)*cos(zenit);
        float z = r*sin(zenit);
        return vec3(x, y, z);
}


}
// V
float getFValue(vec2 vec){
    if (objectType==0) {
        return 0;
    } else { // V
        return -(vec.x*vec.x*3+vec.y*vec.y*3);
    }
}

// polokoule
vec3 getTrampoline(vec2 vec){
    float zenit = vec.x * PI;
    float azimut = vec.y * PI;
    float r = 2 + sin(zenit + azimut);

    float x = sin(zenit)*cos(azimut);
    float y = sin(azimut)*sin(zenit);
    float z = cos(zenit);

    return vec3(x*3, y*3, z*3);
}

// sombrero
vec3 getSombrero(vec2 vec) {
    float azimut = vec.x * 2 * PI;
    float r = vec.y * 2 * PI;
    float v = 2 * sin(r);

    float x = r * cos(azimut);
    float y = r * sin(azimut);
    float z = v;

    return vec3(x, y, z);
}

// Donut
vec3 getDonut(vec2 vec) {
    	float zenit = vec.x * 2.0 * PI;
    	float azimut = vec.y * 2.0 * PI;

    	float x = 3 * cos(azimut)+cos(zenit)*cos(azimut);
    	float y = 3 * sin(azimut)+cos(zenit)*sin(azimut);
    	float z = sin(zenit);

    	return vec3(x, y, z);
}

// koule - svetlo
vec3 getSphere(vec2 vec) {
    float azimut = vec.x * PI *2;
    float zenit = vec.y * PI;
    float r = 1;

    float x = r*cos(azimut)*cos(zenit);
    float y = r*sin(azimut)*cos(zenit);
    float z = r*sin(zenit);
    return vec3(x, y, z);
}

vec3 getNormal(vec2 pos){
    float delta = 0.01;
    vec3 u = vec3(pos.x + delta, pos.y, getFValue(pos + vec2(delta, 0)))
    - vec3(pos - vec2(delta, 0), getFValue(pos - vec2(delta, 0)));
    vec3 v = vec3(pos + vec2(0, delta), getFValue(pos + vec2(0, delta)))
    - vec3(pos - vec2(0, delta), getFValue(pos - vec2(0, delta)));
    return cross(u, v);
}

void main() {
    // prirazeni objektu
    posIO = inPosition;
    vec2 position = inPosition - 0.5;
        if (objectType < 2) {
            float z = getFValue(position.xy);
            objPos = vec4(position.x, position.y, z, 1.0);
        } else if (objectType == 2) {
            objPos = vec4(getSphere(position), 1.0);
        } else if (objectType == 3) {
            objPos = vec4(getCone(position), 1.0);
        } else if (objectType == 4) {
            objPos = vec4(getElephantHead(position), 1.0);
        } else if (objectType == 5){
            objPos = vec4(getTrampoline(position), 1.0);
        } else if (objectType == 6){
            objPos = vec4(getSombrero(inPosition), 1.0);
        } else if (objectType == 7) {
            objPos = vec4(getDonut(position), 1.0);
        }
    objPos = model * objPos;

    vec3 normal;
    // blinn-phong
        lightDirection = normalize((mat3(view) * lightPos).xyz - (view * objPos).xyz);
        viewDirection = normalize((mat3(view) * viewPos).xyz - (view * objPos).xyz);
        normal = normalize(getNormal(position.xy));
        normal = inverse(transpose(mat3(view * model))) * normal;
        normalIO = normal;

    gl_Position = proj * view * objPos;




}
