/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SpreadingSnowyBlock;

public class BotaniaGrassBlock extends SpreadingSnowyBlock {
	public BotaniaGrassBlock(Properties builder) {
		// PORT-26.3: the block it spreads from and reverts to
		super(builder, ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("dirt")));
	}

}
