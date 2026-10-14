/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.client.render.item;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

/**
 * PORT-26.3: the built-in item renderers and the baked model wrappers (tiny potato, mana blaster) are special model
 * renderers now, named by the item definition JSON ({@code {"type": "minecraft:special", "model": {"type":
 * "botania:<name>", ...}, "base": ...}}). The loaders register them.
 */
public final class BotaniaSpecialModelRenderers {
	public static void register(BiConsumer<Identifier, MapCodec<? extends SpecialModelRenderer.Unbaked<?>>> consumer) {
		consumer.accept(botaniaRL("block_entity"), BlockEntityItemSpecialRenderer.Unbaked.MAP_CODEC);
		consumer.accept(botaniaRL("mana_blaster"), ManaBlasterSpecialRenderer.Unbaked.MAP_CODEC);
		consumer.accept(botaniaRL("tiny_potato"), TinyPotatoSpecialRenderer.Unbaked.MAP_CODEC);
		consumer.accept(botaniaRL("gaia_head"), GaiaHeadSpecialRenderer.Unbaked.MAP_CODEC);
		consumer.accept(botaniaRL("lexicon"), LexiconSpecialRenderer.Unbaked.MAP_CODEC);
	}

	private BotaniaSpecialModelRenderers() {}
}
