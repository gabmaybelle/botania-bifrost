/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.datafix;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.SavedDataStorage;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Finds Botania saved data that 1.21.1 wrote under its old file name. 26.3 names saved data files by identifier
 * ({@code data/botania/<name>.dat}) and moved the vanilla dimensions to {@code dimensions/minecraft/<name>}, while
 * the old files stay where they were. The old file is read once, then renamed to {@code <name>.dat.migrated}.
 */
public final class LegacySavedData {
	private LegacySavedData() {}

	public static <T extends SavedData> T get(ServerLevel level, SavedDataType<T> type, String legacyName) {
		SavedDataStorage storage = level.getDataStorage();
		T data = storage.get(type);
		if (data != null) {
			return data;
		}
		T migrated = read(storage, legacyFile(level, legacyName), type, level);
		if (migrated != null) {
			storage.set(type, migrated);
			return migrated;
		}
		return storage.computeIfAbsent(type);
	}

	/** Where 1.21.1 kept a level's saved data file of this name. */
	public static Path legacyFile(ServerLevel level, String legacyName) {
		Path root = level.getServer().getWorldPath(LevelResource.ROOT);
		Path folder;
		if (level.dimension() == Level.OVERWORLD) {
			folder = root.resolve("data");
		} else if (level.dimension() == Level.NETHER) {
			folder = root.resolve("DIM-1").resolve("data");
		} else if (level.dimension() == Level.END) {
			folder = root.resolve("DIM1").resolve("data");
		} else {
			// other dimensions already lived under dimensions/<namespace>/<path>
			folder = root.resolve("dimensions").resolve(level.dimension().identifier().getNamespace())
					.resolve(level.dimension().identifier().getPath()).resolve("data");
		}
		return folder.resolve(legacyName + ".dat");
	}

	/** Reads and upgrades an old saved data file, and renames it so it is only read once. */
	public static <T extends SavedData> @Nullable T read(SavedDataStorage storage, Path file, SavedDataType<T> type, ServerLevel level) {
		if (!Files.isRegularFile(file)) {
			return null;
		}
		try {
			CompoundTag tag = storage.readTagFromDisk(file, type.dataFixType(),
					SharedConstants.getCurrentVersion().dataVersion().version());
			RegistryOps<Tag> ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
			T data = type.codec().parse(ops, tag.get("data"))
					.resultOrPartial(error -> BotaniaAPI.LOGGER.error("Could not read old saved data {}: {}", file, error))
					.orElse(null);
			if (data != null) {
				Files.move(file, file.resolveSibling(file.getFileName() + ".migrated"), StandardCopyOption.REPLACE_EXISTING);
				BotaniaAPI.LOGGER.info("Moved old saved data {} to {}", file, type.id());
			}
			return data;
		} catch (IOException e) {
			BotaniaAPI.LOGGER.error("Could not read old saved data {}", file, e);
			return null;
		}
	}
}
