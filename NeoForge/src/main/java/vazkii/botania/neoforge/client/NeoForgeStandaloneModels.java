/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * PORT-26.3: NeoForge loads "standalone" (non block state, non item) models under keys instead of plain ids.
 * Botania's resource models (MiscellaneousModels, tiny potatoes, spreader parts, ...) are registered here and
 * read back by id once baking has completed.
 */
public final class NeoForgeStandaloneModels {
	private static final Map<Identifier, StandaloneModelKey<BlockStateModel>> KEYS = new HashMap<>();

	public static void register(ModelEvent.RegisterStandalone event, Identifier id) {
		StandaloneModelKey<BlockStateModel> key = KEYS.computeIfAbsent(id, i -> new StandaloneModelKey<>(i::toString));
		event.register(key, SimpleUnbakedStandaloneModel.blockStateModel(id));
	}

	@Nullable
	public static BlockStateModel get(ModelManager modelManager, Identifier id) {
		StandaloneModelKey<BlockStateModel> key = KEYS.get(id);
		return key == null ? null : modelManager.getStandaloneModel(key);
	}

	@Nullable
	public static BlockStateModel get(Identifier id) {
		return get(Minecraft.getInstance().getModelManager(), id);
	}

	private NeoForgeStandaloneModels() {}
}
