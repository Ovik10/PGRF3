#version 150
in vec2 inPosition; // input from the vertex buffer
void main() {
	vec2 position = inPosition;
//	position.x += 0.1;
//	position.x = position.x * 2 - 0.9;
//	position.y = position.y * 2 - 0.9;
	gl_Position = vec4(position, 0.0, 1.0); 
} 
