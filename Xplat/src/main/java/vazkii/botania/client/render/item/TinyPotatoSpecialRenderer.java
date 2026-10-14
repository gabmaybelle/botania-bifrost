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

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.render.block_entity.TinyPotatoBlockEntityRenderer;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.function.Consumer;

/**
 * The tiny potato item: the tater model matching its custom name (or the Halloween one), else the plain model.
 * {@code {"type": "botania:tiny_potato", "model": "botania:block/tiny_potato"}}.
 * PORT-26.3: this was {@code TinyPotatoModel}, an item override on the baked model. The enchanted glint is not drawn.
 */
public class TinyPotatoSpecialRenderer implements SpecialModelRenderer<ItemStack> {
	private final Identifier defaultModel;

	public TinyPotatoSpecialRenderer(Identifier defaultModel) {
		this.defaultModel = defaultModel;
	}

	@Override
	public void submit(@Nullable ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
		BlockStateModel model;
		if (stack != null && (stack.has(DataComponents.CUSTOM_NAME) || ClientProxy.dootDoot)) {
			model = TinyPotatoBlockEntityRenderer.getModelFromDisplayName(stack.getHoverName());
		} else {
			model = ClientXplatAbstractions.instance().getResourceModel(defaultModel);
		}
		if (model != null) {
			poseStack.pushPose();
			poseStack.translate(-0.5F, -0.5F, -0.5F);
			RenderHelper.submitBlockModel(model, poseStack, collector, light, overlay);
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

	public record Unbaked(Identifier model) implements SpecialModelRenderer.Unbaked<ItemStack> {
		public static final MapCodec<Unbaked> MAP_CODEC = Identifier.CODEC.fieldOf("model").xmap(Unbaked::new, Unbaked::model);

		@Override
		public SpecialModelRenderer<ItemStack> bake(BakingContext context) {
			return new TinyPotatoSpecialRenderer(model);
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
