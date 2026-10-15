/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Runs one of Botania's loot sub-providers as a Fabric loot table provider. The sub-provider gets a context whose
 * loot table lookup answers for Botania's own tables too (a stand-alone reference, bound when the data is loaded),
 * so nested tables can be referenced across providers the way the registry-builder path allows.
 */
public class BotaniaSimpleLootWrapper extends SimpleFabricLootTableSubProvider {
	private final LootTableSubProvider.Factory factory;
	private final CompletableFuture<HolderLookup.Provider> registries;

	private BotaniaSimpleLootWrapper(LootTableSubProvider.Factory factory, FabricPackOutput output,
			CompletableFuture<HolderLookup.Provider> registries, ContextKeySet paramSet) {
		super(output, registries, paramSet);
		this.factory = factory;
		this.registries = registries;
	}

	public static FabricDataGenerator.Pack.RegistryDependentFactory<BotaniaSimpleLootWrapper> wrap(LootTableProvider.SubProviderEntry entry) {
		return (output, registries) -> new BotaniaSimpleLootWrapper(entry.bootstrap(), output, registries, entry.paramSet());
	}

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
		factory.create(new Context(registries.join(), output)).run();
	}

	@Override
	public void run() {
		// Fabric asks for generate(BiConsumer) and writes the files itself
	}

	private record Context(HolderLookup.Provider registries, BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output)
			implements
				LootTableSubProvider.Context {
		private HolderLookup.RegistryLookup<LootTable> lootTables() {
			return registries.lookupOrThrow(Registries.LOOT_TABLE);
		}

		@Override
		public Holder.Reference<LootTable> accept(ResourceKey<LootTable> key, LootTable.Builder value) {
			output.accept(key, value);
			return Holder.Reference.createStandAlone(lootTables(), key);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
			if (key.identifier().equals(Registries.LOOT_TABLE.identifier())) {
				HolderLookup.RegistryLookup<LootTable> lootTables = lootTables();
				return (HolderGetter<S>) new HolderGetter<LootTable>() {
					@Override
					public Optional<Holder.Reference<LootTable>> get(ResourceKey<LootTable> id) {
						return Optional.of(lootTables.get(id).orElseGet(() -> Holder.Reference.createStandAlone(lootTables, id)));
					}

					@Override
					public Optional<HolderSet.Named<LootTable>> get(TagKey<LootTable> id) {
						return lootTables.get(id);
					}
				};
			}
			return (HolderGetter<S>) registries.lookupOrThrow(key);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
			return (Stream) registries.lookupOrThrow(key).listElements();
		}
	}
}
