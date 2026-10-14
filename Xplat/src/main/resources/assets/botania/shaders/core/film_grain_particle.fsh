#version 330
#extension GL_ARB_separate_shader_objects : require

// [VanillaCopy] particle.fsh with changes
// PORT-26.3: paired with vanilla's core/particle vertex shader; uniforms come from the Fog and DynamicTransforms blocks

#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
layout(location = 2) in vec2 texCoord0;
layout(location = 3) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

void main() {
    float gs = rand(vec2(int(texCoord0.x * 256), int(texCoord0.y * 512))) * 0.5;
    // texture.rgb and vertexColor deliberately ignored
    vec4 color = vec4(gs, gs, gs, texture(Sampler0, texCoord0).a) * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
