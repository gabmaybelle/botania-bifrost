/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.client;

import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * PORT-26.3: Fabric loads "extra" (non block state, non item) models under keys instead of plain ids. Botania's
 * resource models (MiscellaneousModels, tiny potatoes, spreader parts, ...) are registered here with the model
 * loading plugin and read back by id once the model manager has reloaded.
 */
public final class FabricExtraModels {
	private static final Map<Identifier, ExtraModelKey<BlockStateModel>> KEYS = new HashMap<>();

	public static void register(ModelLoadingPlugin.Context context, Identifier id) {
		ExtraModelKey<BlockStateModel> key = KEYS.computeIfAbsent(id, i -> ExtraModelKey.create(i::toString));
		context.addModel(key, SimpleUnbakedExtraModel.blockStateModel(id));
	}

	@Nullable
	public static BlockStateModel get(Identifier id) {
		ExtraModelKey<BlockStateModel> key = KEYS.get(id);
		return key == null ? null : Minecraft.getInstance().getModelManager().getModel(key);
	}

	private FabricExtraModels() {}
}
