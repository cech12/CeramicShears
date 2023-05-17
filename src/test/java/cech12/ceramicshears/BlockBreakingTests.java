package cech12.ceramicshears;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@GameTestHolder(CeramicShearsMod.MOD_ID)
public class BlockBreakingTests {

    private static final Block[] DROP_THEMSELVES_BLOCKS = {
            //leaves
            Blocks.ACACIA_LEAVES,
            Blocks.AZALEA_LEAVES,
            Blocks.BIRCH_LEAVES,
            Blocks.DARK_OAK_LEAVES,
            Blocks.FLOWERING_AZALEA_LEAVES,
            Blocks.JUNGLE_LEAVES,
            Blocks.MANGROVE_LEAVES,
            Blocks.OAK_LEAVES,
            Blocks.SPRUCE_LEAVES,
            //misc
            Blocks.COBWEB,
            Blocks.DEAD_BUSH,
            Blocks.GLOW_LICHEN,
            Blocks.GRASS,
            Blocks.HANGING_ROOTS,
            Blocks.FERN,
            Blocks.NETHER_SPROUTS,
            Blocks.SEAGRASS,
            Blocks.TWISTING_VINES,
            Blocks.VINE,
            Blocks.WEEPING_VINES
    };

    private static final HashMap<Block, Block> DROP_OTHERS_BLOCKS = new HashMap<>() {{
        put(Blocks.TALL_SEAGRASS, Blocks.SEAGRASS);
    }};

    @GameTestGenerator
    public static List<TestFunction> generateBlockBreakingTests() {
        List<TestFunction> testFunctions = new ArrayList<>();
        Map<Block, Block> blockMap = Arrays.stream(DROP_THEMSELVES_BLOCKS).collect(Collectors.toMap(block -> block, block -> block));
        blockMap.putAll(DROP_OTHERS_BLOCKS);
        for (Map.Entry<Block, Block> blockEntry : blockMap.entrySet()) {
            Block source = blockEntry.getKey();
            Block drop = blockEntry.getValue();
            String sourceName = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(source)).getPath();
            String dropName = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(drop)).getPath();
            String testName = "test_" + sourceName + "_drops_" + dropName + "_when_breaking_with_shears";
            testFunctions.add(new TestFunction(
                    "defaultBatch",
                    testName,
                    new ResourceLocation(CeramicShearsMod.MOD_ID, "empty").toString(),
                    Rotation.NONE,
                    100,
                    0,
                    true,
                    test -> {
                        ItemStack shears = new ItemStack(CeramicShearsMod.CERAMIC_SHEARS.get(), 1);
                        BlockState state = source.defaultBlockState();
                        if (source instanceof MultifaceBlock) {
                            state = state.setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), Boolean.TRUE);
                        }

                        LootContext.Builder lootContextBuilder = (new LootContext.Builder(test.getLevel()))
                                .withRandom(test.getLevel().random)
                                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(new BlockPos(0, 0, 0))) //is needed to generate loot!
                                .withParameter(LootContextParams.TOOL, shears);
                        List<ItemStack> drops = state.getDrops(lootContextBuilder);

                        boolean blockContained = drops.stream().anyMatch(itemStack -> !itemStack.isEmpty() && itemStack.getItem() instanceof BlockItem && ((BlockItem) itemStack.getItem()).getBlock() == drop);
                        test.assertTrue(blockContained, "Breaking " + source + " with shears should drop " + drop + " but instead it drops " + drops);
                        test.succeed();
                    }
            ));
        }
        return testFunctions;
    }


}
