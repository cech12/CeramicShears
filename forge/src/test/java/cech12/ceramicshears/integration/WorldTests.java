package cech12.ceramicshears.integration;
/*
import cech12.ceramicshears.IntegrationTestUtils;
import cech12.ceramicshears.api.item.CeramicShearsItems;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IntegrationTestClass(value = "world")
public class WorldTests {

    private static final BlockPos INTERACTION_POSITION = new BlockPos(1, 1, 1);


    @IntegrationTest(value = "tall_grass_large_fern")
    public void testBreakingTallGrassAndLargeFern(IntegrationTestHelper helper) {
        BlockPos lowerLargeFernPos = new BlockPos(0, 1, 0);
        BlockPos upperTallGrassPos = new BlockPos(1, 2, 0);

        Map<BlockPos, Block> blockPositions = new HashMap<BlockPos, Block>() {{
              put(lowerLargeFernPos, Blocks.FERN);
              put(upperTallGrassPos, Blocks.GRASS);
        }};
        final ServerWorld world = ServerLifecycleHooks.getCurrentServer().overworld();
        ItemStack shears = new ItemStack(CeramicShearsItems.CERAMIC_SHEARS);

        blockPositions.forEach((blockPos, dropBlock) -> {
            helper.relativePos(blockPos).ifPresent(pos -> {
                BlockState state = world.getBlockState(pos);
                LootContext.Builder lootContextBuilder = (new LootContext.Builder(world))
                        .withRandom(world.random)
                        .withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(pos))
                        .withParameter(LootParameters.TOOL, shears);
                List<ItemStack> drops = state.getDrops(lootContextBuilder);
                boolean blockContained = drops.stream().anyMatch(itemStack -> !itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock() == dropBlock);
                helper.assertTrue(() -> blockContained, "Breaking " + state.getBlock()  + " with shears should drop " + dropBlock + " but instead it drops " + drops);
            });
        });
    }

    @IntegrationTest(value = "pumpkin")
    public void testInteractionWithPumpkin(IntegrationTestHelper helper) {
        ItemStack shears = new ItemStack(CeramicShearsItems.CERAMIC_SHEARS);

        ActionResult<ItemStack> result = IntegrationTestUtils.useItem(helper, INTERACTION_POSITION, Direction.NORTH, shears);

        helper.assertTrue(() -> result.getResult() == ActionResultType.SUCCESS || result.getResult() == ActionResultType.CONSUME, "Block interaction should be marked as successful or consumed");
        helper.assertTrue(() -> result.getObject().getDamageValue() == 1, "Shears should loose one durability point");
        helper.assertBlockAt(INTERACTION_POSITION, Blocks.CARVED_PUMPKIN, "Pumpkin should be transformed to a carved pumpkin");
    }

    private void testBeehiveBlocksForHoneyLevel(IntegrationTestHelper helper, int honeyLevel) {
        helper.setBlockState(INTERACTION_POSITION, helper.getBlockState(INTERACTION_POSITION).setValue(BeehiveBlock.HONEY_LEVEL, honeyLevel));
        helper.setBlockState(INTERACTION_POSITION.above(), helper.getBlockState(INTERACTION_POSITION).setValue(BeehiveBlock.HONEY_LEVEL, honeyLevel));

        ActionResult<ItemStack> beeNestResult = IntegrationTestUtils.useItem(helper, INTERACTION_POSITION, Direction.NORTH, new ItemStack(CeramicShearsItems.CERAMIC_SHEARS));
        ActionResult<ItemStack> beeHiveResult = IntegrationTestUtils.useItem(helper, INTERACTION_POSITION.above(), Direction.NORTH, new ItemStack(CeramicShearsItems.CERAMIC_SHEARS));

        if (honeyLevel < 5) {
            helper.assertTrue(() -> beeNestResult.getResult() == ActionResultType.PASS, "Block interaction with bee nest at honey level " + honeyLevel + " should be marked as passed");
            helper.assertTrue(() -> beeHiveResult.getResult() == ActionResultType.PASS, "Block interaction with beehive at honey level " + honeyLevel + " should be marked as passed");
            helper.assertTrue(() -> beeNestResult.getObject().getDamageValue() == 0, "Shears should not loose a durability point when interacting with bee nest at honey level " + honeyLevel);
            helper.assertTrue(() -> beeHiveResult.getObject().getDamageValue() == 0, "Shears should not loose a durability point when interacting with beehive at honey level " + honeyLevel);
            helper.assertBlockAt(INTERACTION_POSITION, blockState -> blockState.getValue(BeehiveBlock.HONEY_LEVEL) == honeyLevel, "Honey level " + honeyLevel + " of bee nest should remain when interacting with shears");
            helper.assertBlockAt(INTERACTION_POSITION.above(), blockState -> blockState.getValue(BeehiveBlock.HONEY_LEVEL) == honeyLevel, "Honey level " + honeyLevel + " of beehive should remain when interacting with shears");
        } else {
            helper.assertTrue(() -> beeNestResult.getResult() == ActionResultType.SUCCESS || beeNestResult.getResult() == ActionResultType.CONSUME, "Block interaction with bee nest at honey level " + honeyLevel + " should be marked as successful or consumed");
            helper.assertTrue(() -> beeHiveResult.getResult() == ActionResultType.SUCCESS || beeNestResult.getResult() == ActionResultType.CONSUME, "Block interaction with beehive at honey level " + honeyLevel + " should be marked as successful or consumed");
            helper.assertTrue(() -> beeNestResult.getObject().getDamageValue() == 1, "Shears should loose a durability point when interacting with bee nest at honey level " + honeyLevel);
            helper.assertTrue(() -> beeHiveResult.getObject().getDamageValue() == 1, "Shears should loose a durability point when interacting with beehive at honey level " + honeyLevel);
            helper.assertBlockAt(INTERACTION_POSITION, blockState -> blockState.getValue(BeehiveBlock.HONEY_LEVEL) == 0, "Honey level " + honeyLevel + " of bee nest should be set to 0 when interacting with shears");
            helper.assertBlockAt(INTERACTION_POSITION.above(), blockState -> blockState.getValue(BeehiveBlock.HONEY_LEVEL) == 0, "Honey level " + honeyLevel + " of beehive should be set to 0 when interacting with shears");
        }
    }

    @IntegrationTest(value = "beehives")
    public void testInteractionWithBeehiveLevel0(IntegrationTestHelper helper) {
        testBeehiveBlocksForHoneyLevel(helper, 0);
    }

    @IntegrationTest(value = "beehives")
    public void testInteractionWithBeehiveLevel1(IntegrationTestHelper helper) {
        testBeehiveBlocksForHoneyLevel(helper, 1);
    }

    @IntegrationTest(value = "beehives")
    public void testInteractionWithBeehiveLevel2(IntegrationTestHelper helper) {
        testBeehiveBlocksForHoneyLevel(helper, 2);
    }

    @IntegrationTest(value = "beehives")
    public void testInteractionWithBeehiveLevel3(IntegrationTestHelper helper) {
        testBeehiveBlocksForHoneyLevel(helper, 3);
    }

    @IntegrationTest(value = "beehives")
    public void testInteractionWithBeehiveLevel4(IntegrationTestHelper helper) {
        testBeehiveBlocksForHoneyLevel(helper, 4);
    }

    @IntegrationTest(value = "beehives")
    public void testInteractionWithBeehiveLevel5(IntegrationTestHelper helper) {
        testBeehiveBlocksForHoneyLevel(helper, 5);
    }

    @IntegrationTest(value = "tripwire")
    public void testBreakingTripwire(IntegrationTestHelper helper) {
        final BlockPos tripwirePos = new BlockPos(3, 1, 1);
        final BlockPos redstoneTorchPos = new BlockPos(0, 1, 0);

        ActionResult<ItemStack> actionResult = IntegrationTestUtils.destroyBlockWithItem(helper, tripwirePos, new ItemStack(CeramicShearsItems.CERAMIC_SHEARS));

        helper.assertAirAt(tripwirePos, "Tripwire should not be there after breaking it with shears");
        helper.assertBlockAt(redstoneTorchPos, blockState -> !blockState.getValue(RedstoneTorchBlock.LIT), "Redstone torch should not be lit when tripwire is broken by shears");
        helper.assertTrue(() -> actionResult.getResult() == ActionResultType.SUCCESS || actionResult.getResult() == ActionResultType.CONSUME, "Tripwire breaking should be marked as successful or consumed");
        helper.assertTrue(() -> actionResult.getObject().getDamageValue() == 1, "Shears should loose a durability point when breaking tripwire");
    }

}
*/
