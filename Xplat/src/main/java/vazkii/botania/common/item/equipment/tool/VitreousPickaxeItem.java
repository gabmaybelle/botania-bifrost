/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.item.SpecialBlockBreakingHandler;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.item.BotaniaItemTiers;
import vazkii.botania.common.item.equipment.tool.manasteel.ManasteelPickaxeItem;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.List;

public class VitreousPickaxeItem extends ManasteelPickaxeItem implements SpecialBlockBreakingHandler {

	public VitreousPickaxeItem(ToolMaterial material, Properties properties) {
		super(material, properties);
	}

	/**
	 * PORT-26.3: the mining rules that used to override {@code Tier#createToolProperties} on the VITREOUS tier;
	 * BotaniaItems puts this on the item after the standard pickaxe properties.
	 */
	public static Tool createTool() {
		HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
		return new Tool(
				List.of(
						// always correct tool for silktouched blocks, relevant e.g. for copper bulb
						Tool.Rule.minesAndDrops(blocks.getOrThrow(BotaniaTags.Blocks.VITREOUS_PICKAXE_SILKTOUCHED), BotaniaItemTiers.VITREOUS.speed()),
						Tool.Rule.deniesDrops(blocks.getOrThrow(BotaniaItemTiers.VITREOUS.incorrectBlocksForDrops())),
						Tool.Rule.minesAndDrops(blocks.getOrThrow(BotaniaTags.Blocks.MINEABLE_WITH_VITREOUS_PICKAXE), BotaniaItemTiers.VITREOUS.speed())
				),
				1.0F, 1, true
		);
	}

	/*
	* No way to modify the loot context so we're gonna go braindead hack workaround here:
	* - When block starting to break, if the tool doesn't have silktouch already, add it and add a "temp silk touch" flag
	* - Every tick, if the "temp silk touch" flag is present, remove it and remove any silk touch enchants from the stack
	*/
	@Override
	public void onBlockStartBreak(ServerLevel level, ItemStack itemstack, BlockPos pos, Player player) {
		BlockState state = level.getBlockState(pos);
		HolderLookup<Enchantment> enchantmentLookup = level.holderLookup(Registries.ENCHANTMENT);
		boolean hasSilk = EnchantmentHelper.getItemEnchantmentLevel(enchantmentLookup.getOrThrow(Enchantments.SILK_TOUCH), itemstack) > 0;
		if (hasSilk || !isGlass(state)) {
			return;
		}

		itemstack.enchant(enchantmentLookup.getOrThrow(Enchantments.SILK_TOUCH), 1);
		itemstack.set(BotaniaDataComponents.SILK_HACK, Unit.INSTANCE);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity player, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, level, player, slot);

		if (stack.has(BotaniaDataComponents.SILK_HACK)) {
			stack.remove(BotaniaDataComponents.SILK_HACK);
			EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.removeIf(
					enchantmentHolder -> enchantmentHolder.is(Enchantments.SILK_TOUCH)));
		}
	}

	private boolean isGlass(BlockState state) {
		return state.is(BotaniaTags.Blocks.VITREOUS_PICKAXE_SILKTOUCHED);
	}

	@Override
	public int getSortingPriority(ItemStack stack, BlockState state) {
		return isGlass(state) ? Integer.MAX_VALUE : 0;
	}

}
