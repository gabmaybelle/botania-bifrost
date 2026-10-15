/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.world;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import vazkii.botania.client.core.SkyblockWorldInfo;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.CoreShaders;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.Random;

/**
 * PORT-26.3: the sky is drawn in its own render pass before the world, straight through a {@code RenderPass} (no
 * tesselator, no GL state). Each shape here lives in a GPU vertex buffer that is rewritten every frame it changes, the
 * pose goes in as the dynamic transform and the old shader colour is the transform's colour modulator.
 */
public class SkyblockSkyRenderer {
	private static final Identifier textureSkybox = Identifier.parse(ResourcesLib.MISC_SKYBOX);
	private static final Identifier textureRainbow = Identifier.parse(ResourcesLib.MISC_RAINBOW);
	private static final Identifier[] planetTextures = new Identifier[] {
			Identifier.parse(ResourcesLib.MISC_PLANET + "0.png"),
			Identifier.parse(ResourcesLib.MISC_PLANET + "1.png"),
			Identifier.parse(ResourcesLib.MISC_PLANET + "2.png"),
			Identifier.parse(ResourcesLib.MISC_PLANET + "3.png"),
			Identifier.parse(ResourcesLib.MISC_PLANET + "4.png"),
			Identifier.parse(ResourcesLib.MISC_PLANET + "5.png")
	};

	private static final int ANGLES = 90;
	private static final int RING_VERTICES = ANGLES * 2;
	private static final int VERTEX_SIZE = DefaultVertexFormat.POSITION_TEX.getVertexSize();

	private static GpuBuffer planetBuffer;
	private static GpuBuffer rayBuffer;
	private static GpuBuffer rainbowBuffer;

	public static boolean isGogSky() {
		Level world = Minecraft.getInstance().level;
		if (world == null) {
			return false;
		}
		boolean isGog = world.getLevelData() instanceof SkyblockWorldInfo skyblockInfo && skyblockInfo.isGardenOfGlass();
		return BotaniaConfig.client().enableFancySkybox()
				&& world.dimension() == Level.OVERWORLD
				&& (BotaniaConfig.client().enableFancySkyboxInNormalWorlds() || isGog);
	}

	private static GpuBuffer vertexBuffer(String label, int vertices) {
		return RenderSystem.getDevice().createBuffer(() -> label, GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, (long) vertices * VERTEX_SIZE);
	}

	private static final float[] RAY_ROT_SPEEDS = { 1F, 0.2F, 2F };
	private static final double[] RAY_FUZZ = { Math.PI * 10 / ANGLES, Math.PI * 14 / ANGLES, Math.PI * 6 / ANGLES };

