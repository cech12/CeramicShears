package cech12.ceramicshears.init;

import cech12.ceramicshears.api.item.CeramicShearsItems;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.OptionalDispenseBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

import static cech12.ceramicshears.CeramicShearsMod.MOD_ID;

@Mod.EventBusSubscriber(modid= MOD_ID, bus= Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        CeramicShearsItems.CLAY_SHEARS_PART = registerItem("clay_shears_part", new Item((new Item.Properties()).group(ItemGroup.MISC)));
        CeramicShearsItems.CERAMIC_SHEARS_PART = registerItem("ceramic_shears_part", new Item((new Item.Properties()).group(ItemGroup.MISC)));
        CeramicShearsItems.CERAMIC_SHEARS = registerItem("ceramic_shears", new ShearsItem((new Item.Properties()).maxDamage(179).group(ItemGroup.TOOLS)));

        //register dispense behaviour (copy of shears)
        DispenserBlock.registerDispenseBehavior(CeramicShearsItems.CERAMIC_SHEARS.asItem(), new OptionalDispenseBehavior() {
            /**
             * Dispense the specified stack, play the dispense sound and spawn particles.
             */
            @SuppressWarnings("deprecation")
            protected @Nonnull ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                World world = source.getWorld();
                if (!world.isRemote()) {
                    this.successful = false;
                    BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));

                    for(net.minecraft.entity.Entity entity : world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(blockpos), e -> !e.isSpectator() && e instanceof net.minecraftforge.common.IShearable)) {
                        net.minecraftforge.common.IShearable target = (net.minecraftforge.common.IShearable)entity;
                        if (target.isShearable(stack, world, blockpos)) {
                            java.util.List<ItemStack> drops = target.onSheared(stack, entity.world, blockpos,
                                    net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.enchantment.Enchantments.FORTUNE, stack));
                            java.util.Random rand = new java.util.Random();
                            drops.forEach(d -> {
                                net.minecraft.entity.item.ItemEntity ent = entity.entityDropItem(d, 1.0F);
                                if (ent == null) return;
                                ent.setMotion(ent.getMotion().add(((rand.nextFloat() - rand.nextFloat()) * 0.1F), (rand.nextFloat() * 0.05F), ((rand.nextFloat() - rand.nextFloat()) * 0.1F)));
                            });
                            if (stack.attemptDamageItem(1, world.rand, null)) {
                                stack.setCount(0);
                            }

                            this.successful = true;
                            break;
                        }
                        if (!this.successful) {
                            BlockState blockstate = world.getBlockState(blockpos);
                            if (blockstate.isIn(BlockTags.field_226151_aa_)) {
                                int i = blockstate.get(BeehiveBlock.field_226873_c_);
                                if (i >= 5) {
                                    if (stack.attemptDamageItem(1, world.rand, null)) {
                                        stack.setCount(0);
                                    }

                                    BeehiveBlock.func_226878_a_(world, blockpos);
                                    ((BeehiveBlock)blockstate.getBlock()).func_226877_a_(world, blockstate, blockpos, null, BeehiveTileEntity.State.BEE_RELEASED);
                                    this.successful = true;
                                }
                            }
                        }
                    }
                }
                return stack;
            }
        });
    }

    private static Item registerItem(String name, Item item) {
        item.setRegistryName(name);
        ForgeRegistries.ITEMS.register(item);
        return item;
    }

}
