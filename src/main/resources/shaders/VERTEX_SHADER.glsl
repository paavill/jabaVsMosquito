#version 330 core
layout (location = 0) in vec3 position;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec2 aTexCoord;

out vec3 Normal;
out vec3 Color;
out vec3 FragPos;
out vec3 aPos;
out vec2 TexCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main()
{
    Color = aColor;
    Normal = aNormal;
    aPos = position;
    TexCoord = aTexCoord;
    FragPos = vec3(model * vec4(position, 1.0f));
    gl_Position = projection * view * model * vec4(position, 1.0f);
}