	/**
	 * PORT-26.3: buffers can't be written while a render pass is open, and the extras draw inside vanilla's sky pass.
	 * {@code SkyRendererMixin} calls this before the pass opens: the planet quad (at height 100, scaled by the
	 * transform) and the rainbow ring are made once, the three ray rings (they move with time) every frame. The
	 * textures are fetched here too, since the first fetch uploads them.
	 */
	public static void prepare(float partialTicks) {
		TextureManager textures = Minecraft.getInstance().getTextureManager();
		textures.getTexture(textureSkybox);
		textures.getTexture(textureRainbow);
		for (Identifier planet : planetTextures) {
			textures.getTexture(planet);
		}
		if (planetBuffer == null) {
			try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(4 * VERTEX_SIZE)) {
				BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX);
				builder.addVertex(-1, 100, -1).setUv(0.0F, 0.0F);
				builder.addVertex(1, 100, -1).setUv(1.0F, 0.0F);
				builder.addVertex(1, 100, 1).setUv(1.0F, 1.0F);
				builder.addVertex(-1, 100, 1).setUv(0.0F, 1.0F);
				planetBuffer = staticBuffer("Botania planet", builder);
			}
		}
		if (rainbowBuffer == null) {
			try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(RING_VERTICES * VERTEX_SIZE)) {
				BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX);
				buildRing(builder, 10F, 2F, 0F, 0, 0, 0);
				rainbowBuffer = staticBuffer("Botania rainbow", builder);
			}
		}
		if (rayBuffer == null) {
			rayBuffer = vertexBuffer("Botania sky rays", RING_VERTICES * RAY_ROT_SPEEDS.length);
		}
		float time = ClientTickHandler.getEntityTicksInGame() + partialTicks;
		try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(RING_VERTICES * RAY_ROT_SPEEDS.length * VERTEX_SIZE)) {
			BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX);
			for (int p = 0; p < RAY_ROT_SPEEDS.length; p++) {
				buildRing(builder, 20F, 2F, 0F, RAY_ROT_SPEEDS[p] * 0.4F * time, RAY_FUZZ[p], 1);
			}
			upload(rayBuffer, builder);
		}
	}

	private static GpuBuffer staticBuffer(String label, BufferBuilder builder) {
		try (MeshData mesh = builder.buildOrThrow()) {
			return RenderSystem.getDevice().createBuffer(() -> label, GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
		}
	}

	private static void upload(GpuBuffer target, BufferBuilder builder) {
		try (MeshData mesh = builder.buildOrThrow()) {
			RenderSystem.getDevice().createCommandEncoder().writeToBuffer(target.slice(), mesh.vertexBuffer());
		}
	}

	/** A ring of quads around the Y axis, as the rays and the rainbow draw it. */
	private static void buildRing(BufferBuilder builder, float scale, float y, float y0, float baseAngle, double fuzzPer, float fuzzScale) {
		float uPer = 1F / 360F;
		float anglePer = 360F / ANGLES;
		for (int i = 0; i < ANGLES; i++) {
			int j = i;
			if (i % 2 == 0) {
				j--;
			}

			float ang = j * anglePer + baseAngle;
			float xp = (float) Math.cos(ang * Math.PI / 180F) * scale;
			float zp = (float) Math.sin(ang * Math.PI / 180F) * scale;
			float yo = (float) Math.sin(fuzzPer * j) * fuzzScale;

			float ut = ang * uPer;
			if (i % 2 == 0) {
				builder.addVertex(xp, yo + y0 + y, zp).setUv(ut, 1F);
				builder.addVertex(xp, yo + y0, zp).setUv(ut, 0);
			} else {
				builder.addVertex(xp, yo + y0, zp).setUv(ut, 0);
				builder.addVertex(xp, yo + y0 + y, zp).setUv(ut, 1F);
			}
		}
	}

	private static Matrix4f modelView(PoseStack ms) {
		return new Matrix4f(RenderSystem.getModelViewStack()).mul(ms.last().pose());
	}

	private static void drawQuads(RenderPass renderPass, String label, RenderPipeline pipeline, Identifier texture,
			GpuBufferSlice vertices, int vertexCount, Matrix4f modelView, Vector4f color) {
		AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(texture);
		RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
		int indexCount = vertexCount / 4 * 6;
		GpuBufferSlice transform = RenderSystem.getDynamicUniforms().writeTransform(modelView, color);
		renderPass.pushDebugGroup(() -> label);
		renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
		RenderSystem.bindDefaultUniforms(renderPass);
		renderPass.setUniform("DynamicTransforms", transform);
		renderPass.setUniform("Sampler0", tex.getTextureView(), tex.getSampler());
		renderPass.setVertexBuffer(0, vertices);
		renderPass.setIndexBuffer(indices.getBuffer(indexCount), indices.type());
		renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
		renderPass.popDebugGroup();
	}

	// PORT-26.3: the day fraction (celAng) comes from the sky state's sun angle; the old time-of-day fraction is gone
	public static void renderExtra(RenderPass renderPass, PoseStack ms, ClientLevel world, float celAng, float partialTicks, float insideVoid) {
		// Botania - Begin extra stuff
		float rain = 1.0F - world.getRainLevel(partialTicks);
		float effCelAng = celAng;
		if (celAng > 0.5) {
			effCelAng = 0.5F - (celAng - 0.5F);
		}

		// === Planets
		float scale = 20F;
		float lowA = Math.max(0F, effCelAng - 0.3F) * rain;
		float a = Math.max(0.1F, lowA);

		ms.pushPose();
		Vector4f planetColor = new Vector4f(1F, 1F, 1F, a * 4 * (1F - insideVoid));
		ms.rotate(new Quaternionf().rotateAxis(VecHelper.toRadians(90), 0.5F, 0.5F, 0F));
		if (planetBuffer == null || rayBuffer == null || rainbowBuffer == null) {
			return; // prepare hasn't run for this frame's sky
		}
		GpuBuffer planet = planetBuffer;
		for (int p = 0; p < planetTextures.length; p++) {
			Matrix4f mat = modelView(ms).scale(scale, 1F, scale);
			drawQuads(renderPass, "Botania planet", CoreShaders.SKY_TEXTURED_TRANSLUCENT, planetTextures[p], planet.slice(), 4, mat, planetColor);

			switch (p) {
				case 0 -> {
					ms.rotate(VecHelper.rotateX(70));
					scale = 12F;
				}
				case 1 -> {
					ms.rotate(VecHelper.rotateZ(120));
					scale = 15F;
				}
				case 2 -> {
					ms.rotate(new Quaternionf().rotateAxis(VecHelper.toRadians(80), 1, 0, 1));
					scale = 25F;
				}
				case 3 -> {
					ms.rotate(VecHelper.rotateZ(100));
					scale = 10F;
				}
				case 4 -> {
					ms.rotate(new Quaternionf().rotateAxis(VecHelper.toRadians(-60), 1, 0, 0.5F));
					scale = 40F;
				}
			}
		}
		ms.popPose();

		// === Rays
		scale = 20F;
		a = lowA;
		ms.pushPose();
		ms.translate(0, -1, 0);
		ms.rotate(VecHelper.rotateX(220));
		Vector4f rayColor = new Vector4f(1F, 1F, 1F, a);
		float rotSpeed = 1F;
		float rotSpeedMod = 0.4F;

		for (int p = 0; p < 3; p++) {
			float time = ClientTickHandler.getEntityTicksInGame() + partialTicks;
			ms.rotate(VecHelper.rotateY(time * 0.25F * rotSpeed * rotSpeedMod));

			// ring p was built by prepare with this ring's speed and fuzz
			GpuBufferSlice ring = rayBuffer.slice((long) p * RING_VERTICES * VERTEX_SIZE, (long) RING_VERTICES * VERTEX_SIZE);
			drawQuads(renderPass, "Botania sky rays", RenderPipelines.CELESTIAL, textureSkybox, ring, RING_VERTICES, modelView(ms), rayColor);

			switch (p) {
				case 0 -> {
					ms.rotate(VecHelper.rotateX(20));
					rayColor = new Vector4f(1F, 0.4F, 0.4F, a);
					rotSpeed = 0.2F;
				}
				case 1 -> {
					ms.rotate(VecHelper.rotateX(50));
					rayColor = new Vector4f(0.4F, 1F, 0.7F, a);
					rotSpeed = 2F;
				}
			}
		}
		ms.popPose();

		// === Rainbow
		ms.pushPose();
		scale = 10F;
		float effCelAng1 = celAng;
		if (effCelAng1 > 0.25F) {
			effCelAng1 = 1F - effCelAng1;
		}
		effCelAng1 = 0.25F - Math.min(0.25F, effCelAng1);

		long time = world.getDefaultClockTime() + 1000;
		int day = (int) (time / 24000L);
		Random rand = new Random(day * 0xFF);
		float angle1 = rand.nextFloat() * 360F;
		float angle2 = rand.nextFloat() * 360F;
		Vector4f rainbowColor = new Vector4f(1F, 1F, 1F, effCelAng1 * (1F - insideVoid));
		ms.rotate(VecHelper.rotateY(angle1));
		ms.rotate(VecHelper.rotateZ(angle2));

		drawQuads(renderPass, "Botania rainbow", CoreShaders.SKY_TEXTURED_TRANSLUCENT, textureRainbow, rainbowBuffer.slice(), RING_VERTICES, modelView(ms), rainbowColor);
		ms.popPose();
	}

	/** Six more passes over vanilla's star buffer, each turned and tinted differently. */
	public static void renderStars(RenderPass renderPass, PoseStack ms, GpuBuffer starBuffer, RenderSystem.AutoStorageIndexBuffer quadIndices,
			int starIndexCount, float celAng, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		float rain = 1.0F - mc.level.getRainLevel(partialTicks);
		float effCelAng = celAng;
		if (celAng > 0.5) {
			effCelAng = 0.5F - (celAng - 0.5F);
		}
		float alpha = rain * Math.max(0.1F, effCelAng * 2);

		float t = (ClientTickHandler.getEntityTicksInGame() + partialTicks + 2000) * 0.005F;
		ms.pushPose();

		drawStars(renderPass, ms, starBuffer, quadIndices, starIndexCount, Axis.YP, t * 3, new Vector4f(1F, 1F, 1F, alpha));
		drawStars(renderPass, ms, starBuffer, quadIndices, starIndexCount, Axis.YP, t, new Vector4f(0.5F, 1F, 1F, alpha));
		drawStars(renderPass, ms, starBuffer, quadIndices, starIndexCount, Axis.YP, t * 2, new Vector4f(1F, 0.75F, 0.75F, alpha));
		drawStars(renderPass, ms, starBuffer, quadIndices, starIndexCount, Axis.ZP, t * 3, new Vector4f(1F, 1F, 1F, 0.25F * alpha));
		drawStars(renderPass, ms, starBuffer, quadIndices, starIndexCount, Axis.ZP, t, new Vector4f(0.5F, 1F, 1F, 0.25F * alpha));
		drawStars(renderPass, ms, starBuffer, quadIndices, starIndexCount, Axis.ZP, t * 2, new Vector4f(1F, 0.75F, 0.75F, 0.25F * alpha));

		ms.popPose();
	}

	private static void drawStars(RenderPass renderPass, PoseStack ms, GpuBuffer starBuffer, RenderSystem.AutoStorageIndexBuffer quadIndices,
			int starIndexCount, Axis axis, float degrees, Vector4f color) {
		ms.pushPose();
		ms.rotateDegrees(axis, degrees);
		GpuBufferSlice transform = RenderSystem.getDynamicUniforms().writeTransform(modelView(ms), color);
		renderPass.pushDebugGroup(() -> "Botania stars");
		renderPass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.STARS));
		RenderSystem.bindDefaultUniforms(renderPass);
		renderPass.setUniform("DynamicTransforms", transform);
		renderPass.setVertexBuffer(0, starBuffer.slice());
		renderPass.setIndexBuffer(quadIndices.getBuffer(starIndexCount), quadIndices.type());
		renderPass.drawIndexed(starIndexCount, 1, 0, 0, 0);
		renderPass.popDebugGroup();
		ms.popPose();
	}
}
