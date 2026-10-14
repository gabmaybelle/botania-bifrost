/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.helper;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.render.block_entity.PylonBlockEntityRenderer;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;
import vazkii.botania.mixin.client.GuiGraphicsExtractorAccessor;
import vazkii.botania.mixin.client.ItemStackRenderStateAccessor;
import vazkii.botania.mixin.client.LayerRenderStateAccessor;
import vazkii.botania.mixin.client.RenderTypeAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * Botania's render types and small drawing helpers.
 * <p>
 * PORT-26.3: a render type is now a {@link RenderPipeline} (the shader and GPU state, see {@link CoreShaders}) plus a
 * {@link RenderSetup} (its textures, lightmap, overlay and sorting). Nothing draws straight into a buffer any more:
 * world drawing goes through a {@link SubmitNodeCollector}, GUI drawing through a {@link GuiGraphicsExtractor}, and
 * both are drawn later by the game. The "item entity target" the old types wrote to no longer exists, so the
 * {@code _DIRECT} pylon types are the same as the plain ones. Line widths are a per-vertex value now
 * ({@link VertexConsumer#setLineWidth}), so the four {@code LINE_*_NO_DEPTH} types are one type and the width is
 * given with the vertices.
 */
public final class RenderHelper {
	private static final RenderType STAR;
	public static final RenderType RECTANGLE;
	public static final RenderType CIRCLE;
	public static final RenderType RED_STRING;
	public static final RenderType LINES_NO_DEPTH;
	public static final RenderType LINE_1_NO_DEPTH;
	public static final RenderType LINE_4_NO_DEPTH;
	public static final RenderType LINE_5_NO_DEPTH;
	public static final RenderType LINE_8_NO_DEPTH;
	public static final RenderType SPARK;
	public static final RenderType LIGHT_RELAY;
	public static final RenderType ICON_OVERLAY;
	public static final RenderType BABYLON_ICON;
	public static final RenderType MANA_POOL_WATER;
	public static final RenderType TERRA_PLATE;
	public static final RenderType ENCHANTER;
	public static final RenderType HALO;
	public static final RenderType MANA_PYLON_GLOW = getPylonGlow("mana_pylon_glow", PylonBlockEntityRenderer.MANA_TEXTURE);
	public static final RenderType NATURA_PYLON_GLOW = getPylonGlow("natura_pylon_glow", PylonBlockEntityRenderer.NATURA_TEXTURE);
	public static final RenderType GAIA_PYLON_GLOW = getPylonGlow("gaia_pylon_glow", PylonBlockEntityRenderer.GAIA_TEXTURE);
	public static final RenderType MANA_PYLON_GLOW_DIRECT = MANA_PYLON_GLOW;
	public static final RenderType NATURA_PYLON_GLOW_DIRECT = NATURA_PYLON_GLOW;
	public static final RenderType GAIA_PYLON_GLOW_DIRECT = GAIA_PYLON_GLOW;

	public static final RenderType ASTROLABE_PREVIEW;
	public static final RenderType STARFIELD;
	public static final RenderType LIGHTNING;
	public static final RenderType TRANSLUCENT;

	private static final int ITEM_AND_PADDING_WIDTH = 20;

	private static final double INITIAL_OFFSET = 0.005;
	private static final double OFFSET_INCREMENT = 0.001;
	// Global y offset so that overlapping gaia traps or radius descriptors do not Z-fight
	private static double offY = INITIAL_OFFSET;

	public static double getOffY() {
		return offY;
	}

	public static void incrementOffY() {
		offY += OFFSET_INCREMENT;
	}

	public static void onWorldRenderLast() {
		offY = INITIAL_OFFSET;
	}

	private static RenderType makeLayer(String name, RenderSetup setup) {
		return RenderTypeAccessor.botania_create(ResourcesLib.PREFIX_MOD + name, setup);
	}

	static {
		STAR = makeLayer("star", RenderSetup.builder(CoreShaders.STAR).createRenderSetup());

		RECTANGLE = makeLayer("rectangle_highlight", RenderSetup.builder(CoreShaders.POSITION_COLOR_TRANSLUCENT).sortOnUpload().createRenderSetup());
		CIRCLE = makeLayer("circle_highlight", RenderSetup.builder(CoreShaders.POSITION_COLOR_TRANSLUCENT_TRIANGLES).createRenderSetup());

		RED_STRING = makeLayer("red_string", RenderSetup.builder(RenderPipelines.LINES)
				.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup());
		LINES_NO_DEPTH = makeLayer("lines_no_depth", RenderSetup.builder(CoreShaders.LINES_NO_DEPTH)
				.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING).createRenderSetup());
		LINE_1_NO_DEPTH = LINES_NO_DEPTH;
		LINE_4_NO_DEPTH = LINES_NO_DEPTH;
		LINE_5_NO_DEPTH = LINES_NO_DEPTH;
		LINE_8_NO_DEPTH = LINES_NO_DEPTH;

		SPARK = makeLayer("spark", blockAtlas(CoreShaders.POSITION_COLOR_TEX_LIGHTMAP).useLightmap().createRenderSetup());
		LIGHT_RELAY = makeLayer("luminizer", blockAtlas(CoreShaders.halo()).createRenderSetup());
		ICON_OVERLAY = makeLayer("icon_overlay", blockAtlas(CoreShaders.POSITION_COLOR_TEX_LIGHTMAP).useLightmap().createRenderSetup());
		MANA_POOL_WATER = makeLayer("mana_pool_water", blockAtlas(CoreShaders.manaPool()).useLightmap().createRenderSetup());
		TERRA_PLATE = makeLayer("terrestrial_agglomeration_plate_rune", blockAtlas(CoreShaders.terraPlate()).useLightmap().createRenderSetup());
		ENCHANTER = makeLayer("mana_enchanter_rune", blockAtlas(CoreShaders.enchanter()).useLightmap().createRenderSetup());

		BABYLON_ICON = makeLayer("babylon", RenderSetup.builder(CoreShaders.halo())
				.withTexture("Sampler0", Identifier.parse(ResourcesLib.MISC_BABYLON)).createRenderSetup());
		HALO = makeLayer("halo", RenderSetup.builder(CoreShaders.halo())
				.withTexture("Sampler0", FlugelTiaraItem.textureHalo).createRenderSetup());

		// [VanillaCopy] End portal, with own shader
		STARFIELD = makeLayer("starfield_creator", RenderSetup.builder(CoreShaders.starfield())
				.withTexture("Sampler0", AbstractEndPortalRenderer.END_SKY_LOCATION)
				.withTexture("Sampler1", AbstractEndPortalRenderer.END_PORTAL_LOCATION)
				.createRenderSetup());
		LIGHTNING = RenderTypes.lightning();
		TRANSLUCENT = RenderTypes.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
		// PORT-26.3: the astrolabe used the block sheet with a 40% colour modulator; the alpha comes with the tint now
		ASTROLABE_PREVIEW = RenderTypes.translucentMovingBlock();
	}

	private RenderHelper() {}

	private static RenderSetup.RenderSetupBuilder blockAtlas(RenderPipeline pipeline) {
		return RenderSetup.builder(pipeline).withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS);
	}

	private static RenderType getPylonGlow(String name, Identifier texture) {
		return makeLayer(name, RenderSetup.builder(CoreShaders.pylon())
				.withTexture("Sampler0", texture)
				.useLightmap()
				.useOverlay()
				.createRenderSetup());
	}

	private static final Function<Identifier, RenderType> HALO_LAYER = Util.memoize(texture -> makeLayer("assembly_halo",
			RenderSetup.builder(CoreShaders.POSITION_TEX_COLOR)
					// the old texture state blurred the halo (linear filtering)
					.withTexture("Sampler0", texture, () -> RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR))
					.sortOnUpload()
					.createRenderSetup()));

	public static RenderType getHaloLayer(Identifier texture) {
		return HALO_LAYER.apply(texture);
	}

	// [VanillaCopy] entity_translucent, with own shader
	private static final Function<Identifier, RenderType> GAIA_NOISE_LAYER = Util.memoize(texture -> makeLayer("gaia_noise",
			RenderSetup.builder(CoreShaders.gaiaNoise())
					.withTexture("Sampler0", texture)
					.useLightmap()
					.useOverlay()
					.affectsCrumbling()
					.sortOnUpload()
					.createRenderSetup()));

	/**
	 * The Gaia Guardian's noise layer. PORT-26.3: the "dynamic" and "constant" variants were one shader with
	 * different uniform values; the values travel with the model's tint colour now (see
	 * {@link CoreShaders#GAIA_NOISE}), so both names give the same layer.
	 */
	public static RenderType getGaiaNoiseDynamicLayer(Identifier texture) {
		return GAIA_NOISE_LAYER.apply(texture);
	}

	public static RenderType getGaiaNoiseConstantLayer(Identifier texture) {
		return GAIA_NOISE_LAYER.apply(texture);
	}

	/** Packs the Gaia noise amounts into a tint colour for {@link #getGaiaNoiseDynamicLayer}. */
	public static int gaiaNoiseTint(float disfiguration, float grainIntensity) {
		return ARGB.colorFromFloat(Mth.clamp(grainIntensity, 0F, 1F), Mth.clamp(disfiguration / 0.1F, 0F, 1F), 1F, 1F);
	}

	/** The default Gaia noise amounts, what the "constant" layer used to show. */
	public static int gaiaNoiseConstantTint() {
		return gaiaNoiseTint(0.025F, 0.05F);
	}

	public static void drawTexturedModalRect(GuiGraphicsExtractor gui, Identifier textureId, int x, int y, int u, int v, int width, int height) {
		gui.blit(RenderPipelines.GUI_TEXTURED, textureId, x, y, u, v, width, height, 256, 256);
	}

	/** The same with an ARGB tint; replaces the shader colour the old code set around a blit. */
	public static void drawTexturedModalRect(GuiGraphicsExtractor gui, Identifier textureId, int x, int y, int u, int v, int width, int height, int color) {
		gui.blit(RenderPipelines.GUI_TEXTURED, textureId, x, y, u, v, width, height, 256, 256, color);
	}

	public static void renderStar(PoseStack ms, SubmitNodeCollector collector, int color, float xScale, float yScale, float zScale, long seed, float partialTicks) {
		float ticks = ClientTickHandler.getEntityTicksInGame() + partialTicks;
		float semiPeriodTicks = 200;
		float f1 = Mth.abs(Mth.sin((float) Math.PI / semiPeriodTicks * ticks)) * 0.9F + 0.1F; // shift to [0.1, 1.0]

		float f2 = f1 > 0.F ? (f1 - 0.7F) / 0.2F : 0;
		int alphaColor = ARGB.color(ARGB.as8BitChannel(f1), color);
		int count = (int) ((f1 + f1 * f1) / 2F * 90F + 30F);

		// PORT-26.3: the geometry is built later, from the pose the collector was given, so the per-star rotations
		// are applied to copies of that matrix instead of pushing and popping the pose stack
		collector.submitCustomGeometry(ms, STAR, (pose, buffer) -> {
			Random random = new Random(seed);
			Matrix4f base = new Matrix4f(pose.pose()).scale(xScale, yScale, zScale);
			for (int i = 0; i < count; i++) {
				Matrix4f mat = new Matrix4f(base);
				mat.rotate(VecHelper.rotateX(random.nextFloat() * 360F));
				mat.rotate(VecHelper.rotateY(random.nextFloat() * 360F));
				mat.rotate(VecHelper.rotateZ(random.nextFloat() * 360F));
				mat.rotate(VecHelper.rotateX(random.nextFloat() * 360F));
				mat.rotate(VecHelper.rotateY(random.nextFloat() * 360F));
				mat.rotate(VecHelper.rotateZ(random.nextFloat() * 360F + f1 * 90F));
				float f3 = random.nextFloat() * 20F + 5F + f2 * 10F;
				float f4 = random.nextFloat() * 2F + 1F + f2 * 2F;
				Runnable center = () -> buffer.addVertex(mat, 0, 0, 0).setColor(alphaColor);
				Runnable[] vertices = {
						() -> buffer.addVertex(mat, -0.866F * f4, f3, -0.5F * f4).setColor(0, 0, 0, 0),
						() -> buffer.addVertex(mat, 0.866F * f4, f3, -0.5F * f4).setColor(0, 0, 0, 0),
						() -> buffer.addVertex(mat, 0, f3, 1F * f4).setColor(0, 0, 0, 0),
						() -> buffer.addVertex(mat, -0.866F * f4, f3, -0.5F * f4).setColor(0, 0, 0, 0)
				};
				triangleFan(center, vertices);
			}
		});
	}

	public static void triangleFan(Runnable center, Runnable... vertices) {
		triangleFan(center, Arrays.asList(vertices));
	}

	/**
	 * With a buffer in GL_TRIANGLES mode, emulates GL_TRIANGLE_FAN on the CPU.
	 * This is because batching of GL_TRIANGLE_FAN makes no sense (the vertices would bleed into one massive fan)
	 */
	public static void triangleFan(Runnable center, List<Runnable> vertices) {
		for (int i = 0; i < vertices.size() - 1; i++) {
			center.run();
			vertices.get(i).run();
			vertices.get(i + 1).run();
		}
	}

	public static void flatRectangle(VertexConsumer buffer, Matrix4fc mat,
			float xMin, float xMax, float y, float zMin, float zMax,
			int r, int g, int b, int a) {
		buffer.addVertex(mat, xMax, y, zMin).setColor(r, g, b, a);
		buffer.addVertex(mat, xMin, y, zMin).setColor(r, g, b, a);
		buffer.addVertex(mat, xMin, y, zMax).setColor(r, g, b, a);
		buffer.addVertex(mat, xMax, y, zMax).setColor(r, g, b, a);
	}

	/** Drawing code written against a vertex consumer, run later by the collector. */
	@FunctionalInterface
	public interface LegacyDraw {
		void draw(PoseStack ms, VertexConsumer buffer);
	}

	/**
	 * Submits drawing code that writes into a buffer of the given render type, the way the old
	 * {@code buffers.getBuffer(type)} code did.
	 * <p>
	 * PORT-26.3: the geometry is built later, from a copy of the pose the collector is given here, so the drawing code
	 * gets a fresh pose stack seeded with that pose and must not touch the caller's.
	 */
	public static void submit(PoseStack ms, SubmitNodeCollector collector, RenderType type, LegacyDraw draw) {
		collector.submitCustomGeometry(ms, type, (pose, buffer) -> {
			PoseStack ps = new PoseStack();
			ps.mulPose(pose.pose());
			draw.draw(ps, buffer);
		});
	}

	/** Submits an item stack for drawing, as {@code ItemRenderer#renderStatic} used to. */
	public static void submitItem(ItemStack stack, ItemDisplayContext context, PoseStack ms, SubmitNodeCollector collector,
			@Nullable Level level, int light, int overlay, int seed) {
		if (stack.isEmpty()) {
			return;
		}
		// PORT-26.3: resolved here rather than in an extract step; the resolver is cheap but this does allocate per frame
		ItemStackRenderState state = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack, context, level, null, seed);
		state.submit(ms, collector, light, overlay, 0);
	}

	private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
	@Nullable
	private static BlockModelResolver blockModelResolver;

	/** Submits a block state's model, tints included, as {@code BlockRenderDispatcher#renderSingleBlock} used to. */
	public static void submitBlockState(BlockState state, PoseStack ms, SubmitNodeCollector collector, int light, int overlay) {
		if (blockModelResolver == null) {
			blockModelResolver = new BlockModelResolver(Minecraft.getInstance().getModelManager());
		}
		BlockModelRenderState renderState = new BlockModelRenderState();
		blockModelResolver.update(renderState, state, BLOCK_DISPLAY_CONTEXT);
		renderState.submit(ms, collector, light, overlay, 0);
	}

	/** The baked model of a block state, for drawing its quads by hand. */
	public static BlockStateModel getBlockStateModel(BlockState state) {
		return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
	}

	/** The sprite a block state's particles use. */
	public static TextureAtlasSprite getParticleSprite(BlockState state) {
		return Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
	}

	/** A block atlas sprite by its texture name, e.g. {@code botania:block/mana_water}. */
	public static TextureAtlasSprite getBlockSprite(SpriteGetter sprites, Identifier texture) {
		return sprites.get(new SpriteId(AtlasIds.BLOCKS, texture));
	}

	private static RenderType blockRenderType(ChunkSectionLayer layer) {
		return switch (layer) {
			case SOLID -> RenderTypes.solidMovingBlock();
			case CUTOUT -> RenderTypes.cutoutMovingBlock();
			default -> RenderTypes.translucentMovingBlock();
		};
	}

	/**
	 * Submits every quad of a baked block model, each on its own layer's render type, with one colour (alpha included)
	 * forced on all of them: this is what {@code ModelBlockRenderer#renderModel} with a colour did.
	 */
	public static void submitBlockModel(BlockStateModel model, PoseStack ms, SubmitNodeCollector collector, int color, int light, int overlay) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(42L), parts);
		submitBlockModelParts(parts, ms, collector, color, light, overlay);
	}

	public static void submitBlockModelParts(List<BlockStateModelPart> parts, PoseStack ms, SubmitNodeCollector collector, int color, int light, int overlay) {
		Map<RenderType, List<BakedQuad>> byType = new LinkedHashMap<>();
		for (BlockStateModelPart part : parts) {
			for (Direction direction : Direction.values()) {
				for (BakedQuad quad : part.getQuads(direction)) {
					byType.computeIfAbsent(blockRenderType(quad.materialInfo().layer()), t -> new ArrayList<>()).add(quad);
				}
			}
			for (BakedQuad quad : part.getQuads(null)) {
				byType.computeIfAbsent(blockRenderType(quad.materialInfo().layer()), t -> new ArrayList<>()).add(quad);
			}
		}
		QuadInstance instance = new QuadInstance();
		instance.setColor(color);
		instance.setLightCoords(light);
		instance.setOverlayCoords(overlay);
		for (var entry : byType.entrySet()) {
			List<BakedQuad> quads = entry.getValue();
			collector.submitCustomGeometry(ms, entry.getKey(), (pose, buffer) -> {
				for (BakedQuad quad : quads) {
					buffer.putBakedQuad(pose, quad, instance);
				}
			});
		}
	}

	/**
	 * Submits an entity for drawing at the current pose, as {@code EntityRenderDispatcher#render} with zero offsets
	 * used to. Needs the frame's camera, so it draws nothing for the item form of a block.
	 */
	public static void submitEntity(Entity entity, float partialTick, PoseStack ms, SubmitNodeCollector collector, @Nullable CameraRenderState camera) {
		if (camera == null) {
			return;
		}
		EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		EntityRenderState state = dispatcher.extractEntity(entity, partialTick);
		dispatcher.submit(state, camera, 0, 0, 0, ms, collector);
	}

	/** Submits a baked block model with no colour change. */
	public static void submitBlockModel(BlockStateModel model, PoseStack ms, SubmitNodeCollector collector, int light, int overlay) {
		submitBlockModel(model, ms, collector, -1, light, overlay);
	}

	/** Submits every quad of a block model on one render type, whatever layer the quads belong to. */
	public static void submitBlockModel(BlockStateModel model, PoseStack ms, SubmitNodeCollector collector, RenderType type, int color, int light, int overlay) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(42L), parts);
		List<BakedQuad> quads = new ArrayList<>();
		for (BlockStateModelPart part : parts) {
			for (Direction direction : Direction.values()) {
				quads.addAll(part.getQuads(direction));
			}
			quads.addAll(part.getQuads(null));
		}
		QuadInstance instance = new QuadInstance();
		instance.setColor(color);
		instance.setLightCoords(light);
		instance.setOverlayCoords(overlay);
		collector.submitCustomGeometry(ms, type, (pose, buffer) -> {
			for (BakedQuad quad : quads) {
				buffer.putBakedQuad(pose, quad, instance);
			}
		});
	}

	/**
	 * Adds a line with a width to a lines buffer (the width is a vertex attribute in 26.3).
	 */
	public static void line(VertexConsumer buffer, PoseStack.Pose pose, float x1, float y1, float z1, float x2, float y2, float z2, int color, float width) {
		float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
		float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
		if (len > 0) {
			dx /= len;
			dy /= len;
			dz /= len;
		}
		buffer.addVertex(pose, x1, y1, z1).setColor(color).setNormal(pose, dx, dy, dz).setLineWidth(width);
		buffer.addVertex(pose, x2, y2, z2).setColor(color).setNormal(pose, dx, dy, dz).setLineWidth(width);
	}

	public static void renderProgressPie(GuiGraphicsExtractor gui, float partialTick, int x, int y, float progress, ItemStack stack) {
		int r = 10;
		int centerX = x + 8;
		int centerY = y + 8;
		int degs = (int) (360 * progress);
		float a = 0.5F + 0.2F * (Mth.cos(ClientTickHandler.getUiAnimationTicks() / 10) * 0.5F + 0.5F);

		// PORT-26.3: GUI drawing is collected as elements now; the pie is one custom element of coloured quads
		((GuiGraphicsExtractorAccessor) gui).botania_getGuiRenderState()
				.addGuiElement(new ProgressPie(new Matrix3x2f(gui.pose()), centerX, centerY, r, degs, a));

		gui.item(stack, x, y);
	}

	/** The progress pie around an item in the HUD. The fan is sent as quads, the shape the GUI pipeline draws. */
	private record ProgressPie(Matrix3x2fc pose, int centerX, int centerY, int radius, int degrees, float alpha) implements GuiElementRenderState {
		@Override
		public void buildVertices(VertexConsumer buffer) {
			int centerColor = ARGB.colorFromFloat(alpha, 0F, 0.5F, 0.5F);
			int edgeColor = ARGB.colorFromFloat(alpha, 0F, 1F, 0.5F);
			float lastX = centerX;
			float lastY = centerY - radius;
			for (int i = degrees - 1; i >= 0; i--) {
				float rad = (i - 90) / 180F * (float) Math.PI;
				float px = centerX + Mth.cos(rad) * radius;
				float py = centerY + Mth.sin(rad) * radius;
				buffer.addVertexWith2DPose(pose, centerX, centerY).setColor(centerColor);
				buffer.addVertexWith2DPose(pose, lastX, lastY).setColor(edgeColor);
				buffer.addVertexWith2DPose(pose, px, py).setColor(edgeColor);
				buffer.addVertexWith2DPose(pose, px, py).setColor(edgeColor);
				lastX = px;
				lastY = py;
			}
		}

		@Override
		public RenderPipeline pipeline() {
			return RenderPipelines.GUI;
		}

		@Override
		public TextureSetup textureSetup() {
			return TextureSetup.noTexture();
		}

		@Override
		public @Nullable ScreenRectangle scissorArea() {
			return null;
		}

		@Override
		public @Nullable ScreenRectangle bounds() {
			return new ScreenRectangle(centerX - radius, centerY - radius, radius * 2, radius * 2);
		}
	}

	/**
	 * Draws an item with every quad in one colour (alpha included).
	 * <p>
	 * PORT-26.3: there is no baked model to walk any more. The item's render state is resolved as usual and its
	 * quads are submitted as custom geometry with the colour forced on every vertex.
	 *
	 * @param color Must include alpha
	 */
	public static void renderItemCustomColor(LivingEntity entity, ItemStack stack, int color, PoseStack ms, SubmitNodeCollector collector, int light, int overlay) {
		ItemStackRenderState state = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForLiving(state, stack, ItemDisplayContext.NONE, entity);

		Map<RenderType, List<BakedQuad>> byType = new LinkedHashMap<>();
		ItemStackRenderStateAccessor layers = (ItemStackRenderStateAccessor) state;
		for (int i = 0; i < layers.botania_getActiveLayerCount(); i++) {
			var quads = ((LayerRenderStateAccessor) layers.botania_getLayers()[i]).botania_getQuads();
			for (BakedQuad quad : quads.all()) {
				byType.computeIfAbsent(quad.materialInfo().itemRenderType(), t -> new ArrayList<>()).add(quad);
			}
		}

		ms.pushPose();
		ms.translate(-0.5D, -0.5D, -0.5D);
		QuadInstance instance = new QuadInstance();
		instance.setColor(color);
		instance.setLightCoords(light);
		instance.setOverlayCoords(overlay);
		for (var entry : byType.entrySet()) {
			List<BakedQuad> quads = entry.getValue();
			collector.submitCustomGeometry(ms, entry.getKey(), (pose, buffer) -> {
				for (BakedQuad quad : quads) {
					buffer.putBakedQuad(pose, quad, instance);
				}
			});
		}
		ms.popPose();
	}

	/**
	 * Draw an icon into the buffer, using the {@link RenderHelper#ICON_OVERLAY} vertex format
	 *
	 * @param startX   Start x position in blocks
	 * @param startY   Start position in blocks
	 * @param endX     End x position in blocks
	 * @param endY     End y position in blocks
	 *
	 * @param uvStartX UV start x position in "pixels" (1/16th sprite size)
	 * @param uvStartY UV start position in "pixels" (1/16th sprite size)
	 * @param uvEndX   UV end x position in "pixels" (1/16th sprite size)
	 * @param uvEndY   UV end y position in "pixels" (1/16th sprite size)
	 */
	public static void renderIconFullBright(
			PoseStack ms, VertexConsumer buffer,
			float startX, float startY, float endX, float endY,
			float uvStartX, float uvStartY, float uvEndX, float uvEndY,
			TextureAtlasSprite icon, int color, float alpha, int light) {
		Matrix4f mat = ms.last().pose();
		float red = ((color >> 16) & 0xFF) / 255F;
		float green = ((color >> 8) & 0xFF) / 255F;
		float blue = (color & 0xFF) / 255F;

		buffer.addVertex(mat, startX, endY, 0).setColor(red, green, blue, alpha).setUv(icon.getU(uvStartX), icon.getV(uvEndY)).setLight(light);
		buffer.addVertex(mat, endX, endY, 0).setColor(red, green, blue, alpha).setUv(icon.getU(uvEndX), icon.getV(uvEndY)).setLight(light);
		buffer.addVertex(mat, endX, startY, 0).setColor(red, green, blue, alpha).setUv(icon.getU(uvEndX), icon.getV(uvStartY)).setLight(light);
		buffer.addVertex(mat, startX, startY, 0).setColor(red, green, blue, alpha).setUv(icon.getU(uvStartX), icon.getV(uvStartY)).setLight(light);
	}

	/**
	 * Draw an icon into the buffer, using the {@link RenderHelper#ICON_OVERLAY} vertex format
	 *
	 * @param uvStartX UV start x position in "pixels" (1/16th sprite size)
	 * @param uvStartY UV start position in "pixels" (1/16th sprite size)
	 * @param uvEndX   UV end x position in "pixels" (1/16th sprite size)
	 * @param uvEndY   UV end y position in "pixels" (1/16th sprite size)
	 */
	public static void renderIconCropped(
			PoseStack ms, VertexConsumer buffer,
			int uvStartX, int uvStartY, int uvEndX, int uvEndY,
			TextureAtlasSprite icon, int color, float alpha, int light) {
		renderIconFullBright(
				ms, buffer,
				uvStartX / 16F, uvStartY / 16F, uvEndX / 16F, uvEndY / 16F,
				uvStartX / 16F, uvStartY / 16F, uvEndX / 16F, uvEndY / 16F,
				icon, color, alpha, light
		);
	}

	/**
	 * Draw an icon into the buffer, using the {@link RenderHelper#ICON_OVERLAY} vertex format
	 * Renders the icon at a 1 block size with a full 16x16 UV
	 */
	public static void renderIconFullBright(
			PoseStack ms, VertexConsumer buffer,
			TextureAtlasSprite icon, int color, float alpha, int light) {
		renderIconCropped(
				ms, buffer,
				0, 0, 16, 16,
				icon, color, alpha, light
		);
	}

	/**
	 * Draw an icon into the buffer, using the {@link RenderHelper#ICON_OVERLAY} vertex format
	 * Renders the icon in fullbright, at a 1 block size with a full 16x16 UV
	 */
	public static void renderIconFullBright(
			PoseStack ms, VertexConsumer buffer,
			TextureAtlasSprite icon, int color, float alpha) {
		int fullbright = 0xF000F0;
		renderIconFullBright(ms, buffer, icon, color, alpha, fullbright);
	}

	/**
	 * Draw an icon into the buffer, using the {@link RenderHelper#ICON_OVERLAY} vertex format
	 * Renders the icon in fullbright, with no color modification, at a 1 block size with a full 16x16 UV
	 */
	public static void renderIconFullBright(
			PoseStack ms, VertexConsumer buffer,
			TextureAtlasSprite icon, float alpha) {
		renderIconFullBright(ms, buffer, icon, 0xFFFFFF, alpha);
	}

	/**
	 * Draws an item in the GUI.
	 * <p>
	 * PORT-26.3: the old version drew the item through a wrapped buffer to make it translucent. GUI items are
	 * collected and drawn by the game now, with no way to fade one, so the alpha is ignored until the GUI pass
	 * finds a way (the item is drawn fully instead of not at all).
	 */
	public static void renderGuiItemAlpha(GuiGraphicsExtractor gui, ItemStack stack, int x, int y, int alpha) {
		if (alpha > 0) {
			gui.item(stack, x, y);
		}
	}

	/*
	* Renders a transparent black box with a soft border. The parameters describe the inner box, there will also be drawn
	* another box that is 2px bigger in each direction
	*/
	public static void renderHUDBox(GuiGraphicsExtractor gui, int startX, int startY, int endX, int endY) {
		gui.fill(startX, startY, endX, endY, 0x40000000);
		gui.fill(startX - 2, startY - 2, endX + 2, endY + 2, 0x40000000);
	}

	/*
	* Renders an item and its name, vertically centered next to it. Renders nothing if the stack is empty
	* Note: The item renderer does not respect the PoseStack
	*/
	public static void renderItemWithName(GuiGraphicsExtractor gui, Font font, ItemStack itemStack, int startX, int startY, int color) {
		if (!itemStack.isEmpty()) {
			gui.text(font, itemStack.getHoverName(), startX + ITEM_AND_PADDING_WIDTH, startY + 4, color);
			gui.item(itemStack, startX, startY);
		}
	}

	public static void renderItemWithNameCentered(GuiGraphicsExtractor gui, Window window, Font font, ItemStack itemStack, int startY, int color) {
		int centerX = window.getGuiScaledWidth() / 2;
		int startX = centerX - itemWithNameWidth(itemStack, font) / 2;
		renderItemWithName(gui, font, itemStack, startX, startY, color);
	}

	/*
	* Returns the width of an item and its text, as rendered by renderItemWithName
	*/
	public static int itemWithNameWidth(ItemStack itemStack, Font font) {
		return ITEM_AND_PADDING_WIDTH + font.width(itemStack.getHoverName());
	}

	// Borrowed with permission from https://github.com/XFactHD/FramedBlocks/blob/14f468810fc416b39447512810f0aa86e1012335/src/main/java/xfacthd/framedblocks/client/util/GhostVertexConsumer.java
	// TODO: is this even still relevant? it doesn't exist anymore where it was borrowed from
	public record GhostVertexConsumer(VertexConsumer wrapped, int alpha) implements VertexConsumer {
		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			return wrapped.addVertex(x, y, z);
		}

		@Override
		public VertexConsumer setColor(int red, int green, int blue, int alpha) {
			return wrapped.setColor(red, green, blue, (alpha * this.alpha) / 0xFF);
		}

		@Override
		public VertexConsumer setColor(int color) {
			return setColor(ARGB.red(color), ARGB.green(color), ARGB.blue(color), ARGB.alpha(color));
		}

		@Override
		public VertexConsumer setUv(float u, float v) {
			return wrapped.setUv(u, v);
		}

		@Override
		public VertexConsumer setUv1(int u, int v) {
			return wrapped.setUv1(u, v);
		}

		@Override
		public VertexConsumer setUv2(int u, int v) {
			return wrapped.setUv2(u, v);
		}

		@Override
		public VertexConsumer setUv3(float u, float v) {
			return wrapped.setUv3(u, v);
		}

		@Override
		public VertexConsumer setNormal(float x, float y, float z) {
			return wrapped.setNormal(x, y, z);
		}

		@Override
		public VertexConsumer setLineWidth(float width) {
			return wrapped.setLineWidth(width);
		}
	}
}
