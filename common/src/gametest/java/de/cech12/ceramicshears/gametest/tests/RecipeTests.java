package de.cech12.ceramicshears.gametest.tests;

import de.cech12.ceramicshears.Constants;
import de.cech12.ceramicshears.gametest.TestUtils;
import de.cech12.ceramicshears.gametest.CeramicShearsGameTests;
import de.cech12.ceramicshears.platform.Services;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.RecipeUnlockedTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Crafting and smelting recipes of the ceramic shears and their parts, recipe unlock advancements and repairing by
 * combining two ceramic shears.
 */
public final class RecipeTests {

    private static final ResourceKey<Recipe<?>> RECIPE = recipeKey("ceramic_shears");
    private static final ResourceKey<Recipe<?>> CLAY_PART_RECIPE = recipeKey("clay_shears_part");
    private static final ResourceKey<Recipe<?>> CERAMIC_PART_RECIPE = recipeKey("ceramic_shears_part");
    /** Remaining durability of the two shears that are combined by the repair tests (before the bonus). */
    private static final int FIRST_REMAINING = 30;
    private static final int SECOND_REMAINING = 40;

    private RecipeTests() {}

    public static void register(CeramicShearsGameTests.TestCollector tests) {
        tests.add("recipe/crafted_from_two_ceramic_shears_parts", RecipeTests::craftedFromTwoCeramicShearsParts);
        tests.add("recipe/not_crafted_from_other_items", RecipeTests::notCraftedFromOtherItems);
        tests.add("recipe/clay_shears_part_crafted_from_two_diagonal_clay_balls", RecipeTests::clayShearsPartCraftedFromTwoDiagonalClayBalls);
        tests.add("recipe/clay_shears_part_not_crafted_from_two_clay_balls_in_a_row", RecipeTests::clayShearsPartNotCraftedFromTwoClayBallsInARow);
        tests.add("recipe/ceramic_shears_part_smelted_from_clay_shears_part", RecipeTests::ceramicShearsPartSmeltedFromClayShearsPart);
        tests.add("recipe/furnace_smelts_clay_shears_part", RecipeTests::furnaceSmeltsClayShearsPart);
        tests.add("recipe/is_in_equipment_category", RecipeTests::isInEquipmentCategory);
        tests.add("recipe/unlocked_by_having_clay", RecipeTests::unlockedByHavingClay);
        tests.add("repair/combines_two_damaged_shears", RecipeTests::combinesTwoDamagedShears);
        tests.add("repair/grindstone_combines_two_damaged_shears", RecipeTests::grindstoneCombinesTwoDamagedShears);
        tests.add("repair/grindstone_result_follows_config", RecipeTests::grindstoneResultFollowsConfig);
        tests.add("repair/anvil_combines_two_damaged_shears", RecipeTests::anvilCombinesTwoDamagedShears);
        tests.add("repair/fully_repaired_shears_equal_new_shears", RecipeTests::fullyRepairedShearsEqualNewShears);
        tests.add("repair/anvil_fully_repaired_shears_like_vanilla_shears", RecipeTests::anvilFullyRepairedShearsLikeVanillaShears);
        tests.add("repair/grindstone_disenchants_single_shears", RecipeTests::grindstoneDisenchantsSingleShears);
        tests.add("repair/no_repair_material_like_vanilla_shears", RecipeTests::noRepairMaterialLikeVanillaShears);
        tests.add("repair/not_possible_when_unbreakable", RecipeTests::notPossibleWhenUnbreakable);
    }

    private static ResourceKey<Recipe<?>> recipeKey(String name) {
        return ResourceKey.create(Registries.RECIPE, Constants.id(name));
    }

    private static Optional<RecipeHolder<CraftingRecipe>> findRecipe(GameTestHelper helper, CraftingInput input) {
        return helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
    }

    private static ItemStack craft(GameTestHelper helper, CraftingInput input) {
        return findRecipe(helper, input).map(recipe -> recipe.value().assemble(input)).orElse(ItemStack.EMPTY);
    }

    /** The recipe with the given key, with a clear message if it is missing. */
    private static Recipe<?> recipe(GameTestHelper helper, ResourceKey<Recipe<?>> key) {
        var recipe = helper.getLevel().recipeAccess().byKey(key);
        helper.assertTrue(recipe.isPresent(), "recipe " + key.identifier() + " is missing");
        return recipe.get().value();
    }

