/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.helper;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.resources.Identifier;

import vazkii.botania.xplat.BotaniaConfig;

import java.util.List;
import java.util.function.Consumer;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

/**
 * Botania's shaders, as 26.3 render pipelines.
 * <p>
 * PORT-26.3: shader JSONs and {@code ShaderInstance} are gone since 1.21.5. A shader is now a {@link RenderPipeline}:
 * the GLSL files, the vertex format, the uniform blocks it reads (bind group layouts) and the blend/depth state, all
 * declared here instead of in a JSON. Values the old code set as uniforms every frame ({@code BotaniaGrainIntensity},
 * {@code BotaniaDisfiguration}, {@code BotaniaHpFract}) have no per-draw channel any more, so those shaders read them
 * from the vertex colour instead; see the comments on each pipeline. The "use shaders" option is read when the
 * render types are created, so it takes effect after a restart rather than immediately.
 */
public class CoreShaders {
	// Everything a world-space shader needs: camera matrices, model/colour transforms and fog (globals carry GameTime)
	private static final RenderPipeline.Snippet MATRICES_FOG_SNIPPET = RenderPipeline.builder()
			.withBindGroupLayout(BindGroupLayouts.GLOBALS)
			.withBindGroupLayout(BindGroupLayouts.PROJECTION)
			.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
			.withBindGroupLayout(BindGroupLayouts.FOG)
			.buildSnippet();

	// [VanillaCopy] RenderPipelines.ENTITY_SNIPPET: vanilla's entity vertex shader with lighting, lightmap and overlay
	private static final RenderPipeline.Snippet ENTITY_SNIPPET = RenderPipeline.builder(MATRICES_FOG_SNIPPET)
			.withVertexShader("core/entity")
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withBindGroupLayout(BindGroupLayouts.LIGHTING)
			.withBindGroupLayout(BindGroupLayouts.SAMPLER2)
			.withBindGroupLayout(BindGroupLayouts.SAMPLER1)
			.withVertexBinding(0, DefaultVertexFormat.ENTITY)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withDepthStencilState(DepthStencilState.DEFAULT)
			.buildSnippet();

