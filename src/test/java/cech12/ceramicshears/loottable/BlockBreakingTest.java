package cech12.ceramicshears.loottable;

import cech12.ceramicshears.api.item.CeramicShearsItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockBreakingTest {

    @Test
    public void testLeavesDropThemselvesWhenBreakingWithShears() {
        Block[] leaves = new Block[] {
                Blocks.ACACIA_LEAVES,
                Blocks.BIRCH_LEAVES,
                Blocks.DARK_OAK_LEAVES,
                Blocks.JUNGLE_LEAVES,
                Blocks.OAK_LEAVES,
                Blocks.SPRUCE_LEAVES
        };
        dropThemselvesTest(leaves);
    }

    @Test
    public void testPlantsDropThemselvesWhenBreakingWithShears() {
        Block[] plants = new Block[] {
                Blocks.GRASS,
                Blocks.SEAGRASS,
                Blocks.FERN,
                Blocks.DEAD_BUSH,
                Blocks.NETHER_SPROUTS,
                Blocks.VINE,
        };
        dropThemselvesTest(plants);
    }

    @Test
    public void testTallPlantsDropSmallVariantWhenBreakingWithShears() {
        //tall grass & large fern are tested as integration test, because their loot tables are checking upper/lower blocks
        dropTest(Blocks.TALL_SEAGRASS, Blocks.SEAGRASS);
    }

    @Test
    public void testOtherMiscBlocksDropThemselvesWhenBreakingWithShears() {
        Block[] plants = new Block[] {
                Blocks.COBWEB
        };
        dropThemselvesTest(plants);
    }

    private void dropThemselvesTest(Block[] blocks) {
        for (Block block : blocks) {
            dropTest(block, block);
        }
    }

    private void dropTest(Block block, Block dropBlock) {
        final ServerWorld world = ServerLifecycleHooks.getCurrentServer().overworld();
        final ItemStack shears = new ItemStack(CeramicShearsItems.CERAMIC_SHEARS);
        final BlockState state = block.defaultBlockState();

        LootContext.Builder lootContextBuilder = (new LootContext.Builder(world))
                .withRandom(world.random)
                .withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(new BlockPos(0, 0, 0))) //is needed to generate loot!
                .withParameter(LootParameters.TOOL, shears);
        List<ItemStack> drops = state.getDrops(lootContextBuilder);

        boolean blockContained = drops.stream().anyMatch(itemStack -> !itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock() == dropBlock);

        assertTrue(blockContained, "Breaking " + block  + " with shears should drop " + dropBlock + " but instead it drops " + drops);
    }

}
