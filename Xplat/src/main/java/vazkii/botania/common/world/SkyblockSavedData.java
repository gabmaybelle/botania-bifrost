/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.world;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import vazkii.botania.common.datafix.LegacySavedData;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class SkyblockSavedData extends SavedData {
	// PORT-26.3: saved data is described by a type with a codec, and the file is named by an identifier now
	// (data/botania/gog_skyblock_islands.dat instead of data/gog_skyblock_islands.dat)
	private static final Identifier NAME = botaniaRL("gog_skyblock_islands");

	private record Island(IslandPos pos, UUID player) {
	}

	private static final Codec<Island> ISLAND_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			IslandPos.MAP_CODEC.forGetter(Island::pos),
			UUIDUtil.CODEC.fieldOf("Player").forGetter(Island::player)
	).apply(instance, Island::new));
	private static final Codec<SkyblockSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ISLAND_CODEC.listOf().fieldOf("Islands").forGetter(SkyblockSavedData::islands),
			Codec.INT_STREAM.optionalFieldOf("SpiralState").forGetter(data -> Optional.of(IntStream.of(data.spiral.toIntArray())))
	).apply(instance, SkyblockSavedData::new));
	public static final SavedDataType<SkyblockSavedData> TYPE = new SavedDataType<>(NAME, SkyblockSavedData::new, CODEC, DataFixTypes.LEVEL);

	/** The offset is chosen to put islands under default settings in the center of a chunk region. */
	private static final int OFFSET = 1;

	public final BiMap<IslandPos, UUID> skyblocks;
	private final Spiral spiral;

	public SkyblockSavedData() {
		this.skyblocks = HashBiMap.create();
		this.spiral = new Spiral();
	}

	private SkyblockSavedData(List<Island> islands, Optional<IntStream> spiralState) {
		HashBiMap<IslandPos, UUID> map = HashBiMap.create();
		for (Island island : islands) {
			map.put(island.pos(), island.player());
		}
		this.skyblocks = map;
		this.spiral = spiralState.map(state -> Spiral.fromArray(state.toArray())).orElseGet(Spiral::new);
	}

	private List<Island> islands() {
		return skyblocks.entrySet().stream().map(entry -> new Island(entry.getKey(), entry.getValue())).toList();
	}

	public static SkyblockSavedData get(ServerLevel world) {
		return LegacySavedData.get(world, TYPE, NAME.getPath());
	}

	public IslandPos getSpawn() {
		if (skyblocks.containsValue(Util.NIL_UUID)) {
			return skyblocks.inverse().get(Util.NIL_UUID);
		}
		IslandPos pos = new IslandPos(OFFSET, OFFSET);
		skyblocks.put(pos, Util.NIL_UUID);
		setDirty();
		return pos;
	}

	public IslandPos create(UUID playerId) {
		int scale = BotaniaConfig.common().gogIslandScaleMultiplier();
		IslandPos islandPos;
		do {
			int[] pos = spiral.next();
			islandPos = new IslandPos(pos[0] * scale + OFFSET, pos[1] * scale + OFFSET);
		} while (skyblocks.containsKey(islandPos));

		skyblocks.put(islandPos, playerId);
		setDirty();
		return islandPos;
	}

	// Adapted from https://stackoverflow.com/questions/398299/looping-in-a-spiral
	private static class Spiral {
		private int x = 0;
		private int y = 0;
		private int dx = 0;
		private int dy = -1;

		Spiral() {}

		Spiral(int x, int y, int dx, int dy) {
			this.x = x;
			this.y = y;
			this.dx = dx;
			this.dy = dy;
		}

		int[] next() {
			if (x == y || x < 0 && x == -y || x > 0 && x == 1 - y) {
				int t = dx;
				dx = -dy;
				dy = t;
			}
			x += dx;
			y += dy;
			return new int[] { x, y };
		}

		int[] toIntArray() {
			return new int[] { x, y, dx, dy };
		}

		static Spiral fromArray(int[] ints) {
			return new Spiral(ints[0], ints[1], ints[2], ints[3]);
		}
	}
}
