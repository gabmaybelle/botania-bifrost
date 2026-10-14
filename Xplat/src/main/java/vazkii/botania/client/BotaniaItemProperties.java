/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.BottledManaItem;
import vazkii.botania.common.item.LexicaBotaniaItem;
import vazkii.botania.common.item.LifeAggregatorItem;
import vazkii.botania.common.item.ManaBlasterItem;
import vazkii.botania.common.item.WandOfTheForestItem;
import vazkii.botania.common.item.brew.BaseBrewItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraTruncatorItem;
import vazkii.botania.common.item.relic.FruitOfGrisaiaItem;
import vazkii.botania.common.item.rod.SkiesRodItem;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

/**
 * PORT-26.3: item model predicates are data-driven now. An item definition JSON (assets/botania/items/*.json) asks
 * for {@code {"type": "minecraft:condition", "property": "botania:test", "test": "<name>", ...}} or
 * {@code {"type": "minecraft:range_dispatch", "property": "botania:range", "range": "<name>", ...}}; the names below
 * are the old item property functions. The loaders register the two property types.
 */
public final class BotaniaItemProperties {
	@FunctionalInterface
	public interface ItemTest {
		boolean test(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context);
	}

	@FunctionalInterface
	public interface ItemRange {
		float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed);
	}

	public static final Identifier TEST_ID = botaniaRL("test");
	public static final Identifier RANGE_ID = botaniaRL("range");
	private static final Map<String, ItemTest> TESTS = new LinkedHashMap<>();
	private static final Map<String, ItemRange> RANGES = new LinkedHashMap<>();

	static {
		TESTS.put("active", (stack, level, entity, seed, ctx) -> stack.has(BotaniaDataComponents.ACTIVE));
		TESTS.put("active_transient", (stack, level, entity, seed, ctx) -> stack.has(BotaniaDataComponents.ACTIVE_TRANSIENT));
		TESTS.put("vuvuzela", (stack, level, entity, seed, ctx) -> stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains("vuvuzela"));
		// same as goat horn:
		TESTS.put("tooting", (stack, level, entity, seed, ctx) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack);
		TESTS.put("has_clip", (stack, level, entity, seed, ctx) -> ManaBlasterItem.hasClip(stack));
		TESTS.put("elven", (stack, level, entity, seed, ctx) -> LexicaBotaniaItem.isElven(stack));
		TESTS.put("totalbiscuit", (stack, level, entity, seed, ctx) -> stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains("totalbiscuit"));
		TESTS.put("life_aggregator_full", (stack, level, entity, seed, ctx) -> LifeAggregatorItem.hasData(stack));
		TESTS.put("bindmode", (stack, level, entity, seed, ctx) -> WandOfTheForestItem.getBindMode(stack));
		TESTS.put("pool_full", (stack, level, entity, seed, ctx) -> stack.has(BotaniaDataComponents.RENDER_FULL));
		TESTS.put("holiday", (stack, level, entity, seed, ctx) -> ClientProxy.jingleTheBells);
		TESTS.put("ring_active", (stack, level, entity, seed, ctx) -> entity instanceof Player player && !player.getCooldowns().isOnCooldown(stack));
		TESTS.put("reddit", (stack, level, entity, seed, ctx) -> stack.getHoverName().getString().equalsIgnoreCase("dammit reddit"));
		TESTS.put("elucidator", (stack, level, entity, seed, ctx) -> "the elucidator".equals(stack.getHoverName().getString().toLowerCase(Locale.ROOT).trim()));
		TESTS.put("terra_truncator_active", (stack, level, entity, seed, ctx) -> !(entity instanceof Player player) || TerraTruncatorItem.shouldBreak(player));
		TESTS.put("tipped", (stack, level, entity, seed, ctx) -> TerraShattererItem.isTipped(stack));
		TESTS.put("terra_shatterer_active", (stack, level, entity, seed, ctx) -> TerraShattererItem.isEnabled(stack));
		TESTS.put("boot", (stack, level, entity, seed, ctx) -> FruitOfGrisaiaItem.isBoot(stack));
		TESTS.put("skies_rod_flying", (stack, level, entity, seed, ctx) -> SkiesRodItem.isFlying(stack));
		// [VanillaCopy] ItemProperties.BOW's minecraft:pulling property
		TESTS.put("pulling", (stack, level, entity, seed, ctx) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack);
		TESTS.put("lexicon_3d", (stack, level, entity, seed, ctx) -> BotaniaConfig.client().lexicon3dModel());

		RANGES.put("swigs_taken", (stack, level, entity, seed) -> {
			int swigsLeft = BottledManaItem.getSwigsLeft(stack) - 1;
			int totalSwigs = BottledManaItem.SWIGS - 1;
			return swigsLeft == totalSwigs ? 0.0F : Math.nextUp((totalSwigs - swigsLeft) / (float) totalSwigs);
		});
		RANGES.put("brew_swigs_taken", (stack, level, entity, seed) -> {
			BaseBrewItem item = ((BaseBrewItem) stack.getItem());
			int swigsLeft = item.getSwigsLeft(stack) - 1;
			int totalSwigs = item.getSwigs(stack) - 1;
			return swigsLeft == totalSwigs ? 0.0F : Math.nextUp((totalSwigs - swigsLeft) / (float) totalSwigs);
		});
		// [VanillaCopy] ItemProperties.BOW's minecraft:pull property
		RANGES.put("pull", (stack, level, entity, seed) -> {
			if (entity == null) {
				return 0.0F;
			} else {
				return entity.getUseItem() != stack ? 0.0F : (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F;
			}
		});
	}

	private static Codec<String> nameCodec(Map<String, ?> names, String what) {
		return Codec.STRING.validate(name -> names.containsKey(name)
				? com.mojang.serialization.DataResult.success(name)
				: com.mojang.serialization.DataResult.error(() -> "Unknown Botania item " + what + " '" + name + "'"));
	}

	/** {@code "property": "botania:test", "test": "<name>"} */
	public record Test(String test) implements ConditionalItemModelProperty {
		public static final MapCodec<Test> MAP_CODEC = nameCodec(TESTS, "test").fieldOf("test").xmap(Test::new, Test::test);

		@Override
		public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
			return TESTS.get(test).test(stack, level, entity, seed, context);
		}

		@Override
		public MapCodec<Test> type() {
			return MAP_CODEC;
		}
	}

	/** {@code "property": "botania:range", "range": "<name>"} */
	public record Range(String range) implements RangeSelectItemModelProperty {
		public static final MapCodec<Range> MAP_CODEC = nameCodec(RANGES, "range").fieldOf("range").xmap(Range::new, Range::range);

		@Override
		public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
			return RANGES.get(range).get(stack, level, owner != null ? owner.asLivingEntity() : null, seed);
		}

		@Override
		public MapCodec<Range> type() {
			return MAP_CODEC;
		}
	}

	public static void registerTests(BiConsumer<Identifier, MapCodec<? extends ConditionalItemModelProperty>> consumer) {
		consumer.accept(TEST_ID, Test.MAP_CODEC);
	}

	public static void registerRanges(BiConsumer<Identifier, MapCodec<? extends RangeSelectItemModelProperty>> consumer) {
		consumer.accept(RANGE_ID, Range.MAP_CODEC);
	}

	private BotaniaItemProperties() {}
}
