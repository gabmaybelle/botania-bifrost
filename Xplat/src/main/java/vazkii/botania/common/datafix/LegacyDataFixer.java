/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.fixes.References;

import vazkii.botania.api.BotaniaAPI;

import java.util.Set;

/**
 * Brings Botania's own data in old saves up to date.
 * <p>
 * Vanilla's data fixers only know vanilla block entities, entities and item components, so they leave whatever
 * Botania keeps inside its own blocks, entities, item components and entity attachments as it was saved. This runs
 * right after vanilla has upgraded a chunk, an entity chunk, a player or a structure, and runs vanilla's item stack
 * and block state fixes on those places too, from the version the data was saved in.
 */
public final class LegacyDataFixer {
	/** The data types whose upgrades can contain Botania block entities, entities or items. */
	private static final Set<DataFixTypes> TYPES = Set.of(DataFixTypes.CHUNK, DataFixTypes.ENTITY_CHUNK,
			DataFixTypes.PLAYER, DataFixTypes.STRUCTURE);

	/** Fields of Botania block entities and entities that hold one item stack. */
	private static final Set<String> STACK_FIELDS = Set.of(
			"stack", // cacophonium
			"requestTarget", // corporea crystal cube
			"item", // mana enchanter
			"lensStack", // mana burst
			"upgrade", // mana spark
			"fly_stack", // thorn chakram
			"Item" // thrown item (an ItemEntity)
	);
	/** Fields of Botania block entities that hold a list of item stacks. */
	private static final Set<String> STACK_LIST_FIELDS = Set.of(
			"Items", // every SimpleInventoryBlockEntity (vanilla ContainerHelper layout, with a Slot byte per stack)
			"portalStacks", // alfheim portal
			"lastFoods" // gourmaryllis
	);
	/** Fields of Botania block entities that hold a block state. */
	private static final Set<String> BLOCK_STATE_FIELDS = Set.of(
			"camo" // abstruse, spectral and infrangible platforms
	);
	/** Botania item components that hold item stacks. */
	private static final Set<String> STACK_COMPONENTS = Set.of(
			BotaniaAPI.MODID + ":cosmetic_override",
			BotaniaAPI.MODID + ":attached_lens"
	);
	private static final Set<String> STACK_LIST_COMPONENTS = Set.of(
			BotaniaAPI.MODID + ":attached_lenses",
			BotaniaAPI.MODID + ":last_foods"
	);
	/** Botania entity attachments that hold item stacks. */
	private static final String KEPT_ITEMS = BotaniaAPI.MODID + ":kept_items";
	private static final String LOONIUM_DROP = BotaniaAPI.MODID + ":loonium_drop";
	private static final String NEOFORGE_ATTACHMENTS = "neoforge:attachments";
	private static final String FABRIC_ATTACHMENTS = "fabric:attachments";

	private LegacyDataFixer() {}

	/**
	 * Called after vanilla upgraded {@code fixed} of the given type from {@code fromVersion} to {@code toVersion}.
	 */
	public static <T> Dynamic<T> afterVanillaFix(DataFixTypes type, DataFixer fixer, Dynamic<T> fixed, int fromVersion, int toVersion) {
		if (fromVersion >= toVersion || !TYPES.contains(type)) {
			return fixed;
		}
		Tag tag = fixed.getValue() instanceof Tag t ? t : fixed.convert(NbtOps.INSTANCE).getValue();
		Tag result;
		try {
			result = new Pass(fixer, fromVersion, toVersion).walk(tag);
		} catch (RuntimeException e) {
			BotaniaAPI.LOGGER.error("Could not upgrade Botania data saved in version {}", fromVersion, e);
			return fixed;
		}
		if (fixed.getOps() == NbtOps.INSTANCE) {
			@SuppressWarnings("unchecked")
			Dynamic<T> same = (Dynamic<T>) new Dynamic<>(NbtOps.INSTANCE, result);
			return same;
		}
		DynamicOps<T> ops = fixed.getOps();
		return new Dynamic<>(ops, NbtOps.INSTANCE.convertTo(ops, result));
	}

	private record Pass(DataFixer fixer, int fromVersion, int toVersion) {
		Tag walk(Tag tag) {
			if (tag instanceof CompoundTag compound) {
				walkCompound(compound);
			} else if (tag instanceof ListTag list) {
				for (int i = 0; i < list.size(); i++) {
					list.set(i, walk(list.get(i)));
				}
			}
			return tag;
		}

		private void walkCompound(CompoundTag compound) {
			if (isBotaniaObject(compound)) {
				for (String key : STACK_FIELDS) {
					fixField(compound, key, References.ITEM_STACK);
				}
				for (String key : STACK_LIST_FIELDS) {
					fixListField(compound, key, References.ITEM_STACK);
				}
				for (String key : BLOCK_STATE_FIELDS) {
					fixField(compound, key, References.BLOCK_STATE);
				}
			}

			if (compound.get("components") instanceof CompoundTag components) {
				for (String key : STACK_COMPONENTS) {
					fixField(components, key, References.ITEM_STACK);
				}
				for (String key : STACK_LIST_COMPONENTS) {
					fixListField(components, key, References.ITEM_STACK);
				}
			}

			if (compound.get(NEOFORGE_ATTACHMENTS) instanceof CompoundTag attachments) {
				// NeoForge 1.21.1 stored codec attachments bare; 26.3 reads them from a "value" field
				for (String key : Set.copyOf(attachments.keySet())) {
					if (key.startsWith(BotaniaAPI.MODID + ":")) {
						CompoundTag wrapped = new CompoundTag();
						wrapped.put("value", attachments.get(key));
						attachments.put(key, wrapped);
						fixAttachmentItems(wrapped, "value", key);
					}
				}
			}
			if (compound.get(FABRIC_ATTACHMENTS) instanceof CompoundTag attachments) {
				for (String key : attachments.keySet()) {
					fixAttachmentItems(attachments, key, key);
				}
			}

			for (String key : compound.keySet()) {
				walk(compound.get(key));
			}
		}

		private void fixAttachmentItems(CompoundTag holder, String field, String attachment) {
			if (attachment.equals(KEPT_ITEMS)) {
				fixListField(holder, field, References.ITEM_STACK);
			} else if (attachment.equals(LOONIUM_DROP)) {
				fixField(holder, field, References.ITEM_STACK);
			}
		}

		private static boolean isBotaniaObject(CompoundTag compound) {
			// Block entities and entities carry their type in "id"; item stacks do too, but have none of the fields
			return compound.getString("id").map(id -> id.startsWith(BotaniaAPI.MODID + ":")).orElse(false);
		}

		private void fixField(CompoundTag holder, String key, DSL.TypeReference type) {
			Tag value = holder.get(key);
			if (value instanceof CompoundTag) {
				holder.put(key, fix(type, value));
			}
		}

		private void fixListField(CompoundTag holder, String key, DSL.TypeReference type) {
			if (holder.get(key) instanceof ListTag list) {
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i) instanceof CompoundTag) {
						list.set(i, fix(type, list.get(i)));
					}
				}
			}
		}

		private Tag fix(DSL.TypeReference type, Tag value) {
			return fixer.update(type, new Dynamic<>(NbtOps.INSTANCE, value), fromVersion, toVersion).getValue();
		}
	}
}
