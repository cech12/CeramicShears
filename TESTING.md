# Testing

Ceramic Shears has two automated test levels. Both run in CI for every loader (Fabric, Forge, NeoForge) on every pull
request and on pushes to version branches and release tags, and turn the build red on failure. Pushes to branches
with a `/` in their name (e.g. `feature/...`) do not start the CI themselves (the branch filter `*` does not match
`/`), only their pull request does. Pushes and pull requests that only change Markdown files (`**/*.md`) do not start
the CI.

| Level | What it checks | Where it runs |
|---|---|---|
| **GameTests** | Every feature of the mod (see table below) in a real game test server | Gradle (`runGameTestServer`), development environment |
| **Server start test** | The built release jar starts cleanly on a real dedicated server, with and without the optional integrations, with the build and the minimum versions of loader and dependencies (and weekly with the newest versions) | Docker image [itzg/minecraft-server](https://github.com/itzg/docker-minecraft-server) |

In CI (`.github/workflows/cicd-workflow.yml`) the game tests run in the build job right after `./gradlew build`, so
they reuse the Minecraft setup and the compiled classes of the build. The server start test runs after the build (it
needs the release jars), and publishing on a tag only starts when build, game tests and server start test succeeded.
Once a week (`.github/workflows/server-starttest-newest.yml`, also startable manually) the server start test runs with
the newest loader and dependency versions. It does not block releases, but shows when a new loader or dependency
release breaks the mod. GitHub runs scheduled workflows only on the default branch (`26.x`), so the older version
branches (`26.2`, `26.1`, `1.21`, ...) never get the weekly run; start it manually (`workflow_dispatch`) on such a
branch if needed. GitHub deactivates scheduled workflows of public repositories after 60 days without activity in the
repository, and mails about failed scheduled runs only go to the user who last changed the cron line. If the weekly
run is deactivated, enable it again in the Actions tab of the repository.

The CI depends on external services: the shared workflows and the server start script are used from `@main` of
[cech12/MinecraftModActions](https://github.com/cech12/MinecraftModActions), the server start test needs Docker Hub
(image `itzg/minecraft-server`) and the Modrinth API (to resolve the dependency versions). If one of them is not
available, the server start test fails and a release on a tag is not published until the run is repeated.

## Running the tests locally

GameTests (Java 25 toolchain is provisioned by Gradle):

```bash
./gradlew runGameTestServer              # all loaders
./gradlew :fabric:runGameTestServer      # one loader
./gradlew :forge:runGameTestServer
./gradlew :neoforge:runGameTestServer
```

The task fails if a test fails or if no test was found. A JUnit report is written to
`<loader>/build/gametest-report.xml`, logs are in `<loader>/runs/gametest/logs/`.

Server start test (needs bash, Docker, curl and Python 3.11+; on Windows use WSL or Git Bash with Docker Desktop). The script is
shared by all mods and lives in [cech12/MinecraftModActions](https://github.com/cech12/MinecraftModActions)
(`scripts/server-starttest.sh`):

```bash
./gradlew build
S=https://raw.githubusercontent.com/cech12/MinecraftModActions/main/scripts/server-starttest.sh
curl -fsSL $S | bash -s -- fabric                        # mod + required mods, build versions
curl -fsSL $S | bash -s -- fabric --with-integrations    # + optional integrations
curl -fsSL $S | bash -s -- fabric --versions minimum     # oldest allowed versions (also: newest)
curl -fsSL $S | bash -s -- forge
curl -fsSL $S | bash -s -- neoforge
```

The server runs with the Minecraft version of `gradle.properties`. `--versions` chooses the loader and dependency
versions, always within the version ranges of the mod metadata (lower and upper bounds): `build` (default) uses the
versions of `gradle.properties`, `minimum` the oldest versions the mod allows (skipped if they equal the build
versions), `newest` the newest versions. The dependencies are read from the metadata of the jar (`fabric.mod.json`,
`mods.toml`, `neoforge.mods.toml`): required dependencies are always installed, optional ones (on Fabric
`recommends`/`suggests`, e.g. ModMenu) only with `--with-integrations`. Mod ids with another name on Modrinth are mapped
centrally in `scripts/server-starttest-mods.txt` of MinecraftModActions. The test fails if the server does not log
"Done" within 10 minutes, stops again right after the start, writes a crash report, logs exceptions, mixin errors or
errors mentioning the mod id, or if the mod or one of its dependencies is not in the mod list of the loader. Known
harmless log lines can be excluded in `scripts/server-starttest.allowlist` (one regex per line, in addition to the
built-in ones of the script). Logs and a summary are written to `.server-starttest/<loader>-<versions>-<mode>/`.
`--with-integrations` is skipped (exit 0) for loaders without optional dependencies (currently Forge and NeoForge).
To reproduce a failure with another loader version, set `STARTTEST_LOADER_VERSION`.

## How the GameTests are organized

```
common/src/gametest/java/.../gametest/          test logic, only vanilla API (shared by all loaders)
  CeramicShearsGameTests                         central list of all test classes
  tests/*Tests                                  the tests, grouped by feature
  DurabilityConfigEnvironment                   test environment that sets the durability config per batch
  KnownIssues                                   marks tests that are known to fail on some loaders
common/src/gametest/resources/data/ceramicshears/
  test_instance/**.json                         one json per test (structure, environment, max ticks)
  test_environment/*.json                       config values (durability) used by the tests
  structure/platform.nbt                        5x4x5 test area with a stone floor
  loot_table/gametest/*.json                    data pack loot tables used by the loot tests
<loader>/src/gametest/                          thin adapter: registers the test functions, changes the config
```

Since Minecraft 1.21.5 test functions are a registry and test instances are data pack entries. The adapters only
register the functions; everything else is plain data, so a port to a new Minecraft version usually only needs
changes in the test code where vanilla API changed. On Fabric and Forge the gametest source set is a separate
test mod (`ceramicshears_gametest`), on NeoForge it is part of the main mod. It is never part of the release jars.

### Writing a new test

1. Add a method `public static void myTest(GameTestHelper helper)` to the matching class in `tests/` and register it
   in its `register` method: `tests.add("group/my_test", Class::myTest);`. Call `helper.succeed()` (or use
   `helper.succeedWhen(...)` for tests that need ticks).
2. Add `common/src/gametest/resources/data/ceramicshears/test_instance/group/my_test.json` (copy an existing one).
   The meta test `meta/every_test_function_has_an_instance` fails if you forget it or if its `function` names another test.
3. Tests that need another durability config value use another `environment` (`test_environment/*.json`).
   Tests of one environment run together, different environments never run at the same time.
4. Run `./gradlew runGameTestServer` and make sure the test fails if the feature is broken.
5. Add the test to the feature table below.

If a test reveals a bug that cannot be fixed in the same change, wrap the assertions in
`KnownIssues.run(helper, "<issue link>", Set.of(Platforms.FORGE), () -> ...)` and open an issue. On the listed
loaders the test passes while the bug exists and fails as soon as it is fixed, so the marker is not forgotten.
Every failed assertion inside the lambda counts as "the bug is still there", so only put the assertion of the bug
itself into the lambda. Setup and plausibility checks (e.g. "the result is ceramic shears at all") belong before
`KnownIssues.run`, otherwise a completely broken feature would still pass with a warning.

## Features and tests

All test names are relative to `ceramicshears:`. Every test runs on Fabric, Forge and NeoForge. Tests of intended
loader differences check the expected behavior of each loader (see "Data packs").

### Item

| Feature | Test(s) |
|---|---|
| The item `ceramicshears:ceramic_shears` exists and behaves as shears | `item/is_registered_as_shears` |
| Ceramic shears do not stack (regression 1.18.2-1.6.1.1) | `item/does_not_stack` |
| Listed in the creative tab "Tools & Utilities" | `item/is_in_tools_and_utilities_tab` |
| Part of the common tag `c:tools/shear` ([WoodenShears#17](https://github.com/cech12/WoodenShears/issues/17)) | `item/is_in_common_shears_tag` |
| Enchantable like vanilla shears with every enchantment (Efficiency, Unbreaking, Mending, Curse of Vanishing), only with an anvil, not at the enchanting table (regression 1.20.6-2.3.0.1) | `item/is_enchantable_like_vanilla_shears` |
| Mending repairs ceramic shears exactly like vanilla shears, shears mended to full durability store no leftover damage | `item/mending_repairs_like_vanilla_shears` |
| All default components equal vanilla shears (tool rules incl. breaking blocks in creative, stack size, enchantability, rarity, ...), except name, model and durability | `item/has_the_components_of_vanilla_shears` |
| Same mining speed and correct-tool behavior as vanilla shears for every block (cobweb, leaves, wool, ...) | `item/mines_like_vanilla_shears` |
| Same durability loss as vanilla shears when breaking any block: 1 for every block except fire, also for blocks without hardness like grass ([WoodenShears#40](https://github.com/cech12/WoodenShears/issues/40) asked for no loss there and was closed in favor of the vanilla behavior) | `item/loses_durability_per_block_like_vanilla_shears` |
| No furnace fuel, like vanilla shears | `item/is_no_fuel_like_vanilla_shears` |
| The parts `ceramicshears:clay_shears_part` and `ceramicshears:ceramic_shears_part` exist as simple items that stack to 64 | `item/shears_parts_are_registered` |
| Both parts are listed in the creative tab "Ingredients" | `item/shears_parts_are_in_ingredients_tab` |

### Durability config option (`durability`, default 179, range 0 - 10000, 0 deactivates the durability)

The test environment `default` (`test_environment/default.json`) sets the durability to 179, so tests do not depend on
a local config file. If the default ever changes, `durability/default_value` fails, and `default.json` and
`DOCUMENTED_DEFAULT` in `DurabilityTests` must be updated together with it. All other tests read the value from the
config (`Services.CONFIG.getDurability()`).

| Feature | Test(s) |
|---|---|
| Default 179, read from the config definition of the loader (config spec on Forge/NeoForge, config class on Fabric), not only from the test environment | `durability/default_value` |
| Custom value (200) | `durability/custom_value` |
| Maximum value 10000 | `durability/maximum_value` |
| Values above 10000 are limited to 10000 | `durability/above_maximum_is_limited` |
| 0 makes the shears unbreakable (not damageable, no max damage) | `durability/zero_makes_shears_unbreakable` |
| Values below 0 behave like 0 | `durability/below_zero_makes_shears_unbreakable` |
| Shears break after 179 uses (the default of the config definition) | `durability/shears_break_when_used_up` |
| Shears with a stored max damage (combined in a grindstone before the fix of WoodenShears#51) use the configured durability | `durability/stored_max_damage_is_ignored` |
| Config changes apply to already existing shears | `durability/config_change_applies_to_existing_shears` |

The limits of `above_maximum_is_limited` and `below_zero_makes_shears_unbreakable` are implemented differently per
loader: on Fabric the mod itself limits the value (`Math.clamp` in `FabricConfigHelper`), so the test checks the mod
code. On Forge and NeoForge the config system corrects values outside the range when it loads the config file
(`defineInRange`). The game test adapter of these loaders applies the same correction (`ValueSpec.correct`) when it
sets the value, so there the tests only check that the range of the config spec is 0 - 10000, not the loading of a
config file. On Forge, setting the value also saves the server config file in `forge/runs/gametest`
(`autosave`); this only affects the local test run directory.

### Crafting and repair

| Feature | Test(s) |
|---|---|
| Two clay balls placed diagonally (both directions) craft a clay shears part | `recipe/clay_shears_part_crafted_from_two_diagonal_clay_balls` |
| Two clay balls in a row / column do not match the clay shears part recipe | `recipe/clay_shears_part_not_crafted_from_two_clay_balls_in_a_row` |
| A clay shears part is smelted to a ceramic shears part (furnace, 200 ticks, 0.3 experience), also in a real furnace | `recipe/ceramic_shears_part_smelted_from_clay_shears_part`, `recipe/furnace_smelts_clay_shears_part` |
| Two ceramic shears parts in any position of the crafting grid craft ceramic shears (shapeless) | `recipe/crafted_from_two_ceramic_shears_parts` |
| One or three ceramic shears parts, two clay shears parts or a mix of both parts do not match the recipe | `recipe/not_crafted_from_other_items` |
| The ceramic shears recipe is in the "equipment" recipe book category, the part recipes in "misc" ([WoodenShears#27](https://github.com/cech12/WoodenShears/issues/27)) | `recipe/is_in_equipment_category` |
| All three recipes are unlocked when the player has clay balls (or already knows the recipe; fixed: the clay shears part advancement checked the ceramic shears part recipe) | `recipe/unlocked_by_having_clay` |
| Two damaged shears can be combined (remaining durability + 5%), result follows the config ([WoodenShears#38](https://github.com/cech12/WoodenShears/issues/38)) | `repair/combines_two_damaged_shears` |
| Fully repaired shears are identical to new shears ([WoodenShears#38](https://github.com/cech12/WoodenShears/issues/38)) | `repair/fully_repaired_shears_equal_new_shears` |
| Two damaged shears can be combined in a grindstone (remaining durability + 5%, enchantments removed) | `repair/grindstone_combines_two_damaged_shears` |
| Shears combined in a grindstone do not store a fixed max damage, so config changes still apply (fixed [WoodenShears#51](https://github.com/cech12/WoodenShears/issues/51)) | `repair/grindstone_result_follows_config` |
| Two damaged shears can be combined in an anvil (remaining durability + 12%, enchantments of both kept) | `repair/anvil_combines_two_damaged_shears` |
| Shears combined to full durability in an anvil store the same components as vanilla shears (only the repair cost; fixed: a damage of 0 was stored) | `repair/anvil_fully_repaired_shears_like_vanilla_shears` |
| A single enchanted shears is disenchanted in a grindstone, the damage is kept and no max damage is stored | `repair/grindstone_disenchants_single_shears` |
| Like vanilla shears there is no repair material: an anvil does not repair them with any item | `repair/no_repair_material_like_vanilla_shears` |
| Unbreakable shears (durability 0) cannot be combined (crafting grid, grindstone, anvil) | `repair/not_possible_when_unbreakable` |

### Shearing entities (right-click)

| Feature | Test(s) |
|---|---|
| Sheep: drops wool, sheared, shears lose one durability | `entity/shears_sheep` |
| Creative mode (sheep, mooshroom, snow golem) works and does not damage the shears | `entity/shears_in_creative_without_damage` |
| Mooshroom turns into a cow and drops red mushrooms, a brown mooshroom (own loot table) drops brown mushrooms | `entity/shears_mooshroom`, `entity/shears_brown_mooshroom` |
| Snow golem loses its pumpkin | `entity/shears_snow_golem` |
| Bogged can be sheared and drops two mushrooms (fixed known issue 1.21-3.1.0.0) | `entity/shears_bogged` |
| Copper golem loses its flower (1.21.9-3.6.0.1) | `entity/shears_copper_golem` |
| Sulfur cube ejects its swallowed item | `entity/shears_sulfur_cube` |
| Mobs that are not ready for shearing (sheared sheep and bogged, baby sheep and mooshroom, snow golem without pumpkin, copper golem without flower, empty sulfur cube, pig without saddle) behave like with vanilla shears: no drops, no durability loss | `entity/does_not_shear_when_not_ready` |
| Equipment like a saddle can be cut off, with the same durability loss as vanilla shears | `entity/removes_saddle` |
| Wolf armor can be removed by the owner, with the same durability loss as vanilla shears, and grants the advancement "Shear Brilliance" (fixed known issue 1.21-3.1.0.0) | `entity/removes_wolf_armor` |
| Saddles, horse and nautilus armor, llama carpets and happy ghast harnesses are removed from every mob that can wear them, exactly like with vanilla shears (pig, strider, horses, donkey, mule, camels, llamas, happy ghast, nautiluses) | `entity/removes_equipment_like_vanilla_shears` |
| Equipment is not removed when vanilla shears would not remove it either (sneaking player, ridden mob, Curse of Binding in survival, wolf armor of another owner): no drop, no durability loss | `entity/does_not_remove_equipment_when_not_allowed` |
| Leash of an entity can be cut (1.21.9-3.6.0.1) | `entity/cuts_leash_of_entity` |
| Leash knot on a fence can be removed and the shears lose one durability (1.21.9-3.6.0.1) | `entity/cuts_leash_knot` |

### Blocks

| Feature | Test(s) |
|---|---|
| Pumpkins can be carved (fixed 1.17.1-1.5.0.1) | `block/carves_pumpkin` |
| Full beehives and bee nests can be harvested (fixed 1.17.1-1.5.0.1) | `block/harvests_full_beehives` |
| Beehives and bee nests that are not full (honey level 0 - 4) are not harvested and the shears are not damaged | `block/does_not_harvest_beehives_without_honey` |
| Cutting a tripwire disarms it (fixed 1.17.1-1.5.0.1) | `block/disarms_tripwire`, control test `block/tripwire_triggers_without_shears` |
| Using shears on a plant tip stops its growth like vanilla shears, for every growing plant (twisting and weeping vines, cave vines, kelp, ...) | `block/stops_growth_of_plant_tips` |
| Every vanilla block whose loot table checks for shears drops the expected item with ceramic shears, in every block state that drops something (35 blocks in 26.3: all leaves, short/tall grass, fern, large fern, short/tall dry grass, bush, dead bush, seagrass, tall seagrass, nether sprouts, hanging roots, pale hanging moss, small dripleaf, glow lichen, vine, cobweb, twisting/weeping vines and their plant blocks). Every state is rolled with 16 fixed seeds (spread, because small consecutive seeds give almost the same first random value); with shears every seed must drop the same, so a failure is also noticed for the vines, which drop themselves with 33% without shears. Only the item is checked, not the count. The list of expected items must be complete: the vanilla blocks whose loot table mentions shears (on Forge/NeoForge as their shears tool action `shears_dig`) must be exactly the listed blocks. Blocks of other mods are intentionally not covered | `block/drops_like_vanilla_shears` |
| Breaking leaves, cobweb, grass, fern, dead bush and glow lichen (#11) drops them and damages the shears | `block/breaking_drops_shearable_blocks` |

### Dispenser

| Feature | Test(s) |
|---|---|
| Dispensers shear sheep, mooshrooms, snow golems, bogged, copper golems and sulfur cubes | `dispenser/shears_sheep`, `dispenser/shears_mooshroom`, `dispenser/shears_snow_golem`, `dispenser/shears_bogged`, `dispenser/shears_copper_golem`, `dispenser/shears_sulfur_cube` |
| Dispensers harvest full beehives (fixed crash #9) | `dispenser/harvests_full_beehive` |
| Dispensers cut leashes | `dispenser/cuts_leash` |
| Dispensers shear brown mooshrooms (brown mushrooms) | `dispenser/shears_brown_mooshroom` |
| Dispensers do not shear mobs that are not ready (sheared sheep) and keep the shears without a target: no drop, no durability loss, the shears stay in the dispenser | `dispenser/does_not_shear_when_not_ready`, `dispenser/keeps_shears_without_target` |
| Dispensers do not harvest beehives that are not full (honey level 4) | `dispenser/does_not_harvest_beehive_without_honey` |

### Data packs

Recommended for data packs: check for shears with the vanilla predicate `minecraft:tool/can_shear` (like vanilla
loot tables do) or with the tag `c:tools/shear`. Both accept ceramic shears on all loaders.

| Feature | Test(s) |
|---|---|
| Loot conditions that reference `minecraft:tool/can_shear` accept ceramic shears | `loot/can_shear_predicate_accepts_ceramic_shears` |
| `match_tool` conditions with the tag `c:tools/shear` accept ceramic shears; such data packs do not crash the world loading ([CeramicShears#28](https://github.com/cech12/CeramicShears/issues/28)) | `loot/match_tool_with_shears_tag_accepts_ceramic_shears` |
| `match_tool` conditions that name `minecraft:shears` directly accept ceramic shears on Fabric, but not on Forge and NeoForge (intended loader difference, see below) | `loot/match_tool_with_vanilla_shears_accepts_ceramic_shears_only_on_fabric` |

**Intended loader difference:** Forge and NeoForge replace the vanilla predicate `minecraft:tool/can_shear` with
their shears tool action (`forge:can_tool_perform_action` / `neoforge:can_item_perform_ability`, `shears_dig`),
which ceramic shears support as a shears item. Fabric has no such tool action and keeps the vanilla predicate, so the
mod adds ceramic shears to every `match_tool` condition that contains vanilla shears (`MatchToolMixin`). That is why a
data pack that names `minecraft:shears` directly also accepts ceramic shears on Fabric, but not on Forge and NeoForge.
This is not a bug: such data packs should use the predicate or the tag, which work everywhere. The test checks the
expected result on each loader, so a change of this behavior is noticed.

### Resources and test setup

| Feature | Test(s) |
|---|---|
| Names of the ceramic shears and both parts translated in all shipped languages (all files in `assets/ceramicshears/lang/`, found automatically), English "Ceramic Shears", "Ceramic Shears Part", "Clay Shears Part" | `resources/item_name_is_translated` |
| Item model definition, model and texture of all three items exist and reference each other | `resources/item_model_and_texture_exist` |
| The config screen tooltip (Fabric) shows the real default (179) and range (0 - 10000) in every language that translates it | `resources/config_tooltip_shows_default_and_range` |
| Every test function has a test instance that runs exactly this function | `meta/every_test_function_has_an_instance` |
| The platform name of the mod names the running loader (fixed: the Fabric version reported "NeoForge") | `meta/platform_name_matches_the_loader` |
| The release jar starts on a dedicated server, with and without integrations (Cloth Config and ModMenu on Fabric) | server start test |

### Known issues (marked with `KnownIssues`)

Currently none.

### Checked against the Minecraft Wiki

The feature list was checked against the [Minecraft Wiki page about shears](https://minecraft.wiki/w/Shears) (all
uses and the complete version history up to 26.3). Vanilla behavior that does not depend on the kind of shears is not
tested separately: pumpkin face direction, bees getting angry, sounds, the "used" statistic (carving a pumpkin counts
for vanilla shears on all loaders, `PumpkinBlock` names `Items.SHEARS` directly; accepted), loot of chests and villager trades of vanilla
shears.

## Not tested automatically

These features cannot be checked on a dedicated test server. Check them manually before a release:

| Feature | Why | Manual check |
|---|---|---|
| Item textures and models (shears and both parts) in the inventory and in the hand | Client rendering | Start `runClient`, look at the item in the inventory, hotbar and in first/third person |
| Item name and tooltip in the game | Client language loading (the files are tested) | Switch the language once, check the item name |
| Config screen via ModMenu (Fabric) | Client GUI | Fabric client: Mods > Ceramic Shears > config, change durability, reopen |
| Config file creation and editing (`config/ceramicshears.toml` on Fabric, `config/ceramicshears-synced.toml` on NeoForge, server config `serverconfig/ceramicshears-server.toml` on Forge) | The tests change the config at runtime, not via the file | Start a server, change `durability` in the file, restart, check the max damage of new shears |
| Creative tabs in the client | Client GUI (the tab contents are tested) | Open the "Tools & Utilities" and the "Ingredients" tab |
