/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.test.block;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.TagValueInput;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.ForceRelayBlock;
import vazkii.botania.common.block.block_entity.RunicAltarBlockEntity;
import vazkii.botania.common.component.BotaniaDataComponents;
import vazkii.botania.common.component.SingleItem;
import vazkii.botania.common.datafix.LegacySavedData;
import vazkii.botania.common.entity.ThrownItemEntity;
import vazkii.botania.common.internal_caps.LooniumDrop;
import vazkii.botania.common.world.IslandPos;
import vazkii.botania.common.world.SkyblockSavedData;
import vazkii.botania.test.BotaniaGameTest;
import vazkii.botania.test.TestingUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Checks that data saved by Botania for 1.21.1 loads in 26.3. Vanilla's data fixers leave Botania's own block entity,
 * entity, item component and attachment data alone, so items stored there are upgraded by Botania; saved data files
 * moved and are read from their old place.
 */
public class LegacyDataTest {
	/** The data version of Minecraft 1.21.1. */
	private static final int V1_21_1 = 3955;
	private static final String OLD_NAME = "Old Sword";
	private static final BlockPos ALTAR = new BlockPos(1, 1, 1);

	/** A sword with a name and Sharpness V, the way 1.21.1 saved it. */
	private static CompoundTag oldSword() {
		CompoundTag levels = new CompoundTag();
		levels.putInt("minecraft:sharpness", 5);
		CompoundTag enchantments = new CompoundTag();
		enchantments.put("levels", levels);
		enchantments.putBoolean("show_in_tooltip", true);
		CompoundTag components = new CompoundTag();
		components.put("minecraft:enchantments", enchantments);
		components.putString("minecraft:custom_name", "{\"text\":\"" + OLD_NAME + "\"}");
		CompoundTag stack = new CompoundTag();
		stack.putString("id", "minecraft:diamond_sword");
		stack.putInt("count", 1);
		stack.put("components", components);
		return stack;
	}

