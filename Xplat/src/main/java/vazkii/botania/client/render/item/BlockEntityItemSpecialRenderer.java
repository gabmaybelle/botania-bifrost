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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import vazkii.botania.client.render.block_entity.BlockEntityItemRenderer;
import vazkii.botania.client.render.entity.EntityRenderers;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Draws a block item through its block entity renderer (pylons, the brewery, the avatar and so on).
 * {@code {"type": "botania:block_entity", "block": "botania:mana_pylon", "context": "gui"}}; the context is what the
 * old built-in renderer got told and only the pylons care.
 */
public class BlockEntityItemSpecialRenderer implements SpecialModelRenderer<ItemStack> {
	private final BlockEntityItemRenderer renderer;
	private final ItemDisplayContext context;

	public BlockEntityItemSpecialRenderer(BlockEntityItemRenderer renderer, ItemDisplayContext context) {
		this.renderer = renderer;
		this.context = context;
	}

	@Override
	public void submit(@Nullable ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
		if (stack != null) {
			poseStack.pushPose();
			// block entity renderers draw in block space; a special model is drawn around the item's centre
			poseStack.translate(-0.5F, -0.5F, -0.5F);
			renderer.render(stack, context, poseStack, collector, light, overlay);
			poseStack.popPose();
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

	public record Unbaked(Identifier block, ItemDisplayContext context) implements SpecialModelRenderer.Unbaked<ItemStack> {
		public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Identifier.CODEC.fieldOf("block").forGetter(Unbaked::block),
				ItemDisplayContext.CODEC.optionalFieldOf("context", ItemDisplayContext.NONE).forGetter(Unbaked::context)
		).apply(i, Unbaked::new));

		@Override
		public @Nullable SpecialModelRenderer<ItemStack> bake(BakingContext context) {
			Block b = BuiltInRegistries.BLOCK.getValue(block);
			Function<Block, BlockEntityItemRenderer> factory = EntityRenderers.BE_ITEM_RENDERER_FACTORIES.get(b);
			return factory == null ? null : new BlockEntityItemSpecialRenderer(factory.apply(b), this.context);
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
