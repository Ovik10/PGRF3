#version 150
out vec4 outColor;
in vec2 posIO;
in vec4 objPos;
in vec3 normalIO;
in vec3 lightDirection;
in vec3 viewDirection;
in float intensity;
in vec3 vertColor;
uniform sampler2D textureID;
uniform sampler2D textureDepth;
uniform mat4 matMVPLight;
uniform int objectType;
uniform int lightModelType;
uniform int colorType;
uniform int spotlight;

void main() {
    vec4 shadowCoord = matMVPLight* vec4(objPos.xyz, 1.0);
    vec3 texCoord = shadowCoord.xyz/shadowCoord.w*0.5+0.5;
    float bias = 0.0005 * tan(acos(dot(normalize(normalIO), normalize(lightDirection))));
    texCoord.z = texCoord.z - bias;
    vec4 finalColor;
    vec4 normalColor = vec4 (normalize(normalIO), 1.0);
    // ambientni slozka
    vec4 ambient = vec4(0.1, 0.1, 0.1, 1);

    // difuzni slozka
    float NdotL = max(dot(normalize(normalIO), normalize(lightDirection)), 0);
    vec4 diffuse = vec4(NdotL*vec3(1.0), 1);

    // zrcadlova slozka
    vec3 halfVector = normalize(normalize(lightDirection)+normalize(viewDirection));
    float NdotH = dot(normalize(normalIO), halfVector);
    vec4 specular = vec4(pow(NdotH, 16)*vec3(1.0), 1);

    if (lightModelType == 0) { // blinn-phong osvetleni
        finalColor = ambient + diffuse + specular;

    } else if (lightModelType == 1) { //ambientni
        finalColor = ambient;

    } else if (lightModelType == 2) { // difuzni
        finalColor = diffuse;

    } else if (lightModelType == 3) { // zrcadlova
             finalColor = specular;
         }

    // barva svetla
    if (objectType == 2) {
        outColor = vec4(1.0, 1.0, 1.0, 0.0);

    } else  {
        if (texture(textureDepth, texCoord.xy).z < texCoord.z) {
            if (spotlight==1){
                //reflektor
                float spotCutOff=0.885;
                vec3 spotDirection=vec3(1, 1, 0);
                float spotEffect = max(dot(normalize(spotDirection), normalize(lightDirection)), 0);
                float blend = clamp((spotEffect-spotCutOff)/(1-spotCutOff), 0.0, 1.0);
                outColor=mix((ambient*normalColor), (normalColor*(ambient + diffuse + specular)), blend);
            } else {
                if (colorType == 0) {
                    outColor = vec4(0.1) * vec4(texture(textureID, posIO).rgb, 1.0);
                } else if (colorType == 1) {
                    outColor = vec4(0.1) * vec4(objPos.xyz, 1.0);
                } else if (colorType == 2) {
                    // normala
                    outColor = vec4(0.1) * vec4(normalize(normalIO), 1.0);
                } else if (colorType == 3) {
                    // pozice do textury
                    outColor = vec4(0.1) * vec4(posIO, 0, 1.0);
                } else if (colorType == 4) {
                    // cervena
                    outColor = vec4(0.1) * vec4(1, 0, 0, 1);
                }  }
        } else {
            if (spotlight==1){
                //reflektor
                float spotCutOff=0.89;
                vec3 spotDirection=vec3(1, 1, 10);
                float spotEffect = max(dot(normalize(spotDirection), normalize(lightDirection)), 0);
                float blend = clamp((spotEffect-spotCutOff)/(1-spotCutOff), 0.0, 1.0);
                outColor=mix((ambient*normalColor), (normalColor*(ambient + diffuse + specular)), blend);
            } else {
                // kdyz neni ve stínu
                if (colorType == 0) {
                    outColor = vec4(texture(textureID, posIO).rgb, 1.0) * finalColor;
                } else if (colorType == 1) {
                    outColor = vec4(objPos.xyz, 1.0) * finalColor;
                } else if (colorType == 2) {
                    // podle normaly
                    outColor = vec4(normalize(normalIO), 1.0) * finalColor;
                } else if (colorType == 3) {
                    // pozice do textury
                    outColor = vec4(posIO, 0, 1.0) * finalColor;
                } else if (colorType == 4) {
                    // cervena
                    outColor = vec4(1, 0, 0, 1) * finalColor;
                }
            } }
    }
}

