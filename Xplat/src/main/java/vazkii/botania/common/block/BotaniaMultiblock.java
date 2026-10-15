/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * PORT-26.3: Botania's own check that a structure is built, for the structures gameplay depends on (the terrestrial
 * agglomeration plate, the alfheim portal and the mana enchanter). These were Patchouli multiblocks, and Patchouli has
 * no 26.3 release: without it the game could not load these classes, and with only its API every check failed. The
 * patterns and rules are the ones Patchouli's dense multiblocks use, so the same builds count:
 * <ul>
 * <li>{@code pattern[layer][row]} is a string of columns; the first layer is the top one.</li>
 * <li>{@code '_'} matches anything, {@code ' '} matches air, and {@code '0'} marks the anchor (air unless mapped).</li>
 * <li>A target is a {@link Block} (any state of it), a {@link TagKey} of blocks, or a {@link Predicate} of states.</li>
 * <li>{@link #validate(BlockGetter, BlockPos)} tries every rotation and returns the first that fits.</li>
 * </ul>
 * The Patchouli versions, for the Lexica's previews, live in
 * {@link vazkii.botania.common.integration.patchouli.PatchouliMultiblocks}.
 */
public final class BotaniaMultiblock {
	private final Predicate<BlockState>[][][] matchers;
	private final int sizeX, sizeY, sizeZ;
	private final int offX, offY, offZ;

	@SuppressWarnings("unchecked")
	public BotaniaMultiblock(String[][] pattern, Object... targets) {
		Map<Character, Predicate<BlockState>> byChar = new HashMap<>();
		for (int i = 0; i + 1 < targets.length; i += 2) {
			byChar.put((Character) targets[i], toMatcher(targets[i + 1]));
		}
		if (targets.length % 2 != 0) {
			throw new IllegalArgumentException("Targets must come in character and matcher pairs");
		}
		byChar.putIfAbsent('_', state -> true);
		byChar.putIfAbsent(' ', BlockState::isAir);
		byChar.putIfAbsent('0', BlockState::isAir);

		sizeY = pattern.length;
		sizeX = pattern[0].length;
		sizeZ = pattern[0][0].length();
		matchers = new Predicate[sizeX][sizeY][sizeZ];
		int centerX = -1, centerY = -1, centerZ = -1;
		for (int layer = 0; layer < sizeY; layer++) {
			if (pattern[layer].length != sizeX) {
				throw new IllegalArgumentException("Every layer needs " + sizeX + " rows");
			}
			int y = sizeY - layer - 1;
			for (int x = 0; x < sizeX; x++) {
				String row = pattern[layer][x];
				if (row.length() != sizeZ) {
					throw new IllegalArgumentException("Every row needs " + sizeZ + " columns");
				}
				for (int z = 0; z < sizeZ; z++) {
					char c = row.charAt(z);
					Predicate<BlockState> matcher = byChar.get(c);
					if (matcher == null) {
						throw new IllegalArgumentException("Character " + c + " isn't mapped");
					}
					if (c == '0') {
						if (centerX != -1) {
							throw new IllegalArgumentException("A structure can't have two anchors");
						}
						centerX = x;
						centerY = y;
						centerZ = z;
					}
					matchers[x][y][z] = matcher;
				}
			}
		}
		if (centerX == -1) {
			throw new IllegalArgumentException("A structure needs an anchor ('0')");
		}
		offX = centerX;
		offY = centerY;
		offZ = centerZ;
	}

	@SuppressWarnings("unchecked")
	private static Predicate<BlockState> toMatcher(Object target) {
		if (target instanceof Block block) {
			return state -> state.is(block);
		} else if (target instanceof TagKey<?> tag) {
			return state -> state.is((TagKey<Block>) tag);
		} else if (target instanceof Predicate<?> predicate) {
			return (Predicate<BlockState>) predicate;
		}
		throw new IllegalArgumentException("Invalid target " + target);
	}

	/** @return the first rotation the structure anchored at {@code anchor} is built in, or null if it isn't. */
	@Nullable
	public Rotation validate(BlockGetter level, BlockPos anchor) {
		for (Rotation rotation : Rotation.values()) {
			if (validate(level, anchor, rotation)) {
				return rotation;
			}
		}
		return null;
	}

	/** @return whether the structure anchored at {@code anchor} is built in the given rotation. */
	public boolean validate(BlockGetter level, BlockPos anchor, Rotation rotation) {
		BlockPos origin = anchor.offset(new BlockPos(-offX, -offY, -offZ).rotate(rotation));
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				for (int z = 0; z < sizeZ; z++) {
					BlockPos pos = origin.offset(new BlockPos(x, y, z).rotate(rotation));
					if (!matchers[x][y][z].test(level.getBlockState(pos))) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
