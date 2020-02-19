package cech12.brickshears;

import cech12.brickshears.api.item.BrickShearsItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cech12.brickshears.BrickShearsMod.MOD_ID;

@Mod(MOD_ID)
@Mod.EventBusSubscriber
public class BrickShearsMod {

    public static final String MOD_ID = "brickshears";

    //TODO tripwire activation

    /**
     * Add carving behaviour to pumpkins. mostly copied from Pumpkin class.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState blockState = event.getWorld().getBlockState(event.getPos());
        ItemStack itemStack = event.getPlayer().getHeldItem(event.getHand());
        //check for pumpkin and brick shears
        if (blockState.getBlock() == Blocks.PUMPKIN && itemStack.getItem() == BrickShearsItems.BRICK_SHEARS) {
            World worldIn = event.getWorld();
            if (!worldIn.isRemote) {
                PlayerEntity player = event.getPlayer();
                BlockPos pos = event.getPos();
                Direction direction = event.getFace();
                if (direction == null) return;
                Direction direction1 = direction.getAxis() == Direction.Axis.Y ? player.getHorizontalFacing().getOpposite() : direction;
                worldIn.playSound(null, pos, SoundEvents.BLOCK_PUMPKIN_CARVE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                worldIn.setBlockState(pos, Blocks.CARVED_PUMPKIN.getDefaultState().with(CarvedPumpkinBlock.FACING, direction1), 11);
                ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX() + 0.5D + (double)direction1.getXOffset() * 0.65D, (double)pos.getY() + 0.1D, (double)pos.getZ() + 0.5D + (double)direction1.getZOffset() * 0.65D, new ItemStack(Items.PUMPKIN_SEEDS, 4));
                itementity.setMotion(0.05D * (double)direction1.getXOffset() + worldIn.rand.nextDouble() * 0.02D, 0.05D, 0.05D * (double)direction1.getZOffset() + worldIn.rand.nextDouble() * 0.02D);
                worldIn.addEntity(itementity);
                itemStack.damageItem(1, player, (p_220282_1_) -> p_220282_1_.sendBreakAnimation(event.getHand()));
            }
        }
    }

}
