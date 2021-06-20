package cech12.ceramicshears.integration;

import cech12.ceramicshears.IntegrationTestUtils;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.util.math.BlockPos;

@IntegrationTestClass(value = "dispenser")
public class DispenserTests {

    private static final BlockPos BUTTON_POSITION = new BlockPos(0, 1, 0);
    private static final BlockPos DISPENSER_INTERACTION_POSITION = new BlockPos(1, 1, 1);

    //--------------------------------------
    //----------- Entity Tests -------------
    //--------------------------------------

    @IntegrationTest(value = "dispenser")
    public void testDispenserShearsSheep(IntegrationTestHelper helper) {
        SheepEntity entity = (SheepEntity) IntegrationTestUtils.placeEntity(helper, DISPENSER_INTERACTION_POSITION, EntityType.SHEEP);

        helper.pushButton(BUTTON_POSITION);

        helper.assertTrue(entity::isSheared, "Sheep should should be sheared");
    }

    @IntegrationTest(value = "dispenser")
    public void testDispenserShearsMooshroom(IntegrationTestHelper helper) {
        IntegrationTestUtils.placeEntity(helper, DISPENSER_INTERACTION_POSITION, EntityType.MOOSHROOM);

        helper.pushButton(BUTTON_POSITION);

        helper.assertTrue(() -> {
            Entity entity = IntegrationTestUtils.getEntity(helper, DISPENSER_INTERACTION_POSITION);
            return entity != null && entity.getType() == EntityType.COW;
        }, "Mooshroom should have been sheared to a cow");
    }

    @IntegrationTest(value = "dispenser")
    public void testDispenserShearsSnowGolem(IntegrationTestHelper helper) {
        SnowGolemEntity entity = (SnowGolemEntity) IntegrationTestUtils.placeEntity(helper, DISPENSER_INTERACTION_POSITION, EntityType.SNOW_GOLEM);

        helper.pushButton(BUTTON_POSITION);

        helper.assertTrue(() -> !entity.hasPumpkin(), "Snow Golem should have not pumpkin after shearing");
    }

}
