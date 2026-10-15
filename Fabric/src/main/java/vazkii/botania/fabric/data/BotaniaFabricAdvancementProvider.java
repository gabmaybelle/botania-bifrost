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

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

import vazkii.botania.data.AdvancementProvider;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** Runs Botania's advancement generators (bootstrap sub-providers) through Fabric's advancement provider. */
public class BotaniaFabricAdvancementProvider extends FabricAdvancementProvider {
	private static final HolderOwner<Advancement> OWNER = new HolderOwner<>() {};

	public BotaniaFabricAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	public void generateAdvancement(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer) {
		BootstrapContext<Advancement> context = new BootstrapContext<>() {
			@Override
			public Holder.Reference<Advancement> register(ResourceKey<Advancement> key, Advancement value) {
				consumer.accept(new AdvancementHolder(key.identifier(), value));
				return Holder.Reference.createStandAlone(OWNER, key);
			}

			@SuppressWarnings("unchecked")
			@Override
			public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
				return (HolderGetter<S>) registries.lookupOrThrow(key);
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
				return (Stream) registries.lookupOrThrow(key).listElements();
			}
		};
		for (AdvancementSubProvider.Factory factory : AdvancementProvider.subProviders()) {
			factory.create(context).generate();
		}
	}
}
