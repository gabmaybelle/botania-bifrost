#version 330
#extension GL_ARB_separate_shader_objects : require

// [VanillaCopy] Originally based on entity.fsh (the translucent variant), but heavily changed
// PORT-26.3: paired with vanilla's core/entity vertex shader without its lighting defines, so the inputs are the
// plain vertexColor, lightMapColor, overlayColor and texCoord0 at the locations entity.vsh writes them to.
// GameTime comes from the Globals block.

#include <minecraft:fog.glsl>
#include <minecraft:globals.glsl>
#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
layout(location = 2) in vec4 vertexColor;
layout(location = 4) in vec4 lightMapColor;
layout(location = 5) in vec4 overlayColor;
layout(location = 6) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 color = vec4(0);
    for(int i = -4 ; i < 4; i++) {
        for(int j = -3; j < 3; j++) {
            color += texture(Sampler0, texCoord0 + vec2(j, i));
        }
    }
    if (color.a < 0.1) {
        discard;
    }
    // GameTime is in [0, 1) over course of the day, turn it back into ticks
    float time = GameTime * 24000;
    float brightness = sin(time / 20.0) * 0.5 + 0.15;
    float alpha = min(1, sin(time / 30.0) * 0.5 + 0.75);

    color = vec4((color * color * 0.0005 * brightness + texture(Sampler0, texCoord0)).rgb, alpha * vertexColor.a);
    color *= ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
