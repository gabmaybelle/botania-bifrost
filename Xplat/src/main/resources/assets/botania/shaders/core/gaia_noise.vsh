#version 330
#extension GL_ARB_separate_shader_objects : require

// [VanillaCopy] entity.vsh (the translucent variant, no per-face lighting), changes noted
// PORT-26.3: the two amounts the renderer used to set as uniforms now ride on the model's tint colour:
// Color.r is the disfiguration (times 0.1), Color.a the grain intensity. The model itself is drawn white.

#include <minecraft:light.glsl>
#include <minecraft:fog.glsl>
#include <minecraft:globals.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
#include <minecraft:sample_lightmap.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV1;
layout(location = 4) in ivec2 UV2;
layout(location = 5) in vec3 Normal;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;
layout(location = 2) out vec4 vertexColor;
layout(location = 4) out vec4 lightMapColor;
layout(location = 5) out vec4 overlayColor;
layout(location = 6) out vec2 texCoord0;
layout(location = 7) out float grainIntensity;

float rand(vec2 co) {
    return (fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453) - 0.5) * 2;
}

void main() {
    // Botania: Blur Position using disfiguration
    float disfiguration = Color.r * 0.1;
    float seed = rand(vec2(GameTime, GameTime));
    vec3 offset = disfiguration * vec3(
        rand(seed * Position.yz),
        rand(seed * Position.xz),
        rand(seed * Position.xy)
    );
    vec3 modifiedPos = Position + offset;
    gl_Position = ProjMat * ModelViewMat * vec4(modifiedPos, 1.0);

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, vec4(1.0, 1.0, 1.0, 1.0));
    lightMapColor = sample_lightmap(Sampler2, UV2);
    overlayColor = texelFetch(Sampler1, UV1, 0);
    texCoord0 = UV0;
    grainIntensity = Color.a;
}
