/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.common.handler;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.item.BotaniaItems;

import java.util.function.BiConsumer;

public class CompostingData {
	public static void init(BiConsumer<ItemLike, Float> registrationMethod) {
		// common vanilla composting chances:
		final float chanceLowest = 0.3f;
		final float chanceLow = 0.5f;
		final float chanceMid = 0.65f;
		final float chanceHigh = 0.85f;
		// unused here: final float chanceHighest = 1.0f;

		// see https://github.com/VazkiiMods/Botania/issues/4263#issuecomment-1529130978
		ColorHelper.supportedColors().forEach(dyeColor -> {
			registrationMethod.accept(BotaniaItems.getPetal(dyeColor), chanceLowest);
			registrationMethod.accept(BotaniaBlocks.getPetalBlock(dyeColor), chanceLow);
			registrationMethod.accept(BotaniaBlocks.getMysticalFlower(dyeColor), chanceMid);
			registrationMethod.accept(BotaniaBlocks.getTallMysticalFlower(dyeColor), chanceMid);
			registrationMethod.accept(BotaniaBlocks.getShimmeringMushroom(dyeColor), chanceMid);
		});

		registrationMethod.accept(BotaniaBlocks.CELLULAR_BLOCK, chanceHigh);
	}

	// PORT-26.3: composting is a data component now (DataComponents.COMPOSTABLE) whose "layers" value is a data-driven
	// provider. Vanilla ships one provider per classic chance tier, so the chances above map onto those keys.
	public static ResourceKey<ContextIntProvider> compostableLayers(float chance) {
		if (chance >= 1.0f) {
			return ContextIntProviders.COMPOSTABLE_ALWAYS_ADD_ONE;
		} else if (chance >= 0.85f) {
			return ContextIntProviders.COMPOSTABLE_MEDIUM_HIGH;
		} else if (chance >= 0.65f) {
			return ContextIntProviders.COMPOSTABLE_MEDIUM;
		} else if (chance >= 0.5f) {
			return ContextIntProviders.COMPOSTABLE_LOW_MEDIUM;
		}
		return ContextIntProviders.COMPOSTABLE_LOW;
	}
}
