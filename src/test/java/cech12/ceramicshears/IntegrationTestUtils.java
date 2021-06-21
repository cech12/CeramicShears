package cech12.ceramicshears;

import com.alcatrazescapee.mcjunitlib.framework.IntegrationTestHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IntegrationTestUtils {

    public static void placeFluid(IntegrationTestHelper helper, BlockPos pos, Fluid fluid) {
        helper.setBlockState(pos, fluid.defaultFluidState().createLegacyBlock());
    }

    public static Entity placeEntity(IntegrationTestHelper helper, BlockPos pos, EntityType<?> entity) {
        return helper.relativePos(pos).map(actualPos -> entity.spawn(helper.getWorld(), null, null, actualPos, SpawnReason.STRUCTURE, true, true)).orElse(null);
    }

    public static Entity getEntity(IntegrationTestHelper helper, BlockPos pos) {
        List<Entity> entityList = getEntities(helper, pos);
        return entityList.isEmpty() ? null : entityList.get(0);
    }

    public static Entity getEntity(IntegrationTestHelper helper, BlockPos pos, EntityType<?> entityType) {
        List<Entity> entityList = getEntities(helper, pos).stream().filter(entity -> entity.getType() == entityType).collect(Collectors.toList());
        return entityList.isEmpty() ? null : entityList.get(0);
    }

    public static List<Entity> getEntities(IntegrationTestHelper helper, BlockPos pos) {
        return helper.relativePos(pos).map(p -> helper.getWorld().getEntities(null, new AxisAlignedBB(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1))).orElse(new ArrayList<>());
    }

    public static ActionResult<ItemStack> useItem(IntegrationTestHelper helper, BlockPos pos, Item item) {
        return useItem(helper, pos, new ItemStack(item));
    }

    public static ActionResult<ItemStack> useItem(IntegrationTestHelper helper, BlockPos pos, ItemStack stack) {
        return useItem(helper, pos, Direction.UP, stack);
    }

    private static void placePlayer(PlayerEntity player, BlockPos pos, Direction direction) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        float xRot = 0F;
        float yRot = 0F;
        switch (direction) {
            case UP:
                y += 1.0D;
                yRot = 90F;
                break;
            case DOWN:
                y -= 2.0D;
                yRot = -90F;
                break;
            case NORTH:
                y -= 1.0D;
                z += 1.0D;
                xRot = 0F;
                break;
            case EAST:
                y -= 1.0D;
                x -= 1.0D;
                xRot = 90F;
                break;
            case SOUTH:
                y -= 1.0D;
                z -= 1.0D;
                xRot = -180F;
                break;
            case WEST:
                y -= 1.0D;
                x += 1.0D;
                xRot = -90F;
                break;
        }
        player.absMoveTo(x, y, z, xRot, yRot);
    }

    public static ActionResult<ItemStack> useItem(IntegrationTestHelper helper, BlockPos pos, Direction direction, ItemStack stack) {
        return helper.relativePos(pos).map(actualPos -> {
            ServerPlayerEntity player = FakePlayerFactory.getMinecraft(helper.getWorld());
            player.setItemInHand(Hand.MAIN_HAND, stack);
            placePlayer(player, actualPos, direction);
            BlockRayTraceResult rayTrace = new BlockRayTraceResult(Vector3d.ZERO, direction, actualPos, false);
            ActionResultType actionResultType = player.gameMode.useItemOn(player, helper.getWorld(), stack, Hand.MAIN_HAND, rayTrace);
            return new ActionResult<>(actionResultType, player.getItemInHand(Hand.MAIN_HAND));
        }).orElse(ActionResult.fail(ItemStack.EMPTY));
    }

    public static ActionResult<ItemStack> useItemOnEntity(IntegrationTestHelper helper, Entity entity, ItemStack stack) {
        PlayerEntity player = FakePlayerFactory.getMinecraft(helper.getWorld());
        player.setItemInHand(Hand.MAIN_HAND, stack);
        ActionResultType actionResultType = player.interactOn(entity, Hand.MAIN_HAND);
        return new ActionResult<>(actionResultType, player.getItemInHand(Hand.MAIN_HAND));
    }

    public static ActionResult<ItemStack> destroyBlockWithItem(IntegrationTestHelper helper, BlockPos pos, ItemStack stack) {
        return helper.relativePos(pos).map(actualPos -> {
            ServerPlayerEntity player = FakePlayerFactory.getMinecraft(helper.getWorld());
            player.setItemInHand(Hand.MAIN_HAND, stack);
            placePlayer(player, actualPos, Direction.DOWN);
            ActionResultType actionResultType = ActionResultType.FAIL;
            //fake player of forge needs a dummy connection here to "send" world updates
            player.connection = new ServerPlayNetHandler(helper.getWorld().getServer(), new NetworkManager(PacketDirection.SERVERBOUND), player);
            if (player.gameMode.destroyBlock(actualPos)) {
                actionResultType = ActionResultType.SUCCESS;
            }
            return new ActionResult<>(actionResultType, player.getItemInHand(Hand.MAIN_HAND));
        }).orElse(ActionResult.fail(ItemStack.EMPTY));
    }

}
