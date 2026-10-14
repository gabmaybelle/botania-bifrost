/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import vazkii.botania.network.clientbound.ThundercallerEffectPacket;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.List;
import java.util.function.Predicate;

// PORT-26.3: SwordItem is gone; the sword properties come from Item.Properties#sword in BotaniaItems.
public class ThundercallerItem extends Item {
	public ThundercallerItem(Properties properties) {
		super(properties);
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity attacker) {
		double range = 8;
		IntList alreadyTargetedEntities = new IntArrayList();

		Predicate<Entity> selector = e -> e instanceof LivingEntity && e instanceof Enemy && !(e instanceof Player) && !alreadyTargetedEntities.contains(e.getId());

		LivingEntity prevTarget = entity;
		int hops = entity.level().isThundering() ? 10 : 4;
		int dmg = hops + 1;
		for (int i = 0; i < hops; i++) {
			List<Entity> entities = entity.level().getEntities(prevTarget, new AABB(prevTarget.getX() - range, prevTarget.getY() - range, prevTarget.getZ() - range, prevTarget.getX() + range, prevTarget.getY() + range, prevTarget.getZ() + range), selector);
			if (entities.isEmpty()) {
				break;
			}

			LivingEntity target = (LivingEntity) entities.get(entity.level().getRandom().nextInt(entities.size()));
			if (attacker instanceof Player player) {
				target.hurt(player.damageSources().playerAttack(player), dmg);
			} else {
				target.hurt(attacker.damageSources().mobAttack(attacker), dmg);
			}

			alreadyTargetedEntities.add(target.getId());
			prevTarget = target;
			dmg--;
		}

		if (!alreadyTargetedEntities.isEmpty()) {
			XplatAbstractions.INSTANCE.sendToTracking(attacker,
					new ThundercallerEffectPacket(attacker.getId(), alreadyTargetedEntities));
		}

		super.hurtEnemy(stack, entity, attacker);
	}

}
