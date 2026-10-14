/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool.manasteel;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.item.SortableTool;
import vazkii.botania.common.item.equipment.tool.BotaniaMaterialTool;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

public class ManasteelShovelItem extends Item implements BotaniaMaterialTool, SortableTool {
	// PORT-26.3: ShovelItem is gone; the material and the tool behaviour come from the item properties
	// (Item.Properties#shovel), the material is remembered for ToolCommons.
	private final ToolMaterial material;

	public ManasteelShovelItem(ToolMaterial material, Properties properties) {
		super(properties);
		this.material = material;
	}

	@Override
	public ToolMaterial getToolMaterial() {
		return material;
	}

	@Override
	public int getSortingPriority(ItemStack stack, BlockState state) {
		return ToolCommons.getToolPriority(stack);
	}
}
