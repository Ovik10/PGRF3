#version 150
in vec2 inPosition; // input from the vertex buffer

uniform mat4 view;
uniform mat4 projection;

out vec2 texCoord;

const float PI = 3.14159;

vec3 getSphere(vec2 pos) {
	float az = pos.x * PI; // souřadnice X je <-1; 1> a chceme v rozsahu <-PI; PI>
	float ze = pos.y * PI / 2; // souřadnice Y je <-1; 1> a chceme v rozsahu <-PI/2; PI/2>
	float r = 1;

	float x = r * cos(az) * cos(ze);
	float y = r * sin(az) * cos(ze);
	float z = r * sin(ze);

	return vec3(x, y, z);
}

float getZ(vec2 pos) {
	return sin(pos.x * 5);
}

void main() {
	vec2 position = inPosition * 2 - 1; // inPosition <0; 1> | position <-1; 1>
	texCoord = inPosition;

	vec3 pos3 = getSphere(position);
	//	vec3 pos3 = vec3(position, getZ(position));

	gl_Position = projection * view * vec4(pos3, 1.0);
} 
