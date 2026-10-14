/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import com.google.common.base.Suppliers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.util.Function3;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(ItemPredicate.class)
public abstract class ItemPredicateFabricMixin {
	// static initialization means these will happen way earlier than item registration, so wrap in memoized suppliers
	@Unique
	private static final Supplier<Holder<Item>> VANILLA_SHEARS_HOLDER = Suppliers.memoize(
			() -> BuiltInRegistries.ITEM.wrapAsHolder(Items.SHEARS));
	@Unique
	private static final Supplier<Holder<Item>> MANASTEEL_SHEARS_HOLDER = Suppliers.memoize(
			() -> BuiltInRegistries.ITEM.wrapAsHolder(BotaniaItems.MANASTEEL_SHEARS));
	@Unique
	private static final Supplier<Holder<Item>> ELEMENTIUM_SHEARS_HOLDER = Suppliers.memoize(
			() -> BuiltInRegistries.ITEM.wrapAsHolder(BotaniaItems.ELEMENTIUM_SHEARS));

	@WrapOperation(
		// lambda expression within the static field initializer for ItemPredicate#CODEC
		// PORT-26.3: Minecraft is unobfuscated, so this is javac's name for that lambda (checked with javap on the 26.3 jar);
		// the record has three parts now (items, count, component matchers)
		method = "lambda$static$0",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/datafixers/Products$P3;apply(Lcom/mojang/datafixers/kinds/Applicative;Lcom/mojang/datafixers/util/Function3;)Lcom/mojang/datafixers/kinds/App;"
		)
	)
	private static <F extends K1> App<F, ItemPredicate> addBotaniaShears(
			Products.P3<F, Optional<HolderSet<Item>>, MinMaxBounds.Ints, DataComponentMatchers> groupResult,
			Applicative<F, ?> instance,
			Function3<Optional<HolderSet<Item>>, MinMaxBounds.Ints, DataComponentMatchers, ItemPredicate> function,
			Operation<App<F, ItemPredicate>> original) {
		if (XplatAbstractions.instance().isDataGen()) {
			// don't mess with anything during datagen so the correct loot tables are written
			return original.call(groupResult, instance, function);
		}

		return original.call(groupResult, instance, botania_wrapDecodeForShears(function));
	}

	@Unique
	private static Function3<Optional<HolderSet<Item>>, MinMaxBounds.Ints, DataComponentMatchers, ItemPredicate> botania_wrapDecodeForShears(
			Function3<Optional<HolderSet<Item>>, MinMaxBounds.Ints, DataComponentMatchers, ItemPredicate> function) {
		return (items, count, components) -> {
			if (items.isPresent() && !(items.get() instanceof HolderSet.Named<Item>) && items.get().contains(VANILLA_SHEARS_HOLDER.get())) {
				var newItems = new ReferenceOpenHashSet<Holder<Item>>();
				items.get().stream().forEach(newItems::add);
				newItems.add(MANASTEEL_SHEARS_HOLDER.get());
				newItems.add(ELEMENTIUM_SHEARS_HOLDER.get());
				return function.apply(Optional.of(HolderSet.direct(List.copyOf(newItems))), count, components);
			}
			return function.apply(items, count, components);
		};
	}
}
