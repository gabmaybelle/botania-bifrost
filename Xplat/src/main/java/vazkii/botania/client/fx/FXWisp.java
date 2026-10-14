/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import vazkii.botania.client.core.helper.CoreShaders;

public class FXWisp extends SingleQuadParticle {
	private final boolean depthTest;
	private final float moteParticleScale;
	private final int moteHalfLife;

	public FXWisp(ClientLevel world, double d, double d1, double d2, double xSpeed, double ySpeed, double zSpeed,
			float size, float red, float green, float blue, boolean depthTest, float maxAgeMul, boolean noClip, float g,
			TextureAtlasSprite sprite) {
		super(world, d, d1, d2, sprite);
		// super applies wiggle to motion so set it here instead
		xd = xSpeed;
		yd = ySpeed;
		zd = zSpeed;
		rCol = red;
		gCol = green;
		bCol = blue;
		alpha = 0.375F;
		gravity = g;
		quadSize = (this.random.nextFloat() * 0.5f + 0.5f) * 2 * size;
		moteParticleScale = quadSize;
		lifetime = (int) (maxAgeMul * 28 / (this.random.nextFloat() * 0.3f + 0.7f));
		this.depthTest = depthTest;

		moteHalfLife = lifetime / 2;
		setSize(0.01f, 0.01f);

		xo = x;
		yo = y;
		zo = z;
		this.hasPhysics = !noClip;
	}

	@Override
	public float getQuadSize(float scaleFactor) {
		float agescale = (float) age / moteHalfLife;
		if (agescale > 1) {
			agescale = 2 - agescale;
		}

		quadSize = moteParticleScale * agescale * 0.5f;
		return quadSize;
	}

	@Override
	protected int getLightCoords(float partialTicks) {
		return 0xF000F0;
	}

	@Override
	protected Layer getLayer() {
		return depthTest ? NORMAL_LAYER : DIW_LAYER;
	}

	// [VanillaCopy] of super, without drag when onGround is true
	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;

		if (this.age++ >= this.lifetime) {
			this.remove();
		}

		this.yd -= this.gravity;
		this.move(this.xd, this.yd, this.zd);
		if (gravity == 0) {
			this.xd *= 0.98;
			this.yd *= 0.98;
			this.zd *= 0.98;
		}
	}

	public void setGravity(float value) {
		gravity = value;
	}

	// PORT-26.3: see FXSparkle for the layer notes
	private static final Layer NORMAL_LAYER = new Layer(false, TextureAtlas.LOCATION_PARTICLES, CoreShaders.ADDITIVE_PARTICLE);
	private static final Layer DIW_LAYER = new Layer(false, TextureAtlas.LOCATION_PARTICLES, CoreShaders.ADDITIVE_PARTICLE_NO_DEPTH);
}
