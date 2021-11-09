#version 150
in vec2 texCoord;
in vec3 normal;
in vec3 light;
in vec3 viewDirection;
in vec4 depthTextureCoord;

uniform sampler2D depthTexture;
uniform sampler2D mosaic;

out vec4 outColor; // output from the fragment shader

void main() {
    vec3 ambient = vec3(0.1);

    float NdotL = max(0, dot(normalize(light), normalize(normal)));
    vec3 diffuse = vec3(NdotL * vec3(0.5));

    vec3 halfVector = normalize(light + viewDirection);
    float NdotH = max(0.0, dot(normalize(normal), halfVector));
    vec3 specular = vec3(pow(NdotH, 16.0));

    vec3 finalColorIntensity = ambient + diffuse + specular;
    vec3 textureColor = texture(mosaic, texCoord).rgb;

    // "z" hodnota z textury
    // R, G i B složky jsou stejné, protože gl_FragCoord.zzz
    // r -> v light.frag ukládáme gl_FragCoord.zzz, takže jsou všechny hodnoty stejné
    float zLight = texture(depthTexture, depthTextureCoord.xy).r; // Z hodnota ke světlu nejbližšího pixelu na této pozici

    // aktuální hodnota
    float zActual = depthTextureCoord.z;

    // 0.01 - bias na ostranění tzv. akné
    // lze vyzkoušet různé hodnoty 0.01, 0.001, 0.0001, 0.00001
    bool shadow = zActual > zLight + 0.001;

    if (shadow) {
//        outColor = vec4(0.5, 0.5, 0.0, 1.0);
        outColor = vec4(ambient * textureColor, 1.0);
    } else {
//        outColor = vec4(0.0, 1.0, 1.0, 1.0);
        outColor = vec4(finalColorIntensity * textureColor, 1.0);
    }
}
