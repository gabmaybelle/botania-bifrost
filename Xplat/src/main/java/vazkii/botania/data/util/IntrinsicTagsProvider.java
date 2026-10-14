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

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A tags provider whose appenders take the registry objects themselves (blocks, items, entity types...).
 * PORT-26.3: Minecraft dropped {@code IntrinsicHolderTagsProvider}; tag appenders only take resource keys now.
 * This keeps the old shape so the tag providers can stay as they were.
 */
public abstract class IntrinsicTagsProvider<T> extends TagsProvider<T> {
	private final Function<T, ResourceKey<T>> keyExtractor;

	protected IntrinsicTagsProvider(PackOutput output, ResourceKey<? extends Registry<T>> registryKey,
			CompletableFuture<HolderLookup.Provider> lookupProvider, Function<T, ResourceKey<T>> keyExtractor) {
		super(output, registryKey, lookupProvider);
		this.keyExtractor = keyExtractor;
	}

	protected IntrinsicTagsProvider(PackOutput output, ResourceKey<? extends Registry<T>> registryKey,
			CompletableFuture<HolderLookup.Provider> lookupProvider,
			CompletableFuture<TagsProvider.TagLookup<T>> parentProvider, Function<T, ResourceKey<T>> keyExtractor) {
		super(output, registryKey, lookupProvider, parentProvider);
		this.keyExtractor = keyExtractor;
	}

	@Override
	protected IntrinsicTagAppender<T> tag(TagKey<T> tag) {
		return new IntrinsicTagAppender<>(getOrCreateRawBuilder(tag), registryKey, keyExtractor);
	}
}