    /** A crafting recipe with the given key, to check it directly (another recipe could win the recipe lookup). */
    @SuppressWarnings("unchecked")
    private static Recipe<CraftingInput> craftingRecipe(GameTestHelper helper, ResourceKey<Recipe<?>> key) {
        Recipe<?> recipe = recipe(helper, key);
        helper.assertTrue(recipe instanceof CraftingRecipe, "recipe " + key.identifier() + " is no crafting recipe");
        return (Recipe<CraftingInput>) recipe;
    }

    private static ItemStack clayShearsPart() {
        return new ItemStack(TestUtils.clayShearsPartItem());
    }

    private static ItemStack ceramicShearsPart() {
        return new ItemStack(TestUtils.ceramicShearsPartItem());
    }

    /** Two ceramic shears parts in any position of the crafting grid craft one pair of ceramic shears (shapeless). */
    public static void craftedFromTwoCeramicShearsParts(GameTestHelper helper) {
        ItemStack part = ceramicShearsPart();
        ItemStack empty = ItemStack.EMPTY;
        for (CraftingInput input : List.of(
                CraftingInput.of(2, 1, List.of(part, part)),
                CraftingInput.of(1, 2, List.of(part, part)),
                CraftingInput.of(2, 2, List.of(empty, part, part, empty)),
                CraftingInput.of(2, 2, List.of(part, empty, empty, part)),
                CraftingInput.of(3, 3, List.of(part, empty, empty, empty, empty, empty, empty, empty, part)))) {
            ItemStack result = craft(helper, input);
            helper.assertTrue(result.is(TestUtils.ceramicShearsItem()) && result.getCount() == 1,
                    "two ceramic shears parts in a " + input.width() + "x" + input.height() + " grid do not craft ceramic shears but " + result);
        }
        helper.succeed();
    }

    /**
     * Negative cases: one ceramic shears part, three ceramic shears parts, two clay shears parts (not burned yet) and a
     * ceramic shears part with a clay shears part do not match the ceramic shears recipe.
     */
    public static void notCraftedFromOtherItems(GameTestHelper helper) {
        Recipe<CraftingInput> recipe = craftingRecipe(helper, RECIPE);
        ItemStack part = ceramicShearsPart();
        ItemStack clay = clayShearsPart();
        for (CraftingInput input : List.of(
                CraftingInput.of(1, 1, List.of(part)),
                CraftingInput.of(3, 1, List.of(part, part, part)),
                CraftingInput.of(2, 1, List.of(clay, clay)),
                CraftingInput.of(2, 1, List.of(part, clay)))) {
            helper.assertTrue(!recipe.matches(input, helper.getLevel()), "the ceramic shears recipe matches " + input.items());
        }
        helper.succeed();
    }

    /** Two clay balls placed diagonally (in both directions) craft one clay shears part. */
    public static void clayShearsPartCraftedFromTwoDiagonalClayBalls(GameTestHelper helper) {
        ItemStack clay = new ItemStack(Items.CLAY_BALL);
        for (List<ItemStack> grid : List.of(
                List.of(ItemStack.EMPTY, clay, clay, ItemStack.EMPTY),
                List.of(clay, ItemStack.EMPTY, ItemStack.EMPTY, clay))) {
            ItemStack result = craft(helper, CraftingInput.of(2, 2, grid));
            helper.assertTrue(result.is(TestUtils.clayShearsPartItem()) && result.getCount() == 1,
                    "two diagonal clay balls do not craft a clay shears part but " + result);
        }
        helper.succeed();
    }

    /** Negative case: two clay balls next to or above each other do not match the clay shears part recipe. */
    public static void clayShearsPartNotCraftedFromTwoClayBallsInARow(GameTestHelper helper) {
        Recipe<CraftingInput> recipe = craftingRecipe(helper, CLAY_PART_RECIPE);
        ItemStack clay = new ItemStack(Items.CLAY_BALL);
        for (CraftingInput input : List.of(CraftingInput.of(2, 1, List.of(clay, clay)), CraftingInput.of(1, 2, List.of(clay, clay)))) {
            helper.assertTrue(!recipe.matches(input, helper.getLevel()),
                    "two clay balls in a " + input.width() + "x" + input.height() + " grid match the clay shears part recipe");
        }
        helper.succeed();
    }

