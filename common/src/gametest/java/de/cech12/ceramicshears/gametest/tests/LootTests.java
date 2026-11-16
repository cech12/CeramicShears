package de.cech12.ceramicshears.gametest.tests;

import de.cech12.ceramicshears.Constants;
import de.cech12.ceramicshears.gametest.Platforms;
import de.cech12.ceramicshears.gametest.TestUtils;
import de.cech12.ceramicshears.gametest.CeramicShearsGameTests;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Loot tables of data packs that check for shears. The test loot tables are part of the gametest resources
 * (data/ceramicshears/loot_table/gametest/*.json) and drop a diamond if their shears condition matches.
 * <p>
 * Recommended for data packs (works on all loaders): use the tag "c:tools/shear". Minecraft 1.21.11 has no shears predicate
 * yet ("minecraft:tool/can_shear" was added in 26.3), vanilla loot tables name "minecraft:shears" directly. Forge and
 * NeoForge replace these vanilla conditions with their shears tool action, Fabric keeps them and adds ceramic shears to
 * them (MatchToolMixin).
 */
public final class LootTests {

    private LootTests() {}

    public static void register(CeramicShearsGameTests.TestCollector tests) {
        tests.add("loot/match_tool_with_shears_tag_accepts_ceramic_shears", LootTests::matchToolWithShearsTag);
        tests.add("loot/match_tool_with_vanilla_shears_accepts_ceramic_shears_only_on_fabric", LootTests::matchToolWithVanillaShears);
    }

    private static List<ItemStack> roll(GameTestHelper helper, String table, ItemStack tool) {
        var level = helper.getLevel();
        LootTable lootTable = level.getServer().reloadableRegistries()
                .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, Constants.id("gametest/" + table)));
        helper.assertTrue(lootTable != LootTable.EMPTY, "test loot table " + table + " is missing");
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(Vec3.atCenterOf(TestUtils.CENTER)))
                .withParameter(LootContextParams.BLOCK_STATE, Blocks.STONE.defaultBlockState())
                .withParameter(LootContextParams.TOOL, tool)
                .create(LootContextParamSets.BLOCK);
        return lootTable.getRandomItems(params, 1L); // fixed seed, 0 would use the random of the level
    }

    private static boolean dropsDiamond(List<ItemStack> drops) {
        return drops.stream().anyMatch(s -> s.is(Items.DIAMOND));
    }

    /** Checks that the test loot table itself works: vanilla shears match, an empty hand does not. */
    private static void assertTableWorks(GameTestHelper helper, String table) {
        helper.assertTrue(dropsDiamond(roll(helper, table, new ItemStack(Items.SHEARS))),
                "test loot table " + table + " is broken: vanilla shears do not match");
        helper.assertTrue(roll(helper, table, ItemStack.EMPTY).isEmpty(), "test loot table " + table + " is broken: an empty hand matches");
    }

    private static void assertMatches(GameTestHelper helper, String table) {
        assertTableWorks(helper, table);
        helper.assertTrue(dropsDiamond(roll(helper, table, TestUtils.ceramicShears())),
                "ceramic shears do not match the shears condition of loot table " + table);
    }

    /**
     * Regression https://github.com/cech12/CeramicShears/issues/28: data packs with a shears tag as "match_tool"
     * condition crashed the world loading (this test would not even start) and must accept ceramic shears.
     */
    public static void matchToolWithShearsTag(GameTestHelper helper) {
        assertMatches(helper, "match_tool_shears_tag");
        helper.succeed();
    }

    /**
     * Intended loader difference for "match_tool" conditions that name vanilla shears directly ("items": "minecraft:shears"):
     * on Fabric they also accept ceramic shears, because Fabric has no shears tool action and the MatchToolMixin extends
     * these conditions. Forge and NeoForge intentionally keep such explicit item checks as they are (data packs should use
     * the tag, see above), so there ceramic shears must NOT match. Any change of this behavior fails here.
     */
    public static void matchToolWithVanillaShears(GameTestHelper helper) {
        String table = "match_tool_vanilla_shears";
        assertTableWorks(helper, table);
        boolean matches = dropsDiamond(roll(helper, table, TestUtils.ceramicShears()));
        if (Platforms.isFabric()) {
            helper.assertTrue(matches, "on Fabric ceramic shears must match a match_tool condition that names minecraft:shears");
        } else {
            helper.assertTrue(!matches, "on " + Platforms.current() + " ceramic shears must not match a match_tool condition that names "
                    + "minecraft:shears (intended loader difference, see TESTING.md), but they do");
        }
        helper.succeed();
    }

}
