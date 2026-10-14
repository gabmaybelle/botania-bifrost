/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * PORT-26.3: the cauldron interaction maps became {@code CauldronInteraction.Dispatcher}, whose {@code put}
 * is package-private; Botania registers its undyeing and Phantom Ink interactions through it.
 */
@Mixin(CauldronInteraction.Dispatcher.class)
public interface CauldronInteractionDispatcherAccessor {
	@Invoker("put")
	void botania_put(Item item, CauldronInteraction interaction);
}