	// Position + colour + texture + lightmap, the format the pool, plate, enchanter and icon overlays draw with
	private static final RenderPipeline.Snippet POSITION_COLOR_TEX_LIGHTMAP_SNIPPET = RenderPipeline.builder(MATRICES_FOG_SNIPPET)
			.withVertexShader(botaniaRL("core/position_color_tex_lightmap"))
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2)
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(DepthStencilState.DEFAULT)
			.buildSnippet();

	// Position + texture + colour with vanilla's own vertex shader (halo, luminizer, babylon weapons)
	private static final RenderPipeline.Snippet POSITION_TEX_COLOR_SNIPPET = RenderPipeline.builder(MATRICES_FOG_SNIPPET)
			.withVertexShader("core/position_tex_color")
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(DepthStencilState.DEFAULT)
			.buildSnippet();

	/** Plain position + colour, drawn translucently and from both sides: highlight rectangles and circles. */
	public static final RenderPipeline POSITION_COLOR_TRANSLUCENT = RenderPipeline.builder(MATRICES_FOG_SNIPPET)
			.withLocation(botaniaRL("pipeline/position_color_translucent"))
			.withVertexShader("core/position_color")
			.withFragmentShader("core/position_color")
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(DepthStencilState.DEFAULT)
			.withCull(false)
			.build();

	/** As above, but triangles. */
	public static final RenderPipeline POSITION_COLOR_TRANSLUCENT_TRIANGLES = RenderPipeline.builder(MATRICES_FOG_SNIPPET)
			.withLocation(botaniaRL("pipeline/position_color_translucent_triangles"))
			.withVertexShader("core/position_color")
			.withFragmentShader("core/position_color")
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(DepthStencilState.DEFAULT)
			.withCull(false)
			.build();

	/** The star burst around pylons and sparks: additive triangles that don't write depth. */
	public static final RenderPipeline STAR = RenderPipeline.builder(MATRICES_FOG_SNIPPET)
			.withLocation(botaniaRL("pipeline/star"))
			.withVertexShader("core/position_color")
			.withFragmentShader("core/position_color")
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
			.withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
			.build();

	/** Vanilla's lines, drawn on top of everything (bound-block and wand lines). */
	public static final RenderPipeline LINES_NO_DEPTH = RenderPipeline.builder(MATRICES_FOG_SNIPPET)
			.withLocation(botaniaRL("pipeline/lines_no_depth"))
			.withVertexShader("core/rendertype_lines")
			.withFragmentShader("core/rendertype_lines")
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
			.withPrimitiveTopology(PrimitiveTopology.LINES)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
			.withCull(false)
			.build();

	/** Textured, lit, translucent quads with no colour games: sparks and icon overlays. */
	public static final RenderPipeline POSITION_COLOR_TEX_LIGHTMAP = RenderPipeline.builder(POSITION_COLOR_TEX_LIGHTMAP_SNIPPET)
			.withLocation(botaniaRL("pipeline/position_color_tex_lightmap"))
			.withFragmentShader(botaniaRL("core/position_color_tex_lightmap"))
			.build();

	public static final RenderPipeline MANA_POOL = RenderPipeline.builder(POSITION_COLOR_TEX_LIGHTMAP_SNIPPET)
			.withLocation(botaniaRL("pipeline/mana_pool"))
			.withFragmentShader(botaniaRL("core/mana_pool"))
			.build();

	public static final RenderPipeline TERRA_PLATE = RenderPipeline.builder(POSITION_COLOR_TEX_LIGHTMAP_SNIPPET)
			.withLocation(botaniaRL("pipeline/terrestrial_agglomeration_plate_rune"))
			.withFragmentShader(botaniaRL("core/terrestrial_agglomeration_plate_rune"))
			.build();

	public static final RenderPipeline ENCHANTER = RenderPipeline.builder(POSITION_COLOR_TEX_LIGHTMAP_SNIPPET)
			.withLocation(botaniaRL("pipeline/mana_enchanter_rune"))
			.withFragmentShader(botaniaRL("core/mana_enchanter_rune"))
			.build();

	/** Vanilla's position_tex_color, used when the halo shader is turned off. */
	public static final RenderPipeline POSITION_TEX_COLOR = RenderPipeline.builder(POSITION_TEX_COLOR_SNIPPET)
			.withLocation(botaniaRL("pipeline/position_tex_color"))
			.withFragmentShader("core/position_tex_color")
			.withCull(false)
			.build();

	public static final RenderPipeline HALO = RenderPipeline.builder(POSITION_TEX_COLOR_SNIPPET)
			.withLocation(botaniaRL("pipeline/halo"))
			.withFragmentShader(botaniaRL("core/halo"))
			.withCull(false)
			.build();

	/** Vanilla's entity_translucent, used for the pylon glow and the Gaia Guardian when the shaders are off. */
	public static final RenderPipeline ENTITY_TRANSLUCENT = RenderPipeline.builder(ENTITY_SNIPPET)
			.withLocation(botaniaRL("pipeline/entity_translucent"))
			.withFragmentShader("core/entity")
			.withShaderDefine("ALPHA_CUTOUT", 0.1F)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withCull(false)
			.build();

	public static final RenderPipeline PYLON = RenderPipeline.builder(ENTITY_SNIPPET)
			.withLocation(botaniaRL("pipeline/pylon"))
			.withFragmentShader(botaniaRL("core/pylon"))
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withCull(false)
			.build();

	/**
	 * The Gaia Guardian's grainy, jittering look. The two amounts come from the model's tint colour: red is the
	 * disfiguration (times 0.1) and alpha the grain intensity, the shader uses white for the actual colour.
	 */
	public static final RenderPipeline GAIA_NOISE = RenderPipeline.builder(ENTITY_SNIPPET)
			.withLocation(botaniaRL("pipeline/gaia_noise"))
			.withVertexShader(botaniaRL("core/gaia_noise"))
			.withFragmentShader(botaniaRL("core/gaia_noise"))
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withCull(false)
			.build();

	/** The starfield creator's sky: the end portal shader with its own colours. */
	public static final RenderPipeline STARFIELD = RenderPipeline.builder(MATRICES_FOG_SNIPPET)
			.withLocation(botaniaRL("pipeline/starfield_creator"))
			.withVertexShader("core/rendertype_end_portal")
			.withFragmentShader(botaniaRL("core/starfield_creator"))
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1)
			.withVertexBinding(0, DefaultVertexFormat.POSITION)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withShaderDefine("PORTAL_LAYERS", 15)
			.withColorTargetState(ColorTargetState.DEFAULT)
			.withDepthStencilState(DepthStencilState.DEFAULT)
			.build();

	/** Vanilla's particle shaders with additive blending and no depth write: wisps and sparkles. */
	private static final RenderPipeline.Snippet ADDITIVE_PARTICLE_SNIPPET = RenderPipeline.builder(MATRICES_FOG_SNIPPET)
			.withVertexShader("core/particle")
			.withFragmentShader("core/particle")
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER2)
			.withVertexBinding(0, DefaultVertexFormat.PARTICLE)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
			.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
			.buildSnippet();

	public static final RenderPipeline ADDITIVE_PARTICLE = RenderPipeline.builder(ADDITIVE_PARTICLE_SNIPPET)
			.withLocation(botaniaRL("pipeline/additive_particle"))
			.build();

	/** The same, drawn through everything (the depth-ignoring wisps). */
	public static final RenderPipeline ADDITIVE_PARTICLE_NO_DEPTH = RenderPipeline.builder(ADDITIVE_PARTICLE_SNIPPET)
			.withLocation(botaniaRL("pipeline/additive_particle_no_depth"))
			.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
			.build();

	/** The additive particle with the film-grain fragment shader (the Gaia Guardian's corrupt sparkles). */
	public static final RenderPipeline FILM_GRAIN_PARTICLE = RenderPipeline.builder(ADDITIVE_PARTICLE_SNIPPET)
			.withLocation(botaniaRL("pipeline/film_grain_particle"))
			.withFragmentShader(botaniaRL("core/film_grain_particle"))
			.build();

	/**
	 * The Gaia Guardian's boss bar. A GUI pipeline; the vertex colour carries the grain intensity in red and the
	 * health fraction in alpha.
	 */
	public static final RenderPipeline GAIA_BOSS_BAR = RenderPipeline.builder()
			.withLocation(botaniaRL("pipeline/gaia_boss_bar"))
			.withBindGroupLayout(BindGroupLayouts.GLOBALS)
			.withBindGroupLayout(BindGroupLayouts.PROJECTION)
			.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
			.withVertexShader("core/position_tex_color")
			.withFragmentShader(botaniaRL("core/gaia_boss_bar"))
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	/**
	 * Vanilla's celestial pipeline (textured quads in the sky pass, no fog) with ordinary translucent blending: the
	 * Garden of Glass planets and rainbow. The additive one (rays) is {@code RenderPipelines.CELESTIAL} itself.
	 */
	public static final RenderPipeline SKY_TEXTURED_TRANSLUCENT = RenderPipeline.builder()
			.withBindGroupLayout(BindGroupLayouts.GLOBALS)
			.withBindGroupLayout(BindGroupLayouts.PROJECTION)
			.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
			.withLocation(botaniaRL("pipeline/sky_textured_translucent"))
			.withVertexShader("core/position_tex")
			.withFragmentShader("core/position_tex")
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.build();

	private static final List<RenderPipeline> ALL = List.of(
			POSITION_COLOR_TRANSLUCENT, POSITION_COLOR_TRANSLUCENT_TRIANGLES, STAR, LINES_NO_DEPTH,
			POSITION_COLOR_TEX_LIGHTMAP, MANA_POOL, TERRA_PLATE, ENCHANTER,
			POSITION_TEX_COLOR, HALO, ENTITY_TRANSLUCENT, PYLON, GAIA_NOISE,
			STARFIELD, ADDITIVE_PARTICLE, ADDITIVE_PARTICLE_NO_DEPTH, FILM_GRAIN_PARTICLE, GAIA_BOSS_BAR, SKY_TEXTURED_TRANSLUCENT
	);

	/** Hands every pipeline to the loader, where it wants to know about them ahead of time (NeoForge precompiles). */
	public static void init(Consumer<RenderPipeline> registrations) {
		ALL.forEach(registrations);
	}

	private static boolean useShaders() {
		return BotaniaConfig.client().useShaders();
	}

	/** The pylon glow, or vanilla's translucent entity pipeline when the shaders are off. */
	public static RenderPipeline pylon() {
		return useShaders() ? PYLON : ENTITY_TRANSLUCENT;
	}

	public static RenderPipeline gaiaNoise() {
		return useShaders() ? GAIA_NOISE : ENTITY_TRANSLUCENT;
	}

	public static RenderPipeline manaPool() {
		return useShaders() ? MANA_POOL : POSITION_COLOR_TEX_LIGHTMAP;
	}

	public static RenderPipeline terraPlate() {
		return useShaders() ? TERRA_PLATE : POSITION_COLOR_TEX_LIGHTMAP;
	}

	public static RenderPipeline enchanter() {
		return useShaders() ? ENCHANTER : POSITION_COLOR_TEX_LIGHTMAP;
	}

	public static RenderPipeline halo() {
		return useShaders() ? HALO : POSITION_TEX_COLOR;
	}

	/** The film-grain particle shader, or the plain additive particle pipeline when the shaders are off. */
	public static RenderPipeline filmGrainParticle() {
		return useShaders() ? FILM_GRAIN_PARTICLE : ADDITIVE_PARTICLE;
	}

	/** The boss bar shader, or null when the shaders are off (the bar is then drawn plainly). */
	public static RenderPipeline gaiaBossBar() {
		return useShaders() ? GAIA_BOSS_BAR : null;
	}

	/** The starfield does not respect the option: like the end portal, the effect is the shader. */
	public static RenderPipeline starfield() {
		return STARFIELD;
	}

	/** Pipelines resolve their GLSL by these ids; kept for anything that wants to reference the files. */
	public static Identifier shader(String name) {
		return botaniaRL("core/" + name);
	}
}
