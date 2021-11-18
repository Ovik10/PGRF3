#version 150
in vec2 inPosition;
uniform mat4 model;
uniform mat4 view;
uniform mat4 proj;
uniform int objectType;
uniform int lightModelType;
uniform float time;
out vec4 pos;

// getter pro z hodnotu
float getFValue(vec2 vec){
    if (objectType==0) {
        return 0;
    } else {
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
    float az = vec.x * 2 * 3.14;
    float r = vec.y * 2 * 3.14;
    float v = 2 * sin(r);

    float x = r * cos(az);
    float y = r * sin(az);
    float z = v;

    return vec3(x, y, z);
}

// sloni hlava - sfericke souradnice
vec3 getElephantHead(vec2 vec) {
    float az = vec.x * 2 * 3.14;
    float ze = vec.y * 3.14;
    float r = 3+cos(4*az);

    float x = r*cos(az)*cos(ze);
    float y = r*sin(az)*cos(ze);
    float z = r*sin(ze);

    return vec3(x, y, z);
}

// polokoule - sfericke souradnice
vec3 getTrampoline(vec2 vec){
    float z = vec.x * 3.14;
    float az = vec.y * 3.14;
    float r = 2 + sin(z + az);

    float x = sin(z)*cos(az);
    float y = sin(az)*sin(z);
    float z = cos(z)*0.1*5;

    return vec3(x, y, z);
}

// koule - osvetleni
vec3 getSphere(vec2 vec) {
    float az = vec.x * 3.14 *2;
    float ze = vec.y * 3.14;
    float r = 1;

    float x = r*cos(az)*cos(ze);
    float y = r*sin(az)*cos(ze);
    float z = r*sin(ze);

    return vec3(x, y, z);
}

void main() {
    vec2 position = inPosition-0.5;
    if (lightModelType == 0) { // blinn-phong
        if (objectType < 2) {
            // vypocet z
            float z = getFValue(position.xy);
            pos = vec4(position.x, position.y, z, 1.0);
        } else if (objectType == 2) {
            // vypocet x, y, z
            pos = vec4(getCone(position), 1.0);
        } else if (objectType == 3) {
            pos = vec4(getElephantHead(position), 1.0);
        } else if (objectType == 4) {
            pos = vec4(getTrampoline(position), 1.0);
        } else if (objectType == 5){
            pos = vec4(getSombrero(inPosition), 1.0);
        }
    } else { // per vertex a per pixel
        if (objectType == 7) {
            pos = vec4(getSphere(position), 1.0);
        }
    }
    // model view projekcni transformace
    gl_Position = proj*view*model*pos;
    pos = proj*view*model*pos;
}
