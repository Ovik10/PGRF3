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
    if (lightModelType == 0) { // blinn-phong model
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
            objPos = vec4(getTrampoline(position), 1.0);
        } else if (objectType == 6){
            objPos = vec4(getSombrero(inPosition), 1.0);
        }
    } else { // per vertex a per pixel
        if (objectType == 8) {
            objPos = vec4(getSphere(position), 1.0);
        } else if (objectType == 9) {
            objPos = vec4(getSphere(position), 1.0);
        }
    }
    objPos = model * objPos;// modelova transformace

     vec3 normal;
        if (lightModelType == 0) { // blinn-phong vypocet vektoru pro světlo, vektor pohledu,vypocet, normalizace a transformace normaly
            lightDir = normalize((mat3(view) * lightPos).xyz - (view * objPos).xyz);
            viewDir = normalize((mat3(view) * viewPos).xyz - (view * objPos).xyz);
            normal = normalize(getNormal(position.xy));
            normal = inverse(transpose(mat3(view * model))) * normal;
            normalIO = normal;

    // per vertex
        } else if (lightModelType == 1) {
            normal = normalize(getNormal(position.xy));
            normal = inverse(transpose(mat3(view * model))) * normal;
            lightDir = normalize(lightPos - (view * objPos).xyz);
            intensity = dot(lightDir, normal);
            vertColor = vec3(normal.xyz);

    // per pixel
        } else if (lightModelType == 2)  {
            normal = normalize(getNormal(position.xy));
            normal = inverse(transpose(mat3(view * model))) * normal;
            lightDir = normalize(lightPos - (view * objPos).xyz);
            normalIO = normal;
        }

    // view + projekcni transformace
        gl_Position = proj * view * objPos;
    }



}
