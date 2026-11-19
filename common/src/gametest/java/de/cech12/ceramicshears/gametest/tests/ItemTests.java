package de.cech12.ceramicshears.gametest.tests;

import de.cech12.ceramicshears.gametest.GameTestPlatform;
import de.cech12.ceramicshears.gametest.TestUtils;
import de.cech12.ceramicshears.gametest.CeramicShearsGameTests;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Basic properties of the ceramic shears item.
 */
public final class ItemTests {

    private static final TagKey<Item> COMMON_SHEARS_TAG = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "tools/shear"));

    private ItemTests() {}

    public static void register(CeramicShearsGameTests.TestCollector tests) {
        tests.add("item/is_registered_as_shears", ItemTests::isRegisteredAsShears);
        tests.add("item/does_not_stack", ItemTests::doesNotStack);
        tests.add("item/is_in_tools_and_utilities_tab", ItemTests::isInToolsAndUtilitiesTab);
        tests.add("item/is_in_common_shears_tag", ItemTests::isInCommonShearsTag);
        tests.add("item/is_enchantable_like_vanilla_shears", ItemTests::isEnchantableLikeVanillaShears);
        tests.add("item/mines_like_vanilla_shears", ItemTests::minesLikeVanillaShears);
        tests.add("item/has_the_components_of_vanilla_shears", ItemTests::hasTheComponentsOfVanillaShears);
        tests.add("item/loses_durability_per_block_like_vanilla_shears", ItemTests::losesDurabilityPerBlockLikeVanillaShears);
        tests.add("item/mending_repairs_like_vanilla_shears", ItemTests::mendingRepairsLikeVanillaShears);
        tests.add("item/is_no_fuel_like_vanilla_shears", ItemTests::isNoFuelLikeVanillaShears);
        tests.add("item/shears_parts_are_registered", ItemTests::shearsPartsAreRegistered);
        tests.add("item/shears_parts_are_in_ingredients_tab", ItemTests::shearsPartsAreInIngredientsTab);
    }

    /** "ceramicshears:ceramic_shears" exists and is a shears item (vanilla and loader shears logic applies). */
    public static void isRegisteredAsShears(GameTestHelper helper) {
        Item item = TestUtils.ceramicShearsItem();
        helper.assertTrue(item != Items.AIR, "ceramicshears:ceramic_shears is not registered");
        helper.assertTrue(item instanceof ShearsItem, "ceramic shears are no ShearsItem");
        helper.succeed();
    }

    /** Regression 1.18.2-1.6.1.1: ceramic shears could be stacked. */
    public static void doesNotStack(GameTestHelper helper) {
        helper.assertValueEqual(TestUtils.ceramicShears().getMaxStackSize(), 1, "max stack size");
        helper.succeed();
    }

    /** Ceramic shears are listed in the creative tab "Tools and Utilities". */
    public static void isInToolsAndUtilitiesTab(GameTestHelper helper) {
        var level = helper.getLevel();
        CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(), true, level.registryAccess());
        // own key: CreativeModeTabs.TOOLS_AND_UTILITIES is private in vanilla (only the loaders widen it)
        var tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(
                ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("tools_and_utilities")));
        boolean contained = tab.getDisplayItems().stream().anyMatch(stack -> stack.is(TestUtils.ceramicShearsItem()));
        helper.assertTrue(contained, "ceramic shears are missing in the tools and utilities creative tab");
        helper.succeed();
    }

    /** WoodenShears#17: ceramic shears are part of the common "c:tools/shear" tag, so other mods treat them as shears. */
    public static void isInCommonShearsTag(GameTestHelper helper) {
        helper.assertTrue(TestUtils.ceramicShears().is(COMMON_SHEARS_TAG), "ceramic shears are not in #c:tools/shear");
        helper.succeed();
    }

    /**
     * Regression 1.20.6-2.3.0.1: ceramic shears could not be enchanted.
     * Ceramic shears accept exactly the enchantments vanilla shears accept, at least Efficiency, Unbreaking and Mending.
     */
    public static void isEnchantableLikeVanillaShears(GameTestHelper helper) {
        ItemStack ceramic = TestUtils.ceramicShears();
        ItemStack vanilla = new ItemStack(Items.SHEARS);
        helper.assertTrue(ceramic.is(ItemTags.DURABILITY_ENCHANTABLE), "ceramic shears are not in #minecraft:enchantable/durability");
        helper.assertTrue(ceramic.is(ItemTags.MINING_ENCHANTABLE), "ceramic shears are not in #minecraft:enchantable/mining");
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (var key : List.of(Enchantments.EFFICIENCY, Enchantments.UNBREAKING, Enchantments.MENDING)) {
            helper.assertTrue(enchantments.getOrThrow(key).value().canEnchant(ceramic), "ceramic shears cannot be enchanted with " + key.identifier());
        }
        List<String> differences = new ArrayList<>();
        enchantments.listElements().forEach(holder -> {
            Enchantment enchantment = holder.value();
            if (enchantment.canEnchant(ceramic) != enchantment.canEnchant(vanilla)) {
                differences.add(holder.key().identifier().toString());
            }
        });
        helper.assertTrue(differences.isEmpty(), "enchantability differs from vanilla shears for " + differences);
        // like vanilla shears only enchantable with an anvil, not at the enchanting table
        helper.assertTrue(Objects.equals(ceramic.get(DataComponents.ENCHANTABLE), vanilla.get(DataComponents.ENCHANTABLE)),
                "enchanting table enchantability differs from vanilla shears: " + ceramic.get(DataComponents.ENCHANTABLE));
        helper.succeed();
    }

    /** Components that are allowed to differ from vanilla shears: name, model and the durability (config). */
    private static final java.util.Set<net.minecraft.core.component.DataComponentType<?>> OWN_COMPONENTS = java.util.Set.of(
            DataComponents.ITEM_NAME, DataComponents.ITEM_MODEL, DataComponents.MAX_DAMAGE, DataComponents.DAMAGE);

    /**
     * Ceramic shears have all default components of vanilla shears with the same values (tool rules incl. breaking
     * blocks in creative, stack size, enchantability, repair cost, rarity, ...), except name, model and durability. A
     * new property of vanilla shears in a future Minecraft version is noticed here.
     */
    public static void hasTheComponentsOfVanillaShears(GameTestHelper helper) {
        var mod = TestUtils.ceramicShearsItem().components();
        var vanilla = Items.SHEARS.components();
        java.util.Set<net.minecraft.core.component.DataComponentType<?>> types = new java.util.HashSet<>(mod.keySet());
        types.addAll(vanilla.keySet());
        types.removeAll(OWN_COMPONENTS);
        List<String> differences = new ArrayList<>();
        for (var type : types) {
            if (!Objects.equals(mod.get(type), vanilla.get(type))) {
                differences.add(type + ": " + mod.get(type) + " instead of " + vanilla.get(type));
            }
        }
        helper.assertTrue(differences.isEmpty(), "components differ from vanilla shears: " + differences);
        helper.succeed();
    }

    /** Ceramic shears have the same mining speed and "correct tool" behavior as vanilla shears for every block. */
    public static void minesLikeVanillaShears(GameTestHelper helper) {
        ItemStack ceramic = TestUtils.ceramicShears();
        ItemStack vanilla = new ItemStack(Items.SHEARS);
        helper.assertValueEqual(ceramic.getDestroySpeed(Blocks.COBWEB.defaultBlockState()), 15.0F, "destroy speed on cobweb");
        helper.assertTrue(ceramic.isCorrectToolForDrops(Blocks.COBWEB.defaultBlockState()), "ceramic shears are not the correct tool for cobweb");
        List<String> differences = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            BlockState state = block.defaultBlockState();
            if (ceramic.getDestroySpeed(state) != vanilla.getDestroySpeed(state)
                    || ceramic.isCorrectToolForDrops(state) != vanilla.isCorrectToolForDrops(state)) {
                differences.add(BuiltInRegistries.BLOCK.getKey(block).toString());
            }
        }
        helper.assertTrue(differences.isEmpty(), "mining behavior differs from vanilla shears for " + differences);
        helper.succeed();
    }

    /**
     * Breaking a block costs the same durability as with vanilla shears for every block: 1 for every block except fire,
     * also for blocks without hardness like grass (WoodenShears#40 asked for no loss on such blocks and was closed in
     * favor of the vanilla behavior).
     */
    public static void losesDurabilityPerBlockLikeVanillaShears(GameTestHelper helper) {
        ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, ItemStack.EMPTY);
        BlockPos pos = helper.absolutePos(TestUtils.CENTER);
        List<String> differences = new ArrayList<>();
        int damaging = 0;
        for (Block block : BuiltInRegistries.BLOCK) {
            BlockState state = block.defaultBlockState();
            ItemStack ceramic = TestUtils.ceramicShears();
            ItemStack vanilla = new ItemStack(Items.SHEARS);
            ceramic.mineBlock(helper.getLevel(), state, pos, player);
            vanilla.mineBlock(helper.getLevel(), state, pos, player);
            if (ceramic.getDamageValue() != vanilla.getDamageValue()) {
                differences.add(BuiltInRegistries.BLOCK.getKey(block) + " (" + ceramic.getDamageValue() + " instead of " + vanilla.getDamageValue() + ")");
            }
            if (vanilla.getDamageValue() > 0) {
                damaging++;
            }
        }
        helper.assertTrue(damaging > 0, "test setup broken: vanilla shears lose no durability on any block");
        helper.assertTrue(differences.isEmpty(), "durability loss when breaking blocks differs from vanilla shears: " + differences);
        helper.succeed();
    }

    /**
     * Mending repairs damaged ceramic shears with experience exactly like vanilla shears (the max damage comes from the
     * config, not from the item). Shears repaired to full durability store the same components as vanilla shears.
     */
    public static void mendingRepairsLikeVanillaShears(GameTestHelper helper) {
        var mending = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING);
        List<ItemStack> repaired = new ArrayList<>();
        for (int damage : new int[] {50, 2}) {
            for (ItemStack prototype : List.of(TestUtils.ceramicShears(), new ItemStack(Items.SHEARS))) {
                ItemStack stack = prototype.copy();
                stack.setDamageValue(damage);
                stack.enchant(mending, 1);
                ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, stack);
                new ExperienceOrb(helper.getLevel(), player.getX(), player.getY(), player.getZ(), 10).playerTouch(player);
                repaired.add(player.getMainHandItem());
            }
        }
        helper.assertTrue(repaired.get(0).getDamageValue() < 50, "mending did not repair ceramic shears");
        helper.assertValueEqual(repaired.get(0).getDamageValue(), repaired.get(1).getDamageValue(), "damage of ceramic shears after mending compared to vanilla shears");
        TestUtils.assertDamage(helper, repaired.get(2), 0, "after mending to full durability");
        helper.assertValueEqual(TestUtils.storedComponentTypes(repaired.get(2)),
                TestUtils.storedComponentTypes(repaired.get(3)),
                "stored components of ceramic shears mended to full durability compared to vanilla shears");
        helper.succeed();
    }

    /** Like vanilla shears (and unlike wooden tools), ceramic shears are no furnace fuel. */
    public static void isNoFuelLikeVanillaShears(GameTestHelper helper) {
        helper.assertValueEqual(GameTestPlatform.INSTANCE.burnTime(helper.getLevel(), new ItemStack(Items.SHEARS)), 0, "test setup broken: burn time of vanilla shears");
        helper.assertValueEqual(GameTestPlatform.INSTANCE.burnTime(helper.getLevel(), TestUtils.ceramicShears()), 0, "burn time of ceramic shears");
        helper.succeed();
    }

    /** The clay shears part and the ceramic shears part exist as simple items that stack to 64 (crafting ingredients). */
    public static void shearsPartsAreRegistered(GameTestHelper helper) {
        for (Item part : List.of(TestUtils.clayShearsPartItem(), TestUtils.ceramicShearsPartItem())) {
            helper.assertTrue(part != Items.AIR, "a ceramic shears part is not registered");
            helper.assertTrue(!(part instanceof ShearsItem), BuiltInRegistries.ITEM.getKey(part) + " is a shears item");
            ItemStack stack = new ItemStack(part);
            helper.assertValueEqual(stack.getMaxStackSize(), 64, "max stack size of " + BuiltInRegistries.ITEM.getKey(part));
            helper.assertTrue(!stack.isDamageableItem(), BuiltInRegistries.ITEM.getKey(part) + " is damageable");
        }
        helper.assertTrue(TestUtils.clayShearsPartItem() != TestUtils.ceramicShearsPartItem(), "clay and ceramic shears part are the same item");
        helper.succeed();
    }

    /** Both shears parts are listed in the creative tab "Ingredients". */
    public static void shearsPartsAreInIngredientsTab(GameTestHelper helper) {
        var level = helper.getLevel();
        CreativeModeTabs.tryRebuildTabContents(level.enabledFeatures(), true, level.registryAccess());
        var tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(
                ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("ingredients")));
        for (Item part : List.of(TestUtils.clayShearsPartItem(), TestUtils.ceramicShearsPartItem())) {
            helper.assertTrue(tab.getDisplayItems().stream().anyMatch(stack -> stack.is(part)),
                    BuiltInRegistries.ITEM.getKey(part) + " is missing in the ingredients creative tab");
        }
        helper.succeed();
    }

}
