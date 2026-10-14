/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.neoforge.xplat;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.attachment.DataIdBase;
import vazkii.botania.api.capability.BlockApiNoContext;
import vazkii.botania.api.capability.BlockApiWithContext;
import vazkii.botania.api.capability.EntityApiNoContext;
import vazkii.botania.api.capability.EntityApiWithContext;
import vazkii.botania.api.capability.ItemApiNoContext;
import vazkii.botania.api.capability.ItemApiWithContext;
import vazkii.botania.api.corporea.CorporeaRequestMatcher;
import vazkii.botania.api.corporea.CorporeaSpark;
import vazkii.botania.api.mana.*;
import vazkii.botania.api.neoforge.BotaniaNeoForgeCapabilities;
import vazkii.botania.api.neoforge.corporea.CorporeaIndexRequestEvent;
import vazkii.botania.api.neoforge.corporea.CorporeaRequestEvent;
import vazkii.botania.api.neoforge.mana.ManaDiscountEvent;
import vazkii.botania.api.neoforge.mana.ManaItemsEvent;
import vazkii.botania.api.neoforge.mana.ManaNetworkEvent;
import vazkii.botania.api.neoforge.mana.ManaProficiencyEvent;
import vazkii.botania.api.neoforge.recipe.ElvenPortalUpdateEvent;
import vazkii.botania.common.block.block_entity.red_string.RedStringContainerBlockEntity;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.integration.speedrunigt.BotaniaSpeedrunCategories;
import vazkii.botania.neoforge.internal_caps.NeoForgeInternalEntityCapabilities;
import vazkii.botania.neoforge.mixin.AbstractFurnaceBlockEntityNeoForgeAccessor;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class NeoForgeXplatImpl implements XplatAbstractions {

	@Override
	public boolean isNeoForge() {
		return true;
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isDevEnvironment() {
		return !FMLLoader.getCurrent().isProduction();
	}

	@Override
	public boolean isDataGen() {
		return DatagenModLoader.isRunningDataGen();
	}

	@Override
	public boolean isPhysicalClient() {
		return FMLLoader.getCurrent().getDist() == Dist.CLIENT;
	}

	@Override
	public String getBotaniaVersion() {
		return ModList.get().getModContainerById(BotaniaAPI.MODID).orElseThrow()
				.getModInfo().getVersion().toString();
	}

	// capability API lookup helper methods

	@Override
	public @Nullable <A> A findBlockApi(BlockApiNoContext<A> id, Level level, BlockPos pos, @Nullable BlockState state,
			@Nullable BlockEntity entity) {
		return level.getCapability(BotaniaNeoForgeCapabilities.getBlockApiLookupById(id), pos, state, entity);
	}

	@Override
	public @Nullable <A, C> A findBlockApi(BlockApiWithContext<A, C> id, Level level, BlockPos pos,
			@Nullable BlockState state, @Nullable BlockEntity entity, @Nullable C context) {
		return level.getCapability(BotaniaNeoForgeCapabilities.getBlockApiLookupById(id), pos, state, entity, context);
	}

	@Override
	public @Nullable <A> A findEntityApi(EntityApiNoContext<A> id, Entity entity) {
		return entity.getCapability(BotaniaNeoForgeCapabilities.getEntityApiLookupById(id));
	}

	@Override
	public @Nullable <A, C> A findEntityApi(EntityApiWithContext<A, C> id, Entity entity, @Nullable C context) {
		return entity.getCapability(BotaniaNeoForgeCapabilities.getEntityApiLookupById(id), context);
	}

	@Override
	public @Nullable <A> A findItemApi(ItemApiNoContext<A> id, ItemStack stack) {
		return stack.getCapability(BotaniaNeoForgeCapabilities.getItemApiLookupById(id));
	}

	@Override
	public @Nullable <A, C> A findItemApi(ItemApiWithContext<A, C> id, ItemStack stack, @Nullable C context) {
		return stack.getCapability(BotaniaNeoForgeCapabilities.getItemApiLookupById(id), context);
	}

	// data attachment helper methods

	@Nullable
	@Override
	public <T> T getEntityData(DataIdBase<T> id, Entity entity) {
		return entity.getExistingDataOrNull(NeoForgeInternalEntityCapabilities.getAttachmentTypeById(id));
	}

	@Override
	public <T> void setEntityData(DataIdBase<T> id, Entity entity, T data) {
		entity.setData(NeoForgeInternalEntityCapabilities.getAttachmentTypeById(id), data);
	}

	@Override
	public void removeEntityData(DataIdBase<?> id, Entity entity) {
		entity.removeData(NeoForgeInternalEntityCapabilities.getAttachmentTypeById(id));
	}

	// PORT-26.3: NeoForge's transfer API. A fluid-holding item is a ResourceHandler<FluidResource> reached through an
	// ItemAccess (which also updates the stack or the player's hand), and every change happens inside a Transaction.
	@Override
	public boolean isFluidContainer(ItemEntity item) {
		return item.getItem().getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(item.getItem())) != null;
	}

	@Override
	public boolean extractFluidFromItemEntity(ItemEntity item, Fluid fluid) {
		ItemAccess access = ItemAccess.forStack(item.getItem());
		ResourceHandler<FluidResource> handler = item.getItem().getCapability(Capabilities.Fluid.ITEM, access);
		if (handler == null) {
			return false;
		}
		try (Transaction transaction = Transaction.openRoot()) {
			if (handler.extract(FluidResource.of(fluid), FluidType.BUCKET_VOLUME, transaction) == FluidType.BUCKET_VOLUME) {
				transaction.commit();
				item.setItem(access.getResource().toStack(access.getAmount()));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean extractFluidFromPlayerItem(Player player, InteractionHand hand, Fluid fluid) {
		// the player access leaves a creative player's hand alone by itself
		ItemAccess access = ItemAccess.forPlayerInteraction(player, hand);
		ResourceHandler<FluidResource> handler = player.getItemInHand(hand).getCapability(Capabilities.Fluid.ITEM, access);
		if (handler == null) {
			return false;
		}
		try (Transaction transaction = Transaction.openRoot()) {
			if (handler.extract(FluidResource.of(fluid), FluidType.BUCKET_VOLUME, transaction) == FluidType.BUCKET_VOLUME) {
				transaction.commit();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean insertFluidIntoPlayerItem(Player player, InteractionHand hand, Fluid fluid) {
		var stack = player.getItemInHand(hand);
		if (stack.isEmpty()) {
			return false;
		}
		ItemAccess access = ItemAccess.forPlayerInteraction(player, hand);
		ResourceHandler<FluidResource> handler = stack.getCapability(Capabilities.Fluid.ITEM, access);
		if (handler == null) {
			return false;
		}
		try (Transaction transaction = Transaction.openRoot()) {
			if (handler.insert(FluidResource.of(fluid), FluidType.BUCKET_VOLUME, transaction) == FluidType.BUCKET_VOLUME) {
				transaction.commit();
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack fillItemWithWater(ItemStack stackToFill, Player player) {
		ItemStack split = stackToFill.copyWithCount(1);
		ItemAccess access = ItemAccess.forStack(split);
		ResourceHandler<FluidResource> handler = split.getCapability(Capabilities.Fluid.ITEM, access);
		if (handler != null) {
			try (Transaction transaction = Transaction.openRoot()) {
				if (handler.insert(FluidResource.of(Fluids.WATER), FluidType.BUCKET_VOLUME, transaction) > 0) {
					transaction.commit();
					return access.getResource().toStack(access.getAmount());
				}
			}
		} else if (split.is(Items.GLASS_BOTTLE)) {
			return PotionContents.createItemStack(Items.POTION, Potions.WATER);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean hasInventory(Level level, BlockPos pos, Direction sideOfPos) {
		return level.getCapability(Capabilities.Item.BLOCK, pos, sideOfPos) != null;
	}

	@Override
	public ItemStack insertToInventory(Level level, BlockPos pos, Direction sideOfPos, ItemStack toInsert, boolean simulate) {
		BlockState state = level.getBlockState(pos);
		ResourceHandler<ItemResource> itemHandler = level.getCapability(Capabilities.Item.BLOCK, pos, state, null, sideOfPos);
		if (itemHandler == null) {
			return toInsert;
		}

		// can't do incremental simulations
		if (simulate || !state.is(BotaniaTags.Blocks.SINGLE_ITEM_INSERT)) {
			return ItemUtil.insertItemReturnRemaining(itemHandler, toInsert, simulate, null);
		}

		int maxInserts = toInsert.getCount();
		for (int i = 0; i < maxInserts; i++) {
			ItemStack single = toInsert.copyWithCount(1);
			if (!ItemUtil.insertItemReturnRemaining(itemHandler, single, false, null).isEmpty()) {
				break;
			}
			toInsert.setCount(toInsert.getCount() - 1);
		}
		return toInsert;
	}

	@Override
	public boolean fireCorporeaRequestEvent(CorporeaRequestMatcher matcher, int itemCount, CorporeaSpark spark, boolean dryRun) {
		return NeoForge.EVENT_BUS.post(new CorporeaRequestEvent(matcher, itemCount, spark, dryRun)).isCanceled();
	}

	@Override
	public boolean fireCorporeaIndexRequestEvent(ServerPlayer player, CorporeaRequestMatcher request, int count, CorporeaSpark spark) {
		return NeoForge.EVENT_BUS.post(new CorporeaIndexRequestEvent(player, request, count, spark)).isCanceled();
	}

	@Override
	public void fireManaItemEvent(Player player, List<ItemStack> toReturn) {
		NeoForge.EVENT_BUS.post(new ManaItemsEvent(player, toReturn));
	}

	@Override
	public float fireManaDiscountEvent(Player player, float discount, ItemStack tool) {
		var evt = new ManaDiscountEvent(player, discount, tool);
		NeoForge.EVENT_BUS.post(evt);
		return evt.getDiscount();
	}

	@Override
	public boolean fireManaProficiencyEvent(Player player, ItemStack tool, boolean proficient) {
		var evt = new ManaProficiencyEvent(player, tool, proficient);
		NeoForge.EVENT_BUS.post(evt);
		return evt.isProficient();
	}

	@Override
	public void fireElvenPortalUpdateEvent(BlockEntity portal, AABB bounds, boolean open, List<ItemStack> stacksInside) {
		NeoForge.EVENT_BUS.post(new ElvenPortalUpdateEvent(portal, bounds, open, stacksInside));
	}

	@Override
	public void fireManaNetworkEvent(ManaReceiver thing, ManaBlockType type, ManaNetworkAction action) {
		NeoForge.EVENT_BUS.post(new ManaNetworkEvent(thing, type, action));
	}

	@Override
	public void sendToPlayer(@Nullable Player player, CustomPacketPayload packet) {
		if (player instanceof ServerPlayer serverPlayer && !player.level().isClientSide()) {
			PacketDistributor.sendToPlayer(serverPlayer, packet);
		}
	}

	@Override
	public void sendToNear(Level level, BlockPos pos, CustomPacketPayload packet) {
		if (!level.isClientSide()) {
			PacketDistributor.sendToPlayersNear((ServerLevel) level, null, pos.getX(), pos.getY(), pos.getZ(), 64, packet);
		}
	}

	@Override
	public void sendToTracking(Entity e, CustomPacketPayload packet) {
		if (!e.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(e, packet);
		}
	}

	@Override
	public void registerReloadListener(PackType type, Identifier id, PreparableReloadListener listener) {
		// PORT-26.3: the reload listener events are keyed by id and live on the mod bus now
		var modBus = ModList.get().getModContainerById(BotaniaAPI.MODID).orElseThrow().getEventBus();
		switch (type) {
			case CLIENT_RESOURCES -> modBus.addListener(
					(AddClientReloadListenersEvent e) -> e.addListener(id, listener));
			case SERVER_DATA -> modBus.addListener(
					(AddServerReloadListenersEvent e) -> e.addListener(id, listener));
		}
	}

	@Override
	public Item.Properties noRepairOnNeoForge(Item.Properties builder) {
		// PORT-26.3: repairability is the REPAIRABLE data component; a builder without it is not repairable, so
		// there is nothing to switch off any more
		return builder;
	}

	@Override
	public <T extends AbstractContainerMenu, D> MenuType<T> createMenuType(TriFunction<Integer, Inventory, D, T> constructor, StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec) {
		return IMenuTypeExtension.create((windowId, inv, data) -> constructor.apply(windowId, inv, streamCodec.decode(data)));
	}

	@Nullable
	@Override
	public EquipmentHandler tryCreateEquipmentHandler() {
		// PORT-26.3: disabled. Curios has no 26.3 release yet; it comes back in Phase 5
		// (src/disabled/integration/curios). Until then accessories use the plain inventory handler.
		return null;
	}

	@Override
	public <D> void openMenu(ServerPlayer player, MenuProvider menu, D initialData, StreamCodec<? super RegistryFriendlyByteBuf, D> streamCodec) {
		player.openMenu(menu, buf -> streamCodec.encode(buf, initialData));
	}

	@Override
	public boolean canFurnaceBurn(AbstractFurnaceBlockEntity furnace, @Nullable RecipeHolder<?> recipeHolder, NonNullList<ItemStack> items, int maxStackSize) {
		return AbstractFurnaceBlockEntityNeoForgeAccessor.botania_canBurn(furnace.getLevel().registryAccess(), recipeHolder, items, maxStackSize, furnace);
	}

	@Override
	public Fluid getBucketFluid(BucketItem item) {
		return item.content;
	}

	@Override
	public int getSmeltingBurnTime(Level level, ItemStack stack) {
		// PORT-26.3: fuel is the COOKING_FUEL data component. A data-driven burn time needs a loot context to resolve,
		// built the same way AbstractFurnaceBlockEntity builds its own.
		CookingFuel fuel = stack.get(DataComponents.COOKING_FUEL);
		if (fuel == null) {
			return 0;
		}
		if (fuel.burnTime() instanceof ResolvableInt.Constant constant) {
			return constant.value();
		}
		if (!(level instanceof ServerLevel serverLevel)) {
			return 0;
		}
		LootContext context = new LootContext.Builder(new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY))
				.create(Optional.empty());
		return fuel.burnTime().get(context, 0);
	}

	@Override
	public boolean preventsRemoteMovement(ItemEntity entity) {
		return entity.getPersistentData().getBooleanOr("PreventRemoteMovement", false);
	}

	@Override
	public int transferEnergyToNeighbors(Level level, BlockPos pos, int energy) {
		for (Direction e : Direction.values()) {
			BlockPos neighbor = pos.relative(e);
			if (!level.hasChunkAt(neighbor)) {
				continue;
			}

			EnergyHandler storage = level.getCapability(Capabilities.Energy.BLOCK, pos, e.getOpposite());

			if (storage == null) {
				storage = level.getCapability(Capabilities.Energy.BLOCK, pos, null);
			}

			if (storage != null) {
				try (Transaction transaction = Transaction.openRoot()) {
					energy -= storage.insert(energy, transaction);
					transaction.commit();
				}

				if (energy <= 0) {
					return 0;
				}
			}
		}
		return energy;
	}

	@Nullable
	@Override
	public FoodProperties getFoodProperties(ItemStack stack) {
		// PORT-26.3: NeoForge's entity-aware food hook is gone; the component is the food
		return stack.get(DataComponents.FOOD);
	}

	@Override
	public boolean canToolLightFire(ItemStack stack) {
		return stack.is(Items.FLINT_AND_STEEL) || stack.getItem().canPerformAction(stack, ItemAbilities.FIRESTARTER_LIGHT);
	}

	@Override
	public RailShape getRailDirection(BlockState state, BlockGetter level, BlockPos pos, @Nullable AbstractMinecart cart) {
		return state.getBlock() instanceof BaseRailBlock block
				? block.getRailDirection(state, level, pos, null)
				: RailShape.NORTH_SOUTH;
	}

	@Override
	public ItemStack getCraftingRemainingItem(ItemStack inputStack) {
		ItemStackTemplate remainder = inputStack.getItem().getCraftingRemainder(inputStack);
		return remainder == null ? ItemStack.EMPTY : remainder.create();
	}

	@Override
	public boolean requiresCustomTesting(Ingredient ingredient) {
		return !ingredient.isSimple();
	}

	@Override
	public boolean matchesWithCustomTestIngredients(List<ItemStack> nonEmptyStacks,
			List<Ingredient> ingredients) {
		return RecipeMatcher.findMatches(nonEmptyStacks, ingredients) != null;
	}

	@Override
	public boolean isRedStringContainerTarget(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		BlockEntity be = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
		for (Direction dir : Direction.values()) {
			if (level.getCapability(Capabilities.Item.BLOCK, pos, state, be, dir) != null) {
				return true;
			}
		}
		return level.getCapability(Capabilities.Item.BLOCK, pos, state, be, null) != null;
	}

	@Override
	public RedStringContainerBlockEntity newRedStringContainer(BlockPos pos, BlockState state) {
		return new RedStringContainerBlockEntity(pos, state);
	}

	@Override
	public BlockSetType registerBlockSetType(
			String name,
			boolean canOpenByHand,
			boolean canOpenByWindCharge,
			boolean canButtonBeActivatedByArrows,
			BlockSetType.PressurePlateSensitivity pressurePlateSensitivity,
			SoundType soundType,
			SoundEvent doorClose,
			SoundEvent doorOpen,
			SoundEvent trapdoorClose,
			SoundEvent trapdoorOpen,
			SoundEvent pressurePlateClickOff,
			SoundEvent pressurePlateClickOn,
			SoundEvent buttonClickOff,
			SoundEvent buttonClickOn) {
		return BlockSetType.register(new BlockSetType(BotaniaAPI.MODID + ":" + name,
				canOpenByHand,
				canOpenByWindCharge,
				canButtonBeActivatedByArrows,
				pressurePlateSensitivity,
				soundType,
				doorClose,
				doorOpen,
				trapdoorClose,
				trapdoorOpen,
				pressurePlateClickOff,
				pressurePlateClickOn,
				buttonClickOff,
				buttonClickOn));
	}

	@Override
	public WoodType registerWoodType(String name, BlockSetType setType, SoundType soundType, SoundType hangingSignSoundType, SoundEvent fenceGateClose, SoundEvent fenceGateOpen) {
		return WoodType.register(new WoodType(BotaniaAPI.MODID + ":" + name, setType, soundType, hangingSignSoundType, fenceGateClose, fenceGateOpen));
	}

	@Override
	public RecipeOutput createRecipeOutput(RecipeAcceptor recipeAcceptor, Supplier<Advancement.Builder> advancementBuilder) {
		return new RecipeOutput() {
			@Override
			public Advancement.Builder advancement() {
				return advancementBuilder.get();
			}

			@Override
			public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
				if (conditions.length > 0) {
					throw new UnsupportedOperationException("Conditions are not supported");
				}
				recipeAcceptor.accept(id.identifier(), recipe, advancement);
			}

			@Override
			public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
				// PORT-26.3: RecipeOutput hands out registry lookups now; the parked recipe datagen is the only caller
				// and has to supply them when it comes back (Phase 1, step 4).
				throw new UnsupportedOperationException("Registry lookups are not available here yet");
			}

			@Override
			public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
				throw new UnsupportedOperationException("Registry lookups are not available here yet");
			}
		};
	}

	@Override
	public boolean shouldShowExtendedItemTooltip(TooltipFlag flags) {
		return flags.hasShiftDown();
	}

	@Override
	public boolean isRunningCategory(BotaniaSpeedrunCategories.BotaniaSpeedrunCategory category) {
		return false;
	}

	@Override
	public void completeSpeedrunTimer() {
		throw new IllegalStateException("Attempted to complete a speedruntimer when speedrunIGT is not installed.");
	}
}
