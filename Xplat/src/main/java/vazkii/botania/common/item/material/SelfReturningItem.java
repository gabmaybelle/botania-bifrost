/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.material;

import net.minecraft.world.item.Item;

/**
 * Just a marker class, see {@link vazkii.botania.mixin.ItemMixin#returnSelf}
 */
@SuppressWarnings("JavadocReference")
public class SelfReturningItem extends Item {

	public SelfReturningItem(Item.Properties properties) {
		super(properties);
	}

	// PORT-26.3: hasCraftingRemainingItem is gone; ItemMixin alone makes the item return itself
}
