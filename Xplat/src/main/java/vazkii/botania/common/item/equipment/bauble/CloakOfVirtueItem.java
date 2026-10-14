/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.bauble;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.apache.commons.lang3.mutable.MutableFloat;

import vazkii.botania.client.core.helper.AccessoryRenderHelper;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.client.model.CloakModel;
import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.client.render.AccessoryRenderer;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.helper.DataComponentHelper;
import vazkii.botania.common.proxy.Proxy;

public class CloakOfVirtueItem extends BaubleItem {

	private static final Identifier texture = Identifier.parse(ResourcesLib.MODEL_HOLY_CLOAK);
	private static final Identifier textureGlow = Identifier.parse(ResourcesLib.MODEL_HOLY_CLOAK_GLOW);

	public CloakOfVirtueItem(Properties props) {
		super(props);
		Proxy.INSTANCE.runOnClient(() -> () -> AccessoryRenderRegistry.register(this, new Renderer()));
	}

	public float onPlayerDamage(Player player, DamageSource src, float amount) {
		if (!src.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			ItemStack stack = EquipmentHandler.findOrEmpty(this, player);

			if (!stack.isEmpty() && !isInEffect(stack)) {
				CloakOfVirtueItem cloak = (CloakOfVirtueItem) stack.getItem();
				boolean isOnCooldown = player.getCooldowns().isOnCooldown(stack);

				// Used to prevent StackOverflows with mobs that deal damage when damaged
				setInEffect(stack, true);
				MutableFloat mutAmount = new MutableFloat(amount);
				if (!isOnCooldown && cloak.effectOnDamage(src, mutAmount, player, stack)) {
					player.getCooldowns().addCooldown(stack, cloak.getCooldownTime(stack));
				}
				setInEffect(stack, false);
				return mutAmount.getValue();
			}
		}
		return amount;
	}

	public static class Renderer implements AccessoryRenderer {
		private static CloakModel model = null;

		@Override
		public void doRender(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms, SubmitNodeCollector buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
			CloakOfVirtueItem item = ((CloakOfVirtueItem) stack.getItem());
			AccessoryRenderHelper.rotateIfSneaking(ms, living);
			boolean armor = !living.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
			ms.translate(0F, armor ? -0.07F : -0.01F, 0F);

			if (model == null) {
				model = new CloakModel(Minecraft.getInstance().getEntityModels().bakeLayer(BotaniaModelLayers.CLOAK));
			}

			RenderHelper.submit(ms, buffers, model.renderType(item.getCloakTexture()),
					(ps, buffer) -> model.renderToBuffer(ps, buffer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFF));
			RenderHelper.submit(ms, buffers, model.renderType(item.getCloakGlowTexture()),
					(ps, buffer) -> model.renderToBuffer(ps, buffer, 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFF));
		}
	}

	protected boolean effectOnDamage(DamageSource src, MutableFloat amount, Player player, ItemStack stack) {
		if (!src.is(DamageTypeTags.WITCH_RESISTANT_TO)) {
			amount.setValue(0);
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), BotaniaSounds.CLOAK_OF_VIRTUE, SoundSource.PLAYERS, 1F, 1F);
			for (int i = 0; i < 30; i++) {
				double x = player.getX() + Math.random() * player.getBbWidth() * 2 - player.getBbWidth();
				double y = player.getY() + Math.random() * player.getBbHeight();
				double z = player.getZ() + Math.random() * player.getBbWidth() * 2 - player.getBbWidth();
				boolean yellow = Math.random() > 0.5;
				float r = yellow ? 1F : 0.3F;
				float g = yellow ? 1F : 0.3F;
				float b = yellow ? 0.3F : 1F;
				SparkleParticleData data = SparkleParticleData.sparkle(0.8F + (float) Math.random() * 0.4F, r, g, b, 3);
				player.level().addParticle(data, x, y, z, 0, 0, 0);
			}
			return true;
		}

		return false;
	}

	public int getCooldownTime(ItemStack stack) {
		return 200;
	}

	public static boolean isInEffect(ItemStack stack) {
		return stack.has(BotaniaDataComponents.ACTIVE_TRANSIENT);
	}

	public static void setInEffect(ItemStack stack, boolean effect) {
		DataComponentHelper.setFlag(stack, BotaniaDataComponents.ACTIVE_TRANSIENT, effect);
	}

	Identifier getCloakTexture() {
		return texture;
	}

	Identifier getCloakGlowTexture() {
		return textureGlow;
	}
}
