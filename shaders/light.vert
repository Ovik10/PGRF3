#version 150
in vec2 inPosition;
uniform mat4 model;
uniform mat4 view;
uniform mat4 proj;
uniform int objectType;
uniform int lightModelType;
uniform float time;
uniform mat4 mLight;
uniform mat4 vLight;
uniform mat4 pLight;
out vec4 vertPos;
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
float getFValue(vec2 z){
    if (objectType==0) {
        return 0;
    } else {
        return -(z.x*z.x*3+z.y*z.y*3);
    }
}

// polokoule
vec3 getTrampoline(vec2 xy){
    float zenit = xy.x * PI;
    float azimut = xy.y * PI;
    float r = 2 + sin(zenit + azimut)*10;

    float x = sin(zenit)*cos(azimut);
    float y = sin(azimut)*sin(zenit);
    float z = cos(zenit)*0.1*5;

    return vec3(x*3, y*3, z*3);
}


// sombrero
vec3 getSombrero(vec2 vec) {
    float az = vec.x * 2 * PI;
    float r = vec.y * 2 * PI;
    float v = 2 * sin(r);

    float x = r * cos(az);
    float y = r * sin(az);
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
    float az = vec.x * PI *2;
    float ze = vec.y * PI;
    float r = 1;

    float x = r*cos(az)*cos(ze);
    float y = r*sin(az)*cos(ze);
    float z = r*sin(ze);

    return vec3(x, y, z);
}

void main() {
    // prirazeni objektu
    vec2 position = inPosition*2-1;
        if (objectType < 2) {
            float z = getFValue(position.xy);
            vertPos = vec4(position.x, position.y, z, 1.0);
        } else if (objectType == 2) {
            vertPos = vec4(getCone(position), 1.0);
        } else if (objectType == 3) {
            vertPos = vec4(getElephantHead(position), 1.0);
        } else if (objectType == 4) {
            vertPos = vec4(getTrampoline(position), 1.0);
        } else if (objectType == 5){
            vertPos = vec4(getSombrero(inPosition), 1.0);
        } else if (objectType == 6){
            vertPos = vec4(getDonut(position), 1.0);
        }

    // „normální“ transformace do NDC z pozice kamery
    gl_Position = proj*view*model*vertPos;

    // Transformace s pohledu zdroje světla
    vertPos =  proj*view*model*vertPos;
}
