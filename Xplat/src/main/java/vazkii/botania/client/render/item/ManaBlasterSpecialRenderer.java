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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.ManaBlasterItem;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.function.Consumer;

/**
 * The mana blaster: the gun model (with or without clip, the item definition picks) plus the inserted lens's own
 * item model sitting on the barrel. {@code {"type": "botania:mana_blaster", "gun": "botania:item/mana_blaster"}}.
 * PORT-26.3: this was {@code ManaBlasterBakedModel}, a baked model that composed the lens quads in at bake time.
 */
public class ManaBlasterSpecialRenderer implements SpecialModelRenderer<ItemStack> {
	private final Identifier gunModel;

	public ManaBlasterSpecialRenderer(Identifier gunModel) {
		this.gunModel = gunModel;
	}

	@Override
	public void submit(@Nullable ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
		BlockStateModel gun = ClientXplatAbstractions.instance().getResourceModel(gunModel);
		poseStack.pushPose();
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		if (gun != null) {
			RenderHelper.submitBlockModel(gun, poseStack, collector, light, overlay);
		}
		poseStack.popPose();

		ItemStack lens = stack != null ? ManaBlasterItem.getLens(stack) : ItemStack.EMPTY;
		if (!lens.isEmpty()) {
			poseStack.pushPose();
			poseStack.translate(-0.4F, 0.2F, 0.0F);
			poseStack.rotate(VecHelper.rotateY(90));
			poseStack.scale(0.625F, 0.625F, 0.625F);
			RenderHelper.submitItem(lens, ItemDisplayContext.NONE, poseStack, collector, null, light, OverlayTexture.NO_OVERLAY, 0);
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

	public record Unbaked(Identifier gun) implements SpecialModelRenderer.Unbaked<ItemStack> {
		public static final MapCodec<Unbaked> MAP_CODEC = Identifier.CODEC.fieldOf("gun").xmap(Unbaked::new, Unbaked::gun);

		@Override
		public SpecialModelRenderer<ItemStack> bake(BakingContext context) {
			return new ManaBlasterSpecialRenderer(gun);
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}
	}
}
