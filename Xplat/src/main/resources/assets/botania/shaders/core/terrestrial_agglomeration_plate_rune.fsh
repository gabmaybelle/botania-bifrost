#version 330
#extension GL_ARB_separate_shader_objects : require

// [VanillaCopy] position_color_tex_lightmap.fsh, changes noted
// PORT-26.3: uniforms come from the Globals (GameTime) and DynamicTransforms (ColorModulator) blocks now

#include <minecraft:fog.glsl>
#include <minecraft:globals.glsl>
#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) in float sphericalVertexDistance;
layout(location = 3) in float cylindricalVertexDistance;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    if (color.a < 0.1) {
        discard;
    }

    // Botania - Custom color
    // GameTime is in [0, 1) over course of the day, turn it back into ticks
    float time = GameTime * 24000;
    float gs = (color.r + color.g + color.b) / 16.0;
    float tx = (texCoord0.x + 0.075) * 10;
    float ty = (texCoord0.y) * 10;
    float g = ((sin((time + sin(tx) * 250 + sin(ty) * 50) / 6.0) + 1.0) * 0.5 + 0.5) / 5.0;
    float b = ((cos((time + sin(tx) * 50 + sin(ty) * 100) / 6.0) + 1.0) * 0.5 + 0.5) / 5.0;
    fragColor = apply_fog(vec4(gs - 0.1, gs + g, gs + b, color.a * 1.4) * ColorModulator, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
