#version 150
in vec2 texCoord;
in vec3 normal;
in vec3 light;
in vec3 viewDirection;

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

    outColor = vec4(finalColorIntensity * textureColor, 1.0);
}
