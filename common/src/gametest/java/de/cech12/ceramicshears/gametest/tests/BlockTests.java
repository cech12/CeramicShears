package de.cech12.ceramicshears.gametest.tests;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cech12.ceramicshears.Constants;
import de.cech12.ceramicshears.gametest.TestUtils;
import de.cech12.ceramicshears.gametest.CeramicShearsGameTests;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.LongStream;

/**
 * Using and breaking blocks with ceramic shears does everything vanilla shears do.
 */
public final class BlockTests {

    private BlockTests() {}

    public static void register(CeramicShearsGameTests.TestCollector tests) {
        tests.add("block/carves_pumpkin", BlockTests::carvesPumpkin);
        tests.add("block/harvests_full_beehives", BlockTests::harvestsFullBeehives);
        tests.add("block/does_not_harvest_beehives_without_honey", BlockTests::doesNotHarvestBeehivesWithoutHoney);
        tests.add("block/disarms_tripwire", BlockTests::disarmsTripwire);
        tests.add("block/tripwire_triggers_without_shears", BlockTests::tripwireTriggersWithoutShears);
        tests.add("block/stops_growth_of_plant_tips", BlockTests::stopsGrowthOfPlantTips);
        tests.add("block/drops_like_vanilla_shears", BlockTests::dropsLikeVanillaShears);
        tests.add("block/breaking_drops_shearable_blocks", BlockTests::breakingDropsShearableBlocks);
    }

    private static ServerPlayer survivalPlayer(GameTestHelper helper) {
        return TestUtils.player(helper, GameType.SURVIVAL, TestUtils.ceramicShears());
    }

    /** Fixed 1.17.1-1.5.0.1: using shears on a pumpkin carves it and drops pumpkin seeds. */
    public static void carvesPumpkin(GameTestHelper helper) {
        helper.setBlock(TestUtils.CENTER, Blocks.PUMPKIN);
        ServerPlayer player = survivalPlayer(helper);
        helper.useBlock(TestUtils.CENTER, player);
        helper.assertBlockPresent(Blocks.CARVED_PUMPKIN, TestUtils.CENTER);
        helper.assertItemEntityPresent(Items.PUMPKIN_SEEDS);
        TestUtils.assertDamage(helper, player.getMainHandItem(), 1, "after carving a pumpkin");
        helper.succeed();
    }

