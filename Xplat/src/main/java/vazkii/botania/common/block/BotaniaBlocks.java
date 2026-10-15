/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block;

import net.minecraft.core.*;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.*;
import net.minecraft.core.dispenser.EquipmentDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block.PetalApothecary;
import vazkii.botania.api.internal.Colored;
import vazkii.botania.api.internal.OptionallyColored;
import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.api.state.enums.AlfheimPortalState;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.flower.generating.HydroangeasBlockEntity;
import vazkii.botania.common.block.corporea.*;
import vazkii.botania.common.block.dispenser.*;
import vazkii.botania.common.block.flower.*;
import vazkii.botania.common.block.mana.*;
import vazkii.botania.common.block.red_string.*;
import vazkii.botania.common.brew.BotaniaMobEffects;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.block.ColoredBlockItem;
import vazkii.botania.common.item.block.DecayableSpecialFlowerBlockItem;
import vazkii.botania.common.item.block.SpecialFlowerBlockItem;
import vazkii.botania.common.item.block.TinyPotatoBlockItem;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public final class BotaniaBlocks {

	private static final Map<String, Block> ALL = new LinkedHashMap<>(); // Preserve insertion order

	private static final BlockBehaviour.StateArgumentPredicate<EntityType<?>> NO_SPAWN = (state, world, pos, et) -> false;
	private static final BlockBehaviour.StatePredicate NEVER = (state, world, pos) -> false;

	// small mundane flowers
	public static final MysticalFlowerBlock WHITE_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.WHITE,
			// [VanillaCopy] Blocks.POPPY
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.PLANT)
					.noCollision()
					.instabreak()
					.offsetType(BlockBehaviour.OffsetType.XZ)
					.pushReaction(PushReaction.POPPED)
					.sound(SoundType.GRASS));
	public static final MysticalFlowerBlock ORANGE_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.ORANGE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock MAGENTA_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.MAGENTA,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock LIGHT_BLUE_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.LIGHT_BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock YELLOW_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.YELLOW,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock LIME_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.LIME,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock PINK_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.PINK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock GRAY_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock LIGHT_GRAY_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.LIGHT_GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock CYAN_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.CYAN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock PURPLE_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.PURPLE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock BLUE_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock BROWN_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.BROWN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock GREEN_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.GREEN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock RED_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.RED,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final MysticalFlowerBlock BLACK_MYSTICAL_FLOWER = makeMysticalFlower(DyeColor.BLACK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));

	// glimmering mundane flowers
	public static final GlimmeringFlowerBlock WHITE_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.WHITE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER)
					.lightLevel(constInt(15)));
	public static final GlimmeringFlowerBlock ORANGE_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.ORANGE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock MAGENTA_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.MAGENTA,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock LIGHT_BLUE_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.LIGHT_BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock YELLOW_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.YELLOW,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock LIME_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.LIME,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock PINK_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.PINK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock GRAY_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock LIGHT_GRAY_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.LIGHT_GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock CYAN_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.CYAN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock PURPLE_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.PURPLE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock BLUE_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock BROWN_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.BROWN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock GREEN_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.GREEN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock RED_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.RED,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));
	public static final GlimmeringFlowerBlock BLACK_GLIMMERING_FLOWER = makeGlimmeringFlower(DyeColor.BLACK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_GLIMMERING_FLOWER));

	// buried petals
	public static final Block WHITE_BURIED_PETAL = makeBuriedPetal(DyeColor.WHITE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER)
					.sound(SoundType.MOSS)
					.lightLevel(constInt(4)));
	public static final Block ORANGE_BURIED_PETAL = makeBuriedPetal(DyeColor.ORANGE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block MAGENTA_BURIED_PETAL = makeBuriedPetal(DyeColor.MAGENTA,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block LIGHT_BLUE_BURIED_PETAL = makeBuriedPetal(DyeColor.LIGHT_BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block YELLOW_BURIED_PETAL = makeBuriedPetal(DyeColor.YELLOW,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block LIME_BURIED_PETAL = makeBuriedPetal(DyeColor.LIME,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block PINK_BURIED_PETAL = makeBuriedPetal(DyeColor.PINK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block GRAY_BURIED_PETAL = makeBuriedPetal(DyeColor.GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block LIGHT_GRAY_BURIED_PETAL = makeBuriedPetal(DyeColor.LIGHT_GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block CYAN_BURIED_PETAL = makeBuriedPetal(DyeColor.CYAN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block PURPLE_BURIED_PETAL = makeBuriedPetal(DyeColor.PURPLE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block BLUE_BURIED_PETAL = makeBuriedPetal(DyeColor.BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block BROWN_BURIED_PETAL = makeBuriedPetal(DyeColor.BROWN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block GREEN_BURIED_PETAL = makeBuriedPetal(DyeColor.GREEN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block RED_BURIED_PETAL = makeBuriedPetal(DyeColor.RED,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));
	public static final Block BLACK_BURIED_PETAL = makeBuriedPetal(DyeColor.BLACK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_BURIED_PETAL));

	// floating mundane flowers
	public static final Block WHITE_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.WHITE,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.PLANT)
					.strength(0.5f)
					.sound(SoundType.GRAVEL)
					.lightLevel(state -> state.getValue(BotaniaStateProperties.DIMMED) ? 3 : 15)
	);
	public static final Block ORANGE_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.ORANGE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block MAGENTA_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.MAGENTA,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block LIGHT_BLUE_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.LIGHT_BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block YELLOW_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.YELLOW,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block LIME_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.LIME,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block PINK_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.PINK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block GRAY_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block LIGHT_GRAY_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.LIGHT_GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block CYAN_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.CYAN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block PURPLE_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.PURPLE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block BLUE_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block BROWN_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.BROWN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block GREEN_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.GREEN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block RED_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.RED,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block BLACK_FLOATING_FLOWER = makeFloatingMundaneFlower(DyeColor.BLACK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));

	// petal blocks
	public static final Block WHITE_PETAL_BLOCK = makePetalBlock(DyeColor.WHITE,
			// Note: map color of properties is set to dye color
			BlockBehaviour.Properties.of()
					.strength(0.4f)
					.sound(SoundType.MOSS));
	public static final Block ORANGE_PETAL_BLOCK = makePetalBlock(DyeColor.ORANGE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block MAGENTA_PETAL_BLOCK = makePetalBlock(DyeColor.MAGENTA,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block LIGHT_BLUE_PETAL_BLOCK = makePetalBlock(DyeColor.LIGHT_BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block YELLOW_PETAL_BLOCK = makePetalBlock(DyeColor.YELLOW,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block LIME_PETAL_BLOCK = makePetalBlock(DyeColor.LIME,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block PINK_PETAL_BLOCK = makePetalBlock(DyeColor.PINK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block GRAY_PETAL_BLOCK = makePetalBlock(DyeColor.GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block LIGHT_GRAY_PETAL_BLOCK = makePetalBlock(DyeColor.LIGHT_GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block CYAN_PETAL_BLOCK = makePetalBlock(DyeColor.CYAN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block PURPLE_PETAL_BLOCK = makePetalBlock(DyeColor.PURPLE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block BLUE_PETAL_BLOCK = makePetalBlock(DyeColor.BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block BROWN_PETAL_BLOCK = makePetalBlock(DyeColor.BROWN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block GREEN_PETAL_BLOCK = makePetalBlock(DyeColor.GREEN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block RED_PETAL_BLOCK = makePetalBlock(DyeColor.RED,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));
	public static final Block BLACK_PETAL_BLOCK = makePetalBlock(DyeColor.BLACK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_PETAL_BLOCK));

	// shimmering mushrooms
	public static final ShimmeringMushroomBlock WHITE_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.WHITE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER)
					.lightLevel(constInt(3))
					.offsetType(BlockBehaviour.OffsetType.NONE)
	);
	public static final ShimmeringMushroomBlock ORANGE_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.ORANGE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock MAGENTA_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.MAGENTA,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock LIGHT_BLUE_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.LIGHT_BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock YELLOW_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.YELLOW,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock LIME_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.LIME,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock PINK_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.PINK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock GRAY_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock LIGHT_GRAY_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.LIGHT_GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock CYAN_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.CYAN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock PURPLE_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.PURPLE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock BLUE_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock BROWN_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.BROWN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock GREEN_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.GREEN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock RED_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.RED,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));
	public static final ShimmeringMushroomBlock BLACK_SHIMMERING_MUSHROOM = makeShimmeringMushroom(DyeColor.BLACK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_SHIMMERING_MUSHROOM));

	// tall mundane flowers
	public static final Block WHITE_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.WHITE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block ORANGE_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.ORANGE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block MAGENTA_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.MAGENTA,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block LIGHT_BLUE_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.LIGHT_BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block YELLOW_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.YELLOW,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block LIME_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.LIME,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block PINK_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.PINK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block GRAY_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block LIGHT_GRAY_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.LIGHT_GRAY,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block CYAN_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.CYAN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block PURPLE_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.PURPLE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block BLUE_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.BLUE,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block BROWN_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.BROWN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block GREEN_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.GREEN,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block RED_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.RED,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));
	public static final Block BLACK_TALL_MYSTICAL_FLOWER = makeTallMysticalFlower(DyeColor.BLACK,
			BlockBehaviour.Properties.ofFullCopy(WHITE_TALL_MYSTICAL_FLOWER));

	// potted mundane flowers
	public static final Block POTTED_WHITE_MYSTICAL_FLOWER = makePottedMundaneFlower(WHITE_MYSTICAL_FLOWER);
	public static final Block POTTED_ORANGE_MYSTICAL_FLOWER = makePottedMundaneFlower(ORANGE_MYSTICAL_FLOWER);
	public static final Block POTTED_MAGENTA_MYSTICAL_FLOWER = makePottedMundaneFlower(MAGENTA_MYSTICAL_FLOWER);
	public static final Block POTTED_LIGHT_BLUE_MYSTICAL_FLOWER = makePottedMundaneFlower(LIGHT_BLUE_MYSTICAL_FLOWER);
	public static final Block POTTED_YELLOW_MYSTICAL_FLOWER = makePottedMundaneFlower(YELLOW_MYSTICAL_FLOWER);
	public static final Block POTTED_LIME_MYSTICAL_FLOWER = makePottedMundaneFlower(LIME_MYSTICAL_FLOWER);
	public static final Block POTTED_PINK_MYSTICAL_FLOWER = makePottedMundaneFlower(PINK_MYSTICAL_FLOWER);
	public static final Block POTTED_GRAY_MYSTICAL_FLOWER = makePottedMundaneFlower(GRAY_MYSTICAL_FLOWER);
	public static final Block POTTED_LIGHT_GRAY_MYSTICAL_FLOWER = makePottedMundaneFlower(LIGHT_GRAY_MYSTICAL_FLOWER);
	public static final Block POTTED_CYAN_MYSTICAL_FLOWER = makePottedMundaneFlower(CYAN_MYSTICAL_FLOWER);
	public static final Block POTTED_PURPLE_MYSTICAL_FLOWER = makePottedMundaneFlower(PURPLE_MYSTICAL_FLOWER);
	public static final Block POTTED_BLUE_MYSTICAL_FLOWER = makePottedMundaneFlower(BLUE_MYSTICAL_FLOWER);
	public static final Block POTTED_BROWN_MYSTICAL_FLOWER = makePottedMundaneFlower(BROWN_MYSTICAL_FLOWER);
	public static final Block POTTED_GREEN_MYSTICAL_FLOWER = makePottedMundaneFlower(GREEN_MYSTICAL_FLOWER);
	public static final Block POTTED_RED_MYSTICAL_FLOWER = makePottedMundaneFlower(RED_MYSTICAL_FLOWER);
	public static final Block POTTED_BLACK_MYSTICAL_FLOWER = makePottedMundaneFlower(BLACK_MYSTICAL_FLOWER);

	// potted shimmering flowers
	public static final Block POTTED_WHITE_GLIMMERING_FLOWER = makePottedGlimmeringFlower(WHITE_GLIMMERING_FLOWER);
	public static final Block POTTED_ORANGE_GLIMMERING_FLOWER = makePottedGlimmeringFlower(ORANGE_GLIMMERING_FLOWER);
	public static final Block POTTED_MAGENTA_GLIMMERING_FLOWER = makePottedGlimmeringFlower(MAGENTA_GLIMMERING_FLOWER);
	public static final Block POTTED_LIGHT_BLUE_GLIMMERING_FLOWER = makePottedGlimmeringFlower(LIGHT_BLUE_GLIMMERING_FLOWER);
	public static final Block POTTED_YELLOW_GLIMMERING_FLOWER = makePottedGlimmeringFlower(YELLOW_GLIMMERING_FLOWER);
	public static final Block POTTED_LIME_GLIMMERING_FLOWER = makePottedGlimmeringFlower(LIME_GLIMMERING_FLOWER);
	public static final Block POTTED_PINK_GLIMMERING_FLOWER = makePottedGlimmeringFlower(PINK_GLIMMERING_FLOWER);
	public static final Block POTTED_GRAY_GLIMMERING_FLOWER = makePottedGlimmeringFlower(GRAY_GLIMMERING_FLOWER);
	public static final Block POTTED_LIGHT_GRAY_GLIMMERING_FLOWER = makePottedGlimmeringFlower(LIGHT_GRAY_GLIMMERING_FLOWER);
	public static final Block POTTED_CYAN_GLIMMERING_FLOWER = makePottedGlimmeringFlower(CYAN_GLIMMERING_FLOWER);
	public static final Block POTTED_PURPLE_GLIMMERING_FLOWER = makePottedGlimmeringFlower(PURPLE_GLIMMERING_FLOWER);
	public static final Block POTTED_BLUE_GLIMMERING_FLOWER = makePottedGlimmeringFlower(BLUE_GLIMMERING_FLOWER);
	public static final Block POTTED_BROWN_GLIMMERING_FLOWER = makePottedGlimmeringFlower(BROWN_GLIMMERING_FLOWER);
	public static final Block POTTED_GREEN_GLIMMERING_FLOWER = makePottedGlimmeringFlower(GREEN_GLIMMERING_FLOWER);
	public static final Block POTTED_RED_GLIMMERING_FLOWER = makePottedGlimmeringFlower(RED_GLIMMERING_FLOWER);
	public static final Block POTTED_BLACK_GLIMMERING_FLOWER = makePottedGlimmeringFlower(BLACK_GLIMMERING_FLOWER);

	// potted shimmering mushrooms
	public static final Block POTTED_WHITE_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(WHITE_SHIMMERING_MUSHROOM);
	public static final Block POTTED_ORANGE_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(ORANGE_SHIMMERING_MUSHROOM);
	public static final Block POTTED_MAGENTA_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(MAGENTA_SHIMMERING_MUSHROOM);
	public static final Block POTTED_LIGHT_BLUE_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(LIGHT_BLUE_SHIMMERING_MUSHROOM);
	public static final Block POTTED_YELLOW_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(YELLOW_SHIMMERING_MUSHROOM);
	public static final Block POTTED_LIME_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(LIME_SHIMMERING_MUSHROOM);
	public static final Block POTTED_PINK_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(PINK_SHIMMERING_MUSHROOM);
	public static final Block POTTED_GRAY_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(GRAY_SHIMMERING_MUSHROOM);
	public static final Block POTTED_LIGHT_GRAY_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(LIGHT_GRAY_SHIMMERING_MUSHROOM);
	public static final Block POTTED_CYAN_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(CYAN_SHIMMERING_MUSHROOM);
	public static final Block POTTED_PURPLE_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(PURPLE_SHIMMERING_MUSHROOM);
	public static final Block POTTED_BLUE_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(BLUE_SHIMMERING_MUSHROOM);
	public static final Block POTTED_BROWN_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(BROWN_SHIMMERING_MUSHROOM);
	public static final Block POTTED_GREEN_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(GREEN_SHIMMERING_MUSHROOM);
	public static final Block POTTED_RED_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(RED_SHIMMERING_MUSHROOM);
	public static final Block POTTED_BLACK_SHIMMERING_MUSHROOM = makePottedShimmeringMushroom(BLACK_SHIMMERING_MUSHROOM);

	// special flowers (regular, floating, and potted; potentially each with petite variants)
	public static final Block PURE_DAISY = make(LibBlockNames.SUBTILE_PUREDAISY,
			props -> new SpecialFlowerBlock(
					BotaniaMobEffects.ABSOLUTION,
					1,
					props,
					() -> BotaniaBlockEntities.PURE_DAISY),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_PURE_DAISY = make(floating(LibBlockNames.SUBTILE_PUREDAISY),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.PURE_DAISY),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_PURE_DAISY = make(potted(LibBlockNames.SUBTILE_PUREDAISY),
			flowerPot(PURE_DAISY, 0), flowerPotProperties());

	public static final Block MANASTAR = make(LibBlockNames.SUBTILE_MANASTAR,
			props -> new ManastarBlock(
					MobEffects.GLOWING,
					10,
					props,
					() -> BotaniaBlockEntities.MANASTAR),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_MANASTAR = make(floating(LibBlockNames.SUBTILE_MANASTAR),
			props -> new FloatingManastarBlock(
					props,
					() -> BotaniaBlockEntities.MANASTAR),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_MANASTAR = make(potted(LibBlockNames.SUBTILE_MANASTAR),
			flowerPot(MANASTAR, 0), flowerPotProperties());

	public static final Block HYDROANGEAS = make(LibBlockNames.SUBTILE_HYDROANGEAS,
			props -> new SlowGeneratingFlowerWithCooldownBlock(
					MobEffects.UNLUCK,
					10,
					props,
					() -> BotaniaBlockEntities.HYDROANGEAS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_HYDROANGEAS = make(floating(LibBlockNames.SUBTILE_HYDROANGEAS),
			props -> new FloatingSlowGeneratingFlowerWithCooldownBlock(
					props,
					() -> BotaniaBlockEntities.HYDROANGEAS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_HYDROANGEAS = make(potted(LibBlockNames.SUBTILE_HYDROANGEAS),
			flowerPot(HYDROANGEAS, 0), flowerPotProperties());

	public static final Block ENDOFLAME = make(LibBlockNames.SUBTILE_ENDOFLAME,
			props -> new SlowGeneratingFlowerWithCooldownBlock(
					MobEffects.SLOWNESS,
					10,
					props,
					() -> BotaniaBlockEntities.ENDOFLAME),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_ENDOFLAME = make(floating(LibBlockNames.SUBTILE_ENDOFLAME),
			props -> new FloatingSlowGeneratingFlowerWithCooldownBlock(
					props,
					() -> BotaniaBlockEntities.ENDOFLAME),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_ENDOFLAME = make(potted(LibBlockNames.SUBTILE_ENDOFLAME),
			flowerPot(ENDOFLAME, 0), flowerPotProperties());

	public static final Block THERMALILY = make(LibBlockNames.SUBTILE_THERMALILY,
			props -> new SlowGeneratingFlowerWithCooldownBlock(
					MobEffects.FIRE_RESISTANCE,
					120,
					props,
					() -> BotaniaBlockEntities.THERMALILY),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_THERMALILY = make(floating(LibBlockNames.SUBTILE_THERMALILY),
			props -> new FloatingSlowGeneratingFlowerWithCooldownBlock(
					props,
					() -> BotaniaBlockEntities.THERMALILY),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_THERMALILY = make(potted(LibBlockNames.SUBTILE_THERMALILY),
			flowerPot(THERMALILY, 0), flowerPotProperties());

	public static final Block ROSA_ARCANA = make(LibBlockNames.SUBTILE_ARCANE_ROSE,
			props -> new SpecialFlowerBlock(MobEffects.LUCK,
					64,
					props,
					() -> BotaniaBlockEntities.ROSA_ARCANA),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_ROSA_ARCANA = make(floating(LibBlockNames.SUBTILE_ARCANE_ROSE),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.ROSA_ARCANA),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_ROSA_ARCANA = make(potted(LibBlockNames.SUBTILE_ARCANE_ROSE),
			flowerPot(ROSA_ARCANA, 0), flowerPotProperties());

	public static final Block MUNCHDEW = make(LibBlockNames.SUBTILE_MUNCHDEW,
			props -> new SlowGeneratingFlowerWithCooldownBlock(
					MobEffects.SLOW_FALLING,
					300,
					props,
					() -> BotaniaBlockEntities.MUNCHDEW),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_MUNCHDEW = make(floating(LibBlockNames.SUBTILE_MUNCHDEW),
			props -> new FloatingSlowGeneratingFlowerWithCooldownBlock(
					props,
					() -> BotaniaBlockEntities.MUNCHDEW),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_MUNCHDEW = make(potted(LibBlockNames.SUBTILE_MUNCHDEW),
			flowerPot(MUNCHDEW, 0), flowerPotProperties());

	public static final Block ENTROPINNYUM = make(LibBlockNames.SUBTILE_ENTROPINNYUM,
			props -> new SpecialFlowerBlock(
					MobEffects.RESISTANCE,
					72,
					props,
					() -> BotaniaBlockEntities.ENTROPINNYUM),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_ENTROPINNYUM = make(floating(LibBlockNames.SUBTILE_ENTROPINNYUM),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.ENTROPINNYUM),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_ENTROPINNYUM = make(potted(LibBlockNames.SUBTILE_ENTROPINNYUM),
			flowerPot(ENTROPINNYUM, 0), flowerPotProperties());

	public static final Block KEKIMURUS = make(LibBlockNames.SUBTILE_KEKIMURUS,
			props -> new SpecialFlowerBlock(MobEffects.SATURATION,
					15,
					props,
					() -> BotaniaBlockEntities.KEKIMURUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_KEKIMURUS = make(floating(LibBlockNames.SUBTILE_KEKIMURUS),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.KEKIMURUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_KEKIMURUS = make(potted(LibBlockNames.SUBTILE_KEKIMURUS),
			flowerPot(KEKIMURUS, 0), flowerPotProperties());

	public static final Block GOURMARYLLIS = make(LibBlockNames.SUBTILE_GOURMARYLLIS,
			props -> new SlowGeneratingFlowerBlock(
					MobEffects.HUNGER,
					180,
					props,
					() -> BotaniaBlockEntities.GOURMARYLLIS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_GOURMARYLLIS = make(floating(LibBlockNames.SUBTILE_GOURMARYLLIS),
			props -> new FloatingSlowGeneratingFlowerBlock(
					props,
					() -> BotaniaBlockEntities.GOURMARYLLIS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_GOURMARYLLIS = make(potted(LibBlockNames.SUBTILE_GOURMARYLLIS),
			flowerPot(GOURMARYLLIS, 0), flowerPotProperties());

	public static final Block NARSLIMMUS = make(LibBlockNames.SUBTILE_NARSLIMMUS,
			props -> new SpecialFlowerBlock(BotaniaMobEffects.FEATHER_FEET,
					240,
					props,
					() -> BotaniaBlockEntities.NARSLIMMUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_NARSLIMMUS = make(floating(LibBlockNames.SUBTILE_NARSLIMMUS),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.NARSLIMMUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_NARSLIMMUS = make(potted(LibBlockNames.SUBTILE_NARSLIMMUS),
			flowerPot(NARSLIMMUS, 0), flowerPotProperties());

	public static final Block SPECTROLUS = make(LibBlockNames.SUBTILE_SPECTROLUS,
			props -> new SpecialFlowerBlock(
					MobEffects.BLINDNESS,
					240,
					props,
					() -> BotaniaBlockEntities.SPECTROLUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_SPECTROLUS = make(floating(LibBlockNames.SUBTILE_SPECTROLUS),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.SPECTROLUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_SPECTROLUS = make(potted(LibBlockNames.SUBTILE_SPECTROLUS),
			flowerPot(SPECTROLUS, 0), flowerPotProperties());

	public static final Block DANDELIFEON = make(LibBlockNames.SUBTILE_DANDELIFEON,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.NAUSEA,
					240,
					props,
					() -> BotaniaBlockEntities.DANDELIFEON),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_DANDELIFEON = make(floating(LibBlockNames.SUBTILE_DANDELIFEON),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.DANDELIFEON),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_DANDELIFEON = make(potted(LibBlockNames.SUBTILE_DANDELIFEON),
			flowerPot(DANDELIFEON, 0), flowerPotProperties());

	public static final Block RAFFLOWSIA = make(LibBlockNames.SUBTILE_RAFFLOWSIA,
			props -> new SpecialFlowerBlock(
					MobEffects.HEALTH_BOOST,
					18,
					props,
					() -> BotaniaBlockEntities.RAFFLOWSIA),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_RAFFLOWSIA = make(floating(LibBlockNames.SUBTILE_RAFFLOWSIA),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.RAFFLOWSIA),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_RAFFLOWSIA = make(potted(LibBlockNames.SUBTILE_RAFFLOWSIA),
			flowerPot(RAFFLOWSIA, 0), flowerPotProperties());

	public static final Block SHULK_ME_NOT = make(LibBlockNames.SUBTILE_SHULK_ME_NOT,
			props -> new SpecialFlowerBlock(
					MobEffects.LEVITATION,
					72,
					props,
					() -> BotaniaBlockEntities.SHULK_ME_NOT),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_SHULK_ME_NOT = make(floating(LibBlockNames.SUBTILE_SHULK_ME_NOT),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.SHULK_ME_NOT),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_SHULK_ME_NOT = make(potted(LibBlockNames.SUBTILE_SHULK_ME_NOT),
			flowerPot(SHULK_ME_NOT, 0), flowerPotProperties());

	public static final Block BELLETHORNE = make(LibBlockNames.SUBTILE_BELLETHORN,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.WITHER,
					10,
					props,
					() -> BotaniaBlockEntities.BELLETHORNE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block BELLETHORNE_PETITE = make(petite(LibBlockNames.SUBTILE_BELLETHORN),
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.WITHER,
					10,
					props,
					() -> BotaniaBlockEntities.BELLETHORNE_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_BELLETHORNE = make(floating(LibBlockNames.SUBTILE_BELLETHORN),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.BELLETHORNE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block FLOATING_BELLETHORNE_PETITE = make(petite(floating(LibBlockNames.SUBTILE_BELLETHORN)),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.BELLETHORNE_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_BELLETHORNE = make(potted(LibBlockNames.SUBTILE_BELLETHORN),
			flowerPot(BELLETHORNE, 0), flowerPotProperties());
	public static final Block POTTED_BELLETHORNE_PETITE = make(petite(potted(LibBlockNames.SUBTILE_BELLETHORN)),
			flowerPot(BELLETHORNE_PETITE, 0), flowerPotProperties());

	public static final Block BERGAMUTE = make(LibBlockNames.SUBTILE_BERGAMUTE,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.BLINDNESS,
					10,
					props,
					() -> BotaniaBlockEntities.BERGAMUTE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_BERGAMUTE = make(floating(LibBlockNames.SUBTILE_BERGAMUTE),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.BERGAMUTE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_BERGAMUTE = make(potted(LibBlockNames.SUBTILE_BERGAMUTE),
			flowerPot(BERGAMUTE, 0), flowerPotProperties());

	public static final Block DREADTHORNE = make(LibBlockNames.SUBTILE_DREADTHORN,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.WITHER,
					10,
					props,
					() -> BotaniaBlockEntities.DREADTHORN),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_DREADTHORNE = make(floating(LibBlockNames.SUBTILE_DREADTHORN),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.DREADTHORN),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_DREADTHORNE = make(potted(LibBlockNames.SUBTILE_DREADTHORN),
			flowerPot(DREADTHORNE, 0), flowerPotProperties());

	public static final Block HEISEI_DREAM = make(LibBlockNames.SUBTILE_HEISEI_DREAM,
			props -> new SpecialFlowerBlock(
					BotaniaMobEffects.SOUL_CROSS,
					300,
					props,
					() -> BotaniaBlockEntities.HEISEI_DREAM),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_HEISEI_DREAM = make(floating(LibBlockNames.SUBTILE_HEISEI_DREAM),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.HEISEI_DREAM),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_HEISEI_DREAM = make(potted(LibBlockNames.SUBTILE_HEISEI_DREAM),
			flowerPot(HEISEI_DREAM, 0), flowerPotProperties());

	public static final Block TIGERSEYE = make(LibBlockNames.SUBTILE_TIGERSEYE,
			props -> new SpecialFlowerBlock(
					MobEffects.STRENGTH,
					90,
					props,
					() -> BotaniaBlockEntities.TIGERSEYE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_TIGERSEYE = make(floating(LibBlockNames.SUBTILE_TIGERSEYE),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.TIGERSEYE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_TIGERSEYE = make(potted(LibBlockNames.SUBTILE_TIGERSEYE),
			flowerPot(TIGERSEYE, 0), flowerPotProperties());

	public static final Block JADED_AMARANTHUS = make(LibBlockNames.SUBTILE_JADED_AMARANTHUS,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.INSTANT_HEALTH,
					1,
					props,
					() -> BotaniaBlockEntities.JADED_AMARANTHUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_JADED_AMARANTHUS = make(floating(LibBlockNames.SUBTILE_JADED_AMARANTHUS),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.JADED_AMARANTHUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_JADED_AMARANTHUS = make(potted(LibBlockNames.SUBTILE_JADED_AMARANTHUS),
			flowerPot(JADED_AMARANTHUS, 0), flowerPotProperties());

	public static final Block ORECHID = make(LibBlockNames.SUBTILE_ORECHID,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.HASTE,
					10,
					props,
					() -> BotaniaBlockEntities.ORECHID),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_ORECHID = make(floating(LibBlockNames.SUBTILE_ORECHID),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.ORECHID),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_ORECHID = make(potted(LibBlockNames.SUBTILE_ORECHID),
			flowerPot(ORECHID, 0), flowerPotProperties());

	public static final Block FALLEN_KANADE = make(LibBlockNames.SUBTILE_FALLEN_KANADE,
			props -> new SpecialFlowerBlock(
					MobEffects.REGENERATION,
					90,
					props,
					() -> BotaniaBlockEntities.FALLEN_KANADE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_FALLEN_KANADE = make(floating(LibBlockNames.SUBTILE_FALLEN_KANADE),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.FALLEN_KANADE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_FALLEN_KANADE = make(potted(LibBlockNames.SUBTILE_FALLEN_KANADE),
			flowerPot(FALLEN_KANADE, 0), flowerPotProperties());

	public static final Block EXOFLAME = make(LibBlockNames.SUBTILE_EXOFLAME,
			props -> new SpecialFlowerBlock(MobEffects.SPEED,
					240,
					props,
					() -> BotaniaBlockEntities.EXOFLAME),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_EXOFLAME = make(floating(LibBlockNames.SUBTILE_EXOFLAME),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.EXOFLAME),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_EXOFLAME = make(potted(LibBlockNames.SUBTILE_EXOFLAME),
			flowerPot(EXOFLAME, 0), flowerPotProperties());

	public static final Block AGRICARNATION = make(LibBlockNames.SUBTILE_AGRICARNATION,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.ABSORPTION,
					48,
					props,
					() -> BotaniaBlockEntities.AGRICARNATION),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block AGRICARNATION_PETITE = make(petite(LibBlockNames.SUBTILE_AGRICARNATION),
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.ABSORPTION,
					48,
					props,
					() -> BotaniaBlockEntities.AGRICARNATION_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_AGRICARNATION = make(floating(LibBlockNames.SUBTILE_AGRICARNATION),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.AGRICARNATION),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block FLOATING_AGRICARNATION_PETITE = make(petite(floating(LibBlockNames.SUBTILE_AGRICARNATION)),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.AGRICARNATION_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_AGRICARNATION = make(potted(LibBlockNames.SUBTILE_AGRICARNATION),
			flowerPot(AGRICARNATION, 0), flowerPotProperties());
	public static final Block POTTED_AGRICARNATION_PETITE = make(petite(potted(LibBlockNames.SUBTILE_AGRICARNATION)),
			flowerPot(AGRICARNATION_PETITE, 0), flowerPotProperties());

	public static final Block HOPPERHOCK = make(LibBlockNames.SUBTILE_HOPPERHOCK,
			props -> new HopperhockBlock(MobEffects.SPEED,
					30,
					props,
					() -> BotaniaBlockEntities.HOPPERHOCK),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block HOPPERHOCK_PETITE = make(petite(LibBlockNames.SUBTILE_HOPPERHOCK),
			props -> new HopperhockBlock(
					MobEffects.SPEED,
					30,
					props,
					() -> BotaniaBlockEntities.HOPPERHOCK_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_HOPPERHOCK = make(floating(LibBlockNames.SUBTILE_HOPPERHOCK),
			props -> new FloatingHopperhockBlock(
					props,
					() -> BotaniaBlockEntities.HOPPERHOCK),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block FLOATING_HOPPERHOCK_PETITE = make(petite(floating(LibBlockNames.SUBTILE_HOPPERHOCK)),
			props -> new FloatingHopperhockBlock(
					props,
					() -> BotaniaBlockEntities.HOPPERHOCK_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_HOPPERHOCK = make(potted(LibBlockNames.SUBTILE_HOPPERHOCK),
			flowerPot(HOPPERHOCK, 0), flowerPotProperties());
	public static final Block POTTED_HOPPERHOCK_PETITE = make(petite(potted(LibBlockNames.SUBTILE_HOPPERHOCK)),
			flowerPot(HOPPERHOCK_PETITE, 0), flowerPotProperties());

	public static final Block TANGLEBERRIE = make(LibBlockNames.SUBTILE_TANGLEBERRIE,
			props -> new SpecialFlowerBlock(
					BotaniaMobEffects.BLOODTHIRST,
					120,
					props,
					() -> BotaniaBlockEntities.TANGLEBERRIE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block TANGLEBERRIE_PETITE = make(petite(LibBlockNames.SUBTILE_TANGLEBERRIE),
			props -> new SpecialFlowerBlock(
					BotaniaMobEffects.BLOODTHIRST,
					120,
					props,
					() -> BotaniaBlockEntities.TANGLEBERRIE_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_TANGLEBERRIE = make(floating(LibBlockNames.SUBTILE_TANGLEBERRIE),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.TANGLEBERRIE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block FLOATING_TANGLEBERRIE_PETITE = make(petite(floating(LibBlockNames.SUBTILE_TANGLEBERRIE)),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.TANGLEBERRIE_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_TANGLEBERRIE = make(potted(LibBlockNames.SUBTILE_TANGLEBERRIE),
			flowerPot(TANGLEBERRIE, 0), flowerPotProperties());
	public static final Block POTTED_TANGLEBERRIE_PETITE = make(petite(potted(LibBlockNames.SUBTILE_TANGLEBERRIE)),
			flowerPot(TANGLEBERRIE_PETITE, 0), flowerPotProperties());

	public static final Block JIYUULIA = make(LibBlockNames.SUBTILE_JIYUULIA,
			props -> new SpecialFlowerBlock(
					BotaniaMobEffects.EMPTINESS,
					120,
					props,
					() -> BotaniaBlockEntities.JIYUULIA),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block JIYUULIA_PETITE = make(petite(LibBlockNames.SUBTILE_JIYUULIA),
			props -> new SpecialFlowerBlock(
					BotaniaMobEffects.EMPTINESS,
					120,
					props,
					() -> BotaniaBlockEntities.JIYUULIA_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_JIYUULIA = make(floating(LibBlockNames.SUBTILE_JIYUULIA),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.JIYUULIA),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block FLOATING_JIYUULIA_PETITE = make(petite(floating(LibBlockNames.SUBTILE_JIYUULIA)),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.JIYUULIA_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_JIYULLIA = make(potted(LibBlockNames.SUBTILE_JIYUULIA),
			flowerPot(JIYUULIA, 0), flowerPotProperties());
	public static final Block POTTED_JIYUULIA_PETITE = make(petite(potted(LibBlockNames.SUBTILE_JIYUULIA)),
			flowerPot(JIYUULIA_PETITE, 0), flowerPotProperties());

	public static final Block RANNUNCARPUS = make(LibBlockNames.SUBTILE_RANNUNCARPUS,
			props -> new RannuncarpusBlock(
					MobEffects.JUMP_BOOST,
					30,
					props,
					() -> BotaniaBlockEntities.RANNUNCARPUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block RANNUNCARPUS_PETITE = make(petite(LibBlockNames.SUBTILE_RANNUNCARPUS),
			props -> new RannuncarpusBlock(
					MobEffects.JUMP_BOOST,
					30,
					props,
					() -> BotaniaBlockEntities.RANNUNCARPUS_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_RANNUNCARPUS = make(floating(LibBlockNames.SUBTILE_RANNUNCARPUS),
			props -> new FloatingRannuncarpusBlock(
					props,
					() -> BotaniaBlockEntities.RANNUNCARPUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block FLOATING_RANNUNCARPUS_PETITE = make(petite(floating(LibBlockNames.SUBTILE_RANNUNCARPUS)),
			props -> new FloatingRannuncarpusBlock(
					props,
					() -> BotaniaBlockEntities.RANNUNCARPUS_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_RANNUNCARPUS = make(potted(LibBlockNames.SUBTILE_RANNUNCARPUS),
			flowerPot(RANNUNCARPUS, 0), flowerPotProperties());
	public static final Block POTTED_RANNUNCARPUS_PETITE = make(petite(potted(LibBlockNames.SUBTILE_RANNUNCARPUS)),
			flowerPot(RANNUNCARPUS_PETITE, 0), flowerPotProperties());

	public static final Block HYACIDUS = make(LibBlockNames.SUBTILE_HYACIDUS,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.POISON,
					48,
					props,
					() -> BotaniaBlockEntities.HYACIDUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_HYACIDUS = make(floating(LibBlockNames.SUBTILE_HYACIDUS),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.HYACIDUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_HYACIDUS = make(potted(LibBlockNames.SUBTILE_HYACIDUS),
			flowerPot(HYACIDUS, 0), flowerPotProperties());

	public static final Block POLLIDISIAC = make(LibBlockNames.SUBTILE_POLLIDISIAC,
			props -> new PollidisiacBlock(
					MobEffects.HASTE,
					369,
					props,
					() -> BotaniaBlockEntities.POLLIDISIAC),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_POLLIDISIAC = make(floating(LibBlockNames.SUBTILE_POLLIDISIAC),
			props -> new FloatingPollidisiacBlock(
					props,
					() -> BotaniaBlockEntities.POLLIDISIAC),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_POLLIDISIAC = make(potted(LibBlockNames.SUBTILE_POLLIDISIAC),
			flowerPot(POLLIDISIAC, 0), flowerPotProperties());

	public static final Block CLAYCONIA = make(LibBlockNames.SUBTILE_CLAYCONIA,
			props -> new SpecialFlowerBlock(
					MobEffects.WEAKNESS,
					30,
					props,
					() -> BotaniaBlockEntities.CLAYCONIA),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block CLAYCONIA_PETITE = make(petite(LibBlockNames.SUBTILE_CLAYCONIA),
			props -> new SpecialFlowerBlock(
					MobEffects.WEAKNESS,
					30,
					props,
					() -> BotaniaBlockEntities.CLAYCONIA_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_CLAYCONIA = make(floating(LibBlockNames.SUBTILE_CLAYCONIA),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.CLAYCONIA),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block FLOATING_CLAYCONIA_PETITE = make(petite(floating(LibBlockNames.SUBTILE_CLAYCONIA)),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.CLAYCONIA_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_CLAYCONIA = make(potted(LibBlockNames.SUBTILE_CLAYCONIA),
			flowerPot(CLAYCONIA, 0), flowerPotProperties());
	public static final Block POTTED_CLAYCONIA_PETITE = make(petite(potted(LibBlockNames.SUBTILE_CLAYCONIA)),
			flowerPot(CLAYCONIA_PETITE, 0), flowerPotProperties());

	public static final Block LOONIUM = make(LibBlockNames.SUBTILE_LOONIUM,
			props -> new PoweredSpecialFlowerBlock(
					BotaniaMobEffects.ALLURE,
					900,
					props,
					() -> BotaniaBlockEntities.LOONIUM),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_LOONIUM = make(floating(LibBlockNames.SUBTILE_LOONIUM),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.LOONIUM),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_LOONIUM = make(potted(LibBlockNames.SUBTILE_LOONIUM),
			flowerPot(LOONIUM, 0), flowerPotProperties());

	public static final Block DAFFOMILL = make(LibBlockNames.SUBTILE_DAFFOMILL,
			props -> new DaffomillBlock(
					MobEffects.LEVITATION,
					6,
					props,
					() -> BotaniaBlockEntities.DAFFOMILL),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_DAFFOMILL = make(floating(LibBlockNames.SUBTILE_DAFFOMILL),
			props -> new FloatingDaffomillBlock(
					props,
					() -> BotaniaBlockEntities.DAFFOMILL),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_DAFFOMILL = make(potted(LibBlockNames.SUBTILE_DAFFOMILL),
			flowerPot(DAFFOMILL, 0), flowerPotProperties());

	public static final Block VINCULOTUS = make(LibBlockNames.SUBTILE_VINCULOTUS,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.NIGHT_VISION,
					900,
					props,
					() -> BotaniaBlockEntities.VINCULOTUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_VINCULOTUS = make(floating(LibBlockNames.SUBTILE_VINCULOTUS),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.VINCULOTUS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_VINCULOTUS = make(potted(LibBlockNames.SUBTILE_VINCULOTUS),
			flowerPot(VINCULOTUS, 0), flowerPotProperties());

	public static final Block SPECTRANTHEMUM = make(LibBlockNames.SUBTILE_SPECTRANTHEMUM,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.INVISIBILITY,
					360,
					props,
					() -> BotaniaBlockEntities.SPECTRANTHEMUM),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_SPECTRANTHEMUM = make(floating(LibBlockNames.SUBTILE_SPECTRANTHEMUM),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.SPECTRANTHEMUM),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_SPECTRANTHEMUM = make(potted(LibBlockNames.SUBTILE_SPECTRANTHEMUM),
			flowerPot(SPECTRANTHEMUM, 0), flowerPotProperties());

	public static final Block MEDUMONE = make(LibBlockNames.SUBTILE_MEDUMONE,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.SLOWNESS,
					3600,
					props,
					() -> BotaniaBlockEntities.MEDUMONE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_MEDUMONE = make(floating(LibBlockNames.SUBTILE_MEDUMONE),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.MEDUMONE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_MEDUMONE = make(potted(LibBlockNames.SUBTILE_MEDUMONE),
			flowerPot(MEDUMONE, 0), flowerPotProperties());

	public static final Block MARIMORPHOSIS = make(LibBlockNames.SUBTILE_MARIMORPHOSIS,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.MINING_FATIGUE,
					60,
					props,
					() -> BotaniaBlockEntities.MARIMORPHOSIS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block MARIMORPHOSIS_PETITE = make(petite(LibBlockNames.SUBTILE_MARIMORPHOSIS),
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.MINING_FATIGUE,
					60,
					props,
					() -> BotaniaBlockEntities.MARIMORPHOSIS_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_MARIMORPHOSIS = make(floating(LibBlockNames.SUBTILE_MARIMORPHOSIS),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.MARIMORPHOSIS),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block FLOATING_MARIMORPHOSIS_PETITE = make(petite(floating(LibBlockNames.SUBTILE_MARIMORPHOSIS)),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.MARIMORPHOSIS_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_MARIMORPHOSIS = make(potted(LibBlockNames.SUBTILE_MARIMORPHOSIS),
			flowerPot(MARIMORPHOSIS, 0), flowerPotProperties());
	public static final Block POTTED_MARIMORPHOSIS_PETITE = make(petite(potted(LibBlockNames.SUBTILE_MARIMORPHOSIS)),
			flowerPot(MARIMORPHOSIS_PETITE, 0), flowerPotProperties());

	public static final Block BUBBELL = make(LibBlockNames.SUBTILE_BUBBELL,
			props -> new SpecialFlowerBlock(
					MobEffects.WATER_BREATHING,
					240,
					props,
					() -> BotaniaBlockEntities.BUBBELL),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block BUBBELL_PETITE = make(petite(LibBlockNames.SUBTILE_BUBBELL),
			props -> new SpecialFlowerBlock(MobEffects.WATER_BREATHING,
					240,
					props,
					() -> BotaniaBlockEntities.BUBBELL_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_BUBBELL = make(floating(LibBlockNames.SUBTILE_BUBBELL),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.BUBBELL),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block FLOATING_BUBBELL_PETITE = make(petite(floating(LibBlockNames.SUBTILE_BUBBELL)),
			props -> new FloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.BUBBELL_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_BUBBELL = make(potted(LibBlockNames.SUBTILE_BUBBELL),
			flowerPot(BUBBELL, 0), flowerPotProperties());
	public static final Block POTTED_BUBBELL_PETITE = make(petite(potted(LibBlockNames.SUBTILE_BUBBELL)),
			flowerPot(BUBBELL_PETITE, 0), flowerPotProperties());

	public static final Block SOLEGNOLIA = make(LibBlockNames.SUBTILE_SOLEGNOLIA,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.INSTANT_DAMAGE,
					1,
					props,
					() -> BotaniaBlockEntities.SOLEGNOLIA),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block SOLEGNOLIA_PETITE = make(petite(LibBlockNames.SUBTILE_SOLEGNOLIA),
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.INSTANT_DAMAGE,
					1,
					props,
					() -> BotaniaBlockEntities.SOLEGNOLIA_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_SOLEGNOLIA = make(floating(LibBlockNames.SUBTILE_SOLEGNOLIA),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.SOLEGNOLIA),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block FLOATING_SOLEGNOLIA_PETITE = make(petite(floating(LibBlockNames.SUBTILE_SOLEGNOLIA)),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.SOLEGNOLIA_PETITE),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_SOLEGNOLIA = make(potted(LibBlockNames.SUBTILE_SOLEGNOLIA),
			flowerPot(SOLEGNOLIA, 0), flowerPotProperties());
	public static final Block POTTED_SOLEGNOLIA_PETITE = make(petite(potted(LibBlockNames.SUBTILE_SOLEGNOLIA)),
			flowerPot(SOLEGNOLIA_PETITE, 0), flowerPotProperties());

	public static final Block ORECHID_IGNEM = make(LibBlockNames.SUBTILE_ORECHID_IGNEM,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.FIRE_RESISTANCE,
					600,
					props,
					() -> BotaniaBlockEntities.ORECHID_IGNEM),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_ORECHID_IGNEM = make(floating(LibBlockNames.SUBTILE_ORECHID_IGNEM),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.ORECHID_IGNEM),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_ORECHID_IGNEM = make(potted(LibBlockNames.SUBTILE_ORECHID_IGNEM),
			flowerPot(ORECHID_IGNEM, 0), flowerPotProperties());

	public static final Block LABELLIA = make(LibBlockNames.SUBTILE_LABELLIA,
			props -> new PoweredSpecialFlowerBlock(
					MobEffects.FIRE_RESISTANCE,
					600,
					props,
					() -> BotaniaBlockEntities.LABELLIA),
			BlockBehaviour.Properties.ofFullCopy(WHITE_MYSTICAL_FLOWER));
	public static final Block FLOATING_LABELLIA = make(floating(LibBlockNames.SUBTILE_LABELLIA),
			props -> new PoweredFloatingSpecialFlowerBlock(
					props,
					() -> BotaniaBlockEntities.LABELLIA),
			BlockBehaviour.Properties.ofFullCopy(WHITE_FLOATING_FLOWER));
	public static final Block POTTED_LABELLIA = make(potted(LibBlockNames.SUBTILE_LABELLIA),
			flowerPot(LABELLIA, 0), flowerPotProperties());

	// petal apothecary variants
	public static final Block PETAL_APOTHECARY = makePetalApothecary(LibBlockNames.PETAL_APOTHECARY,
			SoundType.STONE, MapColor.STONE);
	public static final Block PETAL_APOTHECARY_DEEPSLATE = makePetalApothecary("deepslate" + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.DEEPSLATE, MapColor.DEEPSLATE);
	public static final Block PETAL_APOTHECARY_LIVINGROCK = makePetalApothecary("livingrock" + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.STONE, MapColor.TERRACOTTA_WHITE);
	public static final Block PETAL_APOTHECARY_BLACKSTONE = makePetalApothecary("blackstone" + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.STONE, MapColor.COLOR_BLACK);
	public static final Block PETAL_APOTHECARY_NETHER_BRICKS = makePetalApothecary("nether_brick" + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.NETHER_BRICKS, MapColor.NETHER);
	public static final Block PETAL_APOTHECARY_RED_NETHER_BRICKS = makePetalApothecary("red_nether_brick" + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.NETHER_BRICKS, MapColor.NETHER);
	public static final Block PETAL_APOTHECARY_MOSSY = makePetalApothecary("mossy" + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.STONE, MapColor.STONE);
	public static final Block PETAL_APOTHECARY_FUCHSITE = makePetalApothecary(LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.TUFF, MapColor.PLANT);
	public static final Block PETAL_APOTHECARY_TALC = makePetalApothecary(LibBlockNames.METAMORPHIC_VARIANT_TALC + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.CALCITE, MapColor.SNOW);
	public static final Block PETAL_APOTHECARY_GNEISS = makePetalApothecary(LibBlockNames.METAMORPHIC_VARIANT_GNEISS + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.DEEPSLATE_TILES, MapColor.COLOR_LIGHT_GRAY);
	public static final Block PETAL_APOTHECARY_MYCELITE = makePetalApothecary(LibBlockNames.METAMORPHIC_VARIANT_MYCELITE + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.DEEPSLATE_BRICKS, MapColor.CRIMSON_STEM);
	public static final Block PETAL_APOTHECARY_CATACLASITE = makePetalApothecary(LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.DEEPSLATE_TILES, MapColor.TERRACOTTA_BROWN);
	public static final Block PETAL_APOTHECARY_SOLITE = makePetalApothecary(LibBlockNames.METAMORPHIC_VARIANT_SOLITE + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.DEEPSLATE, MapColor.TERRACOTTA_ORANGE);
	public static final Block PETAL_APOTHECARY_LUNITE = makePetalApothecary(LibBlockNames.METAMORPHIC_VARIANT_LUNITE + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.DEEPSLATE, MapColor.COLOR_BLUE);
	public static final Block PETAL_APOTHECARY_ROSY_TALC = makePetalApothecary(LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC + LibBlockNames.PETAL_APOTHECARY_SUFFIX,
			SoundType.CALCITE, MapColor.TERRACOTTA_WHITE);
	public static final Block[] ALL_APOTHECARIES = new Block[] { PETAL_APOTHECARY, PETAL_APOTHECARY_DEEPSLATE,
			PETAL_APOTHECARY_LIVINGROCK, PETAL_APOTHECARY_BLACKSTONE, PETAL_APOTHECARY_NETHER_BRICKS,
			PETAL_APOTHECARY_RED_NETHER_BRICKS, PETAL_APOTHECARY_MOSSY, PETAL_APOTHECARY_FUCHSITE,
			PETAL_APOTHECARY_TALC, PETAL_APOTHECARY_GNEISS, PETAL_APOTHECARY_MYCELITE, PETAL_APOTHECARY_CATACLASITE,
			PETAL_APOTHECARY_SOLITE, PETAL_APOTHECARY_LUNITE, PETAL_APOTHECARY_ROSY_TALC };

	// livingrock blocks
	public static final Block LIVINGROCK = make(LibBlockNames.LIVINGROCK,
			Block::new, BlockBehaviour.Properties.of()
					.strength(2, 10)
					.sound(SoundType.STONE)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.mapColor(MapColor.TERRACOTTA_WHITE)
					.requiresCorrectToolForDrops());
	public static final Block LIVINGROCK_STAIRS = make(LibBlockNames.LIVINGROCK + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(LIVINGROCK.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block LIVINGROCK_SLAB = make(LibBlockNames.LIVINGROCK + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block LIVINGROCK_WALL = make(LibBlockNames.LIVINGROCK + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block LIVINGROCK_BUTTON = make(LibBlockNames.LIVINGROCK + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.LIVINGROCK_BLOCK_SET, 20,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON)
					.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block LIVINGROCK_PRESSURE_PLATE = make(LibBlockNames.LIVINGROCK + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.LIVINGROCK_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)
					.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block POLISHED_LIVINGROCK = make(LibBlockNames.POLISHED_PREFIX + LibBlockNames.LIVINGROCK,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block POLISHED_LIVINGROCK_STAIRS = make(LibBlockNames.POLISHED_PREFIX + LibBlockNames.LIVINGROCK + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(LIVINGROCK.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(POLISHED_LIVINGROCK));
	public static final Block POLISHED_LIVINGROCK_SLAB = make(LibBlockNames.POLISHED_PREFIX + LibBlockNames.LIVINGROCK + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(POLISHED_LIVINGROCK));
	public static final Block POLISHED_LIVINGROCK_WALL = make(LibBlockNames.POLISHED_PREFIX + LibBlockNames.LIVINGROCK + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(POLISHED_LIVINGROCK));
	public static final Block LIVINGROCK_SLATE = make(LibBlockNames.LIVINGROCK + LibBlockNames.SLATE_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block LIVINGROCK_BRICKS = make(LibBlockNames.LIVINGROCK + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block LIVINGROCK_BRICK_STAIRS = make(LibBlockNames.LIVINGROCK + LibBlockNames.BRICK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(LIVINGROCK_BRICKS.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(LIVINGROCK_BRICKS));
	public static final Block LIVINGROCK_BRICK_SLAB = make(LibBlockNames.LIVINGROCK + LibBlockNames.BRICK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK_BRICKS));
	public static final Block LIVINGROCK_BRICK_WALL = make(LibBlockNames.LIVINGROCK + LibBlockNames.BRICK_INFIX + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK_BRICKS));
	public static final Block MOSSY_LIVINGROCK_BRICKS = make(LibBlockNames.MOSSY_PREFIX + LibBlockNames.LIVINGROCK + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block MOSSY_LIVINGROCK_BRICK_STAIRS = make(LibBlockNames.MOSSY_PREFIX + LibBlockNames.LIVINGROCK + LibBlockNames.BRICK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(MOSSY_LIVINGROCK_BRICKS.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(MOSSY_LIVINGROCK_BRICKS));
	public static final Block MOSSY_LIVINGROCK_BRICK_SLAB = make(LibBlockNames.MOSSY_PREFIX + LibBlockNames.LIVINGROCK + LibBlockNames.BRICK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(MOSSY_LIVINGROCK_BRICKS));
	public static final Block MOSSY_LIVINGROCK_BRICK_WALL = make(LibBlockNames.MOSSY_PREFIX + LibBlockNames.LIVINGROCK + LibBlockNames.BRICK_INFIX + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(MOSSY_LIVINGROCK_BRICKS));
	public static final Block CRACKED_LIVINGROCK_BRICKS = make(LibBlockNames.CRACKED_PREFIX + LibBlockNames.LIVINGROCK + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block CHISELED_LIVINGROCK_BRICKS = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.LIVINGROCK + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));

	// livingwood blocks
	public static final Block LIVINGWOOD_LOG = make(LibBlockNames.LIVINGWOOD + LibBlockNames.LOG_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.of()
					.strength(2)
					.sound(SoundType.WOOD)
					.instrument(NoteBlockInstrument.BASS)
					.mapColor(state -> state.getValue(BlockStateProperties.AXIS) == Direction.Axis.Y
							? MapColor.TERRACOTTA_RED
							: MapColor.TERRACOTTA_BROWN));
	public static final Block LIVINGWOOD = make(LibBlockNames.LIVINGWOOD,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_LOG)
					.mapColor(MapColor.TERRACOTTA_BROWN));
	public static final Block LIVINGWOOD_STAIRS = make(LibBlockNames.LIVINGWOOD + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(LIVINGWOOD.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block LIVINGWOOD_SLAB = make(LibBlockNames.LIVINGWOOD + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block LIVINGWOOD_WALL = make(LibBlockNames.LIVINGWOOD + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block STRIPPED_LIVINGWOOD_LOG = make(LibBlockNames.STRIPPED_PREFIX + LibBlockNames.LIVINGWOOD + LibBlockNames.LOG_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_LOG)
					.mapColor(MapColor.TERRACOTTA_RED));
	public static final Block STRIPPED_LIVINGWOOD = make(LibBlockNames.STRIPPED_PREFIX + LibBlockNames.LIVINGWOOD,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(STRIPPED_LIVINGWOOD_LOG));
	public static final Block STRIPPED_LIVINGWOOD_STAIRS = make(LibBlockNames.STRIPPED_PREFIX + LibBlockNames.LIVINGWOOD + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(STRIPPED_LIVINGWOOD.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block STRIPPED_LIVINGWOOD_SLAB = make(LibBlockNames.STRIPPED_PREFIX + LibBlockNames.LIVINGWOOD + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block STRIPPED_LIVINGWOOD_WALL = make(LibBlockNames.STRIPPED_PREFIX + LibBlockNames.LIVINGWOOD + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block GLIMMERING_LIVINGWOOD_LOG = make(LibBlockNames.GLIMMERING_PREFIX + LibBlockNames.LIVINGWOOD + LibBlockNames.LOG_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_LOG)
					.lightLevel(constInt(12)));
	public static final Block GLIMMERING_LIVINGWOOD = make(LibBlockNames.GLIMMERING_PREFIX + LibBlockNames.LIVINGWOOD,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(GLIMMERING_LIVINGWOOD_LOG)
					.mapColor(MapColor.TERRACOTTA_BROWN));
	public static final Block STRIPPED_GLIMMERING_LIVINGWOOD_LOG = make(LibBlockNames.GLIMMERING_PREFIX + LibBlockNames.STRIPPED_PREFIX + LibBlockNames.LIVINGWOOD + LibBlockNames.LOG_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(STRIPPED_LIVINGWOOD_LOG)
					.lightLevel(b -> 8));
	public static final Block STRIPPED_GLIMMERING_LIVINGWOOD = make(LibBlockNames.GLIMMERING_PREFIX + LibBlockNames.STRIPPED_PREFIX + LibBlockNames.LIVINGWOOD,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(STRIPPED_GLIMMERING_LIVINGWOOD_LOG)
					.mapColor(MapColor.TERRACOTTA_BROWN));
	public static final Block LIVINGWOOD_PLANKS = make(LibBlockNames.LIVINGWOOD + LibBlockNames.PLANKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_LOG)
					.mapColor(MapColor.TERRACOTTA_RED));
	public static final Block LIVINGWOOD_PLANK_STAIRS = make(LibBlockNames.LIVINGWOOD + LibBlockNames.PLANK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(LIVINGWOOD_PLANKS.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_PLANKS));
	public static final Block LIVINGWOOD_PLANK_SLAB = make(LibBlockNames.LIVINGWOOD + LibBlockNames.PLANK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_PLANKS));
	public static final Block LIVINGWOOD_FENCE = make(LibBlockNames.LIVINGWOOD + LibBlockNames.FENCE_SUFFIX,
			FenceBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block LIVINGWOOD_FENCE_GATE = make(LibBlockNames.LIVINGWOOD + LibBlockNames.FENCE_GATE_SUFFIX,
			props -> new FenceGateBlock(BotaniaBlockSetTypes.LIVINGWOOD, props), BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block LIVINGWOOD_DOOR = make(LibBlockNames.LIVINGWOOD + LibBlockNames.DOOR_SUFFIX,
			props -> new DoorBlock(BotaniaBlockSetTypes.LIVINGWOOD_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_PLANKS)
					.noOcclusion()
					.pushReaction(PushReaction.POPPED));
	public static final Block LIVINGWOOD_TRAPDOOR = make(LibBlockNames.LIVINGWOOD + LibBlockNames.TRAPDOOR_SUFFIX,
			props -> new TrapDoorBlock(BotaniaBlockSetTypes.LIVINGWOOD_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_PLANKS)
					.noOcclusion()
					.isValidSpawn(NO_SPAWN));
	public static final Block LIVINGWOOD_PRESSURE_PLATE = make(LibBlockNames.LIVINGWOOD + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.LIVINGWOOD_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_PLANKS)
					.noCollision()
					.strength(0.5F)
					.forceSolidOn()
					.pushReaction(PushReaction.POPPED));
	public static final Block LIVINGWOOD_BUTTON = make(LibBlockNames.LIVINGWOOD + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.LIVINGWOOD_BLOCK_SET, 30,
					props),
			BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_PLANKS)
					.noCollision()
					.strength(0.5F)
					.pushReaction(PushReaction.POPPED));
	public static final Block LIVINGWOOD_SIGN = make(LibBlockNames.LIVINGWOOD + LibBlockNames.SIGN_SUFFIX,
			props -> new StandingSignBlock(BotaniaBlockSetTypes.LIVINGWOOD,
					props),
			BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_PLANKS)
					.noCollision()
					.strength(1.0f)
					.forceSolidOn());
	public static final Block LIVINGWOOD_WALL_SIGN = make(LibBlockNames.LIVINGWOOD + LibBlockNames.WALL_INFIX + LibBlockNames.SIGN_SUFFIX,
			props -> new WallSignBlock(BotaniaBlockSetTypes.LIVINGWOOD, props), BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_SIGN));
	public static final Block LIVINGWOOD_HANGING_SIGN = make(LibBlockNames.LIVINGWOOD + LibBlockNames.HANGING_SIGN_SUFFIX,
			props -> new CeilingHangingSignBlock(BotaniaBlockSetTypes.LIVINGWOOD,
					props),
			BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_SIGN));
	public static final Block LIVINGWOOD_WALL_HANGING_SIGN = make(LibBlockNames.LIVINGWOOD + LibBlockNames.WALL_INFIX + LibBlockNames.HANGING_SIGN_SUFFIX,
			props -> new WallHangingSignBlock(BotaniaBlockSetTypes.LIVINGWOOD,
					props),
			BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_SIGN));
	public static final Block MOSSY_LIVINGWOOD_PLANKS = make(LibBlockNames.MOSSY_PREFIX + LibBlockNames.LIVINGWOOD + LibBlockNames.PLANKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_PLANKS));
	public static final Block FRAMED_LIVINGWOOD = make(LibBlockNames.FRAMED_PREFIX + LibBlockNames.LIVINGWOOD,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_PLANKS));
	public static final Block PATTERN_FRAMED_LIVINGWOOD = make(LibBlockNames.PATTERN_FRAMED_PREFIX + LibBlockNames.LIVINGWOOD,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_PLANKS));

	// dreamwood blocks
	public static final Block DREAMWOOD_LOG = make(LibBlockNames.DREAMWOOD + LibBlockNames.LOG_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD_LOG)
					.mapColor(MapColor.QUARTZ));
	public static final Block DREAMWOOD = make(LibBlockNames.DREAMWOOD,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_LOG));
	public static final Block DREAMWOOD_STAIRS = make(LibBlockNames.DREAMWOOD + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(DREAMWOOD.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(DREAMWOOD));
	public static final Block DREAMWOOD_SLAB = make(LibBlockNames.DREAMWOOD + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD));
	public static final Block DREAMWOOD_WALL = make(LibBlockNames.DREAMWOOD + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD));
	public static final Block STRIPPED_DREAMWOOD_LOG = make(LibBlockNames.STRIPPED_PREFIX + LibBlockNames.DREAMWOOD + LibBlockNames.LOG_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_LOG));
	public static final Block STRIPPED_DREAMWOOD = make(LibBlockNames.STRIPPED_PREFIX + LibBlockNames.DREAMWOOD,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_LOG));
	public static final Block STRIPPED_DREAMWOOD_STAIRS = make(LibBlockNames.STRIPPED_PREFIX + LibBlockNames.DREAMWOOD + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(STRIPPED_DREAMWOOD.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(DREAMWOOD));
	public static final Block STRIPPED_DREAMWOOD_SLAB = make(LibBlockNames.STRIPPED_PREFIX + LibBlockNames.DREAMWOOD + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD));
	public static final Block STRIPPED_DREAMWOOD_WALL = make(LibBlockNames.STRIPPED_PREFIX + LibBlockNames.DREAMWOOD + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD));
	public static final Block GLIMMERING_DREAMWOOD_LOG = make(LibBlockNames.GLIMMERING_PREFIX + LibBlockNames.DREAMWOOD + LibBlockNames.LOG_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(GLIMMERING_LIVINGWOOD_LOG).mapColor(MapColor.QUARTZ));
	public static final Block GLIMMERING_DREAMWOOD = make(LibBlockNames.GLIMMERING_PREFIX + LibBlockNames.DREAMWOOD,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(GLIMMERING_DREAMWOOD_LOG));
	public static final Block STRIPPED_GLIMMERING_DREAMWOOD_LOG = make(LibBlockNames.GLIMMERING_PREFIX + LibBlockNames.STRIPPED_PREFIX + LibBlockNames.DREAMWOOD + LibBlockNames.LOG_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(STRIPPED_GLIMMERING_LIVINGWOOD_LOG)
					.mapColor(MapColor.QUARTZ));
	public static final Block STRIPPED_GLIMMERING_DREAMWOOD = make(LibBlockNames.GLIMMERING_PREFIX + LibBlockNames.STRIPPED_PREFIX + LibBlockNames.DREAMWOOD,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(STRIPPED_GLIMMERING_DREAMWOOD_LOG));
	public static final Block DREAMWOOD_PLANKS = make(LibBlockNames.DREAMWOOD + LibBlockNames.PLANKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_LOG));
	public static final Block DREAMWOOD_PLANK_STAIRS = make(LibBlockNames.DREAMWOOD + LibBlockNames.PLANK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(DREAMWOOD_PLANKS.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_PLANKS));
	public static final Block DREAMWOOD_PLANK_SLAB = make(LibBlockNames.DREAMWOOD + LibBlockNames.PLANK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_PLANKS));
	public static final Block DREAMWOOD_FENCE = make(LibBlockNames.DREAMWOOD + LibBlockNames.FENCE_SUFFIX,
			FenceBlock::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_PLANKS));
	public static final Block DREAMWOOD_FENCE_GATE = make(LibBlockNames.DREAMWOOD + LibBlockNames.FENCE_GATE_SUFFIX,
			props -> new FenceGateBlock(BotaniaBlockSetTypes.DREAMWOOD, props), BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_PLANKS));
	public static final Block DREAMWOOD_DOOR = make(LibBlockNames.DREAMWOOD + LibBlockNames.DOOR_SUFFIX,
			props -> new DoorBlock(BotaniaBlockSetTypes.DREAMWOOD_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_PLANKS)
					.noOcclusion()
					.pushReaction(PushReaction.POPPED));
	public static final Block DREAMWOOD_TRAPDOOR = make(LibBlockNames.DREAMWOOD + LibBlockNames.TRAPDOOR_SUFFIX,
			props -> new TrapDoorBlock(BotaniaBlockSetTypes.DREAMWOOD_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_PLANKS)
					.noOcclusion()
					.isValidSpawn(NO_SPAWN));
	public static final Block DREAMWOOD_PRESSURE_PLATE = make(LibBlockNames.DREAMWOOD + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.DREAMWOOD_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_PLANKS)
					.noCollision()
					.strength(0.5F)
					.forceSolidOn()
					.pushReaction(PushReaction.POPPED));
	public static final Block DREAMWOOD_BUTTON = make(LibBlockNames.DREAMWOOD + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.DREAMWOOD_BLOCK_SET, 30,
					props),
			BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_PLANKS)
					.noCollision()
					.strength(0.5F)
					.pushReaction(PushReaction.POPPED));
	public static final Block DREAMWOOD_SIGN = make(LibBlockNames.DREAMWOOD + LibBlockNames.SIGN_SUFFIX,
			props -> new StandingSignBlock(BotaniaBlockSetTypes.DREAMWOOD,
					props),
			BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_PLANKS)
					.noCollision()
					.strength(1.0f)
					.forceSolidOn());
	public static final Block DREAMWOOD_WALL_SIGN = make(LibBlockNames.DREAMWOOD + LibBlockNames.WALL_INFIX + LibBlockNames.SIGN_SUFFIX,
			props -> new WallSignBlock(BotaniaBlockSetTypes.DREAMWOOD, props), BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_SIGN));
	public static final Block DREAMWOOD_HANGING_SIGN = make(LibBlockNames.DREAMWOOD + LibBlockNames.HANGING_SIGN_SUFFIX,
			props -> new CeilingHangingSignBlock(BotaniaBlockSetTypes.DREAMWOOD,
					props),
			BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_SIGN));
	public static final Block DREAMWOOD_WALL_HANGING_SIGN = make(LibBlockNames.DREAMWOOD + LibBlockNames.WALL_INFIX + LibBlockNames.HANGING_SIGN_SUFFIX,
			props -> new WallHangingSignBlock(BotaniaBlockSetTypes.DREAMWOOD,
					props),
			BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_SIGN));
	public static final Block MOSSY_DREAMWOOD_PLANKS = make(LibBlockNames.MOSSY_PREFIX + LibBlockNames.DREAMWOOD + LibBlockNames.PLANKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_PLANKS));
	public static final Block FRAMED_DREAMWOOD = make(LibBlockNames.FRAMED_PREFIX + LibBlockNames.DREAMWOOD,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_PLANKS));
	public static final Block PATTERN_FRAMED_DREAMWOOD = make(LibBlockNames.PATTERN_FRAMED_PREFIX + LibBlockNames.DREAMWOOD,
			Block::new, BlockBehaviour.Properties.ofFullCopy(DREAMWOOD_PLANKS));

	// mana spreaders
	public static final ManaSpreaderBlock MANA_SPREADER = makeBlockWithColoredVariants(LibBlockNames.MANA_SPREADER,
			(color, props) -> new ManaSpreaderBlock(ManaSpreaderBlock.DEFAULT_SPREADER_PARAMETERS, color,
					props),
			() -> BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD).isValidSpawn(NO_SPAWN).isRedstoneConductor(NEVER),
			LibBlockNames.COVERED_INFIX);
	public static final ManaSpreaderBlock PULSE_MANA_SPREADER = makeBlockWithColoredVariants(LibBlockNames.PULSE_MANA_SPREADER,
			(color, props) -> new ManaSpreaderBlock(ManaSpreaderBlock.PULSE_SPREADER_PARAMETERS, color,
					props),
			() -> BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD).isValidSpawn(NO_SPAWN).isRedstoneConductor(NEVER),
			LibBlockNames.COVERED_INFIX);
	public static final ManaSpreaderBlock ELVEN_MANA_SPREADER = makeBlockWithColoredVariants(LibBlockNames.ELVEN_MANA_SPREADER,
			(color, props) -> new ManaSpreaderBlock(ManaSpreaderBlock.ELVEN_SPREADER_PARAMETERS, color,
					props),
			() -> BlockBehaviour.Properties.ofFullCopy(DREAMWOOD).isValidSpawn(NO_SPAWN).isRedstoneConductor(NEVER),
			LibBlockNames.COVERED_INFIX);
	public static final ManaSpreaderBlock GAIA_MANA_SPREADER = makeBlockWithColoredVariants(LibBlockNames.GAIA_MANA_SPREADER,
			(color, props) -> new ManaSpreaderBlock(ManaSpreaderBlock.GAIA_SPREADER_PARAMETERS, color,
					props),
			() -> BlockBehaviour.Properties.ofFullCopy(DREAMWOOD).isValidSpawn(NO_SPAWN).isRedstoneConductor(NEVER),
			LibBlockNames.COVERED_INFIX);

	// mana pools
	public static final ManaPoolBlock MANA_POOL = makeBlockWithColoredVariants(LibBlockNames.MANA_POOL,
			(color, props) -> new ManaPoolBlock(ManaPoolBlock.MAX_MANA, false, false,
					ManaPoolBlock.NORMAL_SHAPE_VARIANT, color, props),
			() -> BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	// PORT-26.3: getDescriptionId is final; the dyed creative pools share the undyed one's name through their properties
	public static final ManaPoolBlock CREATIVE_MANA_POOL = makeBlockWithColoredVariants(LibBlockNames.CREATIVE_MANA_POOL,
			(color, props) -> new ManaPoolBlock(ManaPoolBlock.MAX_MANA, false, true,
					ManaPoolBlock.BIG_SHAPE_VARIANT, color, props),
			() -> BlockBehaviour.Properties.ofFullCopy(LIVINGROCK)
					.overrideDescription(Util.makeDescriptionId("block", botaniaRL(LibBlockNames.CREATIVE_MANA_POOL))));
	public static final ManaPoolBlock DILUTED_MANA_POOL = makeBlockWithColoredVariants(LibBlockNames.DILUTED_MANA_POOL,
			(color, props) -> new ManaPoolBlock(ManaPoolBlock.MAX_MANA_DILUTED, false, false,
					ManaPoolBlock.SMALL_SHAPE_VARIANT, color, props),
			() -> BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final ManaPoolBlock FABULOUS_MANA_POOL = makeBlockWithColoredVariants(LibBlockNames.FABULOUS_MANA_POOL,
			(color, props) -> new ManaPoolBlock(ManaPoolBlock.MAX_MANA, true, false,
					ManaPoolBlock.NORMAL_SHAPE_VARIANT, color, props),
			() -> BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block ALCHEMY_CATALYST = make(LibBlockNames.ALCHEMY_CATALYST,
			AlchemyCatalystBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block CONJURATION_CATALYST = make(LibBlockNames.CONJURATION_CATALYST,
			ConjurationCatalystBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));

	// resource blocks
	public static final Block MANASTEEL_BLOCK = make(LibBlockNames.MANASTEEL_BLOCK,
			Block::new, BlockBehaviour.Properties.of()
					.strength(3, 10)
					.mapColor(MapColor.LAPIS)
					.sound(SoundType.METAL)
					.instrument(NoteBlockInstrument.IRON_XYLOPHONE)
					.requiresCorrectToolForDrops());
	public static final Block TERRASTEEL_BLOCK = make(LibBlockNames.TERRASTEEL_BLOCK,
			Block::new, BlockBehaviour.Properties.ofFullCopy(MANASTEEL_BLOCK)
					.mapColor(MapColor.EMERALD));
	public static final Block ELEMENTIUM_BLOCK = make(LibBlockNames.ELEMENTIUM_BLOCK,
			Block::new, BlockBehaviour.Properties.ofFullCopy(MANASTEEL_BLOCK)
					.mapColor(MapColor.COLOR_PINK));
	public static final Block MANA_DIAMOND_BLOCK = make(LibBlockNames.MANA_DIAMOND_BLOCK,
			Block::new, BlockBehaviour.Properties.ofFullCopy(MANASTEEL_BLOCK)
					.instrument(NoteBlockInstrument.HARP)
					.mapColor(MapColor.DIAMOND));
	public static final Block DRAGONSTONE_BLOCK = make(LibBlockNames.DRAGONSTONE_BLOCK,
			Block::new, BlockBehaviour.Properties.ofFullCopy(MANA_DIAMOND_BLOCK)
					.mapColor(MapColor.COLOR_PINK));

	// glass blocks
	public static final Block MANAGLASS = make(LibBlockNames.MANAGLASS,
			// [VanillaCopy] Blocks.GLASS
			TransparentBlock::new, BlockBehaviour.Properties.of()
					.instrument(NoteBlockInstrument.HAT)
					.strength(0.3F)
					.sound(SoundType.GLASS)
					.noOcclusion()
					.isValidSpawn(NO_SPAWN)
					.isRedstoneConductor(NEVER)
					.isSuffocating(NEVER)
					.isViewBlocking((state, level, pos, aabb) -> false)
					// Botania: mana glasses emit a lot of light
					.lightLevel(constInt(15)));
	public static final Block ALFGLASS = make(LibBlockNames.ALFGLASS,
			TransparentBlock::new, BlockBehaviour.Properties.ofFullCopy(MANAGLASS));
	public static final Block BIFROST_BRIDGE = make(LibBlockNames.BIFROST_BRIDGE,
			BifrostBridgeBlock::new, BlockBehaviour.Properties.ofFullCopy(MANAGLASS)
					.destroyTime(-1));
	public static final Block BIFROST = make(LibBlockNames.BIFROST,
			BifrostBlock::new, BlockBehaviour.Properties.ofFullCopy(MANAGLASS));

	// recipe blocks
	public static final Block RUNIC_ALTAR = make(LibBlockNames.RUNIC_ALTAR,
			RunicAltarBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK)
					.requiresCorrectToolForDrops());
	public static final Block MANA_ENCHANTER = make(LibBlockNames.MANA_ENCHANTER,
			ManaEnchanterBlock::new, BlockBehaviour.Properties.of()
					.mapColor(MapColor.LAPIS)
					.strength(3, 5)
					.lightLevel(constInt(15))
					.sound(SoundType.STONE));
	public static final Block BOTANICAL_BREWERY = make(LibBlockNames.BOTANICAL_BREWERY,
			BotanicalBreweryBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block TERRESTRIAL_AGGLOMERATION_PLATE = make(LibBlockNames.TERRESTRIAL_AGGLOMERATION_PLATE,
			TerrestrialAgglomerationPlateBlock::new, BlockBehaviour.Properties.of()
					.mapColor(MapColor.LAPIS)
					.strength(3, 10)
					.sound(SoundType.METAL)
					.requiresCorrectToolForDrops());
	public static final Block ELVEN_GATEWAY_CORE = make(LibBlockNames.ELVEN_GATEWAY_CORE,
			AlfheimPortalBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD)
					.strength(10)
					.sound(SoundType.WOOD)
					.lightLevel(state -> state.getValue(
							BotaniaStateProperties.ALFPORTAL_STATE) != AlfheimPortalState.OFF ? 15 : 0));

	// pylons
	public static final Block MANA_PYLON = make(LibBlockNames.MANA_PYLON,
			ManaPylonBlock::new, BlockBehaviour.Properties.of()
					.mapColor(DyeColor.LIGHT_BLUE)
					.noOcclusion()
					.strength(5.5F)
					.sound(SoundType.METAL)
					.lightLevel(constInt(7))
					.requiresCorrectToolForDrops());
	public static final Block NATURA_PYLON = make(LibBlockNames.NATURA_PYLON,
			NaturaPylonBlock::new, BlockBehaviour.Properties.ofFullCopy(MANA_PYLON)
					.mapColor(MapColor.EMERALD));
	public static final Block GAIA_PYLON = make(LibBlockNames.GAIA_PYLON,
			GaiaPylonBlock::new, BlockBehaviour.Properties.ofFullCopy(MANA_PYLON)
					.mapColor(DyeColor.PINK));

	public static final Block MANA_SPLITTER = make(LibBlockNames.MANA_SPLITTER,
			ManaSplitterBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block MANA_VOID = make(LibBlockNames.MANA_VOID,
			ManaVoidBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK)
					.strength(2, 2000));
	public static final Block MANA_DETECTOR = make(LibBlockNames.MANA_DETECTOR,
			ManaDetectorBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block FORCE_RELAY = make(LibBlockNames.FORCE_RELAY,
			ForceRelayBlock::new, BlockBehaviour.Properties.of()
					.strength(2, 10)
					.sound(SoundType.METAL)
					.mapColor(MapColor.COLOR_PURPLE)
					.isValidSpawn(NO_SPAWN));
	public static final Block SPREADER_TURNTABLE = make(LibBlockNames.TURNTABLE,
			SpreaderTurntableBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block TINY_PLANET = make(LibBlockNames.TINY_PLANET,
			TinyPlanetBlock::new, BlockBehaviour.Properties.of()
					.mapColor(MapColor.DEEPSLATE)
					.strength(20, 100)
					.sound(SoundType.DEEPSLATE)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops());
	public static final Block DRUM_OF_THE_WILD = make(LibBlockNames.DRUM_OF_THE_WILD,
			DrumOfTheWildBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block DRUM_OF_THE_GATHERING = make(LibBlockNames.DRUM_OF_THE_GATHERING,
			DrumOfTheGatheringBlock::new, BlockBehaviour.Properties.ofFullCopy(DRUM_OF_THE_WILD));
	public static final Block DRUM_OF_THE_CANOPY = make(LibBlockNames.DRUM_OF_THE_CANOPY,
			DrumOfTheCanopyBlock::new, BlockBehaviour.Properties.ofFullCopy(DRUM_OF_THE_WILD));
	public static final Block LIFE_IMBUER = make(LibBlockNames.LIFE_IMBUER,
			LifeImbuerBlock::new, BlockBehaviour.Properties.of()
					.sound(SoundType.METAL)
					.strength(3)
					.requiresCorrectToolForDrops());
	public static final Block MANA_FLUXFIELD = make(LibBlockNames.MANA_FLUXFIELD,
			PowerGeneratorBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block MANA_PRISM = make(LibBlockNames.MANA_PRISM,
			ManaPrismBlock::new, BlockBehaviour.Properties.ofFullCopy(ALFGLASS)
					.noCollision());
	public static final Block MANA_PUMP = make(LibBlockNames.MANA_PUMP,
			ManaPumpBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block SPARK_TINKERER = make(LibBlockNames.SPARK_TINKERER,
			SparkTinkererBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block MANASTORM_CHARGE = make(LibBlockNames.MANASTORM_CHARGE,
			ManastormChargeBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD)
					.strength(12));
	public static final Block MANATIDE_BELLOWS = make(LibBlockNames.MANATIDE_BELLOWS,
			BellowsBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));

	public static final Block OPEN_CRATE = make(LibBlockNames.OPEN_CRATE,
			OpenCrateBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block CRAFTY_CRATE = make(LibBlockNames.CRAFT_CRATE,
			CraftyCrateBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block EYE_OF_THE_ANCIENTS = make(LibBlockNames.EYE_OF_THE_ANCIENTS,
			EyeOfTheAncientsBlock::new, BlockBehaviour.Properties.of()
					.strength(5, 10)
					.sound(SoundType.METAL)
					.requiresCorrectToolForDrops());
	public static final Block SOLID_VINE = make(LibBlockNames.SOLID_VINE,
			SolidVineBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.VINE)
					.overrideDescription(Blocks.VINE.getDescriptionId())); // PORT-26.3: was SolidVineBlock#getDescriptionId
	public static final Block ABSTRUSE_PLATFORM = make(LibBlockNames.PLATFORM_ABSTRUSE,
			AbstrusePlatformBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD)
					.strength(2, 5)
					.isValidSpawn(NO_SPAWN)
					.noOcclusion()
					.isViewBlocking((state, level, pos, aabb) -> false)
					.isSuffocating(NEVER));
	public static final Block SPECTRAL_PLATFORM = make(LibBlockNames.PLATFORM_SPECTRAL,
			SpectralPlatformBlock::new, BlockBehaviour.Properties.ofFullCopy(ABSTRUSE_PLATFORM));
	public static final Block INFRANGIBLE_PLATFORM = make(LibBlockNames.PLATFORM_INFRANGIBLE,
			InfrangiblePlatformBlock::new, BlockBehaviour.Properties.ofFullCopy(ABSTRUSE_PLATFORM)
					.strength(-1, Float.MAX_VALUE)
					.isValidSpawn(NO_SPAWN)
					.noOcclusion());
	public static final Block TINY_POTATO = make(LibBlockNames.TINY_POTATO,
			TinyPotatoBlock::new, BlockBehaviour.Properties.of()
					.strength(0.25F)
					.mapColor(DyeColor.PINK));
	public static final Block ENDER_OVERSEER = make(LibBlockNames.ENDER_OVERSEER,
			EnderOverseerBlock::new, BlockBehaviour.Properties.ofFullCopy(MANASTEEL_BLOCK));
	public static final Block RED_STRINGED_CONTAINER = make(LibBlockNames.RED_STRINGED_CONTAINER,
			RedStringContainerBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block RED_STRINGED_DISPENSER = make(LibBlockNames.RED_STRINGED_DISPENSER,
			RedStringDispenserBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block RED_STRINGED_NUTRIFIER = make(LibBlockNames.RED_STRINGED_NUTRIFIER,
			RedStringNutrifierBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block RED_STRINGED_COMPARATOR = make(LibBlockNames.RED_STRINGED_COMPARATOR,
			RedStringComparatorBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block RED_STRINGED_SPOOFER = make(LibBlockNames.RED_STRINGED_SPOOFER,
			RedStringSpooferBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block RED_STRINGED_INTERCEPTOR = make(LibBlockNames.RED_STRINGED_INTERCEPTOR,
			RedStringInterceptorBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));

	public static final Block CORPOREA_FUNNEL = make(LibBlockNames.CORPOREA_FUNNEL,
			CorporeaFunnelBlock::new, BlockBehaviour.Properties.of()
					.strength(5.5F)
					.mapColor(DyeColor.PURPLE)
					.sound(SoundType.METAL)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops());
	public static final Block CORPOREA_INTERCEPTOR = make(LibBlockNames.CORPOREA_INTERCEPTOR,
			CorporeaInterceptorBlock::new, BlockBehaviour.Properties.ofFullCopy(CORPOREA_FUNNEL));
	public static final Block CORPOREA_INDEX = make(LibBlockNames.CORPOREA_INDEX,
			CorporeaIndexBlock::new, BlockBehaviour.Properties.ofFullCopy(CORPOREA_FUNNEL)
					.noOcclusion());
	public static final Block CORPOREA_CRYSTAL_CUBE = make(LibBlockNames.CORPOREA_CRYSTAL_CUBE,
			CorporeaCrystalCubeBlock::new, BlockBehaviour.Properties.ofFullCopy(CORPOREA_FUNNEL));
	public static final Block CORPOREA_RETAINER = make(LibBlockNames.CORPOREA_RETAINER,
			CorporeaRetainerBlock::new, BlockBehaviour.Properties.ofFullCopy(CORPOREA_FUNNEL));

	public static final Block CORPOREA_BLOCK = make(LibBlockNames.CORPOREA_BLOCK,
			Block::new, BlockBehaviour.Properties.ofFullCopy(CORPOREA_FUNNEL));
	public static final Block CORPOREA_STAIRS = make(LibBlockNames.CORPOREA_PREFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(CORPOREA_BLOCK.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(CORPOREA_BLOCK));
	public static final Block CORPOREA_SLAB = make(LibBlockNames.CORPOREA_PREFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(CORPOREA_BLOCK));
	public static final Block CORPOREA_WALL = make(LibBlockNames.CORPOREA_PREFIX + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(CORPOREA_BLOCK));
	public static final Block CORPOREA_BUTTON = make(LibBlockNames.CORPOREA_PREFIX + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.CORPOREA_BLOCK_SET, 20,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON)
					.mapColor(DyeColor.PURPLE));
	public static final Block CORPOREA_PRESSURE_PLATE = make(LibBlockNames.CORPOREA_PREFIX + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.CORPOREA_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)
					.mapColor(DyeColor.PURPLE));
	public static final Block CORPOREA_BRICKS = make(LibBlockNames.CORPOREA_PREFIX + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(CORPOREA_BLOCK));
	public static final Block CORPOREA_BRICK_STAIRS = make(LibBlockNames.CORPOREA_PREFIX + LibBlockNames.BRICK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(CORPOREA_BRICKS.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(CORPOREA_BRICKS));
	public static final Block CORPOREA_BRICK_SLAB = make(LibBlockNames.CORPOREA_PREFIX + LibBlockNames.BRICK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(CORPOREA_BRICKS));
	public static final Block CORPOREA_BRICK_WALL = make(LibBlockNames.CORPOREA_PREFIX + LibBlockNames.BRICK_INFIX + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(CORPOREA_BRICKS));

	public static final Block INCENSE_PLATE = make(LibBlockNames.INCENSE_PLATE,
			IncensePlateBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD)
					.lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 2 : 0));
	public static final Block HOVERING_HOURGLASS = make(LibBlockNames.HOVERING_HOURGLASS,
			HoveringHourglassBlock::new, BlockBehaviour.Properties.of()
					.mapColor(MapColor.GOLD)
					.strength(2)
					.sound(SoundType.METAL));
	public static final Block SPECTRAL_RAIL = make(LibBlockNames.SPECTRAL_RAIL,
			SpectralRailBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL));
	public static final Block LUMINIZER = make(LibBlockNames.LUMINIZER,
			LuminizerBlock::new, BlockBehaviour.Properties.of()
					.noCollision());
	public static final Block DETECTOR_LUMINIZER = make("detector" + LibBlockNames.LUMINIZER_SUFFIX,
			LuminizerDetectorBlock::new, BlockBehaviour.Properties.ofFullCopy(LUMINIZER));
	public static final Block FORK_LUMINIZER = make("fork" + LibBlockNames.LUMINIZER_SUFFIX,
			LuminizerForkBlock::new, BlockBehaviour.Properties.ofFullCopy(LUMINIZER));
	public static final Block TOGGLE_LUMINIZER = make("toggle" + LibBlockNames.LUMINIZER_SUFFIX,
			LuminizerToggleBlock::new, BlockBehaviour.Properties.ofFullCopy(LUMINIZER));
	public static final Block LUMINIZER_LAUNCHER = make(LibBlockNames.LUMINIZER_LAUNCHER,
			LuminizerLauncherBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block CACOPHONIUM_BLOCK = make(LibBlockNames.CACOPHONIUM,
			CacophoniumBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.NOTE_BLOCK)
					.strength(0.8F));
	public static final Block CELLULAR_BLOCK = make(LibBlockNames.CELLULAR_BLOCK,
			CellularBlock::new, BlockBehaviour.Properties.of()
					.pushReaction(PushReaction.POPPED)
					.sound(SoundType.WOOL));
	public static final Block TERU_TERU_BOZU = make(LibBlockNames.TERU_TERU_BOZU,
			TeruTeruBozuBlock::new, BlockBehaviour.Properties.of().sound(SoundType.WOOL)
					.instrument(NoteBlockInstrument.GUITAR)
					.mapColor(DyeColor.WHITE));
	public static final Block LIVINGWOOD_AVATAR = make(LibBlockNames.LIVINGWOOD_AVATAR,
			AvatarBlock::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block FAKE_AIR = make(LibBlockNames.FAKE_AIR,
			FakeAirBlock::new, BlockBehaviour.Properties.of()
					.overrideDescription(Blocks.AIR.getDescriptionId()) // PORT-26.3: was FakeAirBlock#getDescriptionId
					.replaceable()
					.noCollision()
					.noLootTable()
					.air()
					.randomTicks());
	public static final Block LIVING_ROOT = make(LibBlockNames.LIVING_ROOTS,
			LivingRootBlock::new, BlockBehaviour.Properties.of()
					.strength(1.2F)
					.sound(SoundType.WOOD));
	public static final Block FEL_PUMPKIN = make(LibBlockNames.FEL_PUMPKIN,
			FelPumpkinBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.CARVED_PUMPKIN));
	public static final Block COCOON_OF_CAPRICE = make(LibBlockNames.COCOON_OF_CAPRICE,
			CocoonBlock::new, BlockBehaviour.Properties.of()
					.strength(3, 60)
					.sound(SoundType.WOOL));
	public static final Block ANIMATED_TORCH = make(LibBlockNames.ANIMATED_TORCH,
			AnimatedTorchBlock::new, BlockBehaviour.Properties.of()
					.noCollision()
					.instabreak()
					.lightLevel(state -> state.getValue(AnimatedTorchBlock.TRIGGERED) ? 0 : 7)
					.sound(SoundType.WOOD)
					.pushReaction(PushReaction.POPPED));
	public static final Block STARFIELD_CREATOR = make(LibBlockNames.STARFIELD_CREATOR,
			StarfieldCreatorBlock::new, BlockBehaviour.Properties.of()
					.mapColor(DyeColor.PINK)
					.strength(5, 2000)
					.sound(SoundType.METAL));

	public static final Block AZULEJO_0 = make(LibBlockNames.AZULEJO_PREFIX + 0,
			Block::new, BlockBehaviour.Properties.of()
					.mapColor(MapColor.LAPIS)
					.strength(2, 5)
					.sound(SoundType.STONE)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops());
	public static final Block AZULEJO_1 = make(LibBlockNames.AZULEJO_PREFIX + 1,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_2 = make(LibBlockNames.AZULEJO_PREFIX + 2,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_3 = make(LibBlockNames.AZULEJO_PREFIX + 3,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_4 = make(LibBlockNames.AZULEJO_PREFIX + 4,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_5 = make(LibBlockNames.AZULEJO_PREFIX + 5,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_6 = make(LibBlockNames.AZULEJO_PREFIX + 6,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_7 = make(LibBlockNames.AZULEJO_PREFIX + 7,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_8 = make(LibBlockNames.AZULEJO_PREFIX + 8,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_9 = make(LibBlockNames.AZULEJO_PREFIX + 9,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_10 = make(LibBlockNames.AZULEJO_PREFIX + 10,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_11 = make(LibBlockNames.AZULEJO_PREFIX + 11,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_12 = make(LibBlockNames.AZULEJO_PREFIX + 12,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_13 = make(LibBlockNames.AZULEJO_PREFIX + 13,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_14 = make(LibBlockNames.AZULEJO_PREFIX + 14,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));
	public static final Block AZULEJO_15 = make(LibBlockNames.AZULEJO_PREFIX + 15,
			Block::new, BlockBehaviour.Properties.ofFullCopy(AZULEJO_0));

	public static final Block MANA_FLAME = make(LibBlockNames.MANA_FLAME,
			ManaFlameBlock::new, BlockBehaviour.Properties.of()
					.pushReaction(PushReaction.POPPED)
					.sound(SoundType.WOOL)
					.lightLevel(constInt(15))
					.noCollision());
	public static final Block BLAZE_MESH = make(LibBlockNames.BLAZE_BLOCK,
			Block::new, BlockBehaviour.Properties.ofFullCopy(MANASTEEL_BLOCK)
					.instrument(NoteBlockInstrument.PLING)
					.lightLevel(constInt(15))
					.mapColor(MapColor.GOLD));
	public static final Block GAIA_HEAD = make(LibBlockNames.GAIA_HEAD,
			GaiaHeadBlock::new, BlockBehaviour.Properties.of()
					.pushReaction(PushReaction.POPPED)
					.strength(1));
	public static final Block GAIA_WALL_HEAD_BLOCK = make(LibBlockNames.GAIA_WALL_HEAD,
			GaiaWallHeadBlock::new, BlockBehaviour.Properties.ofFullCopy(GAIA_HEAD)
					.overrideDescription(GAIA_HEAD.getDescriptionId())); // PORT-26.3: wall blocks name themselves like vanilla's wall skulls

	public static final Block SHIMMERROCK = make(LibBlockNames.SHIMMERROCK,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK));
	public static final Block SHIMMERROCK_STAIRS = make(LibBlockNames.SHIMMERROCK + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SHIMMERROCK.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(SHIMMERROCK));
	public static final Block SHIMMERROCK_SLAB = make(LibBlockNames.SHIMMERROCK + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SHIMMERROCK));
	public static final Block SHIMMERROCK_WALL = make(LibBlockNames.SHIMMERROCK + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(SHIMMERROCK));
	public static final Block SHIMMERROCK_BUTTON = make(LibBlockNames.SHIMMERROCK + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.SHIMMERROCK_BLOCK_SET, 20,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON)
					.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block SHIMMERROCK_PRESSURE_PLATE = make(LibBlockNames.SHIMMERROCK + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.SHIMMERROCK_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)
					.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block SHIMMERWOOD_PLANKS = make(LibBlockNames.SHIMMERWOOD + LibBlockNames.PLANKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LIVINGWOOD));
	public static final Block SHIMMERWOOD_PLANK_STAIRS = make(LibBlockNames.SHIMMERWOOD + LibBlockNames.PLANK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SHIMMERWOOD_PLANKS.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(SHIMMERWOOD_PLANKS));
	public static final Block SHIMMERWOOD_PLANK_SLAB = make(LibBlockNames.SHIMMERWOOD + LibBlockNames.PLANK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SHIMMERWOOD_PLANKS));
	public static final Block SHIMMERWOOD_FENCE = make(LibBlockNames.SHIMMERWOOD + LibBlockNames.FENCE_SUFFIX,
			FenceBlock::new, BlockBehaviour.Properties.ofFullCopy(SHIMMERWOOD_PLANKS));
	public static final Block SHIMMERWOOD_FENCE_GATE = make(LibBlockNames.SHIMMERWOOD + LibBlockNames.FENCE_GATE_SUFFIX,
			props -> new FenceGateBlock(BotaniaBlockSetTypes.SHIMMERWOOD,
					props),
			BlockBehaviour.Properties.ofFullCopy(SHIMMERWOOD_PLANKS));
	public static final Block SHIMMERWOOD_PRESSURE_PLATE = make(LibBlockNames.SHIMMERWOOD + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.SHIMMERWOOD_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(SHIMMERWOOD_PLANKS)
					.noCollision()
					.strength(0.5F)
					.forceSolidOn()
					.pushReaction(PushReaction.POPPED));
	public static final Block SHIMMERWOOD_BUTTON = make(LibBlockNames.SHIMMERWOOD + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.SHIMMERWOOD_BLOCK_SET, 30,
					props),
			BlockBehaviour.Properties.ofFullCopy(SHIMMERWOOD_PLANKS)
					.noCollision()
					.strength(0.5F)
					.pushReaction(PushReaction.POPPED));

	// grass blocks
	public static final Block DRY_GRASS_BLOCK = make("dry" + LibBlockNames.ALT_GRASS_SUFFIX,
			BotaniaGrassBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
					.mapColor(MapColor.TERRACOTTA_LIGHT_GREEN));
	public static final Block GOLDEN_GRASS_BLOCK = make("golden" + LibBlockNames.ALT_GRASS_SUFFIX,
			BotaniaGrassBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
					.mapColor(MapColor.GOLD));
	public static final Block VIVID_GRASS_BLOCK = make("vivid" + LibBlockNames.ALT_GRASS_SUFFIX,
			BotaniaGrassBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
					.mapColor(MapColor.PLANT));
	public static final Block SCORCHED_GRASS_BLOCK = make("scorched" + LibBlockNames.ALT_GRASS_SUFFIX,
			BotaniaScorchedGrassBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
					.mapColor(MapColor.NETHER));
	public static final Block INFUSED_GRASS_BLOCK = make("infused" + LibBlockNames.ALT_GRASS_SUFFIX,
			BotaniaInfusedGrassBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
					.mapColor(MapColor.COLOR_CYAN));
	public static final Block MUTATED_GRASS_BLOCK = make("mutated" + LibBlockNames.ALT_GRASS_SUFFIX,
			BotaniaMutatedGrassBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)
					.mapColor(MapColor.WARPED_HYPHAE));

	public static final Block DAYBLOOM_MOTIF = make(LibBlockNames.MOTIF_DAYBLOOM,
			props -> new FlowerMotifBlock(MobEffects.BLINDNESS, 15, props, true), BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY));
	public static final Block NIGHTSHADE_MOTIF = make(LibBlockNames.MOTIF_NIGHTSHADE,
			props -> new FlowerMotifBlock(MobEffects.POISON, 20, props, true), BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY));
	public static final Block HYDROANGEAS_MOTIF = make(LibBlockNames.MOTIF_HYDROANGEAS,
			props -> new FlowerMotifBlock(MobEffects.UNLUCK, 10, props, false), BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY));

	public static final Block POTTED_DAYBLOOM_MOTIF = make(LibBlockNames.POTTED_PREFIX + LibBlockNames.MOTIF_DAYBLOOM,
			flowerPot(DAYBLOOM_MOTIF, 0), flowerPotProperties());
	public static final Block POTTED_NIGHTSHADE_MOTIF = make(LibBlockNames.POTTED_PREFIX + LibBlockNames.MOTIF_NIGHTSHADE,
			flowerPot(NIGHTSHADE_MOTIF, 0), flowerPotProperties());
	public static final Block POTTED_HYDROANGEAS_MOTIF = make(LibBlockNames.POTTED_PREFIX + LibBlockNames.MOTIF_HYDROANGEAS,
			flowerPot(HYDROANGEAS_MOTIF, 0), flowerPotProperties());

	// quartz variants
	public static final Block SMOKEY_QUARTZ_BLOCK = make(LibBlockNames.SMOKEY_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK)
					.mapColor(MapColor.COLOR_BLACK));
	public static final Block SMOKEY_QUARTZ_STAIRS = make(LibBlockNames.SMOKEY_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SMOKEY_QUARTZ_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(SMOKEY_QUARTZ_BLOCK));
	public static final Block SMOKEY_QUARTZ_SLAB = make(LibBlockNames.SMOKEY_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SMOKEY_QUARTZ_BLOCK));
	public static final Block CHISELED_SMOKEY_QUARTZ_BLOCK = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.SMOKEY_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(SMOKEY_QUARTZ_BLOCK));
	public static final Block SMOKEY_QUARTZ_BRICKS = make(LibBlockNames.SMOKEY_QUARTZ + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(SMOKEY_QUARTZ_BLOCK));
	public static final Block SMOKEY_QUARTZ_PILLAR = make(LibBlockNames.SMOKEY_QUARTZ + LibBlockNames.PILLAR_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(SMOKEY_QUARTZ_BLOCK));
	public static final Block SMOOTH_SMOKEY_QUARTZ_BLOCK = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.SMOKEY_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_QUARTZ)
					.mapColor(MapColor.COLOR_BLACK));
	public static final Block SMOOTH_SMOKEY_QUARTZ_STAIRS = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.SMOKEY_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SMOOTH_SMOKEY_QUARTZ_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(SMOOTH_SMOKEY_QUARTZ_BLOCK));
	public static final Block SMOOTH_SMOKEY_QUARTZ_SLAB = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.SMOKEY_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SMOOTH_SMOKEY_QUARTZ_BLOCK));

	public static final Block MANA_QUARTZ_BLOCK = make(LibBlockNames.MANA_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK)
					.mapColor(MapColor.DIAMOND));
	public static final Block MANA_QUARTZ_STAIRS = make(LibBlockNames.MANA_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(MANA_QUARTZ_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(SMOKEY_QUARTZ_BLOCK));
	public static final Block MANA_QUARTZ_SLAB = make(LibBlockNames.MANA_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(MANA_QUARTZ_BLOCK));
	public static final Block CHISELED_MANA_QUARTZ_BLOCK = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.MANA_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(MANA_QUARTZ_BLOCK));
	public static final Block MANA_QUARTZ_BRICKS = make(LibBlockNames.MANA_QUARTZ + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(MANA_QUARTZ_BLOCK));
	public static final Block MANA_QUARTZ_PILLAR = make(LibBlockNames.MANA_QUARTZ + LibBlockNames.PILLAR_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(MANA_QUARTZ_BLOCK));
	public static final Block SMOOTH_MANA_QUARTZ_BLOCK = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.MANA_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_QUARTZ)
					.mapColor(MapColor.DIAMOND));
	public static final Block SMOOTH_MANA_QUARTZ_STAIRS = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.MANA_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SMOOTH_MANA_QUARTZ_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(SMOOTH_MANA_QUARTZ_BLOCK));
	public static final Block SMOOTH_MANA_QUARTZ_SLAB = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.MANA_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SMOOTH_MANA_QUARTZ_BLOCK));

	public static final Block BLAZE_QUARTZ_BLOCK = make(LibBlockNames.BLAZE_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK)
					.mapColor(MapColor.SAND));
	public static final Block BLAZE_QUARTZ_STAIRS = make(LibBlockNames.BLAZE_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(BLAZE_QUARTZ_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(BLAZE_QUARTZ_BLOCK));
	public static final Block BLAZE_QUARTZ_SLAB = make(LibBlockNames.BLAZE_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(BLAZE_QUARTZ_BLOCK));
	public static final Block CHISELED_BLAZE_QUARTZ_BLOCK = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.BLAZE_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(BLAZE_QUARTZ_BLOCK));
	public static final Block BLAZE_QUARTZ_BRICKS = make(LibBlockNames.BLAZE_QUARTZ + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(BLAZE_QUARTZ_BLOCK));
	public static final Block BLAZE_QUARTZ_PILLAR = make(LibBlockNames.BLAZE_QUARTZ + LibBlockNames.PILLAR_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(BLAZE_QUARTZ_BLOCK));
	public static final Block SMOOTH_BLAZE_QUARTZ_BLOCKS = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.BLAZE_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_QUARTZ)
					.mapColor(MapColor.SAND));
	public static final Block SMOOTH_BLAZE_QUARTZ_STAIRS = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.BLAZE_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SMOOTH_BLAZE_QUARTZ_BLOCKS.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(SMOOTH_BLAZE_QUARTZ_BLOCKS));
	public static final Block SMOOTH_BLAZE_QUARTZ_SLAB = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.BLAZE_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SMOOTH_BLAZE_QUARTZ_BLOCKS));

	public static final Block LAVENDER_QUARTZ_BLOCK = make(LibBlockNames.LAVENDER_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK)
					.mapColor(MapColor.COLOR_PINK));
	public static final Block LAVENDER_QUARTZ_STAIRS = make(LibBlockNames.LAVENDER_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(LAVENDER_QUARTZ_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(LAVENDER_QUARTZ_BLOCK));
	public static final Block LAVENDER_QUARTZ_SLAB = make(LibBlockNames.LAVENDER_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(LAVENDER_QUARTZ_BLOCK));
	public static final Block CHISELED_LAVENDER_QUARTZ_BLOCK = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.LAVENDER_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LAVENDER_QUARTZ_BLOCK));
	public static final Block LAVENDER_QUARTZ_BRICKS = make(LibBlockNames.LAVENDER_QUARTZ + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LAVENDER_QUARTZ_BLOCK));
	public static final Block LAVENDER_QUARTZ_PILLAR = make(LibBlockNames.LAVENDER_QUARTZ + LibBlockNames.PILLAR_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(LAVENDER_QUARTZ_BLOCK));
	public static final Block SMOOTH_LAVENDER_QUARTZ_BLOCK = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.LAVENDER_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_QUARTZ)
					.mapColor(MapColor.COLOR_PINK));
	public static final Block SMOOTH_LAVENDER_QUARTZ_STAIRS = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.LAVENDER_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SMOOTH_LAVENDER_QUARTZ_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(SMOOTH_LAVENDER_QUARTZ_BLOCK));
	public static final Block SMOOTH_LAVENDER_QUARTZ_SLAB = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.LAVENDER_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SMOOTH_LAVENDER_QUARTZ_BLOCK));

	public static final Block RED_QUARTZ_BLOCK = make(LibBlockNames.RED_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK)
					.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block RED_QUARTZ_STAIRS = make(LibBlockNames.RED_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(RED_QUARTZ_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(RED_QUARTZ_BLOCK));
	public static final Block RED_QUARTZ_SLAB = make(LibBlockNames.RED_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(RED_QUARTZ_BLOCK));
	public static final Block CHISELED_RED_QUARTZ_BLOCK = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.RED_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(RED_QUARTZ_BLOCK));
	public static final Block RED_QUARTZ_BRICKS = make(LibBlockNames.RED_QUARTZ + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(RED_QUARTZ_BLOCK));
	public static final Block RED_QUARTZ_PILLAR = make(LibBlockNames.RED_QUARTZ + LibBlockNames.PILLAR_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(RED_QUARTZ_BLOCK));
	public static final Block SMOOTH_RED_QUARTZ_BLOCK = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.RED_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_QUARTZ)
					.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block SMOOTH_RED_QUARTZ_STAIRS = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.RED_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SMOOTH_RED_QUARTZ_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(SMOOTH_RED_QUARTZ_BLOCK));
	public static final Block SMOOTH_RED_QUARTZ_SLAB = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.RED_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SMOOTH_RED_QUARTZ_BLOCK));

	public static final Block ELVEN_QUARTZ_BLOCK = make(LibBlockNames.ELVEN_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK)
					.mapColor(MapColor.COLOR_LIGHT_GREEN));
	public static final Block ELVEN_QUARTZ_STAIRS = make(LibBlockNames.ELVEN_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(ELVEN_QUARTZ_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(ELVEN_QUARTZ_BLOCK));
	public static final Block ELVEN_QUARTZ_SLAB = make(LibBlockNames.ELVEN_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(ELVEN_QUARTZ_BLOCK));
	public static final Block CHISELED_ELVEN_QUARTZ_BLOCK = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.ELVEN_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(ELVEN_QUARTZ_BLOCK));
	public static final Block ELVEN_QUARTZ_BRICKS = make(LibBlockNames.ELVEN_QUARTZ + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(ELVEN_QUARTZ_BLOCK));
	public static final Block ELVEN_QUARTZ_PILLAR = make(LibBlockNames.ELVEN_QUARTZ + LibBlockNames.PILLAR_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(ELVEN_QUARTZ_BLOCK));
	public static final Block SMOOTH_ELVEN_QUARTZ_BLOCK = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.ELVEN_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_QUARTZ)
					.mapColor(MapColor.COLOR_LIGHT_GREEN));
	public static final Block SMOOTH_ELVEN_QUARTZ_STAIRS = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.ELVEN_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SMOOTH_ELVEN_QUARTZ_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(SMOOTH_ELVEN_QUARTZ_BLOCK));
	public static final Block SMOOTH_ELVEN_QUARTZ_SLAB = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.ELVEN_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SMOOTH_ELVEN_QUARTZ_BLOCK));

	public static final Block SUNNY_QUART_BLOCK = make(LibBlockNames.SUNNY_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK)
					.mapColor(MapColor.COLOR_YELLOW));
	public static final Block SUNNY_QUARTZ_STAIRS = make(LibBlockNames.SUNNY_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SUNNY_QUART_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(SUNNY_QUART_BLOCK));
	public static final Block SUNNY_QUARTZ_SLAB = make(LibBlockNames.SUNNY_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SUNNY_QUART_BLOCK));
	public static final Block CHISELED_SUNNY_QUARTZ_BLOCK = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.SUNNY_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(SUNNY_QUART_BLOCK));
	public static final Block SUNNY_QUARTZ_BRICKS = make(LibBlockNames.SUNNY_QUARTZ + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(SUNNY_QUART_BLOCK));
	public static final Block SUNNY_QUARTZ_PILLAR = make(LibBlockNames.SUNNY_QUARTZ + LibBlockNames.PILLAR_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(SUNNY_QUART_BLOCK));
	public static final Block SMOOTH_SUNNY_QUARTZ_BLOCK = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.SUNNY_QUARTZ + LibBlockNames.BLOCK_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_QUARTZ)
					.mapColor(MapColor.COLOR_YELLOW));
	public static final Block SMOOTH_SUNNY_QUARTZ_STAIRS = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.SUNNY_QUARTZ + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SMOOTH_SUNNY_QUARTZ_BLOCK.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(SMOOTH_SUNNY_QUARTZ_BLOCK));
	public static final Block SMOOTH_SUNNY_QUARTZ_SLAB = make(LibBlockNames.SMOOTH_PREFIX + LibBlockNames.SUNNY_QUARTZ + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SMOOTH_SUNNY_QUARTZ_BLOCK));

	// metamorphic biome blocks
	public static final Block FUCHSITE = make(LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE,
			Block::new, BlockBehaviour.Properties.of()
					.strength(1.5F, 10)
					.sound(SoundType.TUFF)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops()
					.mapColor(MapColor.WARPED_NYLIUM));
	public static final Block FUCHSITE_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(FUCHSITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(FUCHSITE));
	public static final Block FUCHSITE_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE));
	public static final Block FUCHSITE_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE));
	public static final Block FUCHSITE_BUTTON = make(LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.METAMORPHIC_FOREST_BLOCK_SET, 20,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON)
					.mapColor(MapColor.WARPED_NYLIUM));
	public static final Block FUCHSITE_PRESSURE_PLATE = make(LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.METAMORPHIC_FOREST_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)
					.mapColor(MapColor.WARPED_NYLIUM));
	public static final Block COBBLED_FUCHSITE = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE,
			Block::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE));
	public static final Block COBBLED_FUCHSITE_STAIRS = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE
			+ LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(FUCHSITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(FUCHSITE));
	public static final Block COBBLED_FUCHSITE_SLAB = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE
			+ LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE));
	public static final Block COBBLED_FUCHSITE_WALL = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE
			+ LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE));
	public static final Block FUCHSITE_BRICKS = make(LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE));
	public static final Block FUCHSITE_BRICK_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE + LibBlockNames.BRICK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(FUCHSITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(FUCHSITE));
	public static final Block FUCHSITE_BRICK_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE + LibBlockNames.BRICK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE));
	public static final Block FUCHSITE_BRICK_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE + LibBlockNames.BRICK_INFIX + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE));
	public static final Block CHISELED_FUCHSITE_BRICKS = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_FUCHSITE
			+ LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE));

	public static final Block TALC = make(LibBlockNames.METAMORPHIC_VARIANT_TALC,
			Block::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE)
					.sound(SoundType.CALCITE)
					.mapColor(MapColor.QUARTZ));
	public static final Block TALC_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_TALC + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(TALC.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(TALC));
	public static final Block TALC_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_TALC + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(TALC));
	public static final Block TALC_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_TALC + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(TALC));
	public static final Block TALC_BUTTON = make(LibBlockNames.METAMORPHIC_VARIANT_TALC + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.METAMORPHIC_PLAINS_BLOCK_SET, 20,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON)
					.mapColor(MapColor.QUARTZ));
	public static final Block TALC_PRESSURE_PLATE = make(LibBlockNames.METAMORPHIC_VARIANT_TALC + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.METAMORPHIC_PLAINS_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)
					.mapColor(MapColor.QUARTZ));
	public static final Block COBBLED_TALC = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_TALC,
			Block::new, BlockBehaviour.Properties.ofFullCopy(TALC));
	public static final Block COBBLED_TALC_STAIRS = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_TALC
			+ LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(TALC.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(TALC));
	public static final Block COBBLED_TALC_SLAB = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_TALC
			+ LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(TALC));
	public static final Block COBBLED_TALC_WALL = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_TALC
			+ LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(TALC));
	public static final Block TALC_BRICKS = make(LibBlockNames.METAMORPHIC_VARIANT_TALC + LibBlockNames.BRICKS_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(TALC));
	public static final Block TALC_BRICK_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_TALC + LibBlockNames.BRICK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(TALC.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(TALC));
	public static final Block TALC_BRICK_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_TALC + LibBlockNames.BRICK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(TALC));
	public static final Block TALC_BRICK_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_TALC + LibBlockNames.BRICK_INFIX + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(TALC));
	public static final Block CHISELED_TALC_BRICKS = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_TALC
			+ LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(TALC));

	public static final Block GNEISS = make(LibBlockNames.METAMORPHIC_VARIANT_GNEISS,
			Block::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE)
					.sound(SoundType.DEEPSLATE_TILES)
					.mapColor(MapColor.GLOW_LICHEN));
	public static final Block GNEISS_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_GNEISS + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(GNEISS.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(GNEISS));
	public static final Block GNEISS_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_GNEISS + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(GNEISS));
	public static final Block GNEISS_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_GNEISS + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(GNEISS));
	public static final Block GNEISS_BUTTON = make(LibBlockNames.METAMORPHIC_VARIANT_GNEISS + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.METAMORPHIC_MOUNTAIN_BLOCK_SET, 20,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON)
					.mapColor(MapColor.GLOW_LICHEN));
	public static final Block GNEISS_PRESSURE_PLATE = make(LibBlockNames.METAMORPHIC_VARIANT_GNEISS + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.METAMORPHIC_MOUNTAIN_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)
					.mapColor(MapColor.GLOW_LICHEN));
	public static final Block COBBLED_GNEISS = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_GNEISS,
			Block::new, BlockBehaviour.Properties.ofFullCopy(GNEISS));
	public static final Block COBBLED_GNEISS_STAIRS = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_GNEISS
			+ LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(GNEISS.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(GNEISS));
	public static final Block COBBLED_GNEISS_SLAB = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_GNEISS
			+ LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(GNEISS));
	public static final Block COBBLED_GNEISS_WALL = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_GNEISS
			+ LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(GNEISS));
	public static final Block GNEISS_BRICKS = make(LibBlockNames.METAMORPHIC_VARIANT_GNEISS + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(GNEISS));
	public static final Block GNEISS_BRICK_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_GNEISS + LibBlockNames.BRICK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(GNEISS.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(GNEISS));
	public static final Block GNEISS_BRICK_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_GNEISS + LibBlockNames.BRICK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(GNEISS));
	public static final Block GNEISS_BRICK_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_GNEISS + LibBlockNames.BRICK_INFIX + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(GNEISS));
	public static final Block CHISELED_GNEISS_BRICKS = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_GNEISS
			+ LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(GNEISS));

	public static final Block MYCELITE = make(LibBlockNames.METAMORPHIC_VARIANT_MYCELITE,
			Block::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE)
					.sound(SoundType.DEEPSLATE_BRICKS)
					.mapColor(MapColor.TERRACOTTA_PURPLE));
	public static final Block MYCELITE_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_MYCELITE + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(MYCELITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(MYCELITE));
	public static final Block MYCELITE_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_MYCELITE + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(MYCELITE));
	public static final Block MYCELITE_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_MYCELITE + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(MYCELITE));
	public static final Block MYCELITE_BUTTON = make(LibBlockNames.METAMORPHIC_VARIANT_MYCELITE + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.METAMORPHIC_FUNGAL_BLOCK_SET, 20,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON)
					.mapColor(MapColor.TERRACOTTA_PURPLE));
	public static final Block MYCELITE_PRESSURE_PLATE = make(LibBlockNames.METAMORPHIC_VARIANT_MYCELITE + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.METAMORPHIC_FUNGAL_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)
					.mapColor(MapColor.TERRACOTTA_PURPLE));
	public static final Block COBBLED_MYCELITE = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_MYCELITE,
			Block::new, BlockBehaviour.Properties.ofFullCopy(MYCELITE));
	public static final Block COBBLED_MYCELITE_STAIRS = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_MYCELITE
			+ LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(MYCELITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(MYCELITE));
	public static final Block COBBLED_MYCELITE_SLAB = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_MYCELITE
			+ LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(MYCELITE));
	public static final Block COBBLED_MYCELITE_WALL = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_MYCELITE
			+ LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(MYCELITE));
	public static final Block MYCELITE_BRICKS = make(LibBlockNames.METAMORPHIC_VARIANT_MYCELITE + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(MYCELITE));
	public static final Block MYCELITE_BRICK_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_MYCELITE + LibBlockNames.BRICK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(MYCELITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(MYCELITE));
	public static final Block MYCELITE_BRICK_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_MYCELITE + LibBlockNames.BRICK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(MYCELITE));
	public static final Block MYCELITE_BRICK_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_MYCELITE + LibBlockNames.BRICK_INFIX + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(MYCELITE));
	public static final Block CHISELED_MYCELITE_BRICKS = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_MYCELITE
			+ LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(MYCELITE));

	public static final Block CATACLASITE = make(LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE,
			Block::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE)
					.sound(SoundType.DEEPSLATE_TILES)
					.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY));
	public static final Block CATACLASITE_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(CATACLASITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(CATACLASITE));
	public static final Block CATACLASITE_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(CATACLASITE));
	public static final Block CATACLASITE_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(CATACLASITE));
	public static final Block CATACLASITE_BUTTON = make(LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.METAMORPHIC_SWAMP_BLOCK_SET, 20,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON)
					.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY));
	public static final Block CATACLASITE_PRESSURE_PLATE = make(LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.METAMORPHIC_SWAMP_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)
					.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY));
	public static final Block COBBLED_CATACLASITE = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE,
			Block::new, BlockBehaviour.Properties.ofFullCopy(CATACLASITE));
	public static final Block COBBLED_CATACLASITE_STAIRS = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE
			+ LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(CATACLASITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(CATACLASITE));
	public static final Block COBBLED_CATACLASITE_SLAB = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE
			+ LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(CATACLASITE));
	public static final Block COBBLED_CATACLASITE_WALL = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE
			+ LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(CATACLASITE));
	public static final Block CATACLASITE_BRICKS = make(LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE + LibBlockNames.BRICKS_SUFFIX,
			BotaniaDirectionalBlock::new, BlockBehaviour.Properties.ofFullCopy(CATACLASITE));
	public static final Block CATACLASITE_BRICK_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE + LibBlockNames.BRICK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(CATACLASITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(CATACLASITE));
	public static final Block CATACLASITE_BRICK_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE + LibBlockNames.BRICK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(CATACLASITE));
	public static final Block CATACLASITE_BRICK_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE + LibBlockNames.BRICK_INFIX + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(CATACLASITE));
	public static final Block CHISELED_CATACLASITE_BRICKS = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_CATACLASITE
			+ LibBlockNames.BRICKS_SUFFIX,
			BotaniaDirectionalBlock::new, BlockBehaviour.Properties.ofFullCopy(CATACLASITE));

	public static final Block SOLITE = make(LibBlockNames.METAMORPHIC_VARIANT_SOLITE,
			Block::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE)
					.sound(SoundType.DEEPSLATE)
					.mapColor(MapColor.DIRT));
	public static final Block SOLITE_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_SOLITE + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SOLITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(SOLITE));
	public static final Block SOLITE_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_SOLITE + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SOLITE));
	public static final Block SOLITE_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_SOLITE + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(SOLITE));
	public static final Block SOLITE_BUTTON = make(LibBlockNames.METAMORPHIC_VARIANT_SOLITE + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.METAMORPHIC_DESERT_BLOCK_SET, 20,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON)
					.mapColor(MapColor.DIRT));
	public static final Block SOLITE_PRESSURE_PLATE = make(LibBlockNames.METAMORPHIC_VARIANT_SOLITE + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.METAMORPHIC_DESERT_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)
					.mapColor(MapColor.DIRT));
	public static final Block COBBLED_SOLITE = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_SOLITE,
			Block::new, BlockBehaviour.Properties.ofFullCopy(SOLITE));
	public static final Block COBBLED_SOLITE_STAIRS = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_SOLITE
			+ LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SOLITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(SOLITE));
	public static final Block COBBLED_SOLITE_SLAB = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_SOLITE
			+ LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SOLITE));
	public static final Block COBBLED_SOLITE_WALL = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_SOLITE
			+ LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(SOLITE));
	public static final Block SOLITE_BRICKS = make(LibBlockNames.METAMORPHIC_VARIANT_SOLITE + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(SOLITE));
	public static final Block SOLITE_BRICK_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_SOLITE + LibBlockNames.BRICK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(SOLITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(SOLITE));
	public static final Block SOLITE_BRICK_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_SOLITE + LibBlockNames.BRICK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(SOLITE));
	public static final Block SOLITE_BRICK_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_SOLITE + LibBlockNames.BRICK_INFIX + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(SOLITE));
	public static final Block CHISELED_SOLITE_BRICKS = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_SOLITE
			+ LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(SOLITE));

	public static final Block LUNITE = make(LibBlockNames.METAMORPHIC_VARIANT_LUNITE,
			Block::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE)
					.sound(SoundType.DEEPSLATE)
					.mapColor(MapColor.TERRACOTTA_BLUE));
	public static final Block LUNITE_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_LUNITE + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(LUNITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(LUNITE));
	public static final Block LUNITE_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_LUNITE + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(LUNITE));
	public static final Block LUNITE_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_LUNITE + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(LUNITE));
	public static final Block LUNITE_BUTTON = make(LibBlockNames.METAMORPHIC_VARIANT_LUNITE + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.METAMORPHIC_TAIGA_BLOCK_SET, 20,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON)
					.mapColor(MapColor.TERRACOTTA_BLUE));
	public static final Block LUNITE_PRESSURE_PLATE = make(LibBlockNames.METAMORPHIC_VARIANT_LUNITE + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.METAMORPHIC_TAIGA_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)
					.mapColor(MapColor.TERRACOTTA_BLUE));
	public static final Block COBBLED_LUNITE = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_LUNITE,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LUNITE));
	public static final Block COBBLED_LUNITE_STAIRS = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_LUNITE
			+ LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(LUNITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(LUNITE));
	public static final Block COBBLED_LUNITE_SLAB = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_LUNITE
			+ LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(LUNITE));
	public static final Block COBBLED_LUNITE_WALL = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_LUNITE
			+ LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(LUNITE));
	public static final Block LUNITE_BRICKS = make(LibBlockNames.METAMORPHIC_VARIANT_LUNITE + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LUNITE));
	public static final Block LUNITE_BRICK_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_LUNITE + LibBlockNames.BRICK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(LUNITE.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(LUNITE));
	public static final Block LUNITE_BRICK_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_LUNITE + LibBlockNames.BRICK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(LUNITE));
	public static final Block LUNITE_BRICK_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_LUNITE + LibBlockNames.BRICK_INFIX + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(LUNITE));
	public static final Block CHISELED_LUNITE_BRICKS = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_LUNITE
			+ LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LUNITE));

	public static final Block ROSY_TALC = make(LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC,
			Block::new, BlockBehaviour.Properties.ofFullCopy(FUCHSITE)
					.sound(SoundType.CALCITE)
					.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block ROSY_TALC_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(ROSY_TALC.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(ROSY_TALC));
	public static final Block ROSY_TALC_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(ROSY_TALC));
	public static final Block ROSY_TALC_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(ROSY_TALC));
	public static final Block ROSY_TALC_BUTTON = make(LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC + LibBlockNames.BUTTON_SUFFIX,
			props -> new ButtonBlock(BotaniaBlockSetTypes.METAMORPHIC_MESA_BLOCK_SET, 20,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BUTTON)
					.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block ROSY_TALC_PRESSURE_PLATE = make(LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC + LibBlockNames.PRESSURE_PLATE_SUFFIX,
			props -> new PressurePlateBlock(BotaniaBlockSetTypes.METAMORPHIC_MESA_BLOCK_SET,
					props),
			BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_PRESSURE_PLATE)
					.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block COBBLED_ROSY_TALC = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC,
			Block::new, BlockBehaviour.Properties.ofFullCopy(
					ROSY_TALC));
	public static final Block COBBLED_ROSY_TALC_STAIRS = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC
			+ LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(ROSY_TALC.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(ROSY_TALC));
	public static final Block COBBLED_ROSY_TALC_SLAB = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC
			+ LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(ROSY_TALC));
	public static final Block COBBLED_ROSY_TALC_WALL = make(LibBlockNames.COBBLED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC
			+ LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(ROSY_TALC));
	public static final Block ROSY_TALC_BRICKS = make(LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC + LibBlockNames.BRICKS_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(ROSY_TALC));
	public static final Block ROSY_TALC_BRICK_STAIRS = make(LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC + LibBlockNames.BRICK_INFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(ROSY_TALC.defaultBlockState(), props), BlockBehaviour.Properties.ofFullCopy(ROSY_TALC));
	public static final Block ROSY_TALC_BRICK_SLAB = make(LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC + LibBlockNames.BRICK_INFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(ROSY_TALC));
	public static final Block ROSY_TALC_BRICK_WALL = make(LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC + LibBlockNames.BRICK_INFIX + LibBlockNames.WALL_SUFFIX,
			WallBlock::new, BlockBehaviour.Properties.ofFullCopy(ROSY_TALC));
	public static final Block CHISELED_ROSY_TALC_BRICKS = make(LibBlockNames.CHISELED_PREFIX + LibBlockNames.METAMORPHIC_VARIANT_ROSY_TALC
			+ LibBlockNames.BRICKS_SUFFIX,
			RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(ROSY_TALC));

	// pavements
	public static final Block WHITE_PORTUGUESE_PAVEMENT = make(LibBlockNames.PAVEMENT_VARIANT_WHITE + LibBlockNames.PAVEMENT_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(LIVINGROCK)
					.mapColor(MapColor.TERRACOTTA_WHITE));
	public static final Block WHITE_PORTUGUESE_PAVEMENT_STAIRS = make(LibBlockNames.PAVEMENT_VARIANT_WHITE + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(WHITE_PORTUGUESE_PAVEMENT.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(WHITE_PORTUGUESE_PAVEMENT));
	public static final Block WHITE_PORTUGUESE_PAVEMENT_SLAB = make(LibBlockNames.PAVEMENT_VARIANT_WHITE + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(WHITE_PORTUGUESE_PAVEMENT));

	public static final Block BLACK_PORTUGUESE_PAVEMENT = make(LibBlockNames.PAVEMENT_VARIANT_BLACK + LibBlockNames.PAVEMENT_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(WHITE_PORTUGUESE_PAVEMENT)
					.mapColor(MapColor.COLOR_GRAY));
	public static final Block BLACK_PORTUGUESE_PAVEMENT_STAIRS = make(LibBlockNames.PAVEMENT_VARIANT_BLACK + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(BLACK_PORTUGUESE_PAVEMENT.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(BLACK_PORTUGUESE_PAVEMENT));
	public static final Block BLACK_PORTUGUESE_PAVEMENT_SLAB = make(LibBlockNames.PAVEMENT_VARIANT_BLACK + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(BLACK_PORTUGUESE_PAVEMENT));

	public static final Block BLUE_PORTUGUESE_PAVEMENT = make(LibBlockNames.PAVEMENT_VARIANT_BLUE + LibBlockNames.PAVEMENT_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(WHITE_PORTUGUESE_PAVEMENT)
					.mapColor(MapColor.COLOR_BLUE));
	public static final Block BLUE_PORTUGUESE_PAVEMENT_STAIRS = make(LibBlockNames.PAVEMENT_VARIANT_BLUE + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(BLUE_PORTUGUESE_PAVEMENT.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(BLUE_PORTUGUESE_PAVEMENT));
	public static final Block BLUE_PORTUGUESE_PAVEMENT_SLAB = make(LibBlockNames.PAVEMENT_VARIANT_BLUE + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(BLUE_PORTUGUESE_PAVEMENT));

	public static final Block YELLOW_PORTUGUESE_PAVEMENT = make(LibBlockNames.PAVEMENT_VARIANT_YELLOW + LibBlockNames.PAVEMENT_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(WHITE_PORTUGUESE_PAVEMENT)
					.mapColor(MapColor.TERRACOTTA_YELLOW));
	public static final Block YELLOW_PORTUGUESE_PAVEMENT_STAIRS = make(LibBlockNames.PAVEMENT_VARIANT_YELLOW + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(YELLOW_PORTUGUESE_PAVEMENT.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(YELLOW_PORTUGUESE_PAVEMENT));
	public static final Block YELLOW_PORTUGUESE_PAVEMENT_SLAB = make(LibBlockNames.PAVEMENT_VARIANT_YELLOW + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(YELLOW_PORTUGUESE_PAVEMENT));

	public static final Block RED_PORTUGUESE_PAVEMENT = make(LibBlockNames.PAVEMENT_VARIANT_RED + LibBlockNames.PAVEMENT_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(WHITE_PORTUGUESE_PAVEMENT)
					.mapColor(MapColor.TERRACOTTA_RED));
	public static final Block RED_PORTUGUESE_PAVEMENT_STAIRS = make(LibBlockNames.PAVEMENT_VARIANT_RED + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(RED_PORTUGUESE_PAVEMENT.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(RED_PORTUGUESE_PAVEMENT));
	public static final Block RED_PORTUGUESE_PAVEMENT_SLAB = make(LibBlockNames.PAVEMENT_VARIANT_RED + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(RED_PORTUGUESE_PAVEMENT));

	public static final Block GREEN_PORTUGUESE_PAVEMENT = make(LibBlockNames.PAVEMENT_VARIANT_GREEN + LibBlockNames.PAVEMENT_SUFFIX,
			Block::new, BlockBehaviour.Properties.ofFullCopy(WHITE_PORTUGUESE_PAVEMENT)
					.mapColor(MapColor.TERRACOTTA_GREEN));
	public static final Block GREEN_PORTUGUESE_PAVEMENT_STAIRS = make(LibBlockNames.PAVEMENT_VARIANT_GREEN + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.STAIRS_SUFFIX,
			props -> new StairBlock(GREEN_PORTUGUESE_PAVEMENT.defaultBlockState(),
					props),
			BlockBehaviour.Properties.ofFullCopy(GREEN_PORTUGUESE_PAVEMENT));
	public static final Block GREEN_PORTUGUESE_PAVEMENT_SLAB = make(LibBlockNames.PAVEMENT_VARIANT_GREEN + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.SLAB_SUFFIX,
			SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(GREEN_PORTUGUESE_PAVEMENT));

	public static final Block MANAGLASS_PANE = make(LibBlockNames.MANAGLASS + "_pane",
			IronBarsBlock::new, BlockBehaviour.Properties.ofFullCopy(MANAGLASS));
	public static final Block ALFGLASS_PANE = make(LibBlockNames.ALFGLASS + "_pane",
			IronBarsBlock::new, BlockBehaviour.Properties.ofFullCopy(ALFGLASS));
	public static final Block BIFROST_PANE = make(LibBlockNames.BIFROST + "_pane",
			IronBarsBlock::new, BlockBehaviour.Properties.ofFullCopy(BIFROST));

	// PORT-26.3: the pot's id has to be in its properties before it is built, so this hands out a factory for make
	static Function<BlockBehaviour.Properties, FlowerPotBlock> flowerPot(Block block, int lightLevel) {
		return properties -> new FlowerPotBlock(block, lightLevel > 0
				? properties.lightLevel(blockState -> lightLevel)
				: properties);
	}

	static BlockBehaviour.Properties flowerPotProperties() {
		return BlockBehaviour.Properties.of()
				.instabreak()
				.noOcclusion()
				.pushReaction(PushReaction.POPPED);
	}

	private static String floating(String orig) {
		return "floating_" + orig;
	}

	private static String potted(String orig) {
		return "potted_" + orig;
	}

	private static String petite(String orig) {
		return orig + "_petite";
	}

	public static void registerBlocks(BiConsumer<Block, Identifier> r) {
		for (var e : ALL.entrySet()) {
			r.accept(e.getValue(), botaniaRL(e.getKey()));
		}
	}

	private static <T> ToIntFunction<T> constInt(int value) {
		return ignored -> value;
	}

	// PORT-26.3: furnace fuel is a data component (vanilla's data-driven fuel values replaced the loader fuel registries)
	private static CookingFuel cookingFuel(int burnTime) {
		return new CookingFuel(new ResolvableInt.Constant(burnTime), ResolvableFloat.fromKey(ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER));
	}

	public static void registerItemBlocks(BiConsumer<Item, Identifier> r) {
		// PORT-26.3: every item needs its own properties carrying its id, so these are suppliers now
		Supplier<Item.Properties> props = BotaniaItems::defaultBuilder;
		Supplier<Item.Properties> woodenWallProps = () -> BotaniaItems.defaultBuilder().component(DataComponents.COOKING_FUEL, cookingFuel(300));
		Supplier<Item.Properties> blazeMeshProps = () -> BotaniaItems.defaultBuilder().component(DataComponents.COOKING_FUEL,
				cookingFuel(2400 * (XplatAbstractions.INSTANCE.gogLoaded() ? 5 : 10)));
		Supplier<Item.Properties> uncommonProps = () -> BotaniaItems.defaultBuilder().rarity(Rarity.UNCOMMON);
		Supplier<Item.Properties> rareProps = () -> BotaniaItems.defaultBuilder().rarity(Rarity.RARE);
		Supplier<Item.Properties> epicProps = () -> BotaniaItems.defaultBuilder().rarity(Rarity.EPIC);
		Stream.<Function<DyeColor, Block>>of(
				BotaniaBlocks::getMysticalFlower,
				BotaniaBlocks::getGlimmeringFlower,
				BotaniaBlocks::getFloatingFlower,
				BotaniaBlocks::getPetalBlock,
				BotaniaBlocks::getShimmeringMushroom,
				BotaniaBlocks::getTallMysticalFlower
		).forEach(
				blockGetter -> ColorHelper.supportedColors().map(blockGetter).forEach(
						block -> blockItem(r, block, (b, properties) -> new ColoredBlockItem(b, ((Colored) b).getColor(), properties), props)
				)
		);

		blockItem(r, PURE_DAISY, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_PURE_DAISY, SpecialFlowerBlockItem::new, props);

		blockItem(r, MANASTAR, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_MANASTAR, SpecialFlowerBlockItem::new, props);

		blockItem(r, HYDROANGEAS, (block, properties) -> new DecayableSpecialFlowerBlockItem(block, HydroangeasBlockEntity.DECAY_TIME, properties), props);
		blockItem(r, FLOATING_HYDROANGEAS, (block, properties) -> new DecayableSpecialFlowerBlockItem(block, HydroangeasBlockEntity.DECAY_TIME, properties), props);

		blockItem(r, ENDOFLAME, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_ENDOFLAME, SpecialFlowerBlockItem::new, props);

		blockItem(r, THERMALILY, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_THERMALILY, SpecialFlowerBlockItem::new, props);

		blockItem(r, ROSA_ARCANA, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_ROSA_ARCANA, SpecialFlowerBlockItem::new, props);

		blockItem(r, MUNCHDEW, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_MUNCHDEW, SpecialFlowerBlockItem::new, props);

		blockItem(r, ENTROPINNYUM, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_ENTROPINNYUM, SpecialFlowerBlockItem::new, props);

		blockItem(r, KEKIMURUS, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_KEKIMURUS, SpecialFlowerBlockItem::new, props);

		blockItem(r, GOURMARYLLIS, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_GOURMARYLLIS, SpecialFlowerBlockItem::new, props);

		blockItem(r, NARSLIMMUS, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_NARSLIMMUS, SpecialFlowerBlockItem::new, props);

		blockItem(r, SPECTROLUS, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_SPECTROLUS, SpecialFlowerBlockItem::new, props);

		blockItem(r, DANDELIFEON, SpecialFlowerBlockItem::new, rareProps);
		blockItem(r, FLOATING_DANDELIFEON, SpecialFlowerBlockItem::new, rareProps);

		blockItem(r, RAFFLOWSIA, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_RAFFLOWSIA, SpecialFlowerBlockItem::new, props);

		blockItem(r, SHULK_ME_NOT, SpecialFlowerBlockItem::new, rareProps);
		blockItem(r, FLOATING_SHULK_ME_NOT, SpecialFlowerBlockItem::new, rareProps);

		blockItem(r, BELLETHORNE, SpecialFlowerBlockItem::new, props);
		blockItem(r, BELLETHORNE_PETITE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_BELLETHORNE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_BELLETHORNE_PETITE, SpecialFlowerBlockItem::new, props);

		blockItem(r, BERGAMUTE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_BERGAMUTE, SpecialFlowerBlockItem::new, props);

		blockItem(r, DREADTHORNE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_DREADTHORNE, SpecialFlowerBlockItem::new, props);

		blockItem(r, HEISEI_DREAM, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_HEISEI_DREAM, SpecialFlowerBlockItem::new, props);

		blockItem(r, TIGERSEYE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_TIGERSEYE, SpecialFlowerBlockItem::new, props);

		blockItem(r, JADED_AMARANTHUS, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_JADED_AMARANTHUS, SpecialFlowerBlockItem::new, props);

		blockItem(r, ORECHID, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_ORECHID, SpecialFlowerBlockItem::new, props);

		blockItem(r, FALLEN_KANADE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_FALLEN_KANADE, SpecialFlowerBlockItem::new, props);

		blockItem(r, EXOFLAME, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_EXOFLAME, SpecialFlowerBlockItem::new, props);

		blockItem(r, AGRICARNATION, SpecialFlowerBlockItem::new, props);
		blockItem(r, AGRICARNATION_PETITE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_AGRICARNATION, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_AGRICARNATION_PETITE, SpecialFlowerBlockItem::new, props);

		blockItem(r, HOPPERHOCK, SpecialFlowerBlockItem::new, props);
		blockItem(r, HOPPERHOCK_PETITE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_HOPPERHOCK, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_HOPPERHOCK_PETITE, SpecialFlowerBlockItem::new, props);

		blockItem(r, TANGLEBERRIE, SpecialFlowerBlockItem::new, props);
		blockItem(r, TANGLEBERRIE_PETITE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_TANGLEBERRIE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_TANGLEBERRIE_PETITE, SpecialFlowerBlockItem::new, props);

		blockItem(r, JIYUULIA, SpecialFlowerBlockItem::new, props);
		blockItem(r, JIYUULIA_PETITE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_JIYUULIA, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_JIYUULIA_PETITE, SpecialFlowerBlockItem::new, props);

		blockItem(r, RANNUNCARPUS, SpecialFlowerBlockItem::new, props);
		blockItem(r, RANNUNCARPUS_PETITE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_RANNUNCARPUS, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_RANNUNCARPUS_PETITE, SpecialFlowerBlockItem::new, props);

		blockItem(r, HYACIDUS, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_HYACIDUS, SpecialFlowerBlockItem::new, props);

		blockItem(r, POLLIDISIAC, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_POLLIDISIAC, SpecialFlowerBlockItem::new, props);

		blockItem(r, CLAYCONIA, SpecialFlowerBlockItem::new, props);
		blockItem(r, CLAYCONIA_PETITE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_CLAYCONIA, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_CLAYCONIA_PETITE, SpecialFlowerBlockItem::new, props);

		blockItem(r, LOONIUM, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_LOONIUM, SpecialFlowerBlockItem::new, props);

		blockItem(r, DAFFOMILL, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_DAFFOMILL, SpecialFlowerBlockItem::new, props);

		blockItem(r, VINCULOTUS, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_VINCULOTUS, SpecialFlowerBlockItem::new, props);

		blockItem(r, SPECTRANTHEMUM, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_SPECTRANTHEMUM, SpecialFlowerBlockItem::new, props);

		blockItem(r, MEDUMONE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_MEDUMONE, SpecialFlowerBlockItem::new, props);

		blockItem(r, MARIMORPHOSIS, SpecialFlowerBlockItem::new, props);
		blockItem(r, MARIMORPHOSIS_PETITE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_MARIMORPHOSIS, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_MARIMORPHOSIS_PETITE, SpecialFlowerBlockItem::new, props);

		blockItem(r, BUBBELL, SpecialFlowerBlockItem::new, props);
		blockItem(r, BUBBELL_PETITE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_BUBBELL, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_BUBBELL_PETITE, SpecialFlowerBlockItem::new, props);

		blockItem(r, SOLEGNOLIA, SpecialFlowerBlockItem::new, props);
		blockItem(r, SOLEGNOLIA_PETITE, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_SOLEGNOLIA, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_SOLEGNOLIA_PETITE, SpecialFlowerBlockItem::new, props);

		blockItem(r, ORECHID_IGNEM, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_ORECHID_IGNEM, SpecialFlowerBlockItem::new, props);

		blockItem(r, LABELLIA, SpecialFlowerBlockItem::new, props);
		blockItem(r, FLOATING_LABELLIA, SpecialFlowerBlockItem::new, props);

		blockItem(r, PETAL_APOTHECARY, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_DEEPSLATE, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_LIVINGROCK, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_BLACKSTONE, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_NETHER_BRICKS, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_RED_NETHER_BRICKS, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_MOSSY, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_FUCHSITE, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_TALC, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_GNEISS, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_MYCELITE, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_CATACLASITE, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_SOLITE, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_LUNITE, BlockItem::new, props);
		blockItem(r, PETAL_APOTHECARY_ROSY_TALC, BlockItem::new, props);

		blockItem(r, LIVINGROCK, BlockItem::new, props);
		blockItem(r, LIVINGROCK_STAIRS, BlockItem::new, props);
		blockItem(r, LIVINGROCK_SLAB, BlockItem::new, props);
		blockItem(r, LIVINGROCK_WALL, BlockItem::new, props);
		blockItem(r, LIVINGROCK_BUTTON, BlockItem::new, props);
		blockItem(r, LIVINGROCK_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, POLISHED_LIVINGROCK, BlockItem::new, props);
		blockItem(r, POLISHED_LIVINGROCK_STAIRS, BlockItem::new, props);
		blockItem(r, POLISHED_LIVINGROCK_SLAB, BlockItem::new, props);
		blockItem(r, POLISHED_LIVINGROCK_WALL, BlockItem::new, props);
		blockItem(r, LIVINGROCK_SLATE, BlockItem::new, props);
		blockItem(r, LIVINGROCK_BRICKS, BlockItem::new, props);
		blockItem(r, LIVINGROCK_BRICK_STAIRS, BlockItem::new, props);
		blockItem(r, LIVINGROCK_BRICK_SLAB, BlockItem::new, props);
		blockItem(r, LIVINGROCK_BRICK_WALL, BlockItem::new, props);
		blockItem(r, MOSSY_LIVINGROCK_BRICKS, BlockItem::new, props);
		blockItem(r, MOSSY_LIVINGROCK_BRICK_STAIRS, BlockItem::new, props);
		blockItem(r, MOSSY_LIVINGROCK_BRICK_SLAB, BlockItem::new, props);
		blockItem(r, MOSSY_LIVINGROCK_BRICK_WALL, BlockItem::new, props);
		blockItem(r, CRACKED_LIVINGROCK_BRICKS, BlockItem::new, props);
		blockItem(r, CHISELED_LIVINGROCK_BRICKS, BlockItem::new, props);

		blockItem(r, LIVINGWOOD_LOG, BlockItem::new, props);
		blockItem(r, LIVINGWOOD, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_STAIRS, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_SLAB, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_WALL, BlockItem::new, woodenWallProps);
		blockItem(r, STRIPPED_LIVINGWOOD_LOG, BlockItem::new, props);
		blockItem(r, STRIPPED_LIVINGWOOD, BlockItem::new, props);
		blockItem(r, STRIPPED_LIVINGWOOD_STAIRS, BlockItem::new, props);
		blockItem(r, STRIPPED_LIVINGWOOD_SLAB, BlockItem::new, props);
		blockItem(r, STRIPPED_LIVINGWOOD_WALL, BlockItem::new, woodenWallProps);
		blockItem(r, GLIMMERING_LIVINGWOOD_LOG, BlockItem::new, props);
		blockItem(r, GLIMMERING_LIVINGWOOD, BlockItem::new, props);
		blockItem(r, STRIPPED_GLIMMERING_LIVINGWOOD_LOG, BlockItem::new, props);
		blockItem(r, STRIPPED_GLIMMERING_LIVINGWOOD, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_PLANKS, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_PLANK_STAIRS, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_PLANK_SLAB, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_FENCE, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_FENCE_GATE, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_DOOR, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_TRAPDOOR, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_BUTTON, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_SIGN, (block, properties) -> new StandingAndWallBlockItem(block, LIVINGWOOD_WALL_SIGN, Direction.DOWN, properties), () -> BotaniaItems.defaultBuilder().stacksTo(16).signText());
		blockItem(r, LIVINGWOOD_HANGING_SIGN, (block, properties) -> new HangingSignItem(
				block, LIVINGWOOD_WALL_HANGING_SIGN,
				properties), () -> BotaniaItems.defaultBuilder().stacksTo(16));
		blockItem(r, MOSSY_LIVINGWOOD_PLANKS, BlockItem::new, props);
		blockItem(r, FRAMED_LIVINGWOOD, BlockItem::new, props);
		blockItem(r, PATTERN_FRAMED_LIVINGWOOD, BlockItem::new, props);

		blockItem(r, DREAMWOOD_LOG, BlockItem::new, props);
		blockItem(r, DREAMWOOD, BlockItem::new, props);
		blockItem(r, DREAMWOOD_STAIRS, BlockItem::new, props);
		blockItem(r, DREAMWOOD_SLAB, BlockItem::new, props);
		blockItem(r, DREAMWOOD_WALL, BlockItem::new, woodenWallProps);
		blockItem(r, STRIPPED_DREAMWOOD_LOG, BlockItem::new, props);
		blockItem(r, STRIPPED_DREAMWOOD, BlockItem::new, props);
		blockItem(r, STRIPPED_DREAMWOOD_STAIRS, BlockItem::new, props);
		blockItem(r, STRIPPED_DREAMWOOD_SLAB, BlockItem::new, props);
		blockItem(r, STRIPPED_DREAMWOOD_WALL, BlockItem::new, woodenWallProps);
		blockItem(r, GLIMMERING_DREAMWOOD_LOG, BlockItem::new, props);
		blockItem(r, GLIMMERING_DREAMWOOD, BlockItem::new, props);
		blockItem(r, STRIPPED_GLIMMERING_DREAMWOOD_LOG, BlockItem::new, props);
		blockItem(r, STRIPPED_GLIMMERING_DREAMWOOD, BlockItem::new, props);
		blockItem(r, DREAMWOOD_PLANKS, BlockItem::new, props);
		blockItem(r, DREAMWOOD_PLANK_STAIRS, BlockItem::new, props);
		blockItem(r, DREAMWOOD_PLANK_SLAB, BlockItem::new, props);
		blockItem(r, DREAMWOOD_FENCE, BlockItem::new, props);
		blockItem(r, DREAMWOOD_FENCE_GATE, BlockItem::new, props);
		blockItem(r, DREAMWOOD_DOOR, BlockItem::new, props);
		blockItem(r, DREAMWOOD_TRAPDOOR, BlockItem::new, props);
		blockItem(r, DREAMWOOD_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, DREAMWOOD_BUTTON, BlockItem::new, props);
		blockItem(r, DREAMWOOD_SIGN, (block, properties) -> new StandingAndWallBlockItem(block, DREAMWOOD_WALL_SIGN, Direction.DOWN, properties), () -> BotaniaItems.defaultBuilder().stacksTo(16).signText());
		blockItem(r, DREAMWOOD_HANGING_SIGN, (block, properties) -> new HangingSignItem(
				block, DREAMWOOD_WALL_HANGING_SIGN,
				properties), () -> BotaniaItems.defaultBuilder().stacksTo(16));
		blockItem(r, MOSSY_DREAMWOOD_PLANKS, BlockItem::new, props);
		blockItem(r, FRAMED_DREAMWOOD, BlockItem::new, props);
		blockItem(r, PATTERN_FRAMED_DREAMWOOD, BlockItem::new, props);

		blockItem(r, MANA_SPREADER, BlockItem::new, props);
		blockItem(r, PULSE_MANA_SPREADER, BlockItem::new, props);
		blockItem(r, ELVEN_MANA_SPREADER, BlockItem::new, props);
		blockItem(r, GAIA_MANA_SPREADER, BlockItem::new, rareProps);
		registerBlockItemWithColoredVariants(r, MANA_POOL, props);
		registerBlockItemWithColoredVariants(r, CREATIVE_MANA_POOL, epicProps);
		registerBlockItemWithColoredVariants(r, DILUTED_MANA_POOL, props);
		registerBlockItemWithColoredVariants(r, FABULOUS_MANA_POOL, props);
		blockItem(r, ALCHEMY_CATALYST, BlockItem::new, props);
		blockItem(r, CONJURATION_CATALYST, BlockItem::new, props);
		blockItem(r, MANASTEEL_BLOCK, BlockItem::new, props);
		blockItem(r, TERRASTEEL_BLOCK, BlockItem::new, uncommonProps);
		blockItem(r, ELEMENTIUM_BLOCK, BlockItem::new, props);
		blockItem(r, MANA_DIAMOND_BLOCK, BlockItem::new, props);
		blockItem(r, DRAGONSTONE_BLOCK, BlockItem::new, props);
		blockItem(r, MANAGLASS, BlockItem::new, props);
		blockItem(r, ALFGLASS, BlockItem::new, props);
		blockItem(r, BIFROST, BlockItem::new, props);
		blockItem(r, RUNIC_ALTAR, BlockItem::new, props);
		blockItem(r, MANA_ENCHANTER, BlockItem::new, props);
		blockItem(r, BOTANICAL_BREWERY, BlockItem::new, props);
		blockItem(r, TERRESTRIAL_AGGLOMERATION_PLATE, BlockItem::new, props);
		blockItem(r, ELVEN_GATEWAY_CORE, BlockItem::new, uncommonProps);

		blockItem(r, MANA_PYLON, BlockItem::new, props);
		blockItem(r, NATURA_PYLON, BlockItem::new, uncommonProps);
		blockItem(r, GAIA_PYLON, BlockItem::new, props);
		blockItem(r, MANA_SPLITTER, BlockItem::new, props);
		blockItem(r, MANA_VOID, BlockItem::new, props);
		blockItem(r, MANA_DETECTOR, BlockItem::new, props);
		blockItem(r, FORCE_RELAY, BlockItem::new, props);
		blockItem(r, SPREADER_TURNTABLE, BlockItem::new, props);
		blockItem(r, TINY_PLANET, BlockItem::new, props);
		blockItem(r, DRUM_OF_THE_WILD, BlockItem::new, props);
		blockItem(r, DRUM_OF_THE_GATHERING, BlockItem::new, props);
		blockItem(r, DRUM_OF_THE_CANOPY, BlockItem::new, props);
		blockItem(r, LIFE_IMBUER, BlockItem::new, props);
		blockItem(r, MANA_FLUXFIELD, BlockItem::new, props);
		blockItem(r, MANA_PRISM, BlockItem::new, props);
		blockItem(r, MANA_PUMP, BlockItem::new, props);
		blockItem(r, SPARK_TINKERER, BlockItem::new, props);
		blockItem(r, MANASTORM_CHARGE, BlockItem::new, rareProps);
		blockItem(r, MANATIDE_BELLOWS, BlockItem::new, props);
		blockItem(r, OPEN_CRATE, BlockItem::new, props);
		blockItem(r, CRAFTY_CRATE, BlockItem::new, props);
		blockItem(r, EYE_OF_THE_ANCIENTS, BlockItem::new, props);
		blockItem(r, ABSTRUSE_PLATFORM, TooltipBlockItem::new, props);
		blockItem(r, SPECTRAL_PLATFORM, TooltipBlockItem::new, props);
		blockItem(r, INFRANGIBLE_PLATFORM, TooltipBlockItem::new, epicProps);
		blockItem(r, TINY_POTATO, TinyPotatoBlockItem::new, props);
		blockItem(r, ENDER_OVERSEER, BlockItem::new, props);
		blockItem(r, RED_STRINGED_CONTAINER, BlockItem::new, props);
		blockItem(r, RED_STRINGED_DISPENSER, BlockItem::new, props);
		blockItem(r, RED_STRINGED_NUTRIFIER, BlockItem::new, props);
		blockItem(r, RED_STRINGED_COMPARATOR, BlockItem::new, props);
		blockItem(r, RED_STRINGED_SPOOFER, BlockItem::new, props);
		blockItem(r, RED_STRINGED_INTERCEPTOR, BlockItem::new, props);
		blockItem(r, CORPOREA_FUNNEL, BlockItem::new, props);
		blockItem(r, CORPOREA_INTERCEPTOR, BlockItem::new, props);
		blockItem(r, CORPOREA_INDEX, BlockItem::new, props);
		blockItem(r, CORPOREA_CRYSTAL_CUBE, BlockItem::new, props);
		blockItem(r, CORPOREA_RETAINER, BlockItem::new, props);
		blockItem(r, CORPOREA_BLOCK, BlockItem::new, props);
		blockItem(r, CORPOREA_STAIRS, BlockItem::new, props);
		blockItem(r, CORPOREA_SLAB, BlockItem::new, props);
		blockItem(r, CORPOREA_WALL, BlockItem::new, props);
		blockItem(r, CORPOREA_BUTTON, BlockItem::new, props);
		blockItem(r, CORPOREA_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, CORPOREA_BRICKS, BlockItem::new, props);
		blockItem(r, CORPOREA_BRICK_STAIRS, BlockItem::new, props);
		blockItem(r, CORPOREA_BRICK_SLAB, BlockItem::new, props);
		blockItem(r, CORPOREA_BRICK_WALL, BlockItem::new, props);
		blockItem(r, INCENSE_PLATE, BlockItem::new, props);
		blockItem(r, HOVERING_HOURGLASS, BlockItem::new, props);
		blockItem(r, SPECTRAL_RAIL, BlockItem::new, props);
		blockItem(r, LUMINIZER, BlockItem::new, props);
		blockItem(r, DETECTOR_LUMINIZER, BlockItem::new, props);
		blockItem(r, FORK_LUMINIZER, BlockItem::new, props);
		blockItem(r, TOGGLE_LUMINIZER, BlockItem::new, props);
		blockItem(r, LUMINIZER_LAUNCHER, BlockItem::new, props);
		blockItem(r, CACOPHONIUM_BLOCK, BlockItem::new, props);
		blockItem(r, CELLULAR_BLOCK, BlockItem::new, props);
		blockItem(r, TERU_TERU_BOZU, BlockItem::new, props);
		blockItem(r, LIVINGWOOD_AVATAR, BlockItem::new, props);
		blockItem(r, LIVING_ROOT, BlockItem::new, props);
		blockItem(r, FEL_PUMPKIN, BlockItem::new, props);
		blockItem(r, COCOON_OF_CAPRICE, BlockItem::new, props);
		blockItem(r, ANIMATED_TORCH, BlockItem::new, props);
		blockItem(r, STARFIELD_CREATOR, BlockItem::new, props);
		blockItem(r, AZULEJO_0, BlockItem::new, props);
		blockItem(r, AZULEJO_1, BlockItem::new, props);
		blockItem(r, AZULEJO_2, BlockItem::new, props);
		blockItem(r, AZULEJO_3, BlockItem::new, props);
		blockItem(r, AZULEJO_4, BlockItem::new, props);
		blockItem(r, AZULEJO_5, BlockItem::new, props);
		blockItem(r, AZULEJO_6, BlockItem::new, props);
		blockItem(r, AZULEJO_7, BlockItem::new, props);
		blockItem(r, AZULEJO_8, BlockItem::new, props);
		blockItem(r, AZULEJO_9, BlockItem::new, props);
		blockItem(r, AZULEJO_10, BlockItem::new, props);
		blockItem(r, AZULEJO_11, BlockItem::new, props);
		blockItem(r, AZULEJO_12, BlockItem::new, props);
		blockItem(r, AZULEJO_13, BlockItem::new, props);
		blockItem(r, AZULEJO_14, BlockItem::new, props);
		blockItem(r, AZULEJO_15, BlockItem::new, props);
		blockItem(r, BLAZE_MESH, BlockItem::new, blazeMeshProps);
		blockItem(r, GAIA_HEAD, (block, properties) -> new StandingAndWallBlockItem(block, GAIA_WALL_HEAD_BLOCK, Direction.DOWN, properties), rareProps);
		blockItem(r, SHIMMERROCK, BlockItem::new, props);
		blockItem(r, SHIMMERROCK_STAIRS, BlockItem::new, props);
		blockItem(r, SHIMMERROCK_SLAB, BlockItem::new, props);
		blockItem(r, SHIMMERROCK_WALL, BlockItem::new, props);
		blockItem(r, SHIMMERROCK_BUTTON, BlockItem::new, props);
		blockItem(r, SHIMMERROCK_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, SHIMMERWOOD_PLANKS, BlockItem::new, props);
		blockItem(r, SHIMMERWOOD_PLANK_STAIRS, BlockItem::new, props);
		blockItem(r, SHIMMERWOOD_PLANK_SLAB, BlockItem::new, props);
		blockItem(r, SHIMMERWOOD_FENCE, BlockItem::new, props);
		blockItem(r, SHIMMERWOOD_FENCE_GATE, BlockItem::new, props);
		blockItem(r, SHIMMERWOOD_BUTTON, BlockItem::new, props);
		blockItem(r, SHIMMERWOOD_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, DRY_GRASS_BLOCK, BlockItem::new, props);
		blockItem(r, GOLDEN_GRASS_BLOCK, BlockItem::new, props);
		blockItem(r, VIVID_GRASS_BLOCK, BlockItem::new, props);
		blockItem(r, SCORCHED_GRASS_BLOCK, BlockItem::new, props);
		blockItem(r, INFUSED_GRASS_BLOCK, BlockItem::new, props);
		blockItem(r, MUTATED_GRASS_BLOCK, BlockItem::new, props);
		blockItem(r, DAYBLOOM_MOTIF, TooltipBlockItem::new, props);
		blockItem(r, NIGHTSHADE_MOTIF, TooltipBlockItem::new, props);
		blockItem(r, HYDROANGEAS_MOTIF, BlockItem::new, props);

		blockItem(r, SMOKEY_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, SMOKEY_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, SMOKEY_QUARTZ_SLAB, BlockItem::new, props);
		blockItem(r, CHISELED_SMOKEY_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, SMOKEY_QUARTZ_BRICKS, BlockItem::new, props);
		blockItem(r, SMOKEY_QUARTZ_PILLAR, BlockItem::new, props);
		blockItem(r, SMOOTH_SMOKEY_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, SMOOTH_SMOKEY_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, SMOOTH_SMOKEY_QUARTZ_SLAB, BlockItem::new, props);

		blockItem(r, MANA_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, MANA_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, MANA_QUARTZ_SLAB, BlockItem::new, props);
		blockItem(r, CHISELED_MANA_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, MANA_QUARTZ_BRICKS, BlockItem::new, props);
		blockItem(r, MANA_QUARTZ_PILLAR, BlockItem::new, props);
		blockItem(r, SMOOTH_MANA_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, SMOOTH_MANA_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, SMOOTH_MANA_QUARTZ_SLAB, BlockItem::new, props);

		blockItem(r, BLAZE_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, BLAZE_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, BLAZE_QUARTZ_SLAB, BlockItem::new, props);
		blockItem(r, CHISELED_BLAZE_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, BLAZE_QUARTZ_BRICKS, BlockItem::new, props);
		blockItem(r, BLAZE_QUARTZ_PILLAR, BlockItem::new, props);
		blockItem(r, SMOOTH_BLAZE_QUARTZ_BLOCKS, BlockItem::new, props);
		blockItem(r, SMOOTH_BLAZE_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, SMOOTH_BLAZE_QUARTZ_SLAB, BlockItem::new, props);

		blockItem(r, LAVENDER_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, LAVENDER_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, LAVENDER_QUARTZ_SLAB, BlockItem::new, props);
		blockItem(r, CHISELED_LAVENDER_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, LAVENDER_QUARTZ_BRICKS, BlockItem::new, props);
		blockItem(r, LAVENDER_QUARTZ_PILLAR, BlockItem::new, props);
		blockItem(r, SMOOTH_LAVENDER_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, SMOOTH_LAVENDER_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, SMOOTH_LAVENDER_QUARTZ_SLAB, BlockItem::new, props);

		blockItem(r, RED_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, RED_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, RED_QUARTZ_SLAB, BlockItem::new, props);
		blockItem(r, CHISELED_RED_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, RED_QUARTZ_BRICKS, BlockItem::new, props);
		blockItem(r, RED_QUARTZ_PILLAR, BlockItem::new, props);
		blockItem(r, SMOOTH_RED_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, SMOOTH_RED_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, SMOOTH_RED_QUARTZ_SLAB, BlockItem::new, props);

		blockItem(r, ELVEN_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, ELVEN_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, ELVEN_QUARTZ_SLAB, BlockItem::new, props);
		blockItem(r, CHISELED_ELVEN_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, ELVEN_QUARTZ_BRICKS, BlockItem::new, props);
		blockItem(r, ELVEN_QUARTZ_PILLAR, BlockItem::new, props);
		blockItem(r, SMOOTH_ELVEN_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, SMOOTH_ELVEN_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, SMOOTH_ELVEN_QUARTZ_SLAB, BlockItem::new, props);

		blockItem(r, SUNNY_QUART_BLOCK, BlockItem::new, props);
		blockItem(r, SUNNY_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, SUNNY_QUARTZ_SLAB, BlockItem::new, props);
		blockItem(r, CHISELED_SUNNY_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, SUNNY_QUARTZ_BRICKS, BlockItem::new, props);
		blockItem(r, SUNNY_QUARTZ_PILLAR, BlockItem::new, props);
		blockItem(r, SMOOTH_SUNNY_QUARTZ_BLOCK, BlockItem::new, props);
		blockItem(r, SMOOTH_SUNNY_QUARTZ_STAIRS, BlockItem::new, props);
		blockItem(r, SMOOTH_SUNNY_QUARTZ_SLAB, BlockItem::new, props);

		blockItem(r, FUCHSITE, BlockItem::new, props);
		blockItem(r, FUCHSITE_STAIRS, BlockItem::new, props);
		blockItem(r, FUCHSITE_SLAB, BlockItem::new, props);
		blockItem(r, FUCHSITE_WALL, BlockItem::new, props);
		blockItem(r, FUCHSITE_BUTTON, BlockItem::new, props);
		blockItem(r, FUCHSITE_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, FUCHSITE_BRICKS, BlockItem::new, props);
		blockItem(r, FUCHSITE_BRICK_STAIRS, BlockItem::new, props);
		blockItem(r, FUCHSITE_BRICK_SLAB, BlockItem::new, props);
		blockItem(r, FUCHSITE_BRICK_WALL, BlockItem::new, props);
		blockItem(r, COBBLED_FUCHSITE, BlockItem::new, props);
		blockItem(r, COBBLED_FUCHSITE_STAIRS, BlockItem::new, props);
		blockItem(r, COBBLED_FUCHSITE_SLAB, BlockItem::new, props);
		blockItem(r, COBBLED_FUCHSITE_WALL, BlockItem::new, props);
		blockItem(r, CHISELED_FUCHSITE_BRICKS, BlockItem::new, props);

		blockItem(r, TALC, BlockItem::new, props);
		blockItem(r, TALC_STAIRS, BlockItem::new, props);
		blockItem(r, TALC_SLAB, BlockItem::new, props);
		blockItem(r, TALC_WALL, BlockItem::new, props);
		blockItem(r, TALC_BUTTON, BlockItem::new, props);
		blockItem(r, TALC_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, TALC_BRICKS, BlockItem::new, props);
		blockItem(r, TALC_BRICK_STAIRS, BlockItem::new, props);
		blockItem(r, TALC_BRICK_SLAB, BlockItem::new, props);
		blockItem(r, TALC_BRICK_WALL, BlockItem::new, props);
		blockItem(r, COBBLED_TALC, BlockItem::new, props);
		blockItem(r, COBBLED_TALC_STAIRS, BlockItem::new, props);
		blockItem(r, COBBLED_TALC_SLAB, BlockItem::new, props);
		blockItem(r, COBBLED_TALC_WALL, BlockItem::new, props);
		blockItem(r, CHISELED_TALC_BRICKS, BlockItem::new, props);

		blockItem(r, GNEISS, BlockItem::new, props);
		blockItem(r, GNEISS_STAIRS, BlockItem::new, props);
		blockItem(r, GNEISS_SLAB, BlockItem::new, props);
		blockItem(r, GNEISS_WALL, BlockItem::new, props);
		blockItem(r, GNEISS_BUTTON, BlockItem::new, props);
		blockItem(r, GNEISS_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, GNEISS_BRICKS, BlockItem::new, props);
		blockItem(r, GNEISS_BRICK_STAIRS, BlockItem::new, props);
		blockItem(r, GNEISS_BRICK_SLAB, BlockItem::new, props);
		blockItem(r, GNEISS_BRICK_WALL, BlockItem::new, props);
		blockItem(r, COBBLED_GNEISS, BlockItem::new, props);
		blockItem(r, COBBLED_GNEISS_STAIRS, BlockItem::new, props);
		blockItem(r, COBBLED_GNEISS_SLAB, BlockItem::new, props);
		blockItem(r, COBBLED_GNEISS_WALL, BlockItem::new, props);
		blockItem(r, CHISELED_GNEISS_BRICKS, BlockItem::new, props);

		blockItem(r, MYCELITE, BlockItem::new, props);
		blockItem(r, MYCELITE_STAIRS, BlockItem::new, props);
		blockItem(r, MYCELITE_SLAB, BlockItem::new, props);
		blockItem(r, MYCELITE_WALL, BlockItem::new, props);
		blockItem(r, MYCELITE_BUTTON, BlockItem::new, props);
		blockItem(r, MYCELITE_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, MYCELITE_BRICKS, BlockItem::new, props);
		blockItem(r, MYCELITE_BRICK_STAIRS, BlockItem::new, props);
		blockItem(r, MYCELITE_BRICK_SLAB, BlockItem::new, props);
		blockItem(r, MYCELITE_BRICK_WALL, BlockItem::new, props);
		blockItem(r, COBBLED_MYCELITE, BlockItem::new, props);
		blockItem(r, COBBLED_MYCELITE_STAIRS, BlockItem::new, props);
		blockItem(r, COBBLED_MYCELITE_SLAB, BlockItem::new, props);
		blockItem(r, COBBLED_MYCELITE_WALL, BlockItem::new, props);
		blockItem(r, CHISELED_MYCELITE_BRICKS, BlockItem::new, props);

		blockItem(r, CATACLASITE, BlockItem::new, props);
		blockItem(r, CATACLASITE_STAIRS, BlockItem::new, props);
		blockItem(r, CATACLASITE_SLAB, BlockItem::new, props);
		blockItem(r, CATACLASITE_WALL, BlockItem::new, props);
		blockItem(r, CATACLASITE_BUTTON, BlockItem::new, props);
		blockItem(r, CATACLASITE_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, CATACLASITE_BRICKS, BlockItem::new, props);
		blockItem(r, CATACLASITE_BRICK_STAIRS, BlockItem::new, props);
		blockItem(r, CATACLASITE_BRICK_SLAB, BlockItem::new, props);
		blockItem(r, CATACLASITE_BRICK_WALL, BlockItem::new, props);
		blockItem(r, COBBLED_CATACLASITE, BlockItem::new, props);
		blockItem(r, COBBLED_CATACLASITE_STAIRS, BlockItem::new, props);
		blockItem(r, COBBLED_CATACLASITE_SLAB, BlockItem::new, props);
		blockItem(r, COBBLED_CATACLASITE_WALL, BlockItem::new, props);
		blockItem(r, CHISELED_CATACLASITE_BRICKS, BlockItem::new, props);

		blockItem(r, SOLITE, BlockItem::new, props);
		blockItem(r, SOLITE_STAIRS, BlockItem::new, props);
		blockItem(r, SOLITE_SLAB, BlockItem::new, props);
		blockItem(r, SOLITE_WALL, BlockItem::new, props);
		blockItem(r, SOLITE_BUTTON, BlockItem::new, props);
		blockItem(r, SOLITE_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, SOLITE_BRICKS, BlockItem::new, props);
		blockItem(r, SOLITE_BRICK_STAIRS, BlockItem::new, props);
		blockItem(r, SOLITE_BRICK_SLAB, BlockItem::new, props);
		blockItem(r, SOLITE_BRICK_WALL, BlockItem::new, props);
		blockItem(r, COBBLED_SOLITE, BlockItem::new, props);
		blockItem(r, COBBLED_SOLITE_STAIRS, BlockItem::new, props);
		blockItem(r, COBBLED_SOLITE_SLAB, BlockItem::new, props);
		blockItem(r, COBBLED_SOLITE_WALL, BlockItem::new, props);
		blockItem(r, CHISELED_SOLITE_BRICKS, BlockItem::new, props);

		blockItem(r, LUNITE, BlockItem::new, props);
		blockItem(r, LUNITE_STAIRS, BlockItem::new, props);
		blockItem(r, LUNITE_SLAB, BlockItem::new, props);
		blockItem(r, LUNITE_WALL, BlockItem::new, props);
		blockItem(r, LUNITE_BUTTON, BlockItem::new, props);
		blockItem(r, LUNITE_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, LUNITE_BRICKS, BlockItem::new, props);
		blockItem(r, LUNITE_BRICK_STAIRS, BlockItem::new, props);
		blockItem(r, LUNITE_BRICK_SLAB, BlockItem::new, props);
		blockItem(r, LUNITE_BRICK_WALL, BlockItem::new, props);
		blockItem(r, COBBLED_LUNITE, BlockItem::new, props);
		blockItem(r, COBBLED_LUNITE_STAIRS, BlockItem::new, props);
		blockItem(r, COBBLED_LUNITE_SLAB, BlockItem::new, props);
		blockItem(r, COBBLED_LUNITE_WALL, BlockItem::new, props);
		blockItem(r, CHISELED_LUNITE_BRICKS, BlockItem::new, props);

		blockItem(r, ROSY_TALC, BlockItem::new, props);
		blockItem(r, ROSY_TALC_STAIRS, BlockItem::new, props);
		blockItem(r, ROSY_TALC_SLAB, BlockItem::new, props);
		blockItem(r, ROSY_TALC_WALL, BlockItem::new, props);
		blockItem(r, ROSY_TALC_BUTTON, BlockItem::new, props);
		blockItem(r, ROSY_TALC_PRESSURE_PLATE, BlockItem::new, props);
		blockItem(r, ROSY_TALC_BRICKS, BlockItem::new, props);
		blockItem(r, ROSY_TALC_BRICK_STAIRS, BlockItem::new, props);
		blockItem(r, ROSY_TALC_BRICK_SLAB, BlockItem::new, props);
		blockItem(r, ROSY_TALC_BRICK_WALL, BlockItem::new, props);
		blockItem(r, COBBLED_ROSY_TALC, BlockItem::new, props);
		blockItem(r, COBBLED_ROSY_TALC_STAIRS, BlockItem::new, props);
		blockItem(r, COBBLED_ROSY_TALC_SLAB, BlockItem::new, props);
		blockItem(r, COBBLED_ROSY_TALC_WALL, BlockItem::new, props);
		blockItem(r, CHISELED_ROSY_TALC_BRICKS, BlockItem::new, props);

		blockItem(r, WHITE_PORTUGUESE_PAVEMENT, BlockItem::new, props);
		blockItem(r, WHITE_PORTUGUESE_PAVEMENT_STAIRS, BlockItem::new, props);
		blockItem(r, WHITE_PORTUGUESE_PAVEMENT_SLAB, BlockItem::new, props);

		blockItem(r, BLACK_PORTUGUESE_PAVEMENT, BlockItem::new, props);
		blockItem(r, BLACK_PORTUGUESE_PAVEMENT_SLAB, BlockItem::new, props);
		blockItem(r, BLACK_PORTUGUESE_PAVEMENT_STAIRS, BlockItem::new, props);

		blockItem(r, BLUE_PORTUGUESE_PAVEMENT, BlockItem::new, props);
		blockItem(r, BLUE_PORTUGUESE_PAVEMENT_STAIRS, BlockItem::new, props);
		blockItem(r, BLUE_PORTUGUESE_PAVEMENT_SLAB, BlockItem::new, props);

		blockItem(r, YELLOW_PORTUGUESE_PAVEMENT, BlockItem::new, props);
		blockItem(r, YELLOW_PORTUGUESE_PAVEMENT_STAIRS, BlockItem::new, props);
		blockItem(r, YELLOW_PORTUGUESE_PAVEMENT_SLAB, BlockItem::new, props);

		blockItem(r, RED_PORTUGUESE_PAVEMENT, BlockItem::new, props);
		blockItem(r, RED_PORTUGUESE_PAVEMENT_STAIRS, BlockItem::new, props);
		blockItem(r, RED_PORTUGUESE_PAVEMENT_SLAB, BlockItem::new, props);

		blockItem(r, GREEN_PORTUGUESE_PAVEMENT, BlockItem::new, props);
		blockItem(r, GREEN_PORTUGUESE_PAVEMENT_STAIRS, BlockItem::new, props);
		blockItem(r, GREEN_PORTUGUESE_PAVEMENT_SLAB, BlockItem::new, props);

		blockItem(r, MANAGLASS_PANE, BlockItem::new, props);
		blockItem(r, ALFGLASS_PANE, BlockItem::new, props);
		blockItem(r, BIFROST_PANE, BlockItem::new, props);
	}

	// PORT-26.3: block items need their own properties with the block's id and the block description prefix (vanilla's registerBlock)
	private static <T extends Item> void blockItem(BiConsumer<Item, Identifier> r, Block block, BiFunction<Block, Item.Properties, T> factory,
			Supplier<Item.Properties> properties) {
		Identifier id = BuiltInRegistries.BLOCK.getKey(block);
		r.accept(factory.apply(block, properties.get().useBlockDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, id))), id);
	}

	private static <T extends Block & OptionallyColored> void registerBlockItemWithColoredVariants(
			BiConsumer<Item, Identifier> r, T baseBlock, Supplier<Item.Properties> properties) {
		registerBlockItemWithColoredVariants(r, baseBlock, properties, "_");
	}

	// PORT-26.3: takes a properties supplier, each item gets its own set with its id (see blockItem)
	private static <T extends Block & OptionallyColored> void registerBlockItemWithColoredVariants(
			BiConsumer<Item, Identifier> r, T baseBlock, Supplier<Item.Properties> properties, String coloredBlockInfix) {
		Identifier baseId = BuiltInRegistries.BLOCK.getKey(baseBlock);
		blockItem(r, baseBlock, TooltipBlockItem::new, properties);
		ColorHelper.supportedColors().forEach(color -> {
			Identifier coloredId = baseId.withPrefix(color.getSerializedName() + coloredBlockInfix);
			Block coloredBlock = ALL.get(coloredId.getPath());
			blockItem(r, coloredBlock, TooltipBlockItem::new, properties);
		});
	}

	private static MysticalFlowerBlock makeMysticalFlower(DyeColor color, BlockBehaviour.Properties properties) {
		return make(
				color.getSerializedName() + LibBlockNames.MYSTICAL_FLOWER_SUFFIX,
				props -> new MysticalFlowerBlock(
						color,
						effectForFlower(color),
						MysticalFlowerBlock.STEW_DURATION,
						BotaniaBlocks::getTallMysticalFlower,
						props
				), properties
		);
	}

	private static GlimmeringFlowerBlock makeGlimmeringFlower(DyeColor color, BlockBehaviour.Properties properties) {
		return make(
				color.getSerializedName() + LibBlockNames.GLIMMERING_FLOWER_SUFFIX,
				props -> new GlimmeringFlowerBlock(
						color,
						effectForFlower(color),
						GlimmeringFlowerBlock.STEW_DURATION,
						props
				), properties
		);
	}

	private static FloatingMundaneFlowerBlock makeFloatingMundaneFlower(DyeColor color, BlockBehaviour.Properties properties) {
		return make(
				color.getSerializedName() + LibBlockNames.FLOATING_FLOWER_SUFFIX,
				props -> new FloatingMundaneFlowerBlock(
						color,
						props
				), properties
		);
	}

	private static BuriedPetalBlock makeBuriedPetal(DyeColor color, BlockBehaviour.Properties properties) {
		return make(
				color.getSerializedName() + LibBlockNames.BURIED_PETAL_SUFFIX,
				props -> new BuriedPetalBlock(
						color,
						BotaniaBlocks::getTallMysticalFlower,
						props
				), properties
		);
	}

	private static PetalBlock makePetalBlock(DyeColor color, BlockBehaviour.Properties properties) {
		return make(
				color.getSerializedName() + LibBlockNames.PETAL_BLOCK_SUFFIX,
				props -> new PetalBlock(color, props), properties.mapColor(color)
		);
	}

	private static ShimmeringMushroomBlock makeShimmeringMushroom(DyeColor color, BlockBehaviour.Properties properties) {
		return make(
				color.getSerializedName() + LibBlockNames.SHIMMERING_MUSHROOM_SUFFIX,
				props -> new ShimmeringMushroomBlock(color, props), properties
		);
	}

	private static <T extends Block> T make(String name, T block) {
		var old = ALL.put(name, block);
		if (old != null) {
			throw new IllegalArgumentException("Typo? Duplicate name: " + name);
		}
		return block;
	}

	// PORT-26.3: 26.3 blocks need their registry id in the properties before construction (drops, description id)
	private static <T extends Block> T make(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
		return make(name, factory.apply(properties.setId(ResourceKey.create(Registries.BLOCK, botaniaRL(name)))));
	}

	private static <T extends Block & OptionallyColored> T makeBlockWithColoredVariants(String baseName,
			BiFunction<@Nullable DyeColor, BlockBehaviour.Properties, T> blockFactory, Supplier<BlockBehaviour.Properties> properties) {
		return makeBlockWithColoredVariants(baseName, blockFactory, properties, "_");
	}

	// PORT-26.3: each variant needs its own properties carrying its id, so the factory takes them and the supplier makes a fresh set per block
	private static <T extends Block & OptionallyColored> T makeBlockWithColoredVariants(String baseName,
			BiFunction<@Nullable DyeColor, BlockBehaviour.Properties, T> blockFactory, Supplier<BlockBehaviour.Properties> properties,
			String coloredBlockInfix) {
		T baseBlock = make(baseName, props -> blockFactory.apply(null, props), properties.get());
		ColorHelper.supportedColors().forEach(
				color -> make(color.getSerializedName() + coloredBlockInfix + baseName, props -> blockFactory.apply(color, props), properties.get()));
		return baseBlock;
	}

	private static TallMysticalFlowerBlock makeTallMysticalFlower(DyeColor color, BlockBehaviour.Properties properties) {
		return make(
				color.getSerializedName() + LibBlockNames.TALL_MYSTICAL_FLOWER_SUFFIX,
				props -> new TallMysticalFlowerBlock(color, props), properties
		);
	}

	private static FlowerPotBlock makePottedMundaneFlower(MysticalFlowerBlock flower) {
		return make(
				LibBlockNames.POTTED_PREFIX + flower.color.getSerializedName() + LibBlockNames.MYSTICAL_FLOWER_SUFFIX,
				flowerPot(flower, 0), flowerPotProperties()
		);
	}

	private static FlowerPotBlock makePottedGlimmeringFlower(GlimmeringFlowerBlock flower) {
		return make(
				LibBlockNames.POTTED_PREFIX + flower.color.getSerializedName() + LibBlockNames.GLIMMERING_FLOWER_SUFFIX,
				flowerPot(flower, 15), flowerPotProperties()
		);
	}

	private static FlowerPotBlock makePottedShimmeringMushroom(ShimmeringMushroomBlock mushroom) {
		return make(
				LibBlockNames.POTTED_PREFIX + mushroom.color.getSerializedName() + LibBlockNames.SHIMMERING_MUSHROOM_SUFFIX,
				flowerPot(mushroom, 3), flowerPotProperties()
		);
	}

	private static PetalApothecaryBlock makePetalApothecary(String name, SoundType soundType, MapColor mapColor) {
		return make(name, PetalApothecaryBlock::new, BlockBehaviour.Properties.of()
				.strength(3.5F)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.lightLevel(state -> state.getValue(PetalApothecaryBlock.FLUID) == PetalApothecary.State.LAVA ? 15 : 0)
				.sound(soundType)
				.mapColor(mapColor)
		);
	}

	public static void addDispenserBehaviours() {
		DispenserBlock.registerBehavior(BotaniaItems.WAND_OF_THE_FOREST, new WandBehavior());
		DispenserBlock.registerBehavior(BotaniaItems.PHANTOM_INK, new PhantomInkBehavior());
		DispenserBlock.registerBehavior(BotaniaItems.WAND_OF_THE_ELVEN_FOREST, new WandBehavior());
		DispenserBlock.registerBehavior(BotaniaItems.FLORAL_OBEDIENCE_STICK, new StickBehavior());
		DispenserBlock.registerBehavior(BotaniaItems.MANA_POOL_MINECART, new ManaPoolMinecartBehavior());
		DispenserBlock.registerBehavior(FEL_PUMPKIN, new FelPumpkinBehavior());
		DispenserBlock.registerBehavior(BotaniaItems.MANA_SPARK, new ManaSparkBehavior());
		DispenserBlock.registerBehavior(
				GAIA_HEAD, new OptionalDispenseItemBehavior() {
					@Override
					protected ItemStack execute(BlockSource blockSource, ItemStack item) {
						setSuccess(EquipmentDispenseItemBehavior.dispenseEquipment(blockSource, item));
						return item;
					}
				});

		DispenseItemBehavior behavior = new CorporeaSparkBehavior();
		DispenserBlock.registerBehavior(BotaniaItems.CORPOREA_SPARK, behavior);
		DispenserBlock.registerBehavior(BotaniaItems.MASTER_CORPOREA_SPARK, behavior);
		DispenserBlock.registerBehavior(BotaniaItems.CREATIVE_CORPOREA_SPARK, behavior);
		DispenserBlock.registerBehavior(BotaniaItems.PURE_ENDER_ESSENCE, new ProjectileDispenseBehavior(BotaniaItems.PURE_ENDER_ESSENCE));
		DispenserBlock.registerBehavior(BotaniaItems.ALFGLASS_FLASK, new EnderEssenceBottlingBehavior());

		behavior = new GrassSeedsBehavior();
		Item[] seedItems = {
				BotaniaItems.PASTURE_SEEDS,
				BotaniaItems.BOREAL_SEEDS,
				BotaniaItems.INFESTATION_SPORES,
				BotaniaItems.DRY_SEEDS,
				BotaniaItems.GOLDEN_SEEDS,
				BotaniaItems.VIVID_SEEDS,
				BotaniaItems.SCORCHED_SEEDS,
				BotaniaItems.INFUSED_SEEDS,
				BotaniaItems.MUTATED_SEEDS,
		};
		for (Item seed : seedItems) {
			DispenserBlock.registerBehavior(seed, behavior);
		}

		DispenserBlock.registerBehavior(BotaniaItems.MANASTEEL_SHEARS, new ShearsDispenseItemBehavior());
		DispenserBlock.registerBehavior(BotaniaItems.ELEMENTIUM_SHEARS, new ShearsDispenseItemBehavior());
		DispenserBlock.registerBehavior(BotaniaItems.VINE_BALL, new ProjectileDispenseBehavior(BotaniaItems.VINE_BALL));

		SeedBehaviors.init();
	}

	public static void addAxeStripping(BiConsumer<Block, Block> registrationMethod) {
		registrationMethod.accept(LIVINGWOOD_LOG, STRIPPED_LIVINGWOOD_LOG);
		registrationMethod.accept(GLIMMERING_LIVINGWOOD_LOG, STRIPPED_GLIMMERING_LIVINGWOOD_LOG);
		registrationMethod.accept(LIVINGWOOD, STRIPPED_LIVINGWOOD);
		registrationMethod.accept(GLIMMERING_LIVINGWOOD, STRIPPED_GLIMMERING_LIVINGWOOD);
		registrationMethod.accept(DREAMWOOD_LOG, STRIPPED_DREAMWOOD_LOG);
		registrationMethod.accept(GLIMMERING_DREAMWOOD_LOG, STRIPPED_GLIMMERING_DREAMWOOD_LOG);
		registrationMethod.accept(DREAMWOOD, STRIPPED_DREAMWOOD);
		registrationMethod.accept(GLIMMERING_DREAMWOOD, STRIPPED_GLIMMERING_DREAMWOOD);

		registrationMethod.accept(LIVINGWOOD_STAIRS, STRIPPED_LIVINGWOOD_STAIRS);
		registrationMethod.accept(LIVINGWOOD_SLAB, STRIPPED_LIVINGWOOD_SLAB);
		registrationMethod.accept(LIVINGWOOD_WALL, STRIPPED_LIVINGWOOD_WALL);
		registrationMethod.accept(DREAMWOOD_STAIRS, STRIPPED_DREAMWOOD_STAIRS);
		registrationMethod.accept(DREAMWOOD_SLAB, STRIPPED_DREAMWOOD_SLAB);
		registrationMethod.accept(DREAMWOOD_WALL, STRIPPED_DREAMWOOD_WALL);
	}

	public static Block getMysticalFlower(DyeColor color) {
		return switch (color) {
			case WHITE -> WHITE_MYSTICAL_FLOWER;
			case ORANGE -> ORANGE_MYSTICAL_FLOWER;
			case MAGENTA -> MAGENTA_MYSTICAL_FLOWER;
			case LIGHT_BLUE -> LIGHT_BLUE_MYSTICAL_FLOWER;
			case YELLOW -> YELLOW_MYSTICAL_FLOWER;
			case LIME -> LIME_MYSTICAL_FLOWER;
			case PINK -> PINK_MYSTICAL_FLOWER;
			case GRAY -> GRAY_MYSTICAL_FLOWER;
			case LIGHT_GRAY -> LIGHT_GRAY_MYSTICAL_FLOWER;
			case CYAN -> CYAN_MYSTICAL_FLOWER;
			case PURPLE -> PURPLE_MYSTICAL_FLOWER;
			case BLUE -> BLUE_MYSTICAL_FLOWER;
			case BROWN -> BROWN_MYSTICAL_FLOWER;
			case GREEN -> GREEN_MYSTICAL_FLOWER;
			case RED -> RED_MYSTICAL_FLOWER;
			case BLACK -> BLACK_MYSTICAL_FLOWER;
		};
	}

	public static Block getShimmeringMushroom(DyeColor color) {
		return switch (color) {
			case WHITE -> WHITE_SHIMMERING_MUSHROOM;
			case ORANGE -> ORANGE_SHIMMERING_MUSHROOM;
			case MAGENTA -> MAGENTA_SHIMMERING_MUSHROOM;
			case LIGHT_BLUE -> LIGHT_BLUE_SHIMMERING_MUSHROOM;
			case YELLOW -> YELLOW_SHIMMERING_MUSHROOM;
			case LIME -> LIME_SHIMMERING_MUSHROOM;
			case PINK -> PINK_SHIMMERING_MUSHROOM;
			case GRAY -> GRAY_SHIMMERING_MUSHROOM;
			case LIGHT_GRAY -> LIGHT_GRAY_SHIMMERING_MUSHROOM;
			case CYAN -> CYAN_SHIMMERING_MUSHROOM;
			case PURPLE -> PURPLE_SHIMMERING_MUSHROOM;
			case BLUE -> BLUE_SHIMMERING_MUSHROOM;
			case BROWN -> BROWN_SHIMMERING_MUSHROOM;
			case GREEN -> GREEN_SHIMMERING_MUSHROOM;
			case RED -> RED_SHIMMERING_MUSHROOM;
			case BLACK -> BLACK_SHIMMERING_MUSHROOM;
		};
	}

	public static Block getBuriedPetal(DyeColor color) {
		return switch (color) {
			case WHITE -> WHITE_BURIED_PETAL;
			case ORANGE -> ORANGE_BURIED_PETAL;
			case MAGENTA -> MAGENTA_BURIED_PETAL;
			case LIGHT_BLUE -> LIGHT_BLUE_BURIED_PETAL;
			case YELLOW -> YELLOW_BURIED_PETAL;
			case LIME -> LIME_BURIED_PETAL;
			case PINK -> PINK_BURIED_PETAL;
			case GRAY -> GRAY_BURIED_PETAL;
			case LIGHT_GRAY -> LIGHT_GRAY_BURIED_PETAL;
			case CYAN -> CYAN_BURIED_PETAL;
			case PURPLE -> PURPLE_BURIED_PETAL;
			case BLUE -> BLUE_BURIED_PETAL;
			case BROWN -> BROWN_BURIED_PETAL;
			case GREEN -> GREEN_BURIED_PETAL;
			case RED -> RED_BURIED_PETAL;
			case BLACK -> BLACK_BURIED_PETAL;
		};
	}

	public static Block getGlimmeringFlower(DyeColor color) {
		return switch (color) {
			case WHITE -> WHITE_GLIMMERING_FLOWER;
			case ORANGE -> ORANGE_GLIMMERING_FLOWER;
			case MAGENTA -> MAGENTA_GLIMMERING_FLOWER;
			case LIGHT_BLUE -> LIGHT_BLUE_GLIMMERING_FLOWER;
			case YELLOW -> YELLOW_GLIMMERING_FLOWER;
			case LIME -> LIME_GLIMMERING_FLOWER;
			case PINK -> PINK_GLIMMERING_FLOWER;
			case GRAY -> GRAY_GLIMMERING_FLOWER;
			case LIGHT_GRAY -> LIGHT_GRAY_GLIMMERING_FLOWER;
			case CYAN -> CYAN_GLIMMERING_FLOWER;
			case PURPLE -> PURPLE_GLIMMERING_FLOWER;
			case BLUE -> BLUE_GLIMMERING_FLOWER;
			case BROWN -> BROWN_GLIMMERING_FLOWER;
			case GREEN -> GREEN_GLIMMERING_FLOWER;
			case RED -> RED_GLIMMERING_FLOWER;
			case BLACK -> BLACK_GLIMMERING_FLOWER;
		};
	}

	public static Block getFloatingFlower(DyeColor color) {
		return switch (color) {
			case WHITE -> WHITE_FLOATING_FLOWER;
			case ORANGE -> ORANGE_FLOATING_FLOWER;
			case MAGENTA -> MAGENTA_FLOATING_FLOWER;
			case LIGHT_BLUE -> LIGHT_BLUE_FLOATING_FLOWER;
			case YELLOW -> YELLOW_FLOATING_FLOWER;
			case LIME -> LIME_FLOATING_FLOWER;
			case PINK -> PINK_FLOATING_FLOWER;
			case GRAY -> GRAY_FLOATING_FLOWER;
			case LIGHT_GRAY -> LIGHT_GRAY_FLOATING_FLOWER;
			case CYAN -> CYAN_FLOATING_FLOWER;
			case PURPLE -> PURPLE_FLOATING_FLOWER;
			case BLUE -> BLUE_FLOATING_FLOWER;
			case BROWN -> BROWN_FLOATING_FLOWER;
			case GREEN -> GREEN_FLOATING_FLOWER;
			case RED -> RED_FLOATING_FLOWER;
			case BLACK -> BLACK_FLOATING_FLOWER;
		};
	}

	@Nullable
	public static Block getTallMysticalFlower(TallFlowerGrower grower) {
		return grower instanceof Colored colored ? getTallMysticalFlower(colored.getColor()) : null;
	}

	public static Block getTallMysticalFlower(DyeColor color) {
		return switch (color) {
			case WHITE -> WHITE_TALL_MYSTICAL_FLOWER;
			case ORANGE -> ORANGE_TALL_MYSTICAL_FLOWER;
			case MAGENTA -> MAGENTA_TALL_MYSTICAL_FLOWER;
			case LIGHT_BLUE -> LIGHT_BLUE_TALL_MYSTICAL_FLOWER;
			case YELLOW -> YELLOW_TALL_MYSTICAL_FLOWER;
			case LIME -> LIME_TALL_MYSTICAL_FLOWER;
			case PINK -> PINK_TALL_MYSTICAL_FLOWER;
			case GRAY -> GRAY_TALL_MYSTICAL_FLOWER;
			case LIGHT_GRAY -> LIGHT_GRAY_TALL_MYSTICAL_FLOWER;
			case CYAN -> CYAN_TALL_MYSTICAL_FLOWER;
			case PURPLE -> PURPLE_TALL_MYSTICAL_FLOWER;
			case BLUE -> BLUE_TALL_MYSTICAL_FLOWER;
			case BROWN -> BROWN_TALL_MYSTICAL_FLOWER;
			case GREEN -> GREEN_TALL_MYSTICAL_FLOWER;
			case RED -> RED_TALL_MYSTICAL_FLOWER;
			case BLACK -> BLACK_TALL_MYSTICAL_FLOWER;
		};
	}

	public static Block getPetalBlock(DyeColor color) {
		return switch (color) {
			case WHITE -> WHITE_PETAL_BLOCK;
			case ORANGE -> ORANGE_PETAL_BLOCK;
			case MAGENTA -> MAGENTA_PETAL_BLOCK;
			case LIGHT_BLUE -> LIGHT_BLUE_PETAL_BLOCK;
			case YELLOW -> YELLOW_PETAL_BLOCK;
			case LIME -> LIME_PETAL_BLOCK;
			case PINK -> PINK_PETAL_BLOCK;
			case GRAY -> GRAY_PETAL_BLOCK;
			case LIGHT_GRAY -> LIGHT_GRAY_PETAL_BLOCK;
			case CYAN -> CYAN_PETAL_BLOCK;
			case PURPLE -> PURPLE_PETAL_BLOCK;
			case BLUE -> BLUE_PETAL_BLOCK;
			case BROWN -> BROWN_PETAL_BLOCK;
			case GREEN -> GREEN_PETAL_BLOCK;
			case RED -> RED_PETAL_BLOCK;
			case BLACK -> BLACK_PETAL_BLOCK;
		};
	}

	public static Block getPottedFlower(DyeColor color) {
		return switch (color) {
			case WHITE -> POTTED_WHITE_MYSTICAL_FLOWER;
			case ORANGE -> POTTED_ORANGE_MYSTICAL_FLOWER;
			case MAGENTA -> POTTED_MAGENTA_MYSTICAL_FLOWER;
			case LIGHT_BLUE -> POTTED_LIGHT_BLUE_MYSTICAL_FLOWER;
			case YELLOW -> POTTED_YELLOW_MYSTICAL_FLOWER;
			case LIME -> POTTED_LIME_MYSTICAL_FLOWER;
			case PINK -> POTTED_PINK_MYSTICAL_FLOWER;
			case GRAY -> POTTED_GRAY_MYSTICAL_FLOWER;
			case LIGHT_GRAY -> POTTED_LIGHT_GRAY_MYSTICAL_FLOWER;
			case CYAN -> POTTED_CYAN_MYSTICAL_FLOWER;
			case PURPLE -> POTTED_PURPLE_MYSTICAL_FLOWER;
			case BLUE -> POTTED_BLUE_MYSTICAL_FLOWER;
			case BROWN -> POTTED_BROWN_MYSTICAL_FLOWER;
			case GREEN -> POTTED_GREEN_MYSTICAL_FLOWER;
			case RED -> POTTED_RED_MYSTICAL_FLOWER;
			case BLACK -> POTTED_BLACK_MYSTICAL_FLOWER;
		};
	}

	public static Block getPottedShinyFlower(DyeColor color) {
		return switch (color) {
			case WHITE -> POTTED_WHITE_GLIMMERING_FLOWER;
			case ORANGE -> POTTED_ORANGE_GLIMMERING_FLOWER;
			case MAGENTA -> POTTED_MAGENTA_GLIMMERING_FLOWER;
			case LIGHT_BLUE -> POTTED_LIGHT_BLUE_GLIMMERING_FLOWER;
			case YELLOW -> POTTED_YELLOW_GLIMMERING_FLOWER;
			case LIME -> POTTED_LIME_GLIMMERING_FLOWER;
			case PINK -> POTTED_PINK_GLIMMERING_FLOWER;
			case GRAY -> POTTED_GRAY_GLIMMERING_FLOWER;
			case LIGHT_GRAY -> POTTED_LIGHT_GRAY_GLIMMERING_FLOWER;
			case CYAN -> POTTED_CYAN_GLIMMERING_FLOWER;
			case PURPLE -> POTTED_PURPLE_GLIMMERING_FLOWER;
			case BLUE -> POTTED_BLUE_GLIMMERING_FLOWER;
			case BROWN -> POTTED_BROWN_GLIMMERING_FLOWER;
			case GREEN -> POTTED_GREEN_GLIMMERING_FLOWER;
			case RED -> POTTED_RED_GLIMMERING_FLOWER;
			case BLACK -> POTTED_BLACK_GLIMMERING_FLOWER;
		};
	}

	public static Block getPottedMushroom(DyeColor color) {
		return switch (color) {
			case WHITE -> POTTED_WHITE_SHIMMERING_MUSHROOM;
			case ORANGE -> POTTED_ORANGE_SHIMMERING_MUSHROOM;
			case MAGENTA -> POTTED_MAGENTA_SHIMMERING_MUSHROOM;
			case LIGHT_BLUE -> POTTED_LIGHT_BLUE_SHIMMERING_MUSHROOM;
			case YELLOW -> POTTED_YELLOW_SHIMMERING_MUSHROOM;
			case LIME -> POTTED_LIME_SHIMMERING_MUSHROOM;
			case PINK -> POTTED_PINK_SHIMMERING_MUSHROOM;
			case GRAY -> POTTED_GRAY_SHIMMERING_MUSHROOM;
			case LIGHT_GRAY -> POTTED_LIGHT_GRAY_SHIMMERING_MUSHROOM;
			case CYAN -> POTTED_CYAN_SHIMMERING_MUSHROOM;
			case PURPLE -> POTTED_PURPLE_SHIMMERING_MUSHROOM;
			case BLUE -> POTTED_BLUE_SHIMMERING_MUSHROOM;
			case BROWN -> POTTED_BROWN_SHIMMERING_MUSHROOM;
			case GREEN -> POTTED_GREEN_SHIMMERING_MUSHROOM;
			case RED -> POTTED_RED_SHIMMERING_MUSHROOM;
			case BLACK -> POTTED_BLACK_SHIMMERING_MUSHROOM;
		};
	}

	private static Holder<MobEffect> effectForFlower(DyeColor color) {
		return switch (color) {
			case WHITE -> MobEffects.SPEED;
			case ORANGE -> MobEffects.FIRE_RESISTANCE;
			case MAGENTA -> MobEffects.MINING_FATIGUE;
			case LIGHT_BLUE -> MobEffects.JUMP_BOOST;
			case YELLOW -> MobEffects.ABSORPTION;
			case LIME -> MobEffects.POISON;
			case PINK -> MobEffects.REGENERATION;
			case GRAY -> MobEffects.RESISTANCE;
			case LIGHT_GRAY -> MobEffects.WEAKNESS;
			case CYAN -> MobEffects.WATER_BREATHING;
			case PURPLE -> MobEffects.NAUSEA;
			case BLUE -> MobEffects.NIGHT_VISION;
			case BROWN -> MobEffects.WITHER;
			case GREEN -> MobEffects.HUNGER;
			case RED -> MobEffects.STRENGTH;
			case BLACK -> MobEffects.BLINDNESS;
		};
	}

	public static void registerFlowerPotPlants(BiConsumer<Identifier, Supplier<? extends Block>> consumer) {
		registerBlocks((block, resourceLocation) -> {
			if (block instanceof FlowerPotBlock) {
				var id = BuiltInRegistries.BLOCK.getKey(block);
				consumer.accept(Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath().substring(LibBlockNames.POTTED_PREFIX.length())), () -> block);
			}
		});
	}

	/**
	 * Applies naming conventions to find a block of the same type in the specified color variant.
	 * The ID of dyed variants is expected to use the dye name as prefix of the undyed block's ID.
	 */
	public static <T extends Block & OptionallyColored> T findOptionallyDyedBlock(T referenceBlock, @Nullable DyeColor targetColor) {
		return findOptionallyDyedBlock(referenceBlock, targetColor, "_");
	}

	/**
	 * Applies naming conventions to find a block of the same type in the specified color variant.
	 * The ID of dyed variants is expected to use the dye name as prefix of the undyed block's ID.
	 * The ID part between the dye name and the base block ID (the color block infix) is often "_", but can be longer,
	 * e.g. it would be "_stained_" for colored glass blocks, if those supported this operation.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Block & OptionallyColored> T findOptionallyDyedBlock(T referenceBlock,
			@Nullable DyeColor targetColor, String coloredBlockInfix) {
		@Nullable
		DyeColor referenceColor = referenceBlock.getOptionalColor().orElse(null);
		if (targetColor == referenceColor) {
			// already is the expected color
			return referenceBlock;
		}
		// at most one of reference color and target color is empty

		Identifier referenceId = BuiltInRegistries.BLOCK.getKey(referenceBlock);
		Identifier targetId = getTargetId(referenceId, referenceColor, targetColor, coloredBlockInfix);

		Block targetBlock = BuiltInRegistries.BLOCK.getValue(targetId);
		if (!referenceBlock.getClass().isInstance(targetBlock)) {
			throw new IllegalArgumentException(
					"No target block for %s in %s (looked for %s, found %s)".formatted(referenceBlock, targetColor, targetId, targetBlock));
		}

		return (T) targetBlock;
	}

	private static String getUndyedReferencePath(Identifier referenceId, @Nullable DyeColor referenceColor, String coloredBlockInfix) {
		if (referenceColor == null) {
			return referenceId.getPath();
		}
		String referenceColorPrefix = referenceColor.getSerializedName() + coloredBlockInfix;
		if (!referenceId.getPath().startsWith(referenceColorPrefix)) {
			throw new IllegalArgumentException(
					"Block ID %s should start with color prefix %s".formatted(referenceId, referenceColorPrefix));
		}
		return referenceId.getPath().substring(referenceColorPrefix.length());
	}

	private static Identifier getTargetId(Identifier referenceId,
			@Nullable DyeColor referenceColor, @Nullable DyeColor targetColor, String coloredBlockInfix) {
		if (targetColor == null) {
			return referenceId.withPath(getUndyedReferencePath(referenceId, referenceColor, coloredBlockInfix));
		}
		if (referenceColor == null) {
			return referenceId.withPrefix(targetColor.getSerializedName() + coloredBlockInfix);
		}
		return referenceId.withPath(targetColor.getSerializedName() + coloredBlockInfix +
				getUndyedReferencePath(referenceId, referenceColor, coloredBlockInfix));
	}

	/**
	 * Applies naming conventions to find a block of the same type in the specified color variant. The block ID is
	 * expected to have the same format across all dye variants, with the dye name appearing once the ID.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Block & Colored> T findDyedBlock(T referenceBlock, DyeColor targetColor) {
		DyeColor referenceColor = referenceBlock.getColor();
		if (referenceColor == targetColor) {
			return referenceBlock;
		}

		Identifier referenceId = BuiltInRegistries.BLOCK.getKey(referenceBlock);
		Identifier targetId = referenceId.withPath(path -> path.replaceFirst(
				"(?<=_|\\b)" + referenceColor.getSerializedName() + "(?=_|\\b)", targetColor.getSerializedName()));

		Block targetBlock = BuiltInRegistries.BLOCK.getValue(targetId);
		if (!referenceBlock.getClass().isInstance(targetBlock)) {
			throw new IllegalArgumentException(
					"No target block for %s in %s (found %s)".formatted(referenceBlock, targetColor, targetBlock));
		}

		return (T) targetBlock;
	}

	@FunctionalInterface
	public interface BCapConsumer<T> {
		void accept(Function<BlockState, T> factory, Block... blocks);
	}

	@FunctionalInterface
	public interface BCapFallbackConsumer<T> {
		void accept(Function<BlockState, @Nullable T> factory);
	}
}