    /** A clay shears part is smelted in a furnace to a ceramic shears part (200 ticks, 0.3 experience). */
    public static void ceramicShearsPartSmeltedFromClayShearsPart(GameTestHelper helper) {
        SingleRecipeInput input = new SingleRecipeInput(clayShearsPart());
        var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.SMELTING, input, helper.getLevel());
        helper.assertTrue(recipe.isPresent(), "a clay shears part cannot be smelted");
        helper.assertValueEqual(recipe.get().id(), CERAMIC_PART_RECIPE, "smelting recipe of the clay shears part");
        AbstractCookingRecipe smelting = recipe.get().value();
        ItemStack result = smelting.assemble(input);
        helper.assertTrue(result.is(TestUtils.ceramicShearsPartItem()) && result.getCount() == 1, "smelting a clay shears part results in " + result);
        helper.assertValueEqual(smelting.cookingTime(), 200, "cooking time");
        helper.assertValueEqual(smelting.experience(), 0.3F, "experience");
        helper.succeed();
    }

    /** A real furnace burns a clay shears part to a ceramic shears part. */
    public static void furnaceSmeltsClayShearsPart(GameTestHelper helper) {
        BlockPos pos = TestUtils.CENTER;
        helper.setBlock(pos, Blocks.FURNACE);
        AbstractFurnaceBlockEntity furnace = helper.getBlockEntity(pos, AbstractFurnaceBlockEntity.class);
        furnace.setItem(0, clayShearsPart());
        furnace.setItem(1, new ItemStack(Items.COAL));
        helper.succeedWhen(() -> {
            ItemStack output = furnace.getItem(2);
            helper.assertTrue(output.is(TestUtils.ceramicShearsPartItem()) && output.getCount() == 1, "furnace output is " + output);
            helper.assertTrue(furnace.getItem(0).isEmpty(), "the clay shears part was not consumed");
        });
    }

    /** WoodenShears#27: the ceramic shears recipe is shown in the equipment category, the parts in the misc categories. */
    public static void isInEquipmentCategory(GameTestHelper helper) {
        helper.assertValueEqual(((CraftingRecipe) craftingRecipe(helper, RECIPE)).category(), CraftingBookCategory.EQUIPMENT, "category of the ceramic shears recipe");
        helper.assertValueEqual(((CraftingRecipe) craftingRecipe(helper, CLAY_PART_RECIPE)).category(), CraftingBookCategory.MISC, "category of the clay shears part recipe");
        Recipe<?> smelting = recipe(helper, CERAMIC_PART_RECIPE);
        helper.assertValueEqual(smelting instanceof AbstractCookingRecipe cooking ? cooking.category() : null,
                CookingBookCategory.MISC, "category of the ceramic shears part recipe");
        helper.succeed();
    }

    /**
     * All three recipes (clay shears part, ceramic shears part, ceramic shears) are unlocked by an advancement as soon
     * as a player has clay balls, or when the player already knows the recipe.
     */
    public static void unlockedByHavingClay(GameTestHelper helper) {
        for (var entry : List.of(
                Map.entry("recipes/misc/clay_shears_part", CLAY_PART_RECIPE),
                Map.entry("recipes/misc/ceramic_shears_part", CERAMIC_PART_RECIPE),
                Map.entry("recipes/tools/ceramic_shears", RECIPE))) {
            String name = entry.getKey();
            ResourceKey<Recipe<?>> recipe = entry.getValue();
            AdvancementHolder advancement = helper.getLevel().getServer().getAdvancements().get(Constants.id(name));
            helper.assertTrue(advancement != null, "recipe advancement " + name + " is missing");
            helper.assertTrue(advancement.value().rewards().recipes().contains(recipe), name + " does not unlock the recipe " + recipe.identifier());
            var hasClay = advancement.value().criteria().get("has_clay");
            helper.assertTrue(hasClay != null, name + " has no has_clay criterion");
            helper.assertTrue(hasClay.trigger() == CriteriaTriggers.INVENTORY_CHANGED, "has_clay of " + name + " is no inventory_changed trigger");
            helper.assertTrue(hasClay.triggerInstance() instanceof InventoryChangeTrigger.TriggerInstance inventory
                            && inventory.items().size() == 1 && inventory.items().getFirst().test(new ItemStack(Items.CLAY_BALL))
                            && !inventory.items().getFirst().test(new ItemStack(Items.CLAY)),
                    "has_clay of " + name + " does not check for clay balls");
            var hasTheRecipe = advancement.value().criteria().get("has_the_recipe");
            helper.assertTrue(hasTheRecipe != null, name + " has no has_the_recipe criterion");
            var ownRecipe = helper.getLevel().recipeAccess().byKey(recipe);
            helper.assertTrue(ownRecipe.isPresent(), "recipe " + recipe.identifier() + " is missing");
            helper.assertTrue(hasTheRecipe.triggerInstance() instanceof RecipeUnlockedTrigger.TriggerInstance unlocked && unlocked.matches(ownRecipe.get()),
                    "has_the_recipe of " + name + " does not check for its own recipe " + recipe.identifier() + ": " + hasTheRecipe.triggerInstance());
            // either criterion alone completes the advancement (OR), so having clay balls is enough
            var requirements = advancement.value().requirements();
            helper.assertTrue(requirements.test("has_clay"::equals), name + " is not completed by has_clay alone: " + requirements);
            helper.assertTrue(requirements.test("has_the_recipe"::equals), name + " is not completed by has_the_recipe alone: " + requirements);
            helper.assertTrue(!requirements.test(criterion -> false), name + " is completed without any criterion: " + requirements);
        }
        helper.succeed();
    }

    /**
     * WoodenShears#38: two damaged ceramic shears can be combined in the crafting grid. The result has the remaining durability of
     * both plus a 5% bonus. The max damage is not stored in the result, so it still follows the config.
     */
    public static void combinesTwoDamagedShears(GameTestHelper helper) {
        int max = Services.CONFIG.getDurability(); // set by the test environment "default"
        ItemStack first = TestUtils.ceramicShears();
        first.setDamageValue(max - FIRST_REMAINING);
        ItemStack second = TestUtils.ceramicShears();
        second.setDamageValue(max - SECOND_REMAINING);
        ItemStack result = craft(helper, CraftingInput.of(2, 1, List.of(first, second)));
        helper.assertTrue(result.is(TestUtils.ceramicShearsItem()), "two damaged ceramic shears cannot be combined: " + result);
        int remaining = FIRST_REMAINING + SECOND_REMAINING + max * 5 / 100;
        TestUtils.assertDamage(helper, result, max - remaining, "after repair");
        helper.assertTrue(result.getComponentsPatch().forget(type -> type != DataComponents.MAX_DAMAGE).isEmpty(), "repaired ceramic shears store a fixed max damage");
        helper.succeed();
    }

    private static Holder<Enchantment> enchantment(GameTestHelper helper, ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    /** Puts two items into the menu inputs (slots 0 and 1) and returns the output (slot 2). */
    private static ItemStack combine(AbstractContainerMenu menu, ItemStack first, ItemStack second) {
        menu.getSlot(0).set(first);
        menu.getSlot(1).set(second);
        if (menu instanceof AnvilMenu anvil) {
            anvil.createResult();
        }
        return menu.getSlot(2).getItem();
    }

    /**
     * Like vanilla shears, two damaged ceramic shears can be combined in a grindstone: the remaining durability of both
     * plus a 5% bonus, enchantments (except curses) are removed.
     */
    public static void grindstoneCombinesTwoDamagedShears(GameTestHelper helper) {
        int max = Services.CONFIG.getDurability(); // set by the test environment "default"
        ItemStack result = combineInGrindstone(helper);
        helper.assertTrue(result.is(TestUtils.ceramicShearsItem()), "two damaged ceramic shears cannot be combined in a grindstone: " + result);
        int remaining = FIRST_REMAINING + SECOND_REMAINING + max * 5 / 100;
        TestUtils.assertDamage(helper, result, Math.max(0, max - remaining), "after combining in a grindstone");
        helper.assertTrue(result.getEnchantments().isEmpty(), "grindstone did not remove the enchantments: " + result.getEnchantments());
        helper.succeed();
    }

    /**
     * Shears combined in a grindstone do not store a fixed max damage, so config changes still apply to them (like the
     * crafting repair, WoodenShears#38). Fixed WoodenShears#51.
     */
    public static void grindstoneResultFollowsConfig(GameTestHelper helper) {
        ItemStack result = combineInGrindstone(helper);
        helper.assertTrue(result.is(TestUtils.ceramicShearsItem()), "two damaged ceramic shears cannot be combined in a grindstone: " + result);
        helper.assertTrue(result.getComponentsPatch().forget(type -> type != DataComponents.MAX_DAMAGE).isEmpty(),
                "ceramic shears combined in a grindstone store a fixed max damage: " + result.getComponentsPatch());
        helper.assertValueEqual(result.getMaxDamage(), Services.CONFIG.getDurability(), "max damage of the combined shears");
        helper.succeed();
    }

    /** Combines two damaged ceramic shears (30 and 40 remaining durability, the first with Efficiency I) in a grindstone. */
    private static ItemStack combineInGrindstone(GameTestHelper helper) {
        int max = Services.CONFIG.getDurability(); // set by the test environment "default"
        ItemStack first = TestUtils.ceramicShears();
        first.setDamageValue(max - FIRST_REMAINING);
        first.enchant(enchantment(helper, Enchantments.EFFICIENCY), 1);
        ItemStack second = TestUtils.ceramicShears();
        second.setDamageValue(max - SECOND_REMAINING);
        ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, ItemStack.EMPTY);
        GrindstoneMenu menu = new GrindstoneMenu(0, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(TestUtils.CENTER)));
        return combine(menu, first, second);
    }

    /**
     * Like vanilla shears, two damaged ceramic shears can be combined in an anvil: the remaining durability of both plus
     * a 12% bonus, the enchantments of both are kept. The max damage still follows the config.
     */
    public static void anvilCombinesTwoDamagedShears(GameTestHelper helper) {
        int max = Services.CONFIG.getDurability(); // set by the test environment "default"
        ItemStack first = TestUtils.ceramicShears();
        first.setDamageValue(max - FIRST_REMAINING);
        first.enchant(enchantment(helper, Enchantments.EFFICIENCY), 1);
        ItemStack second = TestUtils.ceramicShears();
        second.setDamageValue(max - SECOND_REMAINING);
        second.enchant(enchantment(helper, Enchantments.UNBREAKING), 1);
        ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, ItemStack.EMPTY);
        AnvilMenu menu = new AnvilMenu(0, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(TestUtils.CENTER)));
        ItemStack result = combine(menu, first, second);
        helper.assertTrue(result.is(TestUtils.ceramicShearsItem()), "two damaged ceramic shears cannot be combined in an anvil: " + result);
        int remaining = FIRST_REMAINING + SECOND_REMAINING + max * 12 / 100;
        TestUtils.assertDamage(helper, result, Math.max(0, max - remaining), "after combining in an anvil");
        helper.assertTrue(result.getEnchantments().getLevel(enchantment(helper, Enchantments.EFFICIENCY)) == 1
                && result.getEnchantments().getLevel(enchantment(helper, Enchantments.UNBREAKING)) == 1,
                "anvil did not keep the enchantments of both shears: " + result.getEnchantments());
        helper.assertTrue(menu.getCost() > 0, "combining in an anvil costs no experience");
        helper.assertTrue(result.getComponentsPatch().forget(type -> type != DataComponents.MAX_DAMAGE).isEmpty(), "ceramic shears combined in an anvil store a fixed max damage");
        helper.succeed();
    }

    /** WoodenShears#38: shears repaired to full durability are identical to new shears (no leftover damage component). */
    public static void fullyRepairedShearsEqualNewShears(GameTestHelper helper) {
        ItemStack first = TestUtils.ceramicShears();
        first.setDamageValue(1);
        ItemStack second = TestUtils.ceramicShears();
        second.setDamageValue(1);
        ItemStack result = craft(helper, CraftingInput.of(2, 1, List.of(first, second)));
        helper.assertTrue(result.is(TestUtils.ceramicShearsItem()), "two damaged ceramic shears cannot be combined: " + result);
        helper.assertTrue(ItemStack.isSameItemSameComponents(result, TestUtils.ceramicShears()),
                "fully repaired ceramic shears differ from new ones: " + result.getComponentsPatch());
        helper.succeed();
    }

    private static AnvilMenu anvil(GameTestHelper helper) {
        ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, ItemStack.EMPTY);
        return new AnvilMenu(0, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(TestUtils.CENTER)));
    }

    /**
     * Shears combined to full durability in an anvil have the same stored components as vanilla shears combined the
     * same way (the repair cost, but no leftover damage or max damage).
     */
    public static void anvilFullyRepairedShearsLikeVanillaShears(GameTestHelper helper) {
        List<ItemStack> results = new java.util.ArrayList<>();
        for (ItemStack prototype : List.of(TestUtils.ceramicShears(), new ItemStack(Items.SHEARS))) {
            ItemStack first = prototype.copy();
            first.setDamageValue(1);
            ItemStack second = prototype.copy();
            second.setDamageValue(1);
            ItemStack result = combine(anvil(helper), first, second);
            helper.assertTrue(result.is(prototype.getItem()) && result.getDamageValue() == 0, "two slightly damaged " + prototype + " are not fully repaired in an anvil: " + result);
            results.add(result);
        }
        var mod = TestUtils.storedComponentTypes(results.get(0));
        var vanilla = TestUtils.storedComponentTypes(results.get(1));
        helper.assertValueEqual(mod, vanilla, "stored components of fully repaired ceramic shears compared to vanilla shears");
        helper.succeed();
    }

    /**
     * Like vanilla shears, a single enchanted and damaged pair of shears is disenchanted in a grindstone: the damage is
     * kept, no fixed max damage is stored.
     */
    public static void grindstoneDisenchantsSingleShears(GameTestHelper helper) {
        ItemStack shears = TestUtils.ceramicShears();
        shears.setDamageValue(10);
        shears.enchant(enchantment(helper, Enchantments.EFFICIENCY), 1);
        ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, ItemStack.EMPTY);
        GrindstoneMenu menu = new GrindstoneMenu(0, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(TestUtils.CENTER)));
        ItemStack result = combine(menu, shears, ItemStack.EMPTY);
        helper.assertTrue(result.is(TestUtils.ceramicShearsItem()), "a single ceramic shears cannot be disenchanted in a grindstone: " + result);
        helper.assertTrue(result.getEnchantments().isEmpty(), "grindstone did not remove the enchantments: " + result.getEnchantments());
        TestUtils.assertDamage(helper, result, 10, "after disenchanting in a grindstone");
        helper.assertTrue(result.getComponentsPatch().forget(type -> type != DataComponents.MAX_DAMAGE).isEmpty(),
                "disenchanted ceramic shears store a fixed max damage: " + result.getComponentsPatch());
        helper.succeed();
    }

    /**
     * Like vanilla shears, ceramic shears have no repair material: an anvil does not repair them with any item (only by
     * combining two shears).
     */
    public static void noRepairMaterialLikeVanillaShears(GameTestHelper helper) {
        helper.assertTrue(java.util.Objects.equals(TestUtils.ceramicShears().get(DataComponents.REPAIRABLE), new ItemStack(Items.SHEARS).get(DataComponents.REPAIRABLE)),
                "repair material differs from vanilla shears: " + TestUtils.ceramicShears().get(DataComponents.REPAIRABLE));
        for (var material : List.of(Items.CLAY_BALL, TestUtils.ceramicShearsPartItem(), Items.BRICK, Items.IRON_INGOT)) {
            ItemStack shears = TestUtils.ceramicShears();
            shears.setDamageValue(20);
            ItemStack result = combine(anvil(helper), shears, new ItemStack(material, 4));
            helper.assertTrue(result.isEmpty(), "ceramic shears are repaired with " + material + " in an anvil: " + result);
        }
        helper.succeed();
    }

    /** Environment "durability_0": unbreakable ceramic shears cannot be damaged, so the repair recipe does not apply. */
    public static void notPossibleWhenUnbreakable(GameTestHelper helper) {
        ItemStack result = craft(helper, CraftingInput.of(2, 1, List.of(TestUtils.ceramicShears(), TestUtils.ceramicShears())));
        helper.assertTrue(result.isEmpty(), "unbreakable ceramic shears can be combined: " + result);
        ServerPlayer player = TestUtils.player(helper, GameType.SURVIVAL, ItemStack.EMPTY);
        GrindstoneMenu grindstone = new GrindstoneMenu(0, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(TestUtils.CENTER)));
        ItemStack grindstoneResult = combine(grindstone, TestUtils.ceramicShears(), TestUtils.ceramicShears());
        helper.assertTrue(grindstoneResult.isEmpty(), "unbreakable ceramic shears can be combined in a grindstone: " + grindstoneResult);
        ItemStack anvilResult = combine(anvil(helper), TestUtils.ceramicShears(), TestUtils.ceramicShears());
        helper.assertTrue(anvilResult.isEmpty(), "unbreakable ceramic shears can be combined in an anvil: " + anvilResult);
        helper.succeed();
    }

}
