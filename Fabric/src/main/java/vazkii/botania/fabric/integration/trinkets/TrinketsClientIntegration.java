/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.integration.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import eu.pb4.trinkets.api.client.TrinketRendererRegistry;

// PORT-26.3: split out of TrinketsIntegration, which servers load too: Trinkets Updated's client classes
// can't be loaded on a dedicated server
public final class TrinketsClientIntegration {
	private TrinketsClientIntegration() {}

	public static void registerRenderer(Item item) {
		TrinketRendererRegistry.registerRenderer(item, new RenderWrapper());
	}

	private static class RenderWrapper implements TrinketRenderer {
		@Override
		public void submit(ItemStack stack, TrinketSlotAccess slotReference, EntityModel<? extends LivingEntityRenderState> contextModel,
				PoseStack ms, SubmitNodeCollector buffers, int light, LivingEntityRenderState state,
				float limbAngle, float limbDistance) {
			// PORT-26.3: renderers get a render state instead of the entity; the accessory renderers still take the
			// entity, which the slot's attachment knows. The render state's age is the entity's tick count plus the
			// partial tick, so the partial tick is the difference.
			LivingEntity living = slotReference.inventory().getAttachment().getEntity();
			if (!(contextModel instanceof HumanoidModel<?> bipedModel)) {
				return;
			}
			render(stack, living, bipedModel, ms, buffers, light, limbAngle, limbDistance, state);
		}

		private static void render(ItemStack stack, LivingEntity living, HumanoidModel<?> bipedModel,
				PoseStack ms, SubmitNodeCollector buffers, int light, float limbAngle, float limbDistance,
				LivingEntityRenderState state) {
			if (!(stack.getItem() instanceof BaubleItem item) || !item.hasRender(stack, living)) {
				return;
			}

			ItemStack cosmetic = item.getCosmeticItem(stack);
			if (!cosmetic.isEmpty()) {
				render(cosmetic, living, bipedModel, ms, buffers, light, limbAngle, limbDistance, state);
			} else {
				var renderer = AccessoryRenderRegistry.get(stack);
				if (renderer != null) {
					float partialTicks = state.ageInTicks - living.tickCount;
					renderer.doRender(bipedModel, stack, living, ms, buffers, light, limbAngle, limbDistance,
							partialTicks, state.ageInTicks, state.yRot, state.xRot);
				}
			}
		}
	}
}
