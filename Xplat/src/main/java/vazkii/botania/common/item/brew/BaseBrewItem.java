/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.brew;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.brew.BrewItem;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.helper.DataComponentHelper;
import vazkii.botania.common.item.CustomCreativeTabContents;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BaseBrewItem extends Item implements BrewItem, CustomCreativeTabContents {

	public static final int DEFAULT_USES_VIAL = 4;
	public static final int DEFAULT_USES_FLASK = 6;

	private final int drinkSpeed;
	private final Supplier<Item> baseItem;

	public BaseBrewItem(int drinkSpeed, Supplier<Item> baseItem, Properties builder) {
		super(builder);
		this.drinkSpeed = drinkSpeed;
		this.baseItem = baseItem;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return drinkSpeed;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.DRINK;
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		return ItemUtils.startUsingInstantly(world, player, hand);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity living) {
		if (!world.isClientSide()) {
			for (MobEffectInstance effect : getBrew(stack).getPotionEffects(stack)) {
				MobEffectInstance newEffect = new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier(), true, true);
				if (effect.getEffect().value().isInstantaneous()) {
					effect.getEffect().value().applyInstantaneousEffect((ServerLevel) world, living, living, living, newEffect.getAmplifier(), 1F);
				} else {
					living.addEffect(newEffect);
				}
			}

			if (world.getRandom().nextBoolean()) {
				world.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1F, 1F);
			}

			int swigs = getSwigsLeft(stack);
			if (living instanceof Player player && !player.hasInfiniteMaterials()) {
				if (swigs <= 1) {
					ItemStack result = getBaseStack();
					if (!player.getInventory().add(result)) {
						return result;
					} else {
						return ItemStack.EMPTY;
					}
				}

				setSwigsLeft(stack, swigs - 1);
			}
		}

		return stack;
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		for (Brew brew : BotaniaAPI.instance().getBrewRegistry()) {
			if (brew == BotaniaBrews.FALLBACK) {
				continue;
			}
			ItemStack stack = new ItemStack(this);
			setBrew(stack, brew);
			output.accept(stack);

		}
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(getDescriptionId(), Component.translatable(getBrew(stack).getTranslationKey(stack)),
				Component.literal(Integer.toString(getSwigsLeft(stack))).withStyle(ChatFormatting.BOLD));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flags) {
		PotionContents.addPotionTooltip(getBrew(stack).getPotionEffects(stack), list, 1, context.tickRate());
	}

	@Override
	public Brew getBrew(ItemStack stack) {
		Identifier id = stack.get(BotaniaDataComponents.BREW);
		return BotaniaAPI.instance().getBrewRegistry().getValue(id);
	}

	public static void setBrew(ItemStack stack, @Nullable Brew brew) {
		Identifier id;
		if (brew != null) {
			id = BotaniaAPI.instance().getBrewRegistry().getKey(brew);
		} else {
			id = Brew.DEFAULT_ID;
		}
		setBrew(stack, id);
	}

	public static void setBrew(ItemStack stack, @Nullable Identifier brew) {
		DataComponentHelper.setOptional(stack, BotaniaDataComponents.BREW, brew);
	}

	public static String getSubtype(ItemStack stack) {
		return Optional.ofNullable(stack.get(BotaniaDataComponents.BREW)).map(Identifier::toString).orElse("none");
	}

	public int getSwigs(ItemStack stack) {
		return stack.getOrDefault(BotaniaDataComponents.MAX_USES, 1);
	}

	public int getSwigsLeft(ItemStack stack) {
		return stack.getOrDefault(BotaniaDataComponents.REMAINING_USES, getSwigs(stack));
	}

	public void setSwigsLeft(ItemStack stack, int swigs) {
		stack.set(BotaniaDataComponents.REMAINING_USES, swigs);
	}

	public ItemStack getBaseStack() {
		return new ItemStack(baseItem.get());
	}
}
