/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockModelGenerators.class)
public interface BlockModelGeneratorsAccessor {
	@Invoker("createSlab")
	static BlockStateGenerator botania_createSlab(Block block, Identifier bottomModel, Identifier topModel, Identifier doubleModel) {
		throw new IllegalStateException();
	}

	@Invoker("createFenceGate")
	static BlockStateGenerator botania_createFenceGate(Block block, Identifier openModel, Identifier closedModel, Identifier openWallModel, Identifier closedWallModel, boolean uvLock) {
		throw new IllegalStateException();
	}

	@Invoker("createFence")
	static BlockStateGenerator botania_createFence(Block block, Identifier postModel, Identifier sideModel) {
		throw new IllegalStateException();
	}

	@Invoker("createDoor")
	static BlockStateGenerator botania_createDoor(Block block,
			Identifier topLeftModelLocation, Identifier topLeftOpenModelLocation,
			Identifier topRightModelLocation, Identifier topRightOpenModelLocation,
			Identifier bottomLeftModelLocation, Identifier bottomLeftOpenModelLocation,
			Identifier bottomRightModelLocation, Identifier bottomRightOpenModelLocation) {
		throw new IllegalStateException();
	}

	@Invoker("createTrapdoor")
	static BlockStateGenerator botania_createTrapdoor(Block block, Identifier topModelLocation, Identifier bottomModelLocation, Identifier openModelLocation) {
		throw new IllegalStateException();
	}

	@Invoker("createOrientableTrapdoor")
	static BlockStateGenerator botania_createOrientableTrapdoor(Block block, Identifier topModelLocation, Identifier bottomModelLocation, Identifier openModelLocation) {
		throw new IllegalStateException();
	}

	@Invoker("createButton")
	static BlockStateGenerator botania_createButton(Block block, Identifier unpoweredModel, Identifier poweredModel) {
		throw new IllegalStateException();
	}

	@Invoker("createPressurePlate")
	static BlockStateGenerator botania_createPreasurePlate(Block block, Identifier unpoweredModel, Identifier poweredModel) {
		throw new IllegalStateException();
	}

	@Invoker("createAxisAlignedPillarBlock")
	static BlockStateGenerator botania_createAxisAlignedPillarBlock(Block block, Identifier model) {
		throw new IllegalStateException();
	}

	@Invoker("createHorizontalFacingDispatch")
	static PropertyDispatch botania_createHorizontalFacingDispatch() {
		throw new IllegalStateException();
	}

	@Invoker("createFacingDispatch")
	static PropertyDispatch botania_createFacingDispatch() {
		throw new IllegalStateException();
	}

	@Invoker("createRotatedVariants")
	static Variant[] botania_createRotatedVariants(Identifier model) {
		throw new IllegalStateException();
	}
}
