/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.data.util;

import com.mojang.serialization.Lifecycle;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * PORT-26.3: item components (name, model and the rest) are bound to the item holders when a server builds its
 * reloadable data. A datagen run has no server, and Botania's recipes still carry {@code ItemStack}s, so the
 * datagen entry points bind the components from the same initializers before any provider runs. It works from
 * the built-in registries alone so it can run before the loader has built its datagen registry set.
 */
public final class DatagenItemComponents {
	private static boolean bound;

	private DatagenItemComponents() {}

	public static void bind() {
		if (bound) {
			return;
		}
		bound = true;
		HolderLookup.Provider builtIn = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lenient(builtIn)).forEach(DataComponentInitializers.PendingComponents::apply);
	}

	/**
	 * Datapack registries (damage types, jukebox songs, banner patterns) are not built-in, and the item initializers
	 * only need references into them, so unknown registries and unknown keys become stand-alone references and
	 * empty tag sets instead of errors.
	 */
	private static HolderLookup.Provider lenient(HolderLookup.Provider registries) {
		return new HolderLookup.Provider() {
			@Override
			public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
				return registries.listRegistryKeys();
			}

			@Override
			public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryKey) {
				Optional<? extends HolderLookup.RegistryLookup<T>> known = registries.lookup(registryKey);
				if (known.isPresent()) {
					return Optional.of(new LenientLookup<>(known.get()));
				}
				return Optional.of(new MissingRegistryLookup<>(registryKey));
			}
		};
	}

	private record LenientLookup<T>(HolderLookup.RegistryLookup<T> parent) implements HolderLookup.RegistryLookup.Delegate<T> {
		@Override
		public Optional<Holder.Reference<T>> get(ResourceKey<T> id) {
			return parent.get(id).or(() -> Optional.of(Holder.Reference.createStandAlone(parent, id)));
		}
	}

	private record MissingRegistryLookup<T>(ResourceKey<? extends Registry<? extends T>> registryKey) implements HolderLookup.RegistryLookup<T> {
		@Override
		public ResourceKey<? extends Registry<? extends T>> key() {
			return registryKey;
		}

		@Override
		public Lifecycle registryLifecycle() {
			return Lifecycle.stable();
		}

		@Override
		public Optional<Holder.Reference<T>> get(ResourceKey<T> id) {
			return Optional.of(Holder.Reference.createStandAlone(this, id));
		}

		@Override
		public Optional<HolderSet.Named<T>> get(TagKey<T> id) {
			return Optional.of(HolderSet.emptyNamed(this, id));
		}

		@Override
		public Stream<Holder.Reference<T>> listElements() {
			return Stream.empty();
		}

		@Override
		public Stream<HolderSet.Named<T>> listTags() {
			return Stream.empty();
		}
	}
}
