/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.mixin;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

// PORT-26.3: the helpers take multi-variants now and the facing dispatches are static fields
@Mixin(BlockModelGenerators.class)
public interface BlockModelGeneratorsAccessor {
	@Invoker("createSlab")
	static BlockModelDefinitionGenerator botania_createSlab(Block block, MultiVariant bottomModel, MultiVariant topModel, MultiVariant doubleModel) {
		throw new IllegalStateException();
	}

	@Invoker("createFenceGate")
	static BlockModelDefinitionGenerator botania_createFenceGate(Block block, MultiVariant openModel, MultiVariant closedModel, MultiVariant openWallModel, MultiVariant closedWallModel, boolean uvLock) {
		throw new IllegalStateException();
	}

	@Invoker("createFence")
	static BlockModelDefinitionGenerator botania_createFence(Block block, MultiVariant postModel, MultiVariant sideModel) {
		throw new IllegalStateException();
	}

	@Invoker("createDoor")
	static BlockModelDefinitionGenerator botania_createDoor(Block block,
			MultiVariant bottomLeftModel, MultiVariant bottomLeftOpenModel,
			MultiVariant bottomRightModel, MultiVariant bottomRightOpenModel,
			MultiVariant topLeftModel, MultiVariant topLeftOpenModel,
			MultiVariant topRightModel, MultiVariant topRightOpenModel) {
		throw new IllegalStateException();
	}

	@Invoker("createTrapdoor")
	static BlockModelDefinitionGenerator botania_createTrapdoor(Block block, MultiVariant topModel, MultiVariant bottomModel, MultiVariant openModel) {
		throw new IllegalStateException();
	}

	@Invoker("createOrientableTrapdoor")
	static BlockModelDefinitionGenerator botania_createOrientableTrapdoor(Block block, MultiVariant topModel, MultiVariant bottomModel, MultiVariant openModel) {
		throw new IllegalStateException();
	}

	@Invoker("createButton")
	static BlockModelDefinitionGenerator botania_createButton(Block block, MultiVariant unpoweredModel, MultiVariant poweredModel) {
		throw new IllegalStateException();
	}

	@Invoker("createPressurePlate")
	static BlockModelDefinitionGenerator botania_createPreasurePlate(Block block, MultiVariant unpoweredModel, MultiVariant poweredModel) {
		throw new IllegalStateException();
	}

	@Invoker("createAxisAlignedPillarBlock")
	static BlockModelDefinitionGenerator botania_createAxisAlignedPillarBlock(Block block, MultiVariant model) {
		throw new IllegalStateException();
	}

	@Accessor("ROTATION_HORIZONTAL_FACING")
	static PropertyDispatch<VariantMutator> botania_getRotationHorizontalFacing() {
		throw new IllegalStateException();
	}

	@Accessor("ROTATION_FACING")
	static PropertyDispatch<VariantMutator> botania_getRotationFacing() {
		throw new IllegalStateException();
	}

	@Invoker("createRotatedVariants")
	static MultiVariant botania_createRotatedVariants(Variant model) {
		throw new IllegalStateException();
	}
}
