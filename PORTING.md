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
| Fabric Loom | latest stable is `1.18.4`; confirm in Fabric's docs which Loom version supports 26.3 |

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

## Known hotspots (from upstream comments)

- Particle render types: both loaders patch `ParticleEngine` with a hack (`ParticleEngineFabricMixin`, `ParticleEngineNeoForgeMixin`). Likely to break.
- `IDeathMessageProviderNeoForgeMixin`: flagged by upstream as not the intended approach.
- `TrinketsIntegration` and `CurioIntegration`: drop-rule handling flagged as hacky.
- `FabricTransferCorporeaNode`: integer truncation hack.
- `TinyPotatoBlockItem`: a NeoForge-only interface not yet implemented on Fabric.

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
