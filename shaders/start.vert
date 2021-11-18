#version 150
in vec2 inPosition;
out float intensity;
out vec2 posIO;
out vec3 normalIO;
out vec3 lightDir;
out vec3 viewDir;
out vec3 vertColor;
out vec4 objPos;
uniform vec3 lightPos;
uniform vec3 viewPos;
uniform mat4 model;
uniform mat4 view;
uniform mat4 proj;
uniform int objectType;
uniform int lightModelType;
uniform float time;

// getter pro z hodnotu
float getFValue(vec2 vec){
    if (objectType==0) { //pro desku
        return 0;
    } else { //pro raketu
        return -(vec.x*vec.x*5+vec.y*vec.y*5);
    }
}

// kuzel - kartezske souradnice
vec3 getCone(vec2 vec) {
    float t = sqrt(vec.x * 1);
    float s = vec.y * 2 * 3.14;

    float x = t*cos(s);
    float y = t*sin(s);
    float z = t;

    return vec3(x, y, z);
}
// sombrero - cylindricke souradnice
vec3 getSombrero(vec2 vec) {
    float azimut = vec.x * 2 * 3.14;
    float r = vec.y * 2 * 3.14;
    float v = 2 * sin(r);

    float x = r * cos(azimut);
    float y = r * sin(azimut);
    float z = v;

    return vec3(x, y, z);
}
// sloni hlava - sfericke souradnice
vec3 getElephantHead(vec2 vec) {
    float azimut = vec.x * 2 * 3.14;
    float zenit = vec.y * 3.14;
    float r = 3+cos(4*azimut);

    float x = r*cos(azimut)*cos(zenit);
    float y = r*sin(azimut)*cos(zenit);
    float z = r*sin(zenit);

    return vec3(x, y, z);
}

// mašle - sfericke souradnice
vec3 getBow(vec2 vec) {
    float azimut =  sqrt(vec.x * 1);
    float zenit = vec.y * 3.14;
    float r = 1+2*sin(4*zenit);

    float x = r*cos(azimut)*cos(zenit);
    float y = r*sin(azimut)*cos(zenit);
    float z = r*sin(zenit);

    return vec3(x, y, z);
}


// koule - zdroj svetla
vec3 getSphere(vec2 vec) {
    float azimut = vec.x * 3.14 *2;
    float zenit = vec.y * 3.14;
    float r = 1;

    float x = r*cos(azimut)*cos(zenit);
    float y = r*sin(azimut)*cos(zenit);
    float z = r*sin(zenit);

    return vec3(x, y, z);
}

vec3 getNormal(vec2 xy){
    float delta = 0.01;
    vec3 u = vec3(xy.x + delta, xy.y, getFValue(xy + vec2(delta, 0)))
    - vec3(xy - vec2(delta, 0), getFValue(xy - vec2(delta, 0)));
    vec3 v = vec3(xy + vec2(0, delta), getFValue(xy + vec2(0, delta)))
    - vec3(xy - vec2(0, delta), getFValue(xy - vec2(0, delta)));
    return cross(u, v);
}

void main() {
    posIO = inPosition;
    vec2 position = inPosition -  0.5;
    if (lightModelType == 0) { // blinn-phong
        // rozdeleni objektu
        if (objectType < 2) {
            // vypocet z
            float z = getFValue(position.xy);
            objPos = vec4(position.x, position.y, z, 1.0);
        } else if (objectType == 2) {
            // vypocet xyz
            objPos = vec4(getSphere(position), 1.0);
        } else if (objectType == 3) {
            objPos = vec4(getCone(position), 1.0);
        } else if (objectType == 4) {
            objPos = vec4(getElephantHead(position), 1.0);
        } else if (objectType == 5){
            objPos = vec4(getBow(position), 1.0);
        } else if (objectType == 6){
            objPos = vec4(getSombrero(inPosition), 1.0);
        } else if (objectType == 7) {
            objPos = vec4(getSpiral(position), 1.0);
        }
    } else { // per vertex a per pixel
        if (objectType == 8) {
            objPos = vec4(getSphere(position), 1.0);
        } else if (objectType == 9) {
            objPos = vec4(getSphere(position), 1.0);
        }
    }
    objPos = model * objPos;// modelova transformace


}
