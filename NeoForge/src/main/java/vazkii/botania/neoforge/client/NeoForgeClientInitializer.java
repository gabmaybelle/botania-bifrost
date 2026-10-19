/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.Model;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.mana.ManaBarTooltip;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.neoforge.BotaniaNeoForgeCapabilities;
import vazkii.botania.client.BotaniaClientCapabilities;
import vazkii.botania.client.BotaniaItemProperties;
import vazkii.botania.client.core.handler.*;
import vazkii.botania.client.core.helper.CoreShaders;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.fx.BotaniaParticles;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.client.gui.ManaBarTooltipComponent;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.client.gui.bag.ColoredContentsPouchScreen;
import vazkii.botania.client.gui.box.TrinketCaseScreen;
import vazkii.botania.client.gui.enderhand.HandOfEnderScreen;
import vazkii.botania.client.integration.iris.IrisIntegration;
import vazkii.botania.client.model.BotaniaLayerDefinitions;
import vazkii.botania.client.model.armor.ArmorModels;
import vazkii.botania.client.render.ColorHandler;
import vazkii.botania.client.render.entity.EntityRenderers;
import vazkii.botania.client.render.item.BotaniaSpecialModelRenderers;
import vazkii.botania.common.block.PlatformBlock;
import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;
import vazkii.botania.common.block.flower.FloatingFlowerBaseBlock;
import vazkii.botania.common.crafting.SyncedRecipes;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.armor.manasteel.ManasteelArmorItem;
import vazkii.botania.common.item.equipment.bauble.RingOfDexterousMotionItem;
import vazkii.botania.xplat.ClientXplatAbstractions;
import vazkii.botania.xplat.XplatAbstractions;
import vazkii.patchouli.api.BookDrawScreenEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

