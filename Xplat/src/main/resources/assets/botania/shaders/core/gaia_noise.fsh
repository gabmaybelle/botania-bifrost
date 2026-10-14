#version 330
#extension GL_ARB_separate_shader_objects : require

// [VanillaCopy] entity.fsh (the translucent variant), changes noted
// PORT-26.3: the grain intensity arrives from gaia_noise.vsh (it rides on the tint colour's alpha)

#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
layout(location = 2) in vec4 vertexColor;
layout(location = 4) in vec4 lightMapColor;
layout(location = 5) in vec4 overlayColor;
layout(location = 6) in vec2 texCoord0;
layout(location = 7) in float grainIntensity;

layout(location = 0) out vec4 fragColor;

float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898,78.233))) * 43758.5453);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    // Botania - Grayscale + Noise with grain intensity
    float r = rand(texCoord0);
    float gs = dot(vec3(0.3, 0.59, 0.11), color.rgb) / 4.5;
    color.rgb = vec3(mix(gs, r, grainIntensity));

    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
