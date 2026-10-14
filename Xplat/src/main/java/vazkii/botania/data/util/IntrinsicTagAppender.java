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

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

import java.util.function.Function;

/**
 * Tag appender that accepts registry objects next to resource keys, see {@link IntrinsicTagsProvider}.
 */
public class IntrinsicTagAppender<T> implements TagAppender<T> {
	private final TagAppender<T> delegate;
	private final TagBuilder builder;
	private final ResourceKey<? extends Registry<T>> registryKey;
	private final Function<T, ResourceKey<T>> keyExtractor;

	public IntrinsicTagAppender(TagBuilder builder, ResourceKey<? extends Registry<T>> registryKey,
			Function<T, ResourceKey<T>> keyExtractor) {
		this.delegate = TagAppender.forBuilder(builder);
		this.builder = builder;
		this.registryKey = registryKey;
		this.keyExtractor = keyExtractor;
	}

	public IntrinsicTagAppender<T> add(T element) {
		delegate.add(keyExtractor.apply(element));
		return this;
	}

	@SafeVarargs
	public final IntrinsicTagAppender<T> add(T... elements) {
		for (T element : elements) {
			add(element);
		}
		return this;
	}

	public IntrinsicTagAppender<T> addOptional(Identifier id) {
		delegate.addOptional(ResourceKey.create(registryKey, id));
		return this;
	}

	public IntrinsicTagAppender<T> addOptionalTag(Identifier id) {
		delegate.addOptionalTag(TagKey.create(registryKey, id));
		return this;
	}

	@Override
	public IntrinsicTagAppender<T> add(ResourceKey<T> element) {
		delegate.add(element);
		return this;
	}

	@Override
	public IntrinsicTagAppender<T> addOptional(ResourceKey<T> element) {
		delegate.addOptional(element);
		return this;
	}

	@Override
	public IntrinsicTagAppender<T> addTag(TagKey<T> tag) {
		delegate.addTag(tag);
		return this;
	}

	@Override
	public IntrinsicTagAppender<T> addOptionalTag(TagKey<T> tag) {
		delegate.addOptionalTag(tag);
		return this;
	}

	// PORT-26.3: NeoForge adds these four to TagAppender (ITagAppenderExtension), so they have to exist for the
	// NeoForge build; no @Override because vanilla's TagAppender has no such methods. Botania never removes entries.
	public IntrinsicTagAppender<T> add(TagEntry entry) {
		builder.add(entry);
		return this;
	}

	public IntrinsicTagAppender<T> replace(boolean value) {
		builder.setReplace(value);
		return this;
	}

	public IntrinsicTagAppender<T> remove(TagKey<T> tag) {
		throw new UnsupportedOperationException("Botania's tag providers never remove entries");
	}

	public IntrinsicTagAppender<T> remove(ResourceKey<T> element) {
		throw new UnsupportedOperationException("Botania's tag providers never remove entries");
	}
}
