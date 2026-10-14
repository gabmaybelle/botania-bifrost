/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * PORT-26.3: {@code FireBlock#setFlammable} went private in 26.1; Botania registers how its wood burns through it.
 */
@Mixin(FireBlock.class)
public interface FireBlockAccessor {
	@Invoker("setFlammable")
	void botania_setFlammable(Block block, int igniteOdds, int burnOdds);
}
