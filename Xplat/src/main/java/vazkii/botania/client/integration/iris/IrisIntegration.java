/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.iris;

import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;

import vazkii.botania.client.core.helper.CoreShaders;

/**
 * PORT-26.3: with a shader pack on, Iris draws each render pipeline with one of its own programs and logs
 * "Missing program ... in override list" for pipelines it doesn't know. Botania's wisps and sparkles are
 * translucent particles, so they get Iris's translucent particle program. Only call this when Iris is loaded.
 */
public final class IrisIntegration {
	private IrisIntegration() {}

	public static void init() {
		IrisApi iris = IrisApi.getInstance();
		iris.assignPipeline(CoreShaders.ADDITIVE_PARTICLE, IrisProgram.PARTICLES_TRANSLUCENT);
		iris.assignPipeline(CoreShaders.ADDITIVE_PARTICLE_NO_DEPTH, IrisProgram.PARTICLES_TRANSLUCENT);
		iris.assignPipeline(CoreShaders.FILM_GRAIN_PARTICLE, IrisProgram.PARTICLES_TRANSLUCENT);
	}
}
