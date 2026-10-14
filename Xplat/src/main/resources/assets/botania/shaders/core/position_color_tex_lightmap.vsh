#version 330
#extension GL_ARB_separate_shader_objects : require

// PORT-26.3: vanilla dropped its position_color_tex_lightmap shader; this is it in the 26.3 include style.
// The vertex format is POSITION_COLOR_TEX_LIGHTMAP, so the attribute locations follow that order.

#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
#include <minecraft:sample_lightmap.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV2;

uniform sampler2D Sampler2;

layout(location = 0) out vec2 texCoord0;
layout(location = 1) out vec4 vertexColor;
layout(location = 2) out float sphericalVertexDistance;
layout(location = 3) out float cylindricalVertexDistance;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    vertexColor = Color * sample_lightmap(Sampler2, UV2);
    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
}
