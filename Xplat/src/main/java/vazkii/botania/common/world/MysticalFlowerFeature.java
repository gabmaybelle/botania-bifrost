/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import vazkii.botania.common.block.flower.MysticalFlowerBlock;

/**
 * This Feature is essentially the same as vanilla's
 * {@link net.minecraft.world.level.levelgen.feature.SimpleBlockFeature},
 * but any of Botania's mystical flowers have a chance to be replaced with their tall variant.
 * <p>
 * PORT-26.3: a feature is a record of its own settings now (no separate configuration class); the configured
 * feature JSON lists {@code tall_chance} and {@code to_place} next to the type.
 */
public record MysticalFlowerFeature(float tallChance, Holder<BlockStateProvider> toPlace) implements Feature {
	public static final MapCodec<MysticalFlowerFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.FLOAT.fieldOf("tall_chance").forGetter(MysticalFlowerFeature::tallChance),
			BlockStateProvider.CODEC.fieldOf("to_place").forGetter(MysticalFlowerFeature::toPlace)
	).apply(instance, MysticalFlowerFeature::new));

	@Override
	public MapCodec<MysticalFlowerFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos pos) {
		BlockState state = toPlace.value().getState(level, random, pos);
		if (state.canSurvive(level, pos)) {
			if (state.getBlock() instanceof MysticalFlowerBlock flower
					&& random.nextFloat() < tallChance) {
				if (!level.isEmptyBlock(pos.above())
						|| !(flower.getTallFlower() instanceof DoublePlantBlock doubleFlower)) {
					return false;
				}
				DoublePlantBlock.placeAt(level, doubleFlower.defaultBlockState(), pos, Block.UPDATE_CLIENTS);
			} else {
				level.setBlock(pos, state, Block.UPDATE_CLIENTS);
			}
			return true;
		} else {
			return false;
		}
	}
}
