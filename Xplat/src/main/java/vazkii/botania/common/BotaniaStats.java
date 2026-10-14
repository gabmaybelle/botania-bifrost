/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class BotaniaStats {
	public static final Identifier CORPOREA_ITEMS_REQUESTED =
			makeCustomStat("corporea_items_requested", StatFormatter.DEFAULT);

	public static final Identifier LUMINIZER_ONE_CM =
			makeCustomStat("luminizer_one_cm", StatFormatter.DISTANCE);

	public static final Identifier TINY_POTATOES_PETTED =
			makeCustomStat("tiny_potatoes_petted", StatFormatter.DEFAULT);

	public static final Identifier MANA_POOLS_CLEANED =
			makeCustomStat("mana_pools_cleaned", StatFormatter.DEFAULT);

	public static final Identifier MANA_LENSES_CLEANED =
			makeCustomStat("mana_lenes_cleaned", StatFormatter.DEFAULT);

	public static final Identifier PHANTOM_INK_CLEANED =
			makeCustomStat("phantom_ink_cleaned", StatFormatter.DEFAULT);

	public static void init() {

	}

	// [VanillaCopy] net.minecraft.stats.Stats#makeCustomStat, except applying Botania namespace instead of hardcoded Minecraft namespace
	private static Identifier makeCustomStat(String key, StatFormatter formatter) {
		Identifier resourcelocation = botaniaRL(key);
		Registry.register(BuiltInRegistries.CUSTOM_STAT, key, resourcelocation);
		Stats.CUSTOM.get(resourcelocation, formatter);
		return resourcelocation;
	}
}
