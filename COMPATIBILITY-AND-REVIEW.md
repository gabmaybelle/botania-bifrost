# Compatibility and review checklist

Companion to `PORTING.md`. Claude Code should check the relevant section at the end of each phase.

Availability checked on Modrinth, 14 Oct 2026. Re-check before each phase.

---

## 1. Compatibility targets

### Performance and rendering (test from Phase 2 onward)

| Mod | On 26.3? | What to check |
|---|---|---|
| Sodium | Yes, Fabric + NeoForge | Botania already has compile-only Sodium hooks. Floating flowers, pool liquid, spreader models and mana bursts must render correctly. |
| Iris + a shader pack | Yes, Fabric + NeoForge | Mana bursts, particles and glowing blocks under shaders. A large share of players use shaders. |
| Lithium, FerriteCore | Yes | Botania mixins must not clash with theirs. Watch the startup log for mixin conflicts. |
| C2ME | Yes, Fabric | Multithreaded chunk generation. Botania's worldgen (mystical flowers and mushrooms) must not touch the world unsafely. |
| Distant Horizons | Yes | Worldgen flowers should look reasonable in distant terrain. Low priority. |
| ModernFix | 26.1.2 only | Re-check later. |
| spark | Yes | Use it to profile flower and block-entity ticking on a busy test base. |

### Information and recipe viewers (Phase 5)

| Mod | On 26.3? | Plan |
|---|---|---|
| JEI | Yes | Re-enable Botania's existing integration. |
| REI | Yes | Re-enable. |
| EMI | No | Keep disabled. |
| Jade | Yes | Nice to have: show mana in pools and spreaders. Botania has its own wand HUD, so this is optional. |
| WTHIT | 26.2 only | Later. |

### Content mods players will run alongside it

| Mod | On 26.3? | Notes |
|---|---|---|
| Farmer's Delight Refabricated | Yes, Fabric | Popular pairing. Check crop and food interactions with Agricarnation, Rannuncarpus and the Gourmaryllis. |
| AE2 | Yes | Its Botania addon (Applied Botanics) isn't on 26.x, so nothing to test yet. |
| AppleSkin | Yes | Food values for Botania foods should display correctly. |
| Create, Quark, Supplementaries, Mekanism | Not yet | Re-check before release. |
| Long-day datapacks | Yes, our own test instance | A datapack with 72,000-tick days changes how long the Daybloom and Nightshade generate. Test that output still feels balanced, and that Botania worldgen sits well in custom terrain. |

### Botania addons

ExtraBotany, Mythic Botany, Botanical Machinery and Applied Botanics have no 26.x versions. Keep the mod ID `botania` and the API package unchanged so their authors could port to your build. Record any API change in `PORTING.md`.

### Modpack tooling

- KubeJS is on 26.1.2. CraftTweaker integration is disabled upstream.
- Keep every recipe data-driven, so pack makers can change them with a datapack.
- Use common `c:` tags (ores, crops, ingots) wherever upstream does, so other mods' items work in Botania machines (the Orechid especially).

---

## 2. Technical review checklist

What a careful reviewer looks at before trusting a port.

### Builds and tests
- [ ] Both loaders build from a clean clone with one command.
- [ ] GitHub Actions builds both loaders and runs the GameTests on every push (update upstream's workflow to Java 25).
- [x] All GameTests pass, or each failure is listed in `PORTING.md` with a reason. (213 required Botania tests on 26.3, on both loaders; only the optional paintslinger test fails, as upstream marks it.)
- [ ] **No test expectation is changed just to make it pass.** If a test must change, the reason goes in the commit message.

### Server and multiplayer
- [ ] A dedicated server starts on both loaders with no client-only classes loaded.
- [ ] Client and server stay in sync: mana bars, pool levels, spreader aim.
- [ ] Packets from clients are validated on the server.
- [ ] No item duplication paths (test Corporea, Ring of Loki, Spectranthemum, mana tablet with hoppers).
- [ ] Two players on one server can use shared mana systems without desyncs.

### Performance
- [ ] Profile a large test base with spark. No single Botania block entity dominates the tick.
- [ ] Nothing force-loads chunks unless upstream does.

### World data
- [ ] New worlds save and reload with all Botania data intact (pool mana, spreader bindings, flower bindings, item mana). `SaveReloadTest` covers these at the block entity and item level: it saves a pool, a spreader and two flowers, rebuilds them from the saved data, and checks the mana and bindings, and it round-trips a mana tablet. A real world saved to disk and reopened is still to check.
- [ ] **Old worlds:** decide and document whether 1.20.1 or 1.21.1 Botania worlds can be upgraded. Item data moved to data components in 1.20.5, so Botania item data (mana in tablets, lens settings) may not carry over. Always test on a copy of a world, never the server's real one.

### Code quality
- [ ] Mixins are as few as possible, with no `@Overwrite`; prefer MixinExtras.
- [ ] Every temporary workaround has a `// PORT-26.3:` comment and is listed in `PORTING.md`.
- [ ] No large blocks of commented-out code.
- [ ] No features quietly removed. Anything disabled is listed.
- [ ] Startup and gameplay logs are free of repeated errors, missing textures and missing translation keys (upstream's `check_lang.sh` and `check_datagen.sh` help).
- [ ] Translation files for other languages are untouched.
- [x] Code style passes `spotlessJavaCheck`.

### AI-assisted code
Claude Code is fast, but a reviewer will look for its typical mistakes:
- [ ] Methods or classes that don't exist in 26.3 (always compile and run).
- [ ] Logic simplified while "fixing" a compile error, changing behaviour. Compare against the upstream 1.21.1 code.
- [ ] Tests weakened or skipped.
- [ ] Unrelated files edited.

### Trust and safety
- [ ] No telemetry, analytics or update checks that phone home.
- [ ] No downloading code or files at runtime.
- [ ] No obfuscated code.

---

## 3. Publishing checklist (Modrinth)

From Modrinth's content rules:

- **AI disclosure:** projects must be honest about generative AI use, and the "Contains AI-generated content" disclosure applies when AI writes a substantial share of the code. Projects can't be mainly AI output. A port where Vazkii's code remains the bulk of the mod fits within that, but tick the disclosure.
- **No AI on the project page:** the description, icon and gallery images can't be AI-made. Write the description yourself and use real in-game screenshots.
- **Credit and licence:** credit Vazkii and the Botania contributors, link the Botania License as the custom licence, and link your source repository.
- **Name:** the title should be only the project's name, and the page must not suggest an official affiliation. Title: **Botania Bifrost**. Summary: "An unofficial port of Botania to 26.3 for Fabric and NeoForge." Slug: `botania-bifrost` (free as of 14 Oct 2026).
- **Dependencies:** list every required dependency on each version (Patchouli once it's back, Trinkets Updated on Fabric, Curios on NeoForge, Fabric API).
- **Versions:** upload the Fabric and NeoForge jars as separate versions with the correct loader tags. Environment: client and server.
- **Description:** what it is, how it differs from the official mod, which features are missing, and where to report bugs.

CurseForge has its own rules; check them separately if you publish there too.

---

## 4. Release readiness

Before the first public release:
- [ ] Every item in section 2 is checked or has a documented exception.
- [ ] A "Known issues and missing features" list is in the README.
- [ ] Your group has played on a test server for at least a few sessions.
- [ ] A changelog exists (upstream keeps one in `web/changelog.md`).
