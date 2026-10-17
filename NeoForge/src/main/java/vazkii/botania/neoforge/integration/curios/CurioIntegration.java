/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.neoforge.integration.curios;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.CuriosResources;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.ResoluteIvyItem;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.proxy.Proxy;

import java.util.function.Predicate;

// PORT-26.3: Curios 17 hands out the equipped items as a ResourceHandler, moved DropRule to api.common, and
// renders from a render state instead of the entity.
public class CurioIntegration extends EquipmentHandler {
	public static void init() {
		NeoForge.EVENT_BUS.addListener(CurioIntegration::keepCurioDrops);
	}

	public static void keepCurioDrops(DropRulesEvent event) { //TODO make this less hacky
		event.addOverride(stack -> {
			if (ResoluteIvyItem.hasIvy(stack)) {
				stack.remove(BotaniaDataComponents.RESOLUTE_IVY);
				return true;
			}
			return false;
		}, DropRule.ALWAYS_KEEP);
	}

	@Override
	protected Container getAllWornItems(LivingEntity living) {
		return CuriosApi.getCuriosInventory(living)
				.map(handler -> new SimpleContainer(handler.findCurios(stack -> !stack.isEmpty()).stream()
						.map(SlotResult::stack)
						.toArray(ItemStack[]::new)))
				.orElseGet(() -> new SimpleContainer(0));
	}

	@Override
	protected ItemStack findItem(Item item, LivingEntity living) {
		return CuriosApi.getCuriosInventory(living)
				.map(i -> i.findFirstCurio(item).map(SlotResult::stack).orElse(ItemStack.EMPTY))
				.orElse(ItemStack.EMPTY);
	}

	@Override
	protected ItemStack findItem(Predicate<ItemStack> pred, LivingEntity living) {
		return CuriosApi.getCuriosInventory(living)
				.map(i -> i.findFirstCurio(pred).map(SlotResult::stack).orElse(ItemStack.EMPTY))
				.orElse(ItemStack.EMPTY);
	}

	/**
	 * Game tests: puts a stack into the first ring slot, the way Curios' right-click equip does for a server player.
	 * A mock player never joins the level, so its slots are set up here first.
	 */
	public static void equipRingForGameTest(Player player, ItemStack stack) {
		CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
			if (handler.getCurios().isEmpty()) {
				handler.reset();
			}
			handler.setEquippedCurio("ring", 0, stack);
		});
	}

	/** Game tests: the player's block reach plus what Curios' slot tick would add for a ring in the first ring slot. */
	public static double ringReachForGameTest(Player player, ItemStack stack) {
		double[] bonus = { player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) };
		ICurioItem.forEachModifier(stack, new SlotContext("ring", player, 0, false, true), (attribute, modifier) -> {
			if (attribute.is(Attributes.BLOCK_INTERACTION_RANGE)) {
				bonus[0] += modifier.amount();
			}
		});
		return bonus[0];
	}

	public void initCapability(RegisterCapabilitiesEvent event, Item... items) {
		event.registerItem(CuriosCapability.ITEM, (stack, context) -> new Wrapper(stack), items);
	}

	@Override
	public void onInit(Item item) {
		Proxy.INSTANCE.runOnClient(() -> () -> ICurioRenderer.register(item, () -> Renderer.INSTANCE));
	}

	@Override
	public boolean isAccessory(ItemStack stack) {
		return super.isAccessory(stack) || stack.getCapability(CuriosCapability.ITEM) != null;
	}

	public static class Wrapper implements ICurio {
		private static final SoundInfo SOUND_INFO = new SoundInfo(BotaniaSounds.EQUIP_BAUBLE, 0.1F, 1.3F);

		private final ItemStack stack;

		Wrapper(ItemStack stack) {
			this.stack = stack;
		}

		private BaubleItem getItem() {
			return (BaubleItem) stack.getItem();
		}

		@Override
		public ItemStack getStack() {
			return stack;
		}

		@Override
		public void curioTick(SlotContext slotContext) {
			getItem().onWornTick(stack, slotContext.entity());
		}

		@Override
		public void onEquip(SlotContext slotContext, ItemStack previousStack) {
			getItem().onEquipped(stack, slotContext.entity());
		}

		@Override
		public void onUnequip(SlotContext slotContext, ItemStack newStack) {
			getItem().onUnequipped(stack, slotContext.entity());
		}

		@Override
		public boolean canEquip(SlotContext slotContext) {
			return getItem().canEquip(stack, slotContext.entity());
		}

		// PORT-26.3: Curios 17 builds an item's slot modifiers once, here, for every slot the item fits. Its default
		// looks those slots up in the client's slot list, which a dedicated server never fills, so worn trinkets gave no
		// attribute bonuses there; this is the same loop using the server's list whenever a server is running.
		@Override
		public CurioAttributeModifiers getDefaultCurioAttributeModifiers() {
			boolean isClient = ServerLifecycleHooks.getCurrentServer() == null;
			CurioAttributeModifiers.Builder builder = CurioAttributeModifiers.builder();
			for (String slot : CuriosSlotTypes.getItemSlotTypes(stack, isClient).keySet()) {
				getItem().getEquippedAttributeModifiers(stack, CuriosResources.resource(slot))
						.forEach((attribute, modifier) -> builder.addModifier(attribute, modifier, slot));
			}
			return builder.build();
		}

		@Override
		public boolean canSync(SlotContext slotContext) {
			return true;
		}

		@Override
		public SoundInfo getEquipSound(SlotContext slotContext) {
			return SOUND_INFO;
		}

		@Override
		public boolean canEquipFromUse(SlotContext slotContext) {
			return true;
		}
	}

	private static class Renderer implements ICurioRenderer {
		private static final Renderer INSTANCE = new Renderer();

		// PORT-26.3: renderers get a render state instead of the entity; the accessory renderers still take the
		// entity, which the slot context knows. The render state's age is the entity's tick count plus the partial
		// tick, so the partial tick is the difference.
		@Override
		public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack,
				SlotContext slotContext, PoseStack matrixStack, SubmitNodeCollector submitNodeCollector,
				int light, S renderState, RenderLayerParent<S, M> renderLayerParent,
				EntityRendererProvider.Context context, float yRotation, float xRotation) {
			LivingEntity livingEntity = slotContext.entity();
			M contextModel = renderLayerParent.getModel();

			if (!(contextModel instanceof HumanoidModel<?> bipedModel)) {
				return;
			}

			matrixStack.pushPose();
			render(stack, livingEntity, bipedModel, matrixStack, submitNodeCollector, light, renderState, yRotation, xRotation);
			matrixStack.popPose();
		}

		private static void render(ItemStack stack, LivingEntity livingEntity, HumanoidModel<?> bipedModel,
				PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, int light,
				LivingEntityRenderState renderState, float yRotation, float xRotation) {
			if (!(stack.getItem() instanceof BaubleItem item) || !item.hasRender(stack, livingEntity)) {
				return;
			}

			ItemStack cosmetic = item.getCosmeticItem(stack);
			if (!cosmetic.isEmpty()) {
				render(cosmetic, livingEntity, bipedModel, matrixStack, submitNodeCollector, light, renderState, yRotation, xRotation);
			} else {
				var renderer = AccessoryRenderRegistry.get(stack);
				if (renderer != null) {
					float partialTicks = renderState.ageInTicks - livingEntity.tickCount;
					renderer.doRender(bipedModel, stack, livingEntity, matrixStack, submitNodeCollector, light,
							renderState.walkAnimationPos, renderState.walkAnimationSpeed, partialTicks,
							renderState.ageInTicks, yRotation, xRotation);
				}
			}
		}
	}

}
