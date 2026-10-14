/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.common.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;
import java.util.stream.Stream;

public final class NbtHelper {

	// PORT-26.3: bridges for Botania APIs that still speak CompoundTag while vanilla saves through ValueOutput/ValueInput.
	/** Stores or reads a whole compound's entries flat, as if they were written on the output itself. */
	public static final MapCodec<CompoundTag> COMPOUND_PASSTHROUGH_CODEC = new MapCodec<>() {
		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.empty();
		}

		@Override
		public <T> DataResult<CompoundTag> decode(DynamicOps<T> ops, MapLike<T> input) {
			CompoundTag tag = new CompoundTag();
			input.entries().forEach(entry -> ops.getStringValue(entry.getFirst()).result()
					.ifPresent(key -> tag.put(key, ops.convertTo(NbtOps.INSTANCE, entry.getSecond()))));
			return DataResult.success(tag);
		}

		@Override
		public <T> RecordBuilder<T> encode(CompoundTag tag, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			for (String key : tag.keySet()) {
				prefix.add(key, NbtOps.INSTANCE.convertTo(ops, tag.get(key)));
			}
			return prefix;
		}
	};

	/** Writes an already-built tag under a key. */
	public static void putTag(ValueOutput output, String key, Tag tag) {
		output.store(key, Codec.PASSTHROUGH, new Dynamic<>(NbtOps.INSTANCE, tag));
	}

	/** Reads a raw tag back out of a key. */
	public static Optional<Tag> getTag(ValueInput input, String key) {
		return input.read(key, Codec.PASSTHROUGH).map(dynamic -> dynamic.convert(NbtOps.INSTANCE).getValue());
	}

	/**
	 * Uses the smallest possible tag type to store the specified integer value in the provided tag. The value can be
	 * read again like a regular INT tag. This is supposed to be used only for network update packets.
	 *
	 * @see net.minecraft.network.codec.ByteBufCodecs#VAR_INT
	 */
	public static void putVarInt(CompoundTag tag, String key, int value) {
		if (value > Short.MAX_VALUE || value < Short.MIN_VALUE) {
			tag.putInt(key, value);
		} else if (value > Byte.MAX_VALUE || value < Byte.MIN_VALUE) {
			tag.putShort(key, (short) value);
		} else {
			tag.putByte(key, (byte) value);
		}
	}

	private NbtHelper() {}
}
