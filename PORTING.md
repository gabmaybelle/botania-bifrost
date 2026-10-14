# Botania Bifrost: porting Botania to Minecraft 26.3 (Fabric + NeoForge)

Botania Bifrost is an unofficial port of Botania by Vazkii and the Botania contributors. Public on GitHub, built from the official `1.21.1-porting` branch.

This file is the working plan and log. Claude Code should read it at the start of every session and update the log at the end.

---

## Licence obligations (Botania License)

- Credit Vazkii as the creator of Botania, and state clearly that this is an altered, unofficial port.
- Keep the source public, allow redistribution and modification, and carry the Botania License (or a clause like its Copyleft clause).
- No money from distribution: no paywalls, ad-gated downloads or link shorteners.
- Botania API code (`vazkii/botania/api`) must stay verbatim.
- Code imported from Mekanism stays under MIT (see `ALTERNATE_LICENSES.txt`).
- Never remove existing credits, licence files or `contributors.properties`.

Full terms: https://botaniamod.net/license.html

---

## Starting point

Upstream: https://github.com/VazkiiMods/Botania, branch `1.21.1-porting`, commit `f0367399` (11 Oct 2026).

| Area | What's there |
|---|---|
| Minecraft / Java | 1.21.1, Java 21 |
| Layout | `Xplat` (shared), `Fabric`, `NeoForge`, plus `build-logic` multiloader scripts |
| Code size | Xplat 1,132 Java files; Fabric 85; NeoForge 53 |
| Mixins | Xplat 55, Fabric 31, NeoForge 4 |
| Tests | 103 GameTests across 26 classes in `Xplat/.../vazkii/botania/test` |
| Disabled | 15 CraftTweaker files in `Xplat/src/disabled` |
| Build tools | Fabric Loom 1.14.7, ModDevGradle 2.0.142, NeoForm 1.21.1 |

The branch is still an unfinished upstream port, so expect some bugs that exist upstream too.

---

## Target toolchain (checked 14 Oct 2026)

