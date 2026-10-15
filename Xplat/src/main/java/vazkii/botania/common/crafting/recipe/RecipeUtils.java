/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.google.common.base.Suppliers;

import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.brew.BrewContainer;
import vazkii.botania.api.recipe.BotaniaRecipe;
import vazkii.botania.api.recipe.ProcessingRecipeInput;
import vazkii.botania.api.recipe.RecipeWithCatalysts;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public final class RecipeUtils {
	/**
	 * Check if every ingredient in {@code recipe} is satisfied by an item in {@code input}.
	 *
	 * @param recipe          The recipe to match. This may be a dummy from {@link #wrapCatalysts(RecipeWithCatalysts)}
	 *                        or {@link #wrapAllIngredients(RecipeWithCatalysts)}.
	 * @param input           The input items to match against.
	 * @param specialMatching Whether some of the ingredients define a dynamic item set and thus cannot be matched using
	 *                        the default method.
	 */
	public static boolean matches(NonNullList<Ingredient> ingredients, ProcessingRecipeInput input,
			boolean specialMatching) {
		if (input.size() != ingredients.size()) {
			return false;
		}
		if (ingredients.size() == 1) {
			return ingredients.getFirst().test(input.getItem(0));
		}
		if (specialMatching) {
			return XplatAbstractions.instance().matchesWithCustomTestIngredients(input.getItems(), ingredients);
		}
		// PORT-26.3: matching by recipe would go through placementInfo(), which Botania's recipes don't have; by ingredients.
		return input.getStackedContents().canCraft(ingredients, null);
	}

	/**
	 * Like the vanilla method on recipe interface, but specialHandler is called first, and if it returns
	 * nonnull, that result is used instead of vanilla's
	 */
	public static NonNullList<ItemStack> getRemainingItemsSub(RecipeInput inv, Function<ItemStack, @Nullable ItemStack> specialHandler) {
		NonNullList<ItemStack> ret = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

		for (int i = 0; i < ret.size(); ++i) {
			ItemStack item = inv.getItem(i);
			ItemStack special = specialHandler.apply(item);
			if (special != null) {
				ret.set(i, special);
			} else if (item.getItem().getCraftingRemainder() != null) {
				ret.set(i, item.getItem().getCraftingRemainder().create());
			}
		}

		return ret;
	}

	/**
	 * Creates a {@link ProcessingRecipeInput} from the stacks in the provided item entities.
	 * Stack sizes greater than one are counted as that many input items.
	 */
	public static ProcessingRecipeInput getInputFromEntities(List<ItemEntity> itemEntities) {
		int count = itemEntities.stream().filter(Entity::isAlive).mapToInt(i -> i.getItem().getCount()).sum();
		ItemStack[] stacks = new ItemStack[count];
		int index = 0;
		for (ItemEntity entity : itemEntities) {
			ItemStack stack = entity.getItem();
			if (stack.getCount() == 1) {
				stacks[index++] = stack;
			} else {
				ItemStack copy = stack.copyWithCount(1);
				for (int i = 0; i < stack.getCount(); i++) {
					stacks[index++] = copy;
				}
			}
		}
		return new StacksProcessingRecipeInput(stacks);
	}

	/**
	 * Creates a {@link ProcessingRecipeInput} from the stacks in the provided list.
	 * Stack sizes greater than one are counted as that many input items.
	 */
	public static ProcessingRecipeInput getInputFromListWithoutUnstacking(List<ItemStack> itemStacks) {
		return new StacksProcessingRecipeInput(itemStacks.toArray(ItemStack[]::new));
	}

	/**
	 * Creates a {@link ProcessingRecipeInput} from the stacks in the provided container.
	 * Stack sizes greater than one are counted as that many input items.
	 */
	public static ProcessingRecipeInput getInputFromContainer(Container container) {
		int numSlots = container.getContainerSize();
		int count = IntStream.range(0, numSlots).map(i -> container.getItem(i).getCount()).sum();
		ItemStack[] stacks = new ItemStack[count];
		int index = 0;
		for (int slot = 0; slot < numSlots; slot++) {
			ItemStack stack = container.getItem(slot);
			if (stack.getCount() == 1) {
				stacks[index++] = stack;
			} else {
				ItemStack copy = stack.copyWithCount(1);
				for (int i = 0; i < stack.getCount(); i++) {
					stacks[index++] = copy;
				}
			}
		}
		return new StacksProcessingRecipeInput(stacks);
	}

	/**
	 * Wrap the catalysts as the ingredients of a dummy recipe, so they can be fed into a
	 * {@link net.minecraft.world.entity.player.StackedContents#canCraft(Recipe, IntList)} call.
	 * The dummy recipe cannot be used for anything else and will throw an exception when trying to do so anyway.
	 */
	public static <T extends RecipeInput> Recipe<T> wrapCatalysts(RecipeWithCatalysts<T> recipeWithCatalysts) {
		return new DummyRecipe<>(recipeWithCatalysts.getCatalysts());
	}

	/**
	 * Wrap the ingredients and catalysts as a dummy recipe with a combined ingredients list, so they can be fed into a
	 * {@link net.minecraft.world.entity.player.StackedContents#canCraft(Recipe, IntList)} call.
	 * The dummy recipe cannot be used for anything else and will throw an exception when trying to do so anyway.
	 */
	public static <T extends RecipeInput> Recipe<T> wrapAllIngredients(RecipeWithCatalysts<T> recipeWithCatalysts) {
		if (recipeWithCatalysts.getCatalysts().isEmpty()) {
			return recipeWithCatalysts;
		}
		if (recipeWithCatalysts.getIngredients().isEmpty()) {
			return wrapCatalysts(recipeWithCatalysts);
		}
		NonNullList<Ingredient> allIngredients = NonNullList.createWithCapacity(
				recipeWithCatalysts.getIngredients().size() + recipeWithCatalysts.getCatalysts().size());
		allIngredients.addAll(recipeWithCatalysts.getIngredients());
		allIngredients.addAll(recipeWithCatalysts.getCatalysts());
		return new DummyRecipe<>(allIngredients);
	}

	@SuppressWarnings("SuspiciousToArrayCall")
	private static final Supplier<Item[]> BREW_CONTAINER_ITEMS_SUPPLIER = Suppliers.memoize(
			() -> BuiltInRegistries.ITEM.stream().filter(BrewContainer.class::isInstance).toArray(Item[]::new));

	/**
	 * Defines an {@link Ingredient} that matches any item implementing {@link BrewContainer}.
	 */
	public static Ingredient getBrewContainerIngredient() {
		return Ingredient.of(BREW_CONTAINER_ITEMS_SUPPLIER.get());
	}

	private RecipeUtils() {}

	/**
	 * PORT-26.3: {@code NonNullList.of(Ingredient.EMPTY, ...)} is gone with {@code Ingredient.EMPTY}; a list without a
	 * default.
	 */
	@SafeVarargs
	public static <T> NonNullList<T> nonNullList(T... items) {
		NonNullList<T> list = NonNullList.create();
		Collections.addAll(list, items);
		return list;
	}

	/** PORT-26.3: {@code Ingredient#getItems} is gone; the ingredient's items as stacks, as it used to give them. */
	public static ItemStack[] stacksOf(Ingredient ingredient) {
		return ingredient.items().map(ItemStack::new).toArray(ItemStack[]::new);
	}

	/**
	 * PORT-26.3: a tag ingredient can't be expanded before its tag is bound (datagen, early world load); the holder
	 * set behind the ingredient is private, so this asks the only way it can.
	 */
	public static boolean canExpand(Ingredient ingredient) {
		try {
			ingredient.items();
			return true;
		} catch (UnsupportedOperationException e) {
			return false;
		}
	}

	private record DummyRecipe<T extends RecipeInput>(NonNullList<Ingredient> ingredients) implements BotaniaRecipe<T> {

		public NonNullList<Ingredient> getIngredients() {
			return ingredients;
		}

		@Override
		public boolean matches(T input, Level level) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ItemStack assemble(T input) {
			throw new UnsupportedOperationException();
		}

		@Override
		public RecipeSerializer<? extends Recipe<T>> getSerializer() {
			throw new UnsupportedOperationException();
		}

		@Override
		public RecipeType<? extends Recipe<T>> getType() {
			throw new UnsupportedOperationException();
		}
	}
}
