/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.block.SpreadingSnowyBlock;

public class BotaniaGrassBlock extends SpreadingSnowyBlock {
	public static final MapCodec<BotaniaGrassBlock> CODEC = simpleCodec(BotaniaGrassBlock::new);

	public BotaniaGrassBlock(Properties builder) {
		super(builder);
	}

	@Override
	protected MapCodec<? extends SpreadingSnowyBlock> codec() {
		return CODEC;
	}
}