@EventBusSubscriber(modid = BotaniaAPI.MODID, value = Dist.CLIENT)
public class NeoForgeClientInitializer {
	@SubscribeEvent
	public static void registerGuiOverlays(RegisterGuiLayersEvent e) {
		e.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL, botaniaRL("hud_early"), HUDHandler::onDrawExperienceBarPost);
		e.registerAbove(VanillaGuiLayers.AIR_LEVEL, botaniaRL("hud_late"), HUDHandler::onDrawAirLevelPost);
	}

	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent evt) {
		// ensure API implementations are loaded
		BotaniaAPI.LOGGER.debug("Client API instances: {}",
				List.of(BotaniaAPIClient.instance(), ClientXplatAbstractions.instance()));

		// PORT-26.3: block render layers are read off the textures since 26.1, BlockRenderLayers is gone
		if (XplatAbstractions.INSTANCE.isModLoaded("iris")) {
			IrisIntegration.init();
		}

		// Events
		var bus = NeoForge.EVENT_BUS;
		// GUIs
		bus.addListener((BookDrawScreenEvent e) -> KonamiHandler.renderBook(e.getBook(), e.getScreen(), e.getMouseX(), e.getMouseY(), e.getPartialTicks(), e.getGraphics()));
		bus.addListener((ClientTickEvent.Post e) -> {
			ClientTickHandler.clientTickEnd(Minecraft.getInstance());
			KonamiHandler.clientTick(Minecraft.getInstance());
		});
		bus.addListener((ItemTooltipEvent e) -> TooltipHandler.onTooltipEvent(e.getItemStack(), e.getContext(), e.getFlags(), e.getToolTip()));
		bus.addListener((ScreenEvent.KeyPressed.Post e) -> CorporeaInputHandler.buttonPressed(e.getKeyEvent()));

		// NeoForge bus events done with Mixins on Fabric
		bus.addListener(EventPriority.HIGH, (ClientChatEvent e) -> {
			var player = Minecraft.getInstance().player;
			if (player != null && CorporeaIndexBlockEntity.ClientHandler.onChat(player, e.getMessage())) {
				e.setCanceled(true);
			}
		});
		// PORT-26.3: the recipe types asked for in OnDatapackSyncEvent arrive here (the ingredient cache is cleared
		// by the store)
		bus.addListener((RecipesReceivedEvent e) -> SyncedRecipes.replace(e.getRecipeTypes(), e.getRecipeMap().values()));
		bus.addListener((CustomizeGuiOverlayEvent.BossEventProgress e) -> {
			var result = BossBarHandler.onBarRender(e.getGuiGraphics(), e.getX(), e.getY(),
					e.getBossEvent(), true);
			result.ifPresent(increment -> {
				e.setCanceled(true);
				e.setIncrement(increment);
			});
		});
		bus.addListener((InputEvent.Key e) -> {
			RingOfDexterousMotionItem.ClientLogic.onKeyDown();
			KonamiHandler.handleInput(e.getKey(), e.getAction(), e.getKeyEvent().modifiers());
		});
		bus.addListener(EventPriority.LOWEST, (RenderTooltipEvent.Pre e) -> {
			var manaItem = ManaItem.LOOKUP.find(e.getItemStack());
			if (manaItem == null) {
				return;
			}
			// NeoForge does not pass the tooltip width to any tooltip event.
			// To avoid a mixin here, we just duplicate the width checking part.
			int width = 0;
			ManaBarTooltipComponent manaBar = null;
			for (ClientTooltipComponent component : e.getComponents()) {
				width = Math.max(width, component.getWidth(e.getFont()));
				if (component instanceof ManaBarTooltipComponent c) {
					manaBar = c;
				}
			}
			if (manaBar != null) {
				manaBar.setContext(e.getX(), e.getY(), width);
			}
		});

		// PORT-26.3: disabled. Ears isn't checked for 26.3 yet (Xplat/src/disabled/integration/ears).
	}

	@SubscribeEvent
	private static void registerMenuScreens(RegisterMenuScreensEvent e) {
		e.register(BotaniaItems.COLORED_CONTENTS_POUCH_MENU_TYPE, ColoredContentsPouchScreen::new);
		e.register(BotaniaItems.TRINKET_CASE_MENU_TYPE, TrinketCaseScreen::new);
		e.register(BotaniaItems.HAND_OF_ENDER_MENU_TYPE, HandOfEnderScreen::new);
	}

	@SubscribeEvent
	public static void registerTooltipComponent(RegisterClientTooltipComponentFactoriesEvent e) {
		e.register(ManaBarTooltip.class, ManaBarTooltipComponent::new);
	}

	@SubscribeEvent
	public static void registerKeys(RegisterKeyMappingsEvent e) {
		ClientProxy.initKeybindings(e::register);
	}

	@SubscribeEvent
	public static void registerDebugEntries(RegisterDebugEntriesEvent e) {
		// PORT-26.3: the F3 screen is built from registered entries; DebugHandler still checks the config flag itself
		DebugScreenEntry entry = (displayer, level, clientChunk, serverChunk) -> {
			List<String> lines = new ArrayList<>();
			DebugHandler.onDrawDebugText(lines);
			lines.forEach(displayer::addLine);
		};
		Identifier id = botaniaRL("debug");
		e.register(id, entry);
		e.includeInProfile(id, DebugScreenProfile.DEFAULT, DebugScreenEntryStatus.IN_OVERLAY);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	private static void createClientCapabilities(RegisterCapabilitiesEvent e) {
		BotaniaClientCapabilities.registerCapabilityTypes(BotaniaNeoForgeCapabilities.getRegistration());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	private static void attachClientFallbackCapabilities(RegisterCapabilitiesEvent e) {
		BotaniaClientCapabilities.registerCapabilityFallbackProviders(BotaniaNeoForgeCapabilities.getProviderRegistration(e));
	}

	@SubscribeEvent
	private static void attachClientCapabilities(RegisterCapabilitiesEvent e) {
		BotaniaClientCapabilities.registerCapabilityProviders(BotaniaNeoForgeCapabilities.getProviderRegistration(e));
	}

	// PORT-26.3: model loaders read the "loader" key of a model file; resource models are standalone models
	// (NeoForgeStandaloneModels); the floating flower in-world models and the platform camouflage are swapped into the
	// baking result; the mana blaster loader is gone (special renderer plus the "has_clip" item model test)
	@SubscribeEvent
	public static void registerModelLoader(ModelEvent.RegisterLoaders evt) {
		evt.register(ClientXplatAbstractions.FLOATING_FLOWER_MODEL_LOADER_ID, NeoForgeFloatingFlowerModel.LOADER);
	}

	@SubscribeEvent
	public static void onModelRegister(ModelEvent.RegisterStandalone evt) {
		var resourceManager = Minecraft.getInstance().getResourceManager();
		MiscellaneousModels.INSTANCE.onModelRegister(resourceManager, id -> NeoForgeStandaloneModels.register(evt, id));
		for (Block block : BuiltInRegistries.BLOCK) {
			if (block instanceof FloatingFlowerBaseBlock) {
				evt.register(NeoForgeFloatingFlowerModel.key(block), NeoForgeFloatingFlowerModel.Standalone.forBlock(block));
			}
		}
	}

	@SubscribeEvent
	public static void onModelBake(ModelEvent.ModifyBakingResult evt) {
		Map<BlockState, BlockStateModel> models = evt.getBakingResult().blockStateModels();
		for (Block block : BuiltInRegistries.BLOCK) {
			if (block instanceof FloatingFlowerBaseBlock) {
				BlockStateModel floating = evt.getBakingResult().standaloneModels().get(NeoForgeFloatingFlowerModel.key(block));
				if (floating != null) {
					for (BlockState state : block.getStateDefinition().getPossibleStates()) {
						models.put(state, floating);
					}
				}
			} else if (block instanceof PlatformBlock) {
				for (BlockState state : block.getStateDefinition().getPossibleStates()) {
					models.computeIfPresent(state, (s, model) -> new NeoForgePlatformModel(model));
				}
			}
		}
	}

	@SubscribeEvent
	public static void onModelsLoaded(ModelEvent.BakingCompleted evt) {
		MiscellaneousModels.INSTANCE.onModelsLoaded(id -> NeoForgeStandaloneModels.get(evt.getModelManager(), id));
	}

	@SubscribeEvent
	public static void registerItemModelProperties(RegisterConditionalItemModelPropertyEvent evt) {
		BotaniaItemProperties.registerTests(evt::register);
	}

	@SubscribeEvent
	public static void registerItemModelRanges(RegisterRangeSelectItemModelPropertyEvent evt) {
		BotaniaItemProperties.registerRanges(evt::register);
	}

	@SubscribeEvent
	public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent evt) {
		BotaniaSpecialModelRenderers.register(evt::register);
	}

	@SubscribeEvent
	public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions evt) {
		BotaniaLayerDefinitions.init(evt::registerLayerDefinition);
	}

	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers evt) {
		EntityRenderers.registerBlockEntityRenderers(evt::registerBlockEntityRenderer);
		EntityRenderers.registerEntityRenderers(evt::registerEntityRenderer);
	}

	@SubscribeEvent
	public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		List<Item> armorItems = new ArrayList<>();
		for (var entry : BuiltInRegistries.ITEM.entrySet()) {
			Item item = entry.getValue();
			Identifier id = entry.getKey().identifier();
			if (item instanceof ManasteelArmorItem armor && id.getNamespace().equals(BotaniaAPI.MODID)) {
				armorItems.add(armor);
			}
		}

		// PORT-26.3: the armour hooks are keyed by equipment layer; the model is Botania's own, the texture comes from the
		// item. NeoForge only asks for these when the item's equipment asset lists layers (datagen, step 4).
		event.registerItem(
				new IClientItemExtensions() {
					@Override
					public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
						var model = ArmorModels.get(stack);
						return model != null ? model : original;
					}

					@Override
					public Identifier getArmorTexture(ItemStack stack, EquipmentClientInfo.LayerType type, EquipmentClientInfo.Layer layer, Identifier fallback) {
						if (stack.getItem() instanceof ManasteelArmorItem armor) {
							return armor.getArmorTexture(stack, null, armor.getEquipmentSlot(), type == EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS);
						}
						return fallback;
					}
				},
				armorItems.toArray(Item[]::new)
		);
		// PORT-26.3: block items drawn by their block entity renderer are special model renderers now (BotaniaSpecialModelRenderers)
	}

	@SubscribeEvent
	public static void registerParticleFactories(RegisterParticleProvidersEvent evt) {
		BotaniaParticles.FactoryHandler.registerFactories(new BotaniaParticles.FactoryHandler.Consumer() {
			@Override
			public <T extends ParticleOptions> void register(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> constructor) {
				evt.registerSpriteSet(type, constructor::apply);
			}
		});
	}

	@SubscribeEvent
	public static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources evt) {
		ColorHandler.submitBlocks((source, blocks) -> evt.register(List.of(source), blocks));
	}

	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.ItemTintSources evt) {
		ColorHandler.submitItemTintSources(evt::register);
	}

	@SubscribeEvent
	public static void initAuxiliaryRender(EntityRenderersEvent.AddLayers evt) {
		for (var playerModelType : evt.getSkins()) {
			var renderer = evt.getPlayerRenderer(playerModelType);
			EntityRenderers.addAuxiliaryPlayerRenders(renderer, renderer::addLayer);
		}
	}

	@SubscribeEvent
	public static void registerShaders(RegisterRenderPipelinesEvent evt) {
		// PORT-26.3: shaders are RenderPipelines now (CoreShaders)
		CoreShaders.init(evt::registerPipeline);
	}
}
