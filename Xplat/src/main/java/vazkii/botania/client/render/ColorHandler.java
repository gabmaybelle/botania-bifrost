/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.brew.BrewItem;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.PlatformBlock;
import vazkii.botania.common.block.block_entity.PlatformBlockEntity;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.ManaBlasterItem;
import vazkii.botania.common.item.WandOfTheForestItem;
import vazkii.botania.common.item.equipment.bauble.TaintedBloodPendantItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;
import vazkii.botania.common.item.lens.LensItem;
import vazkii.botania.common.item.material.MysticalPetalItem;

import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

/**
 * PORT-26.3: block colours are {@code BlockTintSource}s registered per block, item colours are
 * {@code ItemTintSource}s named by the item definition JSON ({@code "tints": [{"type": "botania:<name>"}]}). The
 * loaders register both.
 */
public final class ColorHandler {
	public static final float MANA_HUE = 0.528F;

	public interface BlockHandlerConsumer {
		void register(BlockTintSource handler, Block... blocks);
	}

	private static Block[] getModBlocks(Predicate<Block> blockPredicate) {
		return BuiltInRegistries.BLOCK.stream()
				.filter(blockPredicate)
				.filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(BotaniaAPI.MODID))
				.toArray(Block[]::new);
	}

	private static final BlockTintSource POOL_TINT = new BlockTintSource() {
		@Override
		public int color(BlockState state) {
			return colorInWorld(state, null, null);
		}

		@Override
		public int colorInWorld(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
			if (!(state.getBlock() instanceof ManaPoolBlock poolBlock)) {
				return -1;
			}
			// PORT-26.3: tints are ARGB and item tints keep the alpha, so the RGB mix needs an opaque alpha or the dyed
			// pools' item icons (and the dyed creative pools' bodies) draw fully transparent
			Optional<Integer> optionalColor = poolBlock.getOptionalColor().map(color -> ARGB.opaque(lerpColor(0.8f, 0xFFFFFF, MysticalPetalItem.getPetalLikeColor(color))));
			if (poolBlock.isFabulous()) {
				float time = (ClientTickHandler.getEntityTicksInGame() + ClientTickHandler.getEntityPartialTick()) * 0.005F;
				float posOffset = pos != null ? new Random(state.getSeed(pos)).nextFloat() : 0;
				int fabulousColor = ARGB.opaque(Mth.hsvToRgb((time + posOffset) % 1f, 0.4F, 1F));
				return optionalColor.map(color -> ARGB.multiply(fabulousColor, color))
						.orElse(fabulousColor);
			}
			return optionalColor.orElse(-1);
		}
	};

	public static void submitBlocks(BlockHandlerConsumer blocks) {
		// [VanillaCopy] BlockColors for vine
		blocks.register(new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return FoliageColor.FOLIAGE_DEFAULT;
			}

			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				return BiomeColors.getAverageFoliageColor(level, pos);
			}
		}, BotaniaBlocks.SOLID_VINE);

		// Pool
		blocks.register(POOL_TINT,
				getModBlocks(block -> block instanceof ManaPoolBlock pool && (pool.color != null || pool.isFabulous())));

		// Platforms
		blocks.register(new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return 0xFFFFFF;
			}

			@Override
			public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
				BlockEntity tile = level.getBlockEntity(pos);
				if (tile instanceof PlatformBlockEntity camo) {
					BlockState camoState = camo.getCamoState();
					if (camoState != null) {
						if (camoState.getBlock() instanceof PlatformBlock) {
							return 0xFFFFFF;
						}
						// PORT-26.3: the camouflaged block's own first tint layer
						BlockTintSource camoTint = Minecraft.getInstance().getBlockColors().getTintSource(camoState, 0);
						return camoTint != null ? camoTint.colorInWorld(camoState, level, pos) : 0xFFFFFF;
					}
				}
				return 0xFFFFFF;
			}
		}, BotaniaBlocks.ABSTRUSE_PLATFORM, BotaniaBlocks.SPECTRAL_PLATFORM, BotaniaBlocks.INFRANGIBLE_PLATFORM);
	}

	// === Item tints ===

	private static <T extends ItemTintSource> MapCodec<T> unit(T instance) {
		return MapCodec.unit(instance);
	}

	/** The gaia spirit and ingot: a slowly cycling pale rainbow. */
	public record GaiaSpiritTint() implements ItemTintSource {
		public static final MapCodec<GaiaSpiritTint> MAP_CODEC = unit(new GaiaSpiritTint());

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return ARGB.opaque(Mth.hsvToRgb(ClientTickHandler.getUiAnimationTicks() % 180 / 180, 0.25f, 1.0f));
		}

		@Override
		public MapCodec<GaiaSpiritTint> type() {
			return MAP_CODEC;
		}
	}

	/** The gaia spreader item. */
	public record GaiaSpreaderTint() implements ItemTintSource {
		public static final MapCodec<GaiaSpreaderTint> MAP_CODEC = unit(new GaiaSpreaderTint());

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return ARGB.opaque(Mth.hsvToRgb(ClientTickHandler.getUiAnimationTicks() % 180 / 180, 0.4f, 0.9f));
		}

		@Override
		public MapCodec<GaiaSpreaderTint> type() {
			return MAP_CODEC;
		}
	}

	/** The wand of the forest's two petal colours; {@code "layer": 1} or {@code 2}. */
	public record WandTint(int layer) implements ItemTintSource {
		public static final MapCodec<WandTint> MAP_CODEC = com.mojang.serialization.Codec.INT.fieldOf("layer").xmap(WandTint::new, WandTint::layer);

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return switch (layer) {
				case 1 -> ARGB.opaque(MysticalPetalItem.getPetalLikeColor(WandOfTheForestItem.getColor1(stack)));
				case 2 -> ARGB.opaque(MysticalPetalItem.getPetalLikeColor(WandOfTheForestItem.getColor2(stack)));
				default -> -1;
			};
		}

		@Override
		public MapCodec<WandTint> type() {
			return MAP_CODEC;
		}
	}

	/** A mana pool item takes its block's tint. */
	public record ManaPoolTint() implements ItemTintSource {
		public static final MapCodec<ManaPoolTint> MAP_CODEC = unit(new ManaPoolTint());

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return stack.getItem() instanceof BlockItem blockItem ? POOL_TINT.color(blockItem.getBlock().defaultBlockState()) : -1;
		}

		@Override
		public MapCodec<ManaPoolTint> type() {
			return MAP_CODEC;
		}
	}

	/** Mana mirror and tablet: the fill fraction as saturation. */
	public record ManaItemTint() implements ItemTintSource {
		public static final MapCodec<ManaItemTint> MAP_CODEC = unit(new ManaItemTint());

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			var manaItem = ManaItem.LOOKUP.find(stack);
			return ARGB.opaque(Mth.hsvToRgb(
					MANA_HUE,
					manaItem != null ? (float) manaItem.getMana() / (float) Math.max(1, manaItem.getMaxMana()) : 0,
					1));
		}

		@Override
		public MapCodec<ManaItemTint> type() {
			return MAP_CODEC;
		}
	}

	public record SpellbindingClothTint() implements ItemTintSource {
		public static final MapCodec<SpellbindingClothTint> MAP_CODEC = unit(new SpellbindingClothTint());

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return ARGB.opaque(Mth.hsvToRgb(
					0.55F,
					((float) stack.getMaxDamage() - (float) stack.getDamageValue()) / (float) stack.getMaxDamage() * 0.5F,
					1F));
		}

		@Override
		public MapCodec<SpellbindingClothTint> type() {
			return MAP_CODEC;
		}
	}

	/** Brew vials, flasks, incense sticks and the tainted blood pendant: the brew's colour, pulsing. */
	public record BrewTint() implements ItemTintSource {
		public static final MapCodec<BrewTint> MAP_CODEC = unit(new BrewTint());

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			Brew brew = ((BrewItem) stack.getItem()).getBrew(stack);
			if (brew == BotaniaBrews.FALLBACK) {
				return stack.getItem() instanceof TaintedBloodPendantItem ? 0xFFC6000E : 0xFF989898;
			}
			float speed = stack.is(BotaniaItems.BREW_FLASK) || stack.is(BotaniaItems.BREW_VIAL) ? 0.1f : 0.2f;
			int add = (int) (Mth.sin(ClientTickHandler.getUiAnimationTicks() * speed) * 24);
			return addToColor(brew.getColor(stack), add);
		}

		@Override
		public MapCodec<BrewTint> type() {
			return MAP_CODEC;
		}
	}

	/** The mana blaster's lens slot: the inserted lens's colour. */
	public record ManaBlasterLensTint() implements ItemTintSource {
		public static final MapCodec<ManaBlasterLensTint> MAP_CODEC = unit(new ManaBlasterLensTint());

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			ItemStack lens = ManaBlasterItem.getLens(stack);
			if (!lens.isEmpty() && lens.getItem() instanceof LensItem) {
				return LensTint.INSTANCE.calculate(lens, level, owner);
			}
			return -1;
		}

		@Override
		public MapCodec<ManaBlasterLensTint> type() {
			return MAP_CODEC;
		}
	}

	/** The mana blaster's burst colour. */
	public record ManaBlasterBurstTint() implements ItemTintSource {
		public static final MapCodec<ManaBlasterBurstTint> MAP_CODEC = unit(new ManaBlasterBurstTint());

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			BurstProperties props = ((ManaBlasterItem) stack.getItem())
					.getBurstProps(Minecraft.getInstance().player, stack, false, InteractionHand.MAIN_HAND);
			int add = ARGB.as8BitChannel(Mth.sin(ClientTickHandler.getUiAnimationTicks() / 5) * 0.15f);
			return addToColor(props.color, add);
		}

		@Override
		public MapCodec<ManaBlasterBurstTint> type() {
			return MAP_CODEC;
		}
	}

	public record SoulscribeTint() implements ItemTintSource {
		public static final MapCodec<SoulscribeTint> MAP_CODEC = unit(new SoulscribeTint());

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return ARGB.opaque(Mth.hsvToRgb(0.75f, 1,
					1.5f - Math.min(1, Mth.sin(ClientTickHandler.getUiAnimationTicks() / 2) * 0.5f + 1.2f)));
		}

		@Override
		public MapCodec<SoulscribeTint> type() {
			return MAP_CODEC;
		}
	}

	public record TerraShattererTint() implements ItemTintSource {
		public static final MapCodec<TerraShattererTint> MAP_CODEC = unit(new TerraShattererTint());

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return TerraShattererItem.isEnabled(stack)
					? ARGB.opaque(Mth.hsvToRgb(0.375f,
							Math.min(1, Mth.sin(ClientTickHandler.getUiAnimationTicks() / 4) * 0.5f + 1f),
							1))
					: -1;
		}

		@Override
		public MapCodec<TerraShattererTint> type() {
			return MAP_CODEC;
		}
	}

	/** Every lens: its own colour. */
	public record LensTint() implements ItemTintSource {
		public static final LensTint INSTANCE = new LensTint();
		public static final MapCodec<LensTint> MAP_CODEC = unit(INSTANCE);

		@Override
		public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
			return ARGB.opaque(((LensItem) stack.getItem()).getLensColor(stack, level));
		}

		@Override
		public MapCodec<LensTint> type() {
			return MAP_CODEC;
		}
	}

	public static void submitItemTintSources(BiConsumer<Identifier, MapCodec<? extends ItemTintSource>> consumer) {
		consumer.accept(botaniaRL("gaia_spirit"), GaiaSpiritTint.MAP_CODEC);
		consumer.accept(botaniaRL("gaia_spreader"), GaiaSpreaderTint.MAP_CODEC);
		consumer.accept(botaniaRL("wand"), WandTint.MAP_CODEC);
		consumer.accept(botaniaRL("mana_pool"), ManaPoolTint.MAP_CODEC);
		consumer.accept(botaniaRL("mana_item"), ManaItemTint.MAP_CODEC);
		consumer.accept(botaniaRL("spellbinding_cloth"), SpellbindingClothTint.MAP_CODEC);
		consumer.accept(botaniaRL("brew"), BrewTint.MAP_CODEC);
		consumer.accept(botaniaRL("mana_blaster_lens"), ManaBlasterLensTint.MAP_CODEC);
		consumer.accept(botaniaRL("mana_blaster_burst"), ManaBlasterBurstTint.MAP_CODEC);
		consumer.accept(botaniaRL("soulscribe"), SoulscribeTint.MAP_CODEC);
		consumer.accept(botaniaRL("terra_shatterer"), TerraShattererTint.MAP_CODEC);
		consumer.accept(botaniaRL("lens"), LensTint.MAP_CODEC);
	}

	// [VanillaCopy] the old FastColor.ARGB32#lerp, per channel
	private static int lerpColor(float delta, int from, int to) {
		int a = Mth.lerpInt(delta, ARGB.alpha(from), ARGB.alpha(to));
		int r = Mth.lerpInt(delta, ARGB.red(from), ARGB.red(to));
		int g = Mth.lerpInt(delta, ARGB.green(from), ARGB.green(to));
		int b = Mth.lerpInt(delta, ARGB.blue(from), ARGB.blue(to));
		return ARGB.color(a, r, g, b);
	}

	private static int addToColor(int color, int add) {
		int red = Math.clamp(ARGB.red(color) + add, 0, 255);
		int green = Math.clamp(ARGB.green(color) + add, 0, 255);
		int blue = Math.clamp(ARGB.blue(color) + add, 0, 255);
		return ARGB.color(red, green, blue);
	}

	private ColorHandler() {}
}
