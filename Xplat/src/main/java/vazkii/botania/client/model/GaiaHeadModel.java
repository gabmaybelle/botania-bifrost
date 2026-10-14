/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.SkullModelBase;

import java.util.List;
import java.util.Map;

/**
 * PORT-26.3: a model can no longer hand its drawing to another model (renderToBuffer is final), so the Gaia head
 * renderer picks the viewed skull's model itself. This stays as the registered model for the Gaia head's skull type
 * and draws nothing on its own.
 */
public class GaiaHeadModel extends SkullModelBase {
	public GaiaHeadModel() {
		super(new ModelPart(List.of(), Map.of()));
	}
}
