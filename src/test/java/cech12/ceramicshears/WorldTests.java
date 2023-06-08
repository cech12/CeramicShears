package cech12.ceramicshears;

import cech12.ceramicshears.util.ModTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
@GameTestHolder(CeramicShearsMod.MOD_ID)
public class WorldTests {

    private static final BlockPos INTERACTION_POSITION = new BlockPos(1, 2, 1);

    @GameTest(template = "tall_grass_large_fern")
    public static void test_breaking_large_fern(GameTestHelper test) {
        BlockPos lowerLargeFernPos = new BlockPos(0, 2, 0);
        Block drop = Blocks.FERN;

        ItemStack shears = new ItemStack(CeramicShearsMod.CERAMIC_SHEARS.get());
        BlockPos pos = test.absolutePos(lowerLargeFernPos);
        BlockState state = test.getBlockState(lowerLargeFernPos);
        LootParams.Builder lootContextBuilder = (new LootParams.Builder(test.getLevel()))
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, shears);
        List<ItemStack> drops = state.getDrops(lootContextBuilder);

        boolean blockContained = drops.stream().anyMatch(itemStack -> !itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock() == drop);
        test.assertTrue(blockContained, "Breaking " + state.getBlock()  + " with shears should drop " + drop + " but instead it drops " + drops);
        test.succeed();
    }

    @GameTest(template = "tall_grass_large_fern")
    public static void test_breaking_tall_gras(GameTestHelper test) {
        BlockPos upperTallGrassPos = new BlockPos(1, 3, 0);
        Block drop = Blocks.GRASS;

        ItemStack shears = new ItemStack(CeramicShearsMod.CERAMIC_SHEARS.get());
        BlockPos pos = test.absolutePos(upperTallGrassPos);
        BlockState state = test.getBlockState(upperTallGrassPos);
        LootParams.Builder lootContextBuilder = (new LootParams.Builder(test.getLevel()))
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, shears);
        List<ItemStack> drops = state.getDrops(lootContextBuilder);

        boolean blockContained = drops.stream().anyMatch(itemStack -> !itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock() == drop);
        test.assertTrue(blockContained, "Breaking " + state.getBlock()  + " with shears should drop " + drop + " but instead it drops " + drops);
        test.succeed();
    }


    @GameTest(template = "pumpkin")
    public void test_interaction_with_pumpkin(GameTestHelper test) {
        Player player = test.makeMockSurvivalPlayer();
        ItemStack shears = new ItemStack(CeramicShearsMod.CERAMIC_SHEARS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, shears);

        test.useBlock(INTERACTION_POSITION, player);

        ItemStack usedShears = player.getItemInHand(InteractionHand.MAIN_HAND);
        test.assertTrue(usedShears.getDamageValue() == 1, "Shears should loose one durability point");
        test.assertBlock(INTERACTION_POSITION, block -> block == Blocks.CARVED_PUMPKIN, "Pumpkin should be transformed to a carved pumpkin");
        test.succeed();
    }


    @GameTestGenerator
    public static List<TestFunction> generateBeehiveTests() {
        List<TestFunction> testFunctions = new ArrayList<>();
        for (int honeyLevel = 0; honeyLevel <= 5; honeyLevel++) {
            String testName = "test_interaction_with_beehive_level_" + honeyLevel;
            Integer finalHoneyLevel = honeyLevel;
            testFunctions.add(new TestFunction(
                    "defaultBatch",
                    testName,
                    new ResourceLocation(CeramicShearsMod.MOD_ID, "worldtests.beehives").toString(),
                    Rotation.NONE,
                    100,
                    0,
                    true,
                    test -> {
                        test.setBlock(INTERACTION_POSITION, test.getBlockState(INTERACTION_POSITION).setValue(BeehiveBlock.HONEY_LEVEL, finalHoneyLevel));
                        test.setBlock(INTERACTION_POSITION.above(), test.getBlockState(INTERACTION_POSITION).setValue(BeehiveBlock.HONEY_LEVEL, finalHoneyLevel));
                        Player player = test.makeMockSurvivalPlayer();
                        ItemStack shears = new ItemStack(CeramicShearsMod.CERAMIC_SHEARS.get());
                        player.setItemInHand(InteractionHand.MAIN_HAND, shears);

                        test.useBlock(INTERACTION_POSITION, player);
                        test.useBlock(INTERACTION_POSITION.above(), player);

                        ItemStack usedShears = player.getItemInHand(InteractionHand.MAIN_HAND);
                        if (finalHoneyLevel < 5) {
                            test.assertTrue(usedShears.getDamageValue() == 0, "Shears should not loose a durability point when interacting with bee nests at honey level " + finalHoneyLevel);
                            test.assertBlockProperty(INTERACTION_POSITION, BeehiveBlock.HONEY_LEVEL, value -> Objects.equals(value, finalHoneyLevel), "Honey level " + finalHoneyLevel + " of bee nest should remain when interacting with shears");
                            test.assertBlockProperty(INTERACTION_POSITION.above(), BeehiveBlock.HONEY_LEVEL, value -> Objects.equals(value, finalHoneyLevel), "Honey level " + finalHoneyLevel + " of beehive should remain when interacting with shears");
                        } else {
                            test.assertTrue(usedShears.getDamageValue() == 2, "Shears should lost two durability points after interacting with bee nests at honey level " + finalHoneyLevel);
                            test.assertBlockProperty(INTERACTION_POSITION, BeehiveBlock.HONEY_LEVEL, value -> Objects.equals(value, 0), "Honey level " + finalHoneyLevel + " of bee nest should be set to 0 when interacting with shears");
                            test.assertBlockProperty(INTERACTION_POSITION.above(), BeehiveBlock.HONEY_LEVEL, value -> Objects.equals(value, 0), "Honey level " + finalHoneyLevel + " of beehive should be set to 0 when interacting with shears");
                        }
                        test.succeed();
                    }
            ));
        }
        return testFunctions;
    }

    @GameTest(template = "tripwire")
    public void test_breaking_tripwire(GameTestHelper test) {
        final BlockPos tripwirePos = new BlockPos(3, 2, 1);
        final BlockPos redstoneTorchPos = new BlockPos(0, 2, 0);

        InteractionResultHolder<ItemStack> actionResult = ModTestHelper.destroyBlockWithItem(test, tripwirePos, new ItemStack(CeramicShearsMod.CERAMIC_SHEARS.get()));

        test.assertBlock(tripwirePos, block -> block == Blocks.AIR, "Tripwire should not be there after breaking it with shears");
        test.assertBlockProperty(redstoneTorchPos, RedstoneTorchBlock.LIT, value -> !value, "Redstone torch should not be lit when tripwire is broken by shears");
        test.assertTrue(actionResult.getResult().consumesAction(), "Tripwire breaking should be marked as successful or consumed");
        test.assertTrue(actionResult.getObject().getDamageValue() == 1, "Shears should loose a durability point when breaking tripwire");
        test.succeed();
    }

}
