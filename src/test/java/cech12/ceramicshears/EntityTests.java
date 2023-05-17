package cech12.ceramicshears;

import cech12.ceramicshears.util.ModTestHelper;
import cech12.ceramicshears.util.PlayerInteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
@GameTestHolder(CeramicShearsMod.MOD_ID)
public class EntityTests {

    private static final BlockPos ENTITY_POSITION = new BlockPos(1, 2, 1);

    private static final EntityType<?>[] SHEARABLE_ENTITIES = {EntityType.SHEEP, EntityType.MOOSHROOM, EntityType.SNOW_GOLEM};

    @GameTestGenerator
    public static List<TestFunction> generateShearingTests() {
        List<TestFunction> testFunctions = new ArrayList<>();
        boolean[] creativeStates = { false, true };
        for (EntityType<?> entityType : SHEARABLE_ENTITIES) {
            for (boolean isCreative : creativeStates) {
                String entityName = Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entityType)).getPath();
                String testName = "test_" + ((isCreative) ? "creative" : "survival") + "_shearing_" + entityName;
                testFunctions.add(new TestFunction(
                        "defaultBatch",
                        testName,
                        new ResourceLocation(CeramicShearsMod.MOD_ID, "entitytests.pit").toString(),
                        Rotation.NONE,
                        100,
                        0,
                        true,
                        test -> {
                            ItemStack shears = new ItemStack(CeramicShearsMod.CERAMIC_SHEARS.get(), 1);
                            Entity entity = test.spawn(entityType, ENTITY_POSITION);
                            if (!(entity instanceof IForgeShearable)) {
                                test.fail("Entity " + entityName + " is not a shearable entity");
                            }
                            PlayerInteractionResult result = ModTestHelper.useItemOnEntity(test, shears, entity, isCreative);
                            if (!result.getResult().consumesAction()) {
                                test.fail("Wrong InteractionResult after using wooden shears on a " + entityName + ": " + result.getResult());
                            }
                            if (isCreative && result.getObject().getDamageValue() != 0) {
                                test.fail("The wooden shears were damaged after creative interacting with a " + entityName);
                            }
                            if (!isCreative && result.getObject().getDamageValue() == 0) {
                                test.fail("The wooden shears were not damaged after survival interacting with a " + entityName);
                            }
                            if (entity instanceof IForgeShearable && ((IForgeShearable) entity).isShearable(shears, entity.level, entity.getOnPos())) {
                                test.fail("The " + entityName + " was not sheared after interacting with wooden shears");
                            }
                            test.succeed();
                        }
                ));
            }
        }
        return testFunctions;
    }

}
