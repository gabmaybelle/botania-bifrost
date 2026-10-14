/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.world;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.function.BiConsumer;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class BotaniaFeatures {
	public static final ResourceKey<PlacedFeature> MYSTICAL_FLOWERS_PLACED_FEATURE =
			ResourceKey.create(Registries.PLACED_FEATURE, botaniaRL("mystical_flowers"));
	public static final ResourceKey<PlacedFeature> SHIMMERING_MUSHROOMS_PLACED_FEATURE =
			ResourceKey.create(Registries.PLACED_FEATURE, botaniaRL("shimmering_mushrooms"));

	// PORT-26.3: feature types are registered by their codec (BuiltInRegistries.FEATURE_TYPE)
	public static void registerFeatures(BiConsumer<MapCodec<? extends Feature>, Identifier> r) {
		r.accept(MysticalFlowerFeature.CODEC, botaniaRL("mystical_flower"));
	}

}
