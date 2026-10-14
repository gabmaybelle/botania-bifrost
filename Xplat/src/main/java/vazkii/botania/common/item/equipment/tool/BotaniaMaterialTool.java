/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool;

import net.minecraft.world.item.ToolMaterial;

/**
 * PORT-26.3: {@code DiggerItem#getTier} is gone with {@code DiggerItem} itself, so Botania's tools remember
 * their material themselves; {@link ToolCommons#getToolPriority} reads it from here.
 */
public interface BotaniaMaterialTool {
	ToolMaterial getToolMaterial();
}