	private static void assertUpgradedSword(GameTestHelper helper, ItemStack stack, String where) {
		var sharpness = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS);
		TestingUtil.assertThat(!stack.isEmpty(), () -> "The sword in " + where + " was lost");
		TestingUtil.assertEquals(stack.getHoverName().getString(), OLD_NAME, () -> "The sword in " + where + " lost its name");
		TestingUtil.assertEquals(EnchantmentHelper.getItemEnchantmentLevel(sharpness, stack), 5,
				() -> "The sword in " + where + " lost its enchantment");
	}

	private static int currentVersion() {
		return SharedConstants.getCurrentVersion().dataVersion().version();
	}

	@BotaniaGameTest(template = TestingUtil.EMPTY_STRUCTURE)
	public void oldBlockEntityItemsAreUpgraded(GameTestHelper helper) {
		CompoundTag slotted = oldSword();
		slotted.putByte("Slot", (byte) 0);
		ListTag items = new ListTag();
		items.add(slotted);
		CompoundTag altar = new CompoundTag();
		altar.putString("id", "botania:runic_altar");
		altar.putInt("x", 0);
		altar.putInt("y", 64);
		altar.putInt("z", 0);
		altar.put("Items", items);
		ListTag blockEntities = new ListTag();
		blockEntities.add(altar);
		CompoundTag chunk = new CompoundTag();
		chunk.putInt("DataVersion", V1_21_1);
		chunk.putInt("xPos", 0);
		chunk.putInt("zPos", 0);
		chunk.putInt("yPos", -4);
		chunk.putString("Status", "minecraft:full");
		chunk.put("sections", new ListTag());
		chunk.put("block_entities", blockEntities);

		CompoundTag fixed = DataFixTypes.CHUNK.update(DataFixers.getDataFixer(), chunk, V1_21_1, currentVersion());
		CompoundTag fixedAltar = fixed.getListOrEmpty("block_entities").getCompoundOrEmpty(0);

		helper.setBlock(ALTAR, BotaniaBlocks.RUNIC_ALTAR);
		RunicAltarBlockEntity loaded = TestingUtil.assertBlockEntity(helper, ALTAR, RunicAltarBlockEntity.class);
		loaded.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), fixedAltar));
		assertUpgradedSword(helper, loaded.getItemHandler().getItem(0), "the runic altar");
		helper.succeed();
	}

	@BotaniaGameTest(template = TestingUtil.EMPTY_STRUCTURE)
	public void oldEntityItemsAndAttachmentsAreUpgraded(GameTestHelper helper) {
		CompoundTag attachments = new CompoundTag();
		attachments.put("botania:loonium_drop", oldSword());
		CompoundTag entity = new CompoundTag();
		entity.putString("id", "botania:thrown_item");
		ListTag pos = new ListTag();
		BlockPos at = helper.absolutePos(ALTAR);
		pos.add(DoubleTag.valueOf(at.getX() + 0.5));
		pos.add(DoubleTag.valueOf(at.getY()));
		pos.add(DoubleTag.valueOf(at.getZ() + 0.5));
		entity.put("Pos", pos);
		entity.put("Item", oldSword());
		// each loader reads only its own key; 1.21.1 stored both layouts as the bare value
		entity.put("neoforge:attachments", attachments.copy());
		entity.put("fabric:attachments", attachments.copy());
		ListTag entities = new ListTag();
		entities.add(entity);
		CompoundTag chunk = new CompoundTag();
		chunk.putInt("DataVersion", V1_21_1);
		chunk.put("Position", new IntArrayTag(new int[] { 0, 0 }));
		chunk.put("Entities", entities);

		CompoundTag fixed = DataFixTypes.ENTITY_CHUNK.update(DataFixers.getDataFixer(), chunk, V1_21_1, currentVersion());
		CompoundTag fixedEntity = fixed.getListOrEmpty("Entities").getCompoundOrEmpty(0);

		Entity loaded = EntityType.loadEntityRecursive(
				TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), fixedEntity),
				helper.getLevel(), EntitySpawnReason.LOAD, e -> e);
		TestingUtil.assertThat(loaded instanceof ThrownItemEntity, () -> "The thrown item did not load: " + loaded);
		assertUpgradedSword(helper, ((ThrownItemEntity) loaded).getItem(), "the thrown item");
		LooniumDrop drop = LooniumDrop.HOLDER.getOrDefault(loaded, (LooniumDrop) null);
		TestingUtil.assertThat(drop != null, () -> "The thrown item lost its Loonium drop attachment");
		assertUpgradedSword(helper, drop.stack(), "the Loonium drop attachment");
		helper.succeed();
	}

	@BotaniaGameTest(template = TestingUtil.EMPTY_STRUCTURE)
	public void oldItemComponentStacksAreUpgraded(GameTestHelper helper) {
		CompoundTag components = new CompoundTag();
		components.put("botania:cosmetic_override", oldSword());
		CompoundTag pick = new CompoundTag();
		pick.putString("id", "botania:manasteel_pickaxe");
		pick.putInt("count", 1);
		pick.put("components", components);
		pick.putByte("Slot", (byte) 0);
		ListTag inventory = new ListTag();
		inventory.add(pick);
		CompoundTag player = new CompoundTag();
		player.putInt("DataVersion", V1_21_1);
		player.put("Inventory", inventory);

		CompoundTag fixed = DataFixTypes.PLAYER.update(DataFixers.getDataFixer(), player, V1_21_1, currentVersion());
		HolderLookup.Provider registries = helper.getLevel().registryAccess();
		Tag fixedPick = fixed.getListOrEmpty("Inventory").getCompoundOrEmpty(0);
		ItemStack loaded = ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), fixedPick)
				.resultOrPartial().orElse(ItemStack.EMPTY);
		TestingUtil.assertThat(!loaded.isEmpty(), () -> "The pickaxe was lost: " + fixedPick);
		SingleItem cosmetic = loaded.get(BotaniaDataComponents.COSMETIC_OVERRIDE);
		TestingUtil.assertThat(cosmetic != null, () -> "The pickaxe lost its cosmetic override: " + fixedPick);
		assertUpgradedSword(helper, cosmetic.item(), "the cosmetic override");
		helper.succeed();
	}

	@BotaniaGameTest(template = TestingUtil.EMPTY_STRUCTURE)
	public void oldSavedDataFilesAreRead(GameTestHelper helper) throws IOException {
		Path root = helper.getLevel().getServer().getWorldPath(LevelResource.ROOT);
		TestingUtil.assertEquals(LegacySavedData.legacyFile(helper.getLevel(), "PistonRelayPairs"),
				root.resolve("data").resolve("PistonRelayPairs.dat"), () -> "Wrong old place for overworld data");
		var nether = helper.getLevel().getServer().getLevel(Level.NETHER);
		if (nether != null) {
			TestingUtil.assertEquals(LegacySavedData.legacyFile(nether, "PistonRelayPairs"),
					root.resolve("DIM-1").resolve("data").resolve("PistonRelayPairs.dat"), () -> "Wrong old place for nether data");
		}

		Path folder = Files.createTempDirectory(root, "botania-legacy-test");
		try {
			ListTag list = new ListTag();
			list.add(new IntArrayTag(new int[] { 1, 2, 3 }));
			list.add(new IntArrayTag(new int[] { 4, 5, 6 }));
			CompoundTag relays = new CompoundTag();
			relays.put("list", list);
			Path relayFile = writeOld(folder, "PistonRelayPairs", relays);

			ForceRelayBlock.WorldData relayData = LegacySavedData.read(helper.getLevel().getDataStorage(), relayFile,
					ForceRelayBlock.WorldData.TYPE, helper.getLevel());
			TestingUtil.assertThat(relayData != null, () -> "Old force relay pairs were not read");
			TestingUtil.assertEquals(relayData.mapping.get(new BlockPos(1, 2, 3)), new BlockPos(4, 5, 6),
					() -> "Old force relay pair changed");
			TestingUtil.assertThat(!Files.exists(relayFile) && Files.exists(folder.resolve("PistonRelayPairs.dat.migrated")),
					() -> "Old force relay file was not renamed after reading");

			UUID owner = UUID.fromString("12345678-1234-1234-1234-123456789abc");
			CompoundTag island = new CompoundTag();
			island.putInt("IslandX", 7);
			island.putInt("IslandZ", -3);
			island.put("Player", new IntArrayTag(UUIDUtil.uuidToIntArray(owner)));
			ListTag islands = new ListTag();
			islands.add(island);
			CompoundTag skyblock = new CompoundTag();
			skyblock.put("Islands", islands);
			skyblock.putIntArray("SpiralState", new int[] { 1, 0, 0, -1 });
			Path skyblockFile = writeOld(folder, "gog_skyblock_islands", skyblock);

			SkyblockSavedData skyblockData = LegacySavedData.read(helper.getLevel().getDataStorage(), skyblockFile,
					SkyblockSavedData.TYPE, helper.getLevel());
			TestingUtil.assertThat(skyblockData != null, () -> "Old Garden of Glass islands were not read");
			TestingUtil.assertEquals(skyblockData.skyblocks.get(new IslandPos(7, -3)), owner,
					() -> "Old Garden of Glass island owner changed");
		} finally {
			try (var files = Files.list(folder)) {
				for (Path file : files.toList()) {
					Files.delete(file);
				}
			}
			Files.delete(folder);
		}
		helper.succeed();
	}

	private static Path writeOld(Path folder, String name, CompoundTag data) throws IOException {
		CompoundTag file = new CompoundTag();
		file.put("data", data);
		file.putInt("DataVersion", V1_21_1);
		Path path = folder.resolve(name + ".dat");
		NbtIo.writeCompressed(file, path);
		return path;
	}

}
