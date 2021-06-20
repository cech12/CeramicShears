package cech12.ceramicshears.integration;

import cech12.ceramicshears.IntegrationTestUtils;
import cech12.ceramicshears.api.item.CeramicShearsItems;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTest;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestClass;
import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;

@IntegrationTestClass(value = "entity")
public class EntityTests {

    private static final BlockPos INTERACTION_POSITION = new BlockPos(1, 1, 1);

    @IntegrationTest(value = "entity_pit")
    public void testSheepInteraction(IntegrationTestHelper helper) {
        SheepEntity entity = (SheepEntity) IntegrationTestUtils.placeEntity(helper, INTERACTION_POSITION, EntityType.SHEEP);
        ItemStack shears = new ItemStack(CeramicShearsItems.CERAMIC_SHEARS);

        ActionResult<ItemStack> result = IntegrationTestUtils.useItemOnEntity(helper, entity, shears);

        helper.assertTrue(() -> result.getResult() == ActionResultType.SUCCESS, "Entity interaction should be marked es successful");
        helper.assertTrue(() -> result.getObject().getDamageValue() == 1, "Shears should loose one durability point");
        helper.assertTrue(entity::isSheared, "Sheep should be sheared");
    }

    @IntegrationTest(value = "entity_pit")
    public void testMooshroomInteraction(IntegrationTestHelper helper) {
        MooshroomEntity entity = (MooshroomEntity) IntegrationTestUtils.placeEntity(helper, INTERACTION_POSITION, EntityType.MOOSHROOM);
        ItemStack shears = new ItemStack(CeramicShearsItems.CERAMIC_SHEARS);

        ActionResult<ItemStack> result = IntegrationTestUtils.useItemOnEntity(helper, entity, shears);

        helper.assertTrue(() -> result.getResult() == ActionResultType.SUCCESS, "Entity interaction should be marked es successful");
        helper.assertTrue(() -> result.getObject().getDamageValue() == 1, "Shears should loose one durability point");
        helper.assertTrue(() -> {
            Entity resultEntity = IntegrationTestUtils.getEntity(helper, INTERACTION_POSITION);
            return resultEntity != null && resultEntity.getType() == EntityType.COW;
        }, "Mooshrooms should be a cow after shearing");
    }

    @IntegrationTest(value = "entity_pit")
    public void testSnowGolemInteraction(IntegrationTestHelper helper) {
        SnowGolemEntity entity = (SnowGolemEntity) IntegrationTestUtils.placeEntity(helper, INTERACTION_POSITION, EntityType.SNOW_GOLEM);
        ItemStack shears = new ItemStack(CeramicShearsItems.CERAMIC_SHEARS);

        ActionResult<ItemStack> result = IntegrationTestUtils.useItemOnEntity(helper, entity, shears);

        helper.assertTrue(() -> result.getResult() == ActionResultType.SUCCESS, "Entity interaction should be marked es successful");
        helper.assertTrue(() -> result.getObject().getDamageValue() == 1, "Shears should loose one durability point");
        helper.assertTrue(() -> !entity.hasPumpkin(), "Snow Golem should not have a pumpkin after shearing");
    }

}
