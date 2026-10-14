/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import vazkii.botania.client.core.handler.RenderLexicon;
import vazkii.botania.client.model.BotaniaBookModel;
import vazkii.botania.client.model.BotaniaModelLayers;

import java.util.function.Consumer;

/**
 * The 3D Lexica Botania in the hand. {@code {"type": "botania:lexicon", "left_handed": false}}; the item definition
 * selects it for the first-person display contexts when the config allows the 3D model.
 * PORT-26.3: this was {@code ItemInHandRendererMixin} cancelling vanilla's hand item for the lexicon.
 */
public class LexiconSpecialRenderer implements SpecialModelRenderer<ItemStack> {
	private final BotaniaBookModel model;
	private final SpriteGetter sprites;
	private final boolean leftHanded;

	public LexiconSpecialRenderer(BotaniaBookModel model, SpriteGetter sprites, boolean leftHanded) {
		this.model = model;
		this.sprites = sprites;
		this.leftHanded = leftHanded;
	}

	@Override
	public void submit(@Nullable ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
		if (stack != null) {
			RenderLexicon.render(model, sprites, stack, leftHanded, poseStack, collector, light);
		}
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {
		output.accept(new Vector3f(-0.5F, -0.5F, -0.5F));
		output.accept(new Vector3f(0.5F, 0.5F, 0.5F));
	}

	@Override
	public @Nullable ItemStack extractArgument(ItemStack stack) {
		return stack;
	}

	public record Unbaked(boolean leftHanded) implements SpecialModelRenderer.Unbaked<ItemStack> {
		public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.BOOL.optionalFieldOf("left_handed", false).forGetter(Unbaked::leftHanded)
		).apply(i, Unbaked::new));

		@Override
		public SpecialModelRenderer<ItemStack> bake(BakingContext context) {
			return new LexiconSpecialRenderer(new BotaniaBookModel(context.entityModelSet().bakeLayer(BotaniaModelLayers.BOTANIA_BOOK)),
					context.sprites(), leftHanded);
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