    /**
     * Fixed 1.17.1-1.5.0.1: using shears on a full beehive or bee nest drops honeycombs and empties it. Hive and nest are
     * 4 blocks apart and the drops are searched within 1.5 blocks, so a check never finds the honeycombs of the other.
     */
    public static void harvestsFullBeehives(GameTestHelper helper) {
        BlockPos hive = TestUtils.CENTER.west(2);
        BlockPos nest = TestUtils.CENTER.east(2);
        helper.setBlock(hive, Blocks.BEEHIVE.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL, BeehiveBlock.MAX_HONEY_LEVELS));
        helper.setBlock(nest, Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL, BeehiveBlock.MAX_HONEY_LEVELS));
        ServerPlayer player = survivalPlayer(helper);
        helper.useBlock(hive, player);
        helper.useBlock(nest, player);
        helper.assertBlockProperty(hive, BeehiveBlock.HONEY_LEVEL, 0);
        helper.assertBlockProperty(nest, BeehiveBlock.HONEY_LEVEL, 0);
        helper.assertItemEntityPresent(Items.HONEYCOMB, hive, 1.5);
        helper.assertItemEntityPresent(Items.HONEYCOMB, nest, 1.5);
        TestUtils.assertDamage(helper, player.getMainHandItem(), 2, "after harvesting two beehives");
        helper.succeed();
    }

    /** Beehives and bee nests that are not full (honey level 0 - 4) are not harvested and the shears are not damaged. */
    public static void doesNotHarvestBeehivesWithoutHoney(GameTestHelper helper) {
        BlockPos hive = TestUtils.CENTER.west();
        BlockPos nest = TestUtils.CENTER.east();
        ServerPlayer player = survivalPlayer(helper);
        for (int level = 0; level < BeehiveBlock.MAX_HONEY_LEVELS; level++) {
            helper.setBlock(hive, Blocks.BEEHIVE.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL, level));
            helper.setBlock(nest, Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL, level));
            helper.useBlock(hive, player);
            helper.useBlock(nest, player);
            helper.assertBlockProperty(hive, BeehiveBlock.HONEY_LEVEL, level);
            helper.assertBlockProperty(nest, BeehiveBlock.HONEY_LEVEL, level);
        }
        helper.assertItemEntityNotPresent(Items.HONEYCOMB);
        TestUtils.assertDamage(helper, player.getMainHandItem(), 0, "after using it on beehives without enough honey");
        helper.succeed();
    }

    // Tripwire setup: stone - hook (facing east) - tripwire - hook (facing west) - stone
    private static final BlockPos HOOK_WEST = new BlockPos(1, 1, 2);
    private static final BlockPos WIRE = new BlockPos(2, 1, 2);
    private static final BlockPos HOOK_EAST = new BlockPos(3, 1, 2);

    private static void buildTripwire(GameTestHelper helper) {
        helper.setBlock(HOOK_WEST.west(), Blocks.STONE);
        helper.setBlock(HOOK_EAST.east(), Blocks.STONE);
        helper.setBlock(WIRE, Blocks.TRIPWIRE.defaultBlockState()
                .setValue(TripWireBlock.ATTACHED, true)
                .setValue(TripWireBlock.EAST, true)
                .setValue(TripWireBlock.WEST, true));
        BlockState hook = Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.ATTACHED, true);
        helper.setBlock(HOOK_WEST, hook.setValue(TripWireHookBlock.FACING, Direction.EAST));
        helper.setBlock(HOOK_EAST, hook.setValue(TripWireHookBlock.FACING, Direction.WEST));
    }

    private static boolean anyHookPowered(GameTestHelper helper) {
        return helper.getBlockState(HOOK_WEST).getValue(TripWireHookBlock.POWERED) || helper.getBlockState(HOOK_EAST).getValue(TripWireHookBlock.POWERED);
    }

    /** Fixed 1.17.1-1.5.0.1: cutting a tripwire with shears disarms it, the hooks do not send a redstone signal. */
    public static void disarmsTripwire(GameTestHelper helper) {
        buildTripwire(helper);
        ServerPlayer player = survivalPlayer(helper);
        helper.assertTrue(TestUtils.destroyBlock(helper, player, WIRE), "tripwire could not be broken");
        helper.assertBlockNotPresent(Blocks.TRIPWIRE, WIRE);
        TestUtils.assertDamage(helper, player.getMainHandItem(), 1, "after cutting a tripwire");
        helper.startSequence()
                .thenExecuteFor(15, () -> helper.assertTrue(!anyHookPowered(helper), "tripwire hook was triggered although the tripwire was cut with shears"))
                .thenSucceed();
    }

    /** Control test for disarmsTripwire: without shears the same setup triggers the hooks. */
    public static void tripwireTriggersWithoutShears(GameTestHelper helper) {
        buildTripwire(helper);
        ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, ItemStack.EMPTY);
        helper.assertTrue(TestUtils.destroyBlock(helper, player, WIRE), "tripwire could not be broken");
        helper.assertTrue(anyHookPowered(helper), "tripwire test setup is broken: hooks are not triggered without shears");
        helper.succeed();
    }

    /** Using shears on the tip of a growing plant (e.g. twisting vines) stops its growth. */
    public static void stopsGrowthOfPlantTips(GameTestHelper helper) {
        List<String> tested = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!(block instanceof GrowingPlantHeadBlock head)) {
                continue;
            }
            String name = BuiltInRegistries.BLOCK.getKey(block).toString();
            tested.add(name);
            BlockState vanilla = trimPlantTip(helper, block, new ItemStack(Items.SHEARS));
            BlockState ceramic = trimPlantTip(helper, block, TestUtils.ceramicShears());
            if (vanilla == null || !head.isMaxAge(vanilla)) {
                problems.add(name + ": test setup broken, vanilla shears do not stop the growth");
            } else if (!vanilla.equals(ceramic)) {
                problems.add(name + ": " + ceramic + " instead of " + vanilla);
            }
        }
        helper.assertTrue(tested.containsAll(List.of("minecraft:twisting_vines", "minecraft:weeping_vines", "minecraft:cave_vines", "minecraft:kelp")),
                "test setup broken: known plant tips not found, only " + tested);
        helper.assertTrue(problems.isEmpty(), "using ceramic shears on plant tips differs from vanilla shears: " + problems);
        helper.succeed();
    }

    /** Places a young plant tip, uses the shears on it and returns the new state, or null if the shears were not damaged. */
    private static BlockState trimPlantTip(GameTestHelper helper, Block block, ItemStack shears) {
        BlockState young = block.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, 0);
        if (young.hasProperty(BlockStateProperties.WATERLOGGED)) {
            young = young.setValue(BlockStateProperties.WATERLOGGED, true);
        }
        helper.setBlock(TestUtils.CENTER, young);
        ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, shears);
        helper.useBlock(TestUtils.CENTER, player);
        BlockState result = helper.getBlockState(TestUtils.CENTER);
        boolean damaged = player.getMainHandItem().getDamageValue() == 1;
        helper.setBlock(TestUtils.CENTER, Blocks.AIR);
        return damaged ? result : null;
    }

    /** Position where both halves of double plants are placed for the loot rolls. */
    private static final BlockPos DOUBLE_PLANT_POS = TestUtils.CENTER;

    /**
     * Fixed seeds for the loot rolls. With shears the drops are not random. Several seeds are needed so that a random
     * drop without shears that happens to be the expected item (33% for the vines) cannot hide a failure. Seed 0 is not
     * allowed: vanilla then uses the random of the level. The seeds are spread with the golden ratio multiplier, because
     * the legacy random of the loot context returns almost the same first value for small consecutive seeds (about 0.73
     * for the seeds 1 - 16), which would make them act like a single seed.
     */
    private static final long[] LOOT_SEEDS = LongStream.rangeClosed(1, 16).map(i -> i * 0x9E3779B97F4A7C15L).toArray();

    /**
     * All vanilla blocks whose loot tables check for shears (data/minecraft/loot_table/blocks of 26.3) and the item they
     * drop with shears. With shears the drop of these blocks is not random, only the drops without shears are (leaves,
     * grass, dead bush, vines). The test fails if this list does not match the vanilla loot tables that mention shears,
     * so a new Minecraft version with new shears blocks is noticed.
     */
    private static final Map<String, String> VANILLA_SHEARS_DROPS = Map.ofEntries(
            // leaves: the leaves themselves instead of random saplings, sticks and apples
            Map.entry("acacia_leaves", "acacia_leaves"), Map.entry("azalea_leaves", "azalea_leaves"),
            Map.entry("birch_leaves", "birch_leaves"), Map.entry("cherry_leaves", "cherry_leaves"),
            Map.entry("dark_oak_leaves", "dark_oak_leaves"), Map.entry("flowering_azalea_leaves", "flowering_azalea_leaves"),
            Map.entry("jungle_leaves", "jungle_leaves"), Map.entry("mangrove_leaves", "mangrove_leaves"),
            Map.entry("oak_leaves", "oak_leaves"), Map.entry("orange_poplar_leaves", "orange_poplar_leaves"),
            Map.entry("pale_oak_leaves", "pale_oak_leaves"), Map.entry("red_poplar_leaves", "red_poplar_leaves"),
            Map.entry("spruce_leaves", "spruce_leaves"), Map.entry("yellow_poplar_leaves", "yellow_poplar_leaves"),
            // grass and plants: the plant instead of nothing or random seeds / sticks
            Map.entry("short_grass", "short_grass"), Map.entry("tall_grass", "short_grass"),
            Map.entry("fern", "fern"), Map.entry("large_fern", "fern"),
            Map.entry("short_dry_grass", "short_dry_grass"), Map.entry("tall_dry_grass", "tall_dry_grass"),
            Map.entry("bush", "bush"), Map.entry("dead_bush", "dead_bush"),
            Map.entry("seagrass", "seagrass"), Map.entry("tall_seagrass", "seagrass"),
            Map.entry("nether_sprouts", "nether_sprouts"), Map.entry("hanging_roots", "hanging_roots"),
            Map.entry("pale_hanging_moss", "pale_hanging_moss"), Map.entry("small_dripleaf", "small_dripleaf"),
            Map.entry("glow_lichen", "glow_lichen"), Map.entry("vine", "vine"), Map.entry("cobweb", "cobweb"),
            // vines: always the vines instead of a random 33% chance
            Map.entry("twisting_vines", "twisting_vines"), Map.entry("twisting_vines_plant", "twisting_vines"),
            Map.entry("weeping_vines", "weeping_vines"), Map.entry("weeping_vines_plant", "weeping_vines")
    );

    /**
     * Every block of {@link #VANILLA_SHEARS_DROPS} drops the listed item with ceramic shears (leaves, grass, vines,
     * cobweb, ... drop themselves, tall grass drops short grass, ...). Every block state is rolled with every seed of
     * {@link #LOOT_SEEDS}: every dropped item must be the expected item, and a state must drop it with every seed or
     * with none (some states drop nothing, e.g. glow lichen without a face). Every block must drop it in at least one
     * state. The count is not checked. Fabric extends the vanilla shears conditions
     * (MatchToolMixin), Forge/NeoForge use their shears tool action.
     * <p>
     * Completeness: the vanilla blocks whose serialized loot table mentions shears must be exactly the blocks of
     * {@link #VANILLA_SHEARS_DROPS}. Forge/NeoForge replace "minecraft:tool/can_shear" with their shears tool action
     * ("shears_dig"), which mentions shears as well. Blocks of other mods are not checked.
     */
    public static void dropsLikeVanillaShears(GameTestHelper helper) {
        var level = helper.getLevel();
        var lootTables = level.getServer().reloadableRegistries();
        RegistryOps<JsonElement> ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        ItemStack ceramic = TestUtils.ceramicShears();
        Vec3 origin = helper.absoluteVec(Vec3.atCenterOf(TestUtils.CENTER));
        long startTime = System.nanoTime();
        List<String> problems = new ArrayList<>();
        Set<String> problemBlocks = new TreeSet<>();
        List<String> errors = new ArrayList<>();
        Set<String> blocksMentioningShears = new TreeSet<>();
        int blocks = 0;
        int states = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
            var key = block.getLootTable();
            if (!blockId.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) || key.isEmpty()) {
                continue;
            }
            LootTable table = lootTables.getLootTable(key.get());
            var json = LootTable.DIRECT_CODEC.encodeStart(ops, table);
            if (json.isError()) {
                errors.add(blockId + ": loot table cannot be serialized (" + json.error().map(Object::toString).orElse("") + ")");
            } else if (json.getOrThrow().toString().contains("shear")) {
                blocksMentioningShears.add(blockId.getPath());
            }
            String expected = VANILLA_SHEARS_DROPS.get(blockId.getPath());
            if (expected == null) {
                continue;
            }
            blocks++;
            Identifier expectedItem = Identifier.withDefaultNamespace(expected);
            boolean droppedExpected = false;
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                states++;
                Vec3 lootOrigin = origin;
                boolean doublePlant = state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF);
                if (doublePlant) {
                    // loot tables of double plants (tall grass, large fern, ...) check that the other half exists
                    BlockState lower = state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
                    placeWithoutUpdates(helper, DOUBLE_PLANT_POS, lower);
                    placeWithoutUpdates(helper, DOUBLE_PLANT_POS.above(), lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
                    BlockPos lootPos = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? DOUBLE_PLANT_POS : DOUBLE_PLANT_POS.above();
                    lootOrigin = helper.absoluteVec(Vec3.atCenterOf(lootPos));
                }
                try {
                    List<Long> seedsWithDrop = new ArrayList<>();
                    List<Long> seedsWithoutDrop = new ArrayList<>();
                    for (long seed : LOOT_SEEDS) {
                        List<ItemStack> drops = table.getRandomItems(params(level, lootOrigin, state, ceramic), seed);
                        boolean dropped = false;
                        for (ItemStack drop : drops) {
                            if (drop.isEmpty()) {
                                continue;
                            }
                            if (BuiltInRegistries.ITEM.getKey(drop.getItem()).equals(expectedItem)) {
                                dropped = true;
                            } else {
                                problems.add(state + " drops " + drops + " instead of " + expectedItem + " (seed " + seed + ")");
                                problemBlocks.add(blockId.getPath());
                                break;
                            }
                        }
                        (dropped ? seedsWithDrop : seedsWithoutDrop).add(seed);
                    }
                    if (!seedsWithDrop.isEmpty() && !seedsWithoutDrop.isEmpty()) {
                        problems.add(state + " drops " + expectedItem + " only randomly: with seeds " + seedsWithDrop
                                + ", nothing with seeds " + seedsWithoutDrop);
                        problemBlocks.add(blockId.getPath());
                    }
                    droppedExpected |= !seedsWithDrop.isEmpty();
                } catch (RuntimeException e) {
                    errors.add(state + ": " + e);
                } finally {
                    if (doublePlant) {
                        placeWithoutUpdates(helper, DOUBLE_PLANT_POS.above(), Blocks.AIR.defaultBlockState());
                        placeWithoutUpdates(helper, DOUBLE_PLANT_POS, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            if (!droppedExpected) {
                problems.add(blockId + " never drops " + expectedItem);
                problemBlocks.add(blockId.getPath());
            }
        }
        helper.assertTrue(errors.isEmpty(), errors.size() + " loot tables could not be evaluated: " + errors.stream().limit(10).toList());
        List<String> missing = VANILLA_SHEARS_DROPS.keySet().stream().filter(path -> !blocksMentioningShears.contains(path)).sorted().toList();
        helper.assertTrue(missing.isEmpty(), "loot tables of " + missing + " do not mention shears (anymore), remove them from VANILLA_SHEARS_DROPS");
        List<String> additional = blocksMentioningShears.stream().filter(path -> !VANILLA_SHEARS_DROPS.containsKey(path)).toList();
        helper.assertTrue(additional.isEmpty(), "loot tables of " + additional + " mention shears, add them to VANILLA_SHEARS_DROPS");
        helper.assertTrue(problems.isEmpty(), problems.size() + " unexpected drops with ceramic shears of " + problemBlocks + ": "
                + problems.stream().limit(10).toList());
        Constants.LOG.info("Checked the drops with ceramic shears of {} blocks ({} block states, {} seeds each) in {} ms", blocks, states,
                LOOT_SEEDS.length, (System.nanoTime() - startTime) / 1_000_000);
        helper.succeed();
    }

    /** Sets a block without neighbor updates, so plants do not break on the stone floor. */
    private static void placeWithoutUpdates(GameTestHelper helper, BlockPos pos, BlockState state) {
        helper.getLevel().setBlock(helper.absolutePos(pos), state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
    }

    private static LootParams params(net.minecraft.server.level.ServerLevel level, Vec3 origin, BlockState state, ItemStack tool) {
        return new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, origin)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.TOOL, tool)
                .create(LootContextParamSets.BLOCK);
    }

    /**
     * Fixed #11 (glow lichen) and 1.18.2 breaking behavior: really breaking shearable blocks in survival drops the
     * blocks themselves and damages the shears by one per block.
     */
    public static void breakingDropsShearableBlocks(GameTestHelper helper) {
        Map<BlockPos, BlockState> blocks = Map.of(
                new BlockPos(0, 1, 0), Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true),
                new BlockPos(2, 1, 0), Blocks.COBWEB.defaultBlockState(),
                new BlockPos(4, 1, 0), Blocks.SHORT_GRASS.defaultBlockState(),
                new BlockPos(0, 1, 2), Blocks.FERN.defaultBlockState(),
                new BlockPos(4, 1, 2), Blocks.DEAD_BUSH.defaultBlockState(),
                new BlockPos(0, 1, 4), Blocks.GLOW_LICHEN.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true)
        );
        blocks.forEach((pos, state) -> {
            helper.setBlock(pos.below(), Blocks.GRASS_BLOCK);
            helper.setBlock(pos, state);
        });
        ServerPlayer player = survivalPlayer(helper);
        blocks.forEach((pos, state) -> {
            helper.assertTrue(TestUtils.destroyBlock(helper, player, pos), "could not break " + state);
            helper.assertItemEntityPresent(state.getBlock().asItem(), pos, 1.5);
        });
        TestUtils.assertDamage(helper, player.getMainHandItem(), blocks.size(), "after breaking " + blocks.size() + " blocks");
        helper.succeed();
    }

}