| Piece | Version |
|---|---|
| Minecraft | 26.3 |
| Java | 25 |
| NeoForm | `26.3-1` |
| NeoForge | `26.3.0.77` (builds from 26.3.0.76 no longer carry the beta tag) |
| ModDevGradle | `2.0.148` |
| Fabric Loader | `0.19.5` |
| Fabric API | `0.163.0+26.3` |
| Fabric Loom | `1.18.4` (builds 26.3 mods already; Fabric's develop page is script-rendered, so the maven listing was the check). Needs Gradle 9.7 and a Java 25 Gradle daemon. Use the plain `net.fabricmc.fabric-loom` plugin id, not `-remap` (26.x needs no remapping); it has no `mod*` configurations, so mods are plain `implementation`/`compileOnly`/`localRuntime`. |
| Gradle | `9.7.1` (upstream had 9.6.1; Loom 1.18.4 needs the 9.7 plugin API). `gradle/gradle-daemon-jvm.properties` pins the daemon JVM: 25 now, was 21. |
| Mixin / MixinExtras | `0.17.4+mixin.0.8.7` / `0.5.5`: what Fabric Loader 0.19.5 and NeoForge 26.3.0.77 ship with |
| Parchment | none for any 26.x version yet (404 on parchment-26.1 to 26.3, 14 Oct 2026). Minecraft ships unobfuscated since 26.1, so there are no Mojang mappings to apply either: Loom takes no `mappings` block, and mixin targets are the real names. |

Re-check these before Phase 1 and update the table.

---

## Dependencies

| Dependency | Fabric | NeoForge | Plan |
|---|---|---|---|
| Patchouli (Lexica Botania) | 26.1.x beta only | 26.1.x beta only | **Blocker for the Lexica.** Disable the integration in Phase 1. Revisit in Phase 5: wait for an update, or port Patchouli (CC BY-NC-SA 3.0, Vazkii). |
| Accessories (rings, amulets) | Trinkets ends at 1.21.1. Use **Trinkets Updated** (Patbox fork, MIT, 26.3) | **Curios** `17.0.0-beta.3+26.3` | Disable in Phase 1, wire up in Phase 5. |
| EMI | no 26.3 build | no 26.3 build | Optional (compile-only). Disable. |
| JEI, REI | on 26.3 | on 26.3 | Optional. Disable in Phase 1, re-enable in Phase 5. |
| Sodium | on 26.3 | on 26.3 | Optional compat. Disable in Phase 1, re-enable once rendering works. |
| Embeddium, Ears, TeamReborn Energy, SpeedrunIGT, Carpet | not checked | not checked | Optional compat. Disable until checked. |
| Fiber (Fabric config) | not checked | n/a | Check early; it's needed for config. |

---

## Decisions

- **Name:** Botania Bifrost. Repository and Modrinth slug: `botania-bifrost`. Summary line: "An unofficial port of Botania to 26.3 for Fabric and NeoForge."
- **Mod ID:** keep `botania`, so worlds and addons can move to an official 26.x release later. Display name is "Botania Bifrost"; jars are named `botania-bifrost-<loader>-<mc version>-<build>.jar`.
- **Version jump:** go straight from 1.21.1 to 26.3, reading the porting notes for every release in between, in order, from 1.21.2 through 26.3. NeoForge publishes porting primers for each version; Fabric publishes update blog posts.
- **Upstream:** keep `upstream` as a git remote and cherry-pick fixes from `1.21.1-porting`.
- **Style:** keep upstream's code style (`spotlessJavaApply`) so upstream fixes still apply cleanly.

---

## Phases

Each phase ends with both loaders building and the relevant GameTests passing on both.

### Phase 0: Baseline
1. Fork upstream on GitHub as `botania-bifrost`, create branch `26.3` from `1.21.1-porting`.
2. Build both loaders on Java 21 with no changes. Run the GameTests. Record results below.
3. Add the unofficial notice to the README.

**Done when:** the untouched 1.21.1 branch builds and its test results are recorded.

### Phase 1: Toolchain and compiling
1. Update `gradle.properties` and `gradle/libs.versions.toml` to the target toolchain, Java 25. Set the display name to Botania Bifrost and the jar names as above; leave the mod ID as `botania`.
2. Disable optional integrations and Patchouli, Trinkets and Curios behind clear `// PORT-26.3: disabled` comments.
3. Fix `Xplat` until it compiles, then `Fabric`, then `NeoForge`.
4. Rerun datagen.

**Done when:** both jars build, the game reaches the main menu on both loaders, and a new world loads.

### Phase 2: Core mana loop
Pure Daisy, livingwood and livingrock, mana spreaders and bursts, mana pools, sparks, mana infusion, Petal Apothecary, generating flowers.

**Done when:** related GameTests pass on both loaders and a pool fills from an Endoflame in-game.

### Phase 3: Functional flowers
Start with the farm and mob flowers (Agricarnation, Hopperhock, Rannuncarpus, Pollidisiac, Bellethorne, Dreadthorne and similar), then the rest.

### Phase 4: Items, tools and rendering
Manasteel, Terrasteel, Elementium gear, rods, Runic Altar, Terra Plate, particles and custom rendering. Audit every mixin against 26.3.

### Phase 5: Accessories, Lexica and recipe viewers
Trinkets Updated on Fabric, Curios on NeoForge, Patchouli, and whichever recipe viewers exist for 26.3.

### Phase 6: Endgame and polish
Alfheim portal, elven trades, Gaia Guardian, Corporea, Garden of Glass. First public release.

---

## Disabled for now (Phase 1, step 2)

Each of these is switched off with a `// PORT-26.3: disabled` comment at its call site; the integration
code itself is kept intact under `src/disabled/integration/` in the project it came from (the same way
upstream parked CraftTweaker), so Phase 5 can move it back.

| What | Where it was | Why | Effect until it's back |
|---|---|---|---|
| Patchouli (Lexica Botania) | `Xplat` (29 files), both loaders' initialisers, 5 client mixins (`BookEntryMixin`, `ClientAdvancementsAccessor`, `GuiBookEntryListMixin`, `GuiButtonEntryMixin`, `MultiblockVisualizationHandlerMixin`, now in `Xplat/src/disabled/patchouli-mixins`) | No 26.3 build. The 1.21.1 jar stays on the compile classpath for its API only; `PatchouliAPI.get()` returns the API's own stub when the mod is absent. | No Lexica. **The stub's multiblocks never validate, so the Alfheim portal, Mana Enchanter, Terra Plate and Gaia ritual arena can't form until Phase 5.** The Patchouli page components and processors in `client/patchouli` are kept (only the book ever loads them). |
| Trinkets (Fabric) | `Fabric/.../integration/trinkets` | Ends at 1.21.1; Trinkets Updated replaces it in Phase 5. | Rings, belts, amulets and so on sit in the inventory handler fallback (no equipment slots). |
| Curios (NeoForge) | `NeoForge/.../integration/curios`, plus the `BotaniaCuriosDataProvider` datagen provider | No 26.3 release. | Same as Trinkets. |
| JEI, REI, EMI | `Xplat/.../client/integration/{jei,rei,emi}`, `NeoForge/.../client/integration/rei`, the Fabric entrypoints | JEI and REI exist for 26.3 but are optional and wait for Phase 5; EMI has no 26.3 build. | No recipe-viewer pages for Botania's recipes. |
| Sodium, Embeddium | `Fabric/.../integration/sodium`, `NeoForge/.../client/integration/{sodium,embeddium}` | Their sprite API for 26.3 isn't checked yet. | Animated Botania textures may not animate under Sodium until checked. |
| Ears | `Xplat/.../client/integration/ears` | Not checked for 26.3. | Nothing player-facing. |
| Team Reborn Energy (Fabric) | `Fabric/.../integration/tr_energy` | Not checked for 26.3. | The Mana Fluxfield doesn't push energy to other mods' machines. |
| SpeedRunIGT (Fabric) | `Fabric/.../integration/speedrunigt` | Not checked for 26.3. | Speedrun categories never report as running. |
| Carpet (dev runtime only) | `Fabric/build.gradle` | Not checked for 26.3. | None in the mod. |
| `forged-conditions-21.1.0.jar` (dev runtime only) | `Fabric/build.gradle` | A 1.21.1 jar used while generating data load conditions. | Step 4 (datagen) decides what replaces it. |

| Data generators (temporarily) | `Xplat/.../data` (34 files), `Fabric/.../fabric/data` (26), `NeoForge/.../neoforge/data` (16), three Fabric `HashCache*` mixins; the Fabric `fabric-datagen` entrypoint | Parked during step 3 so the 1.21.4 datagen rewrite (about 1,200 of the compile errors, `BlockstateProvider` alone 599) doesn't hide gameplay errors; they only run at build time. Come back in step 4 (rerun datagen). The `normalizeLanguageFiles` Gradle task points at the parked `LangSortTool` until then. | None in the game; the generated JSON in `src/generated` is unchanged and still ships. |

| Fabric axe stripping of Botania's own blocks | `Fabric/.../mixin/AxeItemFabricMixin` (now `Fabric/src/disabled/axe-stripping`) | `AxeItem` is gone; stripping is data-driven (`BlockTransformers`, 26.3), so the custom strippables from `AxeStrippingData` become data in step 4. | Botania's log-like blocks can't be stripped with an axe until then. |

Kept as they are: `common/integration/corporea`, `InventorySorterIntegration` (NeoForge IMC only) and
`integration/speedrunigt/BotaniaSpeedrunCategories` (plain data, no dependency).

---

## Known hotspots (from upstream comments)

- Particle render types: both loaders patch `ParticleEngine` with a hack (`ParticleEngineFabricMixin`, `ParticleEngineNeoForgeMixin`). Likely to break.
- `IDeathMessageProviderNeoForgeMixin`: flagged by upstream as not the intended approach.
- `TrinketsIntegration` and `CurioIntegration`: drop-rule handling flagged as hacky.
- `FabricTransferCorporeaNode`: integer truncation hack.
- `TinyPotatoBlockItem`: a NeoForge-only interface not yet implemented on Fabric.

---

## API changes (vazkii.botania.api)

The API package is kept verbatim except where 26.3 forces a change. So far, all of it is renames the
whole codebase got: `ResourceLocation` -> `Identifier` (55 API files), `ResourceKey#location()` ->
`identifier()`, `InteractionResultHolder`/`ItemInteractionResult` -> `InteractionResult`,
`BlockPos#getCenter()` -> `Vec3.atCenterOf(pos)`. Tools and armour (step 3): `getManasteelItemTier()`, `getElementiumItemTier()` and `getTerrasteelItemTier()` return `ToolMaterial` (vanilla removed `Tier`), and the four `get...ArmorMaterial()` getters return `ArmorMaterial` instead of `Holder<ArmorMaterial>` (armor materials are no longer a registry). Recipes (step 3): vanilla's `Recipe` changed shape in 1.21.2, so the Botania recipe interfaces follow it. New in the API: `BotaniaRecipe<T>` (all Botania recipe interfaces extend it) with vanilla's new required methods as defaults: `placementInfo()` = not placeable, `recipeBookCategory()` = one Botania category (`botania:botania`, registered by the mod), `group()` empty, `showNotification()` false. Changed: `assemble(input, registries)` -> `assemble(input)` and `ElvenTradeRecipe#tryAssemble(input, registries)` -> `tryAssemble(input)` (vanilla's assemble lost the registries); `getType()` returns the precise `RecipeType<? extends X>`; `canCraftInDimensions` defaults removed (vanilla removed the method). Kept as Botania's own methods, no longer overrides of vanilla: `getIngredients()` (declared on each processing recipe interface), `getResultItem(registries)`, `getRemainingItems(input)` (`RecipeWithCatalysts`, brewery), `getToastSymbol()`. Consequence for later: the server no longer sends machine recipes to clients, so the Lexica pages, recipe viewers and the Assembly Halo's client preview need Botania to sync its own recipes (Phase 5); `PatchouliUtils#getRecipeGroup` is a placeholder until then.

---

## Working rules for Claude Code

- Read this file first. Work on one phase step or subsystem per session.
- At the end of each phase, go through the matching sections of `COMPATIBILITY-AND-REVIEW.md`.
- Small commits with clear messages. No unrelated refactors.
- Build both loaders and run the relevant GameTests before finishing.
- Mark every temporary workaround with `// PORT-26.3:` and a short reason.
- Never edit `vazkii/botania/api` beyond what compiling requires; record any change here.
- End each session by updating the log below: what changed, what's broken, what's next.

---

## Log

| Date | Phase | Notes |
|---|---|---|
| 2026-10-14 | Planning | Inspected upstream `1.21.1-porting` (commit `f0367399`). Checked 26.3 toolchain and dependencies. Baseline build not yet run. |
| 2026-10-14 | Phase 0 | Baseline on the untouched `1.21.1-porting` code (commit `f0367399`), Java 21 (Gradle 9.6.1 provisioned Temurin 21 itself), Windows 11. `./gradlew build`: Xplat, Fabric and NeoForge all build and pass their unit tests, PMD and Spotless checks (28 min 39 s cold). `./gradlew :Fabric:runGameTest`: 207 required tests passed, 1 optional test failed: `paintslingerlenstest.testpainting`, which upstream marks `required = false` with a note that its structure data needs upgrading (a datafixer job). 103 `@GameTest` methods expand to 207 tests because `IdleFlowerTickingTest` generates one test per flower. The NeoForge GameTests weren't run (upstream's CI only runs Fabric's). Added the Botania Bifrost section to the README and this file plus `COMPATIBILITY-AND-REVIEW.md`. Forked as `gabmaybelle/botania-bifrost`, branch `26.3` tracking `upstream/1.21.1-porting`. Next: Phase 1 step 1, the toolchain. |
| 2026-10-14 | Phase 1, step 1 | Re-checked every toolchain version against its source: all as in the table (Fabric API 0.163.0+26.3 was released the same day). Found no Parchment for 26.x, so the three parchment blocks are commented out and the builds use plain Mojang names; Mixin and MixinExtras raised to the versions the loaders ship. `gradle.properties`, `libs.versions.toml`, `multiloader-common.gradle` (jar names), the three `build.gradle` files, the CI workflow (Java 25) changed; display name Botania Bifrost, sources and issues URLs point at this fork. Not compiled yet: diff shown for review first. |
| 2026-10-14 | Phase 1, step 2 | Disabled the optional integrations, Patchouli, Trinkets and Curios (table above): dependency lines commented out with `PORT-26.3` notes, integration sources moved to `src/disabled/integration/` in each project, five Patchouli client mixins moved out and dropped from `botania.xplat.mixins.json`, call sites stubbed with comments, Patchouli and Curios made optional in the mod metadata and the Fabric entrypoints for EMI/REI/JEI/SpeedRunIGT removed. Patchouli's 1.21.1 jar stays as a compile-only API (its stub runs when the mod is absent), which is what keeps the multiblock code compiling. Getting Gradle to configure with the 26.3 toolchain took four more fixes, all in the toolchain table: Gradle 9.7.1 and a Java 25 daemon (`gradle-daemon-jvm.properties`), the no-remap Loom plugin id, no `mappings` block, and the access widener's namespace (`official`). With those, `spotlessJavaCheck` passes on all three projects. A measuring `:Xplat:compileJava` afterwards stops at javac's 2,000-error cap, with errors in 469 of Xplat's 1,132 files; the top missing symbols are `ResourceLocation` (1,532 uses; now `Identifier`), `MultiBufferSource`, `GuiGraphics`, `InteractionResultHolder`, `RenderType`, `BakedModel`, `ItemInteractionResult`, `Tier`, `ArmorMaterial`, `FastColor`, and missing packages `advancements.critereon`, `data.models.model`, `entity.animal.horse`, `item.armortrim`. Step 3 starts with the renames (`ResourceLocation` -> `Identifier` alone is most of it), then the rendering and item API changes. |
| 2026-10-14 | Phase 1, step 3 (passes 1-3) | Read the NeoForge primers 1.21.2 to 26.3 and did the mechanical renames they and the 26.3 jar confirm, by script, with each mapping reviewed: `ResourceLocation` -> `Identifier`; imports of classes that only moved package (entities, advancement predicates and triggers, `util`, `client.data.models`, equipment and trim) rewritten to their one match in the 26.3 jar; `FastColor` -> `ARGB`; `UseAnim` -> `ItemUseAnimation`; the two old interaction-result types -> `InteractionResult` (the four sites that hand back a different stack use `SUCCESS.heldItemTransformedTo`); `DataComponentInput` -> `DataComponentGetter`; `EntityType.X` -> `EntityTypes.X`; `MobSpawnType` -> `EntitySpawnReason`; `BlockPos#getCenter()` -> `Vec3.atCenterOf`; `location()` -> `identifier()`; `RenderShape.ENTITYBLOCK_ANIMATED` -> `INVISIBLE` (removed in 1.21.4; the block entity renderers draw these blocks anyway); `PushReaction.DESTROY` -> `POPPED`; `Enderman`, `FarmlandBlock`, `SpreadingSnowyBlock`, `InstantaneousMobEffect`, `EnumProperty<Direction>`. Game tests: vanilla's `@GameTest`, `@GameTestGenerator` and `TestFunction` are gone (tests are data-driven since 1.21.5), so `vazkii.botania.test` has `BotaniaGameTest`, `BotaniaGameTestGenerator` and `BotaniaTestFunction` carrying the same values; the test classes only changed imports and the exception constructors (`Component` + tick). Registering the tests with 26.3's framework on each loader is still to do. Xplat errors: over 5,760 before (javac was capped at 2,000; the cap is lifted temporarily to measure) -> 5,043 after, in 560 files. What's left groups into: datagen (`BlockstateProvider` alone 599, the tag providers 210, loot, advancements: the 1.21.4 datagen rewrite), rendering (`RenderHelper`, particles, block entity and entity renderers, GUI: `MultiBufferSource`, `GuiGraphics`, `BakedModel`, `RenderType` state shards, shaders, GLFW are all gone), items (`Tier`/`SwordItem`/`ArmorItem` -> data components), recipes (`getIngredients`, `Ingredient.EMPTY`, `getRecipeManager`), and `Block#onRemove`. |
| 2026-10-14 | Phase 1, step 3 (items, passes 4-6) | Parked the data generators (table above). Tools and armour onto data components (`ToolMaterial`, `ArmorMaterial`, `Item.Properties#pickaxe`/`sword`/`humanoidArmor`; API getters changed, see "API changes"). Then three more site-driven passes: `isClientSide()`, `MobEffects` names, `inventoryTick(stack, ServerLevel, entity, EquipmentSlot)`, cooldowns on stacks, armour slots instead of the inventory armour list, dimension clock time, `PoseStack#rotate`, `CompoundTag` `...Or` getters, `AtlasIds`, and accessor mixins for the water cauldron dispatcher and `FireBlock#setFlammable`. Xplat: 5,043 -> 2,988 errors (475 files) this session. Next: recipes (the API's recipe interfaces have to follow vanilla's `Recipe`: `placementInfo()`, `assemble(input)` without registries, `display()`, `recipeBookCategory()`; `getIngredients`/`canCraftInDimensions` are gone; `CustomRecipe` subclasses need the new constructor and serializer generics), then `Block#onRemove`, `EntityType.Builder#build(ResourceKey)`, `WeightedList`, loot condition types, and finally the rendering rewrite (about 1,500 errors in `client/`). |
| 2026-10-14 | Phase 1, step 3 (recipes) | Recipes onto vanilla's 1.21.2+ `Recipe` shape (API changes in "API changes"; commit d91b20b35). Xplat: 2,988 -> 2,494 errors. Lookups go through `BotaniaRecipeTypes.recipeManager(level)` / `getAllRecipesFor(...)`; `getAllRecipesFor` filters all recipes each call (cache it if a profile shows it). Phase 5 must sync Botania's recipes to clients for the Lexica, the recipe viewers and the Assembly Halo preview. Next: blocks and entities one-offs (`Block#onRemove`, `EntityType.Builder#build(ResourceKey)`, `WeightedList`, loot condition types, `ServerLevel` arguments), then rendering (the bulk of what is left). |
