#version 330
#extension GL_ARB_separate_shader_objects : require

// [VanillaCopy] position_tex_color.fsh with the boss bar effects added
// PORT-26.3: paired with vanilla's core/position_tex_color vertex shader. The two amounts the HUD used to set as
// uniforms ride on the vertex colour: Color.r is the grain intensity, Color.a the health fraction.
// GameTime comes from the Globals block.

#include <minecraft:globals.glsl>
#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

// Better randomness for the boss bar than the gaia entities:
// Hash without Sine - https://www.shadertoy.com/view/4djSRW
float hashwithoutsine13(vec3 p3)
{
    p3  = fract(p3 * .1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float rand(vec2 co, float seed)
{
    return hashwithoutsine13(vec3(1033.0 * co, seed));
}

void main() {
    float grainIntensity = vertexColor.r;
    float hpFract = vertexColor.a;

    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0) {
        discard;
    }

    // GameTime is in [0, 1) over course of the day, turn it back into ticks
    float time = GameTime * 24000;
    float s = sin((texCoord0.y + 0.0375) * 100.0 + sin(texCoord0.x * 90.0 - time / 2.0) * hpFract) * 0.5 + 0.5;
    s = 1.0 - s;
    if(s <= 0.98)
    s = 0;
    s *= 0.4;

    vec3 newColor = vec3(
        min(1, color.r + s),
        min(1, color.g + s),
        min(1, color.b + s)
    );

    if(hpFract <= 0.2) {
        float flash = (sin(time * 2.0) - 0.8) * 5;
        if(flash > 0) {
            newColor.g = max(0, newColor.g - flash);
            newColor.b = max(0, newColor.b - flash);
        }
    }

    if(grainIntensity > 0) {
        float gs = rand(texCoord0, time);
        newColor = mix(newColor, vec3(gs), grainIntensity);
    }

    fragColor = vec4(newColor, color.a) * ColorModulator;
}
