/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import vazkii.botania.api.recipe.BotaniaRecipe;
import vazkii.botania.common.crafting.recipe.*;

import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class BotaniaRecipeTypes {
	private static final Map<Identifier, RecipeType<?>> TYPES = new LinkedHashMap<>();

	public static final RecipeType<vazkii.botania.api.recipe.ManaInfusionRecipe> MANA_INFUSION_TYPE = register(
			vazkii.botania.api.recipe.ManaInfusionRecipe.TYPE_ID);
	public static final RecipeType<vazkii.botania.api.recipe.ElvenTradeRecipe> ELVEN_TRADE_TYPE = register(
			vazkii.botania.api.recipe.ElvenTradeRecipe.TYPE_ID);
	public static final RecipeType<vazkii.botania.api.recipe.PureDaisyRecipe> PURE_DAISY_TYPE = register(
			vazkii.botania.api.recipe.PureDaisyRecipe.TYPE_ID);
	public static final RecipeType<vazkii.botania.api.recipe.BotanicalBreweryRecipe> BREW_TYPE = register(
			vazkii.botania.api.recipe.BotanicalBreweryRecipe.TYPE_ID);
	public static final RecipeType<vazkii.botania.api.recipe.PetalApothecaryRecipe> PETAL_APOTHECARY_TYPE = register(
			vazkii.botania.api.recipe.PetalApothecaryRecipe.TYPE_ID);
	public static final RecipeType<vazkii.botania.api.recipe.RunicAltarRecipe> RUNIC_ALTAR_TYPE = register(
			vazkii.botania.api.recipe.RunicAltarRecipe.TYPE_ID);
	public static final RecipeType<vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe> TERRA_PLATE_TYPE = register(
			vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe.TYPE_ID);
	public static final RecipeType<vazkii.botania.api.recipe.OrechidRecipe> ORECHID_TYPE = register(
			vazkii.botania.api.recipe.OrechidRecipe.TYPE_ID);
	public static final RecipeType<vazkii.botania.api.recipe.OrechidRecipe> ORECHID_IGNEM_TYPE = register(
			vazkii.botania.api.recipe.OrechidRecipe.IGNEM_TYPE_ID);
	public static final RecipeType<vazkii.botania.api.recipe.OrechidRecipe> MARIMORPHOSIS_TYPE = register(
			vazkii.botania.api.recipe.OrechidRecipe.MARIMORPHOSIS_TYPE_ID);

	private static <T extends Recipe<?>> RecipeType<T> register(Identifier id) {
		RecipeType<T> type = new BotaniaRecipeType<>(id.getPath());
		if (TYPES.put(id, type) != null) {
			throw new IllegalArgumentException("Multiple recipe types with ID " + id);
		}
		return type;
	}

	private record BotaniaRecipeType<T extends Recipe<?>>(String name) implements RecipeType<T> {
		@Override
		public String toString() {
			return name;
		}
	}

	/** PORT-26.3: every recipe needs a recipe-book category; Botania's all share this one. */
	public static final RecipeBookCategory BOOK_CATEGORY = new RecipeBookCategory();

	public static void submitRecipeBookCategories(BiConsumer<RecipeBookCategory, Identifier> r) {
		r.accept(BOOK_CATEGORY, BotaniaRecipe.BOOK_CATEGORY_ID);
	}

	/** PORT-26.3: Level#getRecipeManager is gone; recipes live on the server. */
	public static RecipeManager recipeManager(Level level) {
		return Objects.requireNonNull(level.getServer(), "recipes are only available on the server").getRecipeManager();
	}

	/**
	 * PORT-26.3: RecipeManager#getAllRecipesFor is gone; filtered from all recipes (worth a cache if it shows up in
	 * profiles).
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Recipe<?>> List<RecipeHolder<T>> getAllRecipesFor(RecipeManager manager, RecipeType<T> type) {
		List<RecipeHolder<T>> result = new ArrayList<>();
		for (RecipeHolder<?> holder : manager.getRecipes()) {
			if (holder.value().getType() == type) {
				result.add((RecipeHolder<T>) holder);
			}
		}
		return result;
	}

	public static <T extends Recipe<?>> List<RecipeHolder<T>> getAllRecipesFor(Level level, RecipeType<T> type) {
		return getAllRecipesFor(recipeManager(level), type);
	}

	public static void submitRecipeTypes(BiConsumer<RecipeType<?>, Identifier> r) {
		TYPES.forEach((resourceLocation, recipeType) -> r.accept(recipeType, resourceLocation));
	}

	public static void submitRecipeSerializers(BiConsumer<RecipeSerializer<?>, Identifier> r) {
		// serializers for our custom recipe types
		r.accept(ManaInfusionRecipe.SERIALIZER, vazkii.botania.api.recipe.ManaInfusionRecipe.TYPE_ID);
		r.accept(ElvenTradeRecipe.SERIALIZER, vazkii.botania.api.recipe.ElvenTradeRecipe.TYPE_ID);
		r.accept(LexiconElvenTradeRecipe.SERIALIZER, vazkii.botania.api.recipe.ElvenTradeRecipe.TYPE_ID_LEXICON);
		r.accept(PureDaisyRecipe.SERIALIZER, vazkii.botania.api.recipe.PureDaisyRecipe.TYPE_ID);
		r.accept(BotanicalBreweryRecipe.SERIALIZER, vazkii.botania.api.recipe.BotanicalBreweryRecipe.TYPE_ID);
		r.accept(PetalApothecaryRecipe.SERIALIZER, vazkii.botania.api.recipe.PetalApothecaryRecipe.TYPE_ID);
		r.accept(RunicAltarRecipe.SERIALIZER, vazkii.botania.api.recipe.RunicAltarRecipe.TYPE_ID);
		r.accept(HeadRecipe.SERIALIZER, vazkii.botania.api.recipe.RunicAltarRecipe.HEAD_TYPE_ID);
		r.accept(TerrestrialAgglomerationRecipe.SERIALIZER, vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe.TYPE_ID);
		r.accept(OrechidRecipe.SERIALIZER, vazkii.botania.api.recipe.OrechidRecipe.TYPE_ID);
		r.accept(OrechidIgnemRecipe.SERIALIZER, vazkii.botania.api.recipe.OrechidRecipe.IGNEM_TYPE_ID);
		r.accept(MarimorphosisRecipe.SERIALIZER, vazkii.botania.api.recipe.OrechidRecipe.MARIMORPHOSIS_TYPE_ID);

		// serializers for crafting recipe variants
		r.accept(AncientWillRecipe.SERIALIZER, botaniaRL("crafting_special_ancient_will_attach"));
		r.accept(ArmorUpgradeRecipe.SERIALIZER, botaniaRL("crafting_shaped_armor_upgrade"));
		r.accept(BlackHoleTalismanExtractRecipe.SERIALIZER, botaniaRL("crafting_special_black_hole_talisman_extract"));
		r.accept(CompositeLensRecipe.SERIALIZER, botaniaRL("crafting_special_composite_lens"));
		r.accept(CosmeticAttachRecipe.SERIALIZER, botaniaRL("crafting_special_cosmetic_attach"));
		r.accept(CosmeticRemoveRecipe.SERIALIZER, botaniaRL("crafting_special_cosmetic_remove"));
		r.accept(LaputaShardUpgradeRecipe.SERIALIZER, botaniaRL("crafting_special_shard_of_laputa_upgrade"));
		r.accept(LensDyeingRecipe.SERIALIZER, botaniaRL("crafting_special_lens_dye"));
		r.accept(LexiconReturningShapelessRecipe.SERIALIZER, botaniaRL("crafting_shapeless_lexicon_return"));
		r.accept(ManaBlasterClipRecipe.SERIALIZER, botaniaRL("crafting_special_mana_blaster_add_clip"));
		r.accept(ManaBlasterLensRecipe.SERIALIZER, botaniaRL("crafting_special_mana_blaster_add_lens"));
		r.accept(ManaBlasterRemoveLensRecipe.SERIALIZER, botaniaRL("crafting_special_mana_blaster_remove_lens"));
		r.accept(ManaUpgradeRecipe.SERIALIZER, botaniaRL("crafting_shaped_mana_upgrade"));
		r.accept(MergeVialRecipe.SERIALIZER, botaniaRL("crafting_special_merge_vial"));
		r.accept(PhantomInkRecipe.SERIALIZER, botaniaRL("crafting_special_phantom_ink_apply"));
		r.accept(ResoluteIvyRecipe.SERIALIZER, botaniaRL("crafting_special_resolute_ivy"));
		r.accept(ShapelessManaUpgradeRecipe.SERIALIZER, botaniaRL("crafting_shapeless_mana_upgrade"));
		r.accept(ShapelessUncoverSpreaderRecipe.SERIALIZER, botaniaRL("crafting_shapeless_uncover_spreader"));
		r.accept(SpellbindingClothRecipe.SERIALIZER, botaniaRL("crafting_special_spell_cloth_apply"));
		r.accept(SplitLensRecipe.SERIALIZER, botaniaRL("crafting_special_split_lens"));
		r.accept(TerraShattererTippingRecipe.SERIALIZER, botaniaRL("crafting_special_terra_shatterer_tipping"));
		r.accept(TiaraWingsRecipe.SERIALIZER, botaniaRL("crafting_special_tiara_wings"));
		r.accept(WandOfTheForestRecipe.SERIALIZER, botaniaRL("crafting_shaped_wand"));
		r.accept(WaterBottleMatchingRecipe.SERIALIZER, botaniaRL("crafting_shaped_water_bottle_matching"));
	}

	@SuppressWarnings("unchecked")
	public static <C extends RecipeInput, T extends Recipe<C>> Optional<RecipeHolder<T>> getRecipe(Level world, Identifier id, RecipeType<T> expectedType) {
		var holder = recipeManager(world).byKey(ResourceKey.create(Registries.RECIPE, id));
		return holder.isPresent() && holder.get().value().getType() == expectedType
				? holder.map(h -> (RecipeHolder<T>) h)
				: Optional.empty();
	}
}
