package de.cech12.ceramicshears.gametest.tests;

import de.cech12.ceramicshears.gametest.KnownIssues;
import de.cech12.ceramicshears.gametest.Platforms;
import de.cech12.ceramicshears.gametest.TestUtils;
import de.cech12.ceramicshears.gametest.CeramicShearsGameTests;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;

import java.util.Set;

/**
 * Dispensers use ceramic shears like vanilla shears.
 */
public final class DispenserTests {

    private static final BlockPos DISPENSER = new BlockPos(2, 1, 1);
    private static final BlockPos TARGET = new BlockPos(2, 1, 2);

    private DispenserTests() {}

    public static void register(CeramicShearsGameTests.TestCollector tests) {
        tests.add("dispenser/shears_sheep", DispenserTests::shearsSheep);
        tests.add("dispenser/shears_mooshroom", DispenserTests::shearsMooshroom);
        tests.add("dispenser/shears_snow_golem", DispenserTests::shearsSnowGolem);
        tests.add("dispenser/shears_bogged", DispenserTests::shearsBogged);
        tests.add("dispenser/shears_copper_golem", DispenserTests::shearsCopperGolem);
        tests.add("dispenser/harvests_full_beehive", DispenserTests::harvestsFullBeehive);
        tests.add("dispenser/cuts_leash", DispenserTests::cutsLeash);
    }

    /** Places a dispenser with ceramic shears that faces the target position and powers it. */
    private static DispenserBlockEntity dispense(GameTestHelper helper) {
        helper.setBlock(DISPENSER, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.SOUTH));
        DispenserBlockEntity dispenser = helper.getBlockEntity(DISPENSER, DispenserBlockEntity.class);
        dispenser.setItem(0, TestUtils.ceramicShears());
        helper.setBlock(DISPENSER.west(), Blocks.REDSTONE_BLOCK);
        return dispenser;
    }

    public static void shearsSheep(GameTestHelper helper) {
        Sheep sheep = helper.spawnWithNoFreeWill(EntityType.SHEEP, TARGET);
        DispenserBlockEntity dispenser = dispense(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(sheep.isSheared(), "sheep was not sheared by the dispenser");
            TestUtils.assertItemEntityPresent(helper, ItemTags.WOOL);
            TestUtils.assertDamage(helper, dispenser.getItem(0), 1, "in dispenser after shearing a sheep");
        });
    }

    public static void shearsMooshroom(GameTestHelper helper) {
        helper.spawnWithNoFreeWill(EntityType.MOOSHROOM, TARGET);
        DispenserBlockEntity dispenser = dispense(helper);
        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertEntityPresent(EntityType.COW);
                    helper.assertEntityNotPresent(EntityType.MOOSHROOM);
                    TestUtils.assertDamage(helper, dispenser.getItem(0), 1, "in dispenser after shearing a mooshroom");
                })
                .thenExecute(() -> KnownIssues.run(helper, "Forge 62.0.x - 64.x (26.1.x): sheared mooshrooms drop the shears instead of the mushrooms (MushroomCow#shearInternal adds the tool instead of the drop, also with vanilla shears), fixed in Forge 65.0.1 for 26.2", Set.of(Platforms.FORGE), () ->
                        helper.assertItemEntityPresent(Items.RED_MUSHROOM)));
    }

    public static void shearsSnowGolem(GameTestHelper helper) {
        SnowGolem golem = helper.spawnWithNoFreeWill(EntityType.SNOW_GOLEM, TARGET);
        DispenserBlockEntity dispenser = dispense(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(!golem.hasPumpkin(), "snow golem was not sheared by the dispenser");
            helper.assertItemEntityPresent(Items.CARVED_PUMPKIN);
            TestUtils.assertDamage(helper, dispenser.getItem(0), 1, "in dispenser after shearing a snow golem");
        });
    }

    public static void shearsBogged(GameTestHelper helper) {
        Bogged bogged = helper.spawnWithNoFreeWill(EntityType.BOGGED, TARGET);
        DispenserBlockEntity dispenser = dispense(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(bogged.isSheared(), "bogged was not sheared by the dispenser");
            TestUtils.assertBoggedMushroomsDropped(helper);
            TestUtils.assertDamage(helper, dispenser.getItem(0), 1, "in dispenser after shearing a bogged");
        });
    }

    public static void shearsCopperGolem(GameTestHelper helper) {
        CopperGolem golem = helper.spawnWithNoFreeWill(EntityType.COPPER_GOLEM, TARGET);
        Item flower = TestUtils.firstOfTag(helper, ItemTags.SHEARABLE_FROM_COPPER_GOLEM);
        golem.setItemSlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA, new ItemStack(flower));
        DispenserBlockEntity dispenser = dispense(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(golem.getItemBySlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA).isEmpty(), "copper golem flower was not sheared by the dispenser");
            helper.assertItemEntityPresent(flower);
            TestUtils.assertDamage(helper, dispenser.getItem(0), 1, "in dispenser after shearing a copper golem");
        });
    }

    public static void harvestsFullBeehive(GameTestHelper helper) {
        helper.setBlock(TARGET, Blocks.BEEHIVE.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL, BeehiveBlock.MAX_HONEY_LEVELS));
        DispenserBlockEntity dispenser = dispense(helper);
        helper.succeedWhen(() -> {
            helper.assertBlockProperty(TARGET, BeehiveBlock.HONEY_LEVEL, 0);
            helper.assertItemEntityPresent(Items.HONEYCOMB);
            TestUtils.assertDamage(helper, dispenser.getItem(0), 1, "in dispenser after harvesting a beehive");
        });
    }

    public static void cutsLeash(GameTestHelper helper) {
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, TARGET);
        BlockPos fence = TARGET.east(2);
        helper.setBlock(fence, Blocks.OAK_FENCE);
        pig.setLeashedTo(LeashFenceKnotEntity.getOrCreateKnot(helper.getLevel(), helper.absolutePos(fence)), true);
        DispenserBlockEntity dispenser = dispense(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(!pig.isLeashed(), "leash was not cut by the dispenser");
            TestUtils.assertDamage(helper, dispenser.getItem(0), 1, "in dispenser after cutting a leash");
        });
    }

}
