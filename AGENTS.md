# AGENTS.md

Instructions for AI coding agents working on this repository.

## About the mod

Ceramic Shears adds shears made of clay: two clay balls craft a clay shears part, which is smelted to a ceramic
shears part, and two ceramic shears parts craft the shears. They behave like vanilla shears (shearing mobs, carving
pumpkins, harvesting beehives, disarming tripwires, block drops, dispensers), have a configurable durability
(`durability`, default 179, 0 = unbreakable) and can be repaired by combining two of them.
The complete feature catalog and the matching tests are in [TESTING.md](TESTING.md).

## Project structure

- `common/` - all game logic, mixins that work on every loader, resources and data (recipes, tags, lang, textures).
  `common/src/gametest/` contains all game tests (vanilla API only).
- `fabric/`, `forge/`, `neoforge/` - loader specific code only: registration, config implementation, loader mixins.
  `<loader>/src/gametest/` is a thin adapter that registers the common tests. Never put test logic there.
- `buildSrc/` - shared Gradle logic (`multiloader-common.gradle`, `multiloader-loader.gradle`, incl. the gametest
  source set wiring).
- `scripts/` - harmless log lines of the server start test (`server-starttest.allowlist`). The script is shared and
  lives in cech12/MinecraftModActions; it reads the dependencies from the mod metadata and tests with the build and
  the minimum versions (weekly also with the newest versions, `server-starttest-newest.yml`; scheduled runs only
  happen on the default branch `26.x`, on this branch start it manually).
- Integrations: Cloth Config (required) and ModMenu (optional) on Fabric only.

## Build and test

```bash
./gradlew build                              # all loaders, release jars in <loader>/build/libs
./gradlew runGameTestServer                  # game tests of all loaders
./gradlew :fabric:runGameTestServer          # game tests of one loader (also :forge:, :neoforge:)
curl -fsSL https://raw.githubusercontent.com/cech12/MinecraftModActions/main/scripts/server-starttest.sh \
    | bash -s -- <fabric|forge|neoforge> [--versions <build|minimum|newest>] [--with-integrations]   # needs Docker, curl, Python 3.11+
```

The game test task fails if a test fails or no test was found. CI (`.github/workflows/cicd-workflow.yml`) runs build
and game tests in one job, then the server start test, for every loader on every pull request.

## Conventions

- All versions live in `gradle.properties`. Every new field must also be added to the `expandProps` map in
  `buildSrc/src/main/groovy/multiloader-common.gradle`.
- One branch per Minecraft version, the default branch is `26.x`.
- `CHANGELOG.md` follows Keep a Changelog, version scheme `<MC version>-<mod version>` (e.g. `26.2-4.1.0.0`).
  Bugfix entries look like "... (thanks to X for the report) #N".
- Every new feature or bugfix gets a game test (bugfixes as regression test that names the issue), and the feature
  table in `TESTING.md` is updated. Check that the new test fails when the feature is broken.
- Each test needs a test instance json in `common/src/gametest/resources/data/ceramicshears/test_instance/`.

## Do not

- Push directly to version branches (`26.x`, `1.21.1`, ...) or `main`. Use a feature branch and a pull request.
- Create tags or releases, change `mod_version`, touch the publish jobs of the CI or any secrets.
- Change mod behavior in a test PR. If a test reveals a bug, mark it with `KnownIssues` and open an issue.

## Mod specific notes

- The durability is not stored in the item: `DataComponentHolderMixin` (common) answers `MAX_DAMAGE`/`DAMAGE`
  from the config, `RepairItemRecipeMixin` (crafting) and `GrindstoneMenuMixin` (grindstone) remove the stored max
  damage after repairing. Config changes therefore apply to existing items.
- Fabric needs mixins for every vanilla `is(Items.SHEARS)` check (`fabric/.../mixin`, entity/block interactions,
  leash knot, tripwire) and `MatchToolMixin` for loot conditions. When a new Minecraft version adds shears
  interactions, check `block/drops_like_vanilla_shears` and the entity tests on Fabric first. The drop test fails if
  a new vanilla loot table checks for shears; then add the block and its shears drop to `VANILLA_SHEARS_DROPS`
  (`BlockTests`). Forge and NeoForge handle shears via their own tool actions/tags; some interactions there depended
  on loader PRs (copper golem, leash).
- Data pack `match_tool` conditions that name `minecraft:shears` directly only accept ceramic shears on Fabric. This is
  intended (Forge/NeoForge replace the shears conditions of the vanilla loot tables with their tool actions; 26.2 has
  no `minecraft:tool/can_shear` predicate yet), see "Data packs" in TESTING.md.
- Cloth Config and ModMenu are often not yet available for a new Minecraft version. The server start test reads them
  from `fabric.mod.json`: a missing optional mod is skipped, a missing required mod fails the test.
