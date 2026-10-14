/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

import vazkii.botania.api.block.Bound;
import vazkii.botania.client.render.block_entity.BotaniaBlockEntityRenderer;
import vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.entity.ManaPoolMinecartEntity;

public class ManaPoolMinecartRenderer extends AbstractMinecartRenderer<ManaPoolMinecartEntity, ManaPoolMinecartRenderState> {
	private static final ManaPoolBlockEntity DUMMY = new ManaPoolBlockEntity(Bound.UNBOUND_POS, BotaniaBlocks.MANA_POOL.defaultBlockState());

	public ManaPoolMinecartRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, ModelLayers.MINECART);
	}

	@Override
	public ManaPoolMinecartRenderState createRenderState() {
		return new ManaPoolMinecartRenderState();
	}

	@Override
	public void extractRenderState(ManaPoolMinecartEntity poolCart, ManaPoolMinecartRenderState state, float partialTicks) {
		super.extractRenderState(poolCart, state, partialTicks);
		state.mana = poolCart.getMana();
	}

	@Override
	protected void submitMinecartContents(ManaPoolMinecartRenderState state, BlockModelRenderState blockModel, PoseStack ms, SubmitNodeCollector buffers, int light) {
		super.submitMinecartContents(state, blockModel, ms, buffers, light);
		ManaPoolBlockEntityRenderer.cartMana = state.mana;
		// TODO: Support other pool types
		ManaPoolBlockEntityRenderer.cartMaxMana = ManaPoolBlock.MAX_MANA;
		ManaPoolBlockEntityRenderer.cartBlock = BotaniaBlocks.MANA_POOL;
		BlockEntityRenderer<?, ?> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(DUMMY);
		if (renderer instanceof BotaniaBlockEntityRenderer<?> botaniaRenderer) {
			botaniaRenderer.render(null, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false), ms, buffers, light, OverlayTexture.NO_OVERLAY, null);
		}
	}
}
