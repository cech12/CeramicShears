package cech12.ceramicshears.init;

import cech12.ceramicshears.api.item.CeramicShearsItems;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.BeehiveDispenseBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ShearsItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import static cech12.ceramicshears.CeramicShearsMod.MOD_ID;

@Mod.EventBusSubscriber(modid= MOD_ID, bus= Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        CeramicShearsItems.CLAY_SHEARS_PART = registerItem("clay_shears_part", new Item((new Item.Properties()).tab(ItemGroup.TAB_MISC)));
        CeramicShearsItems.CERAMIC_SHEARS_PART = registerItem("ceramic_shears_part", new Item((new Item.Properties()).tab(ItemGroup.TAB_MISC)));
        CeramicShearsItems.CERAMIC_SHEARS = registerItem("ceramic_shears", new ShearsItem((new Item.Properties()).durability(179).tab(ItemGroup.TAB_TOOLS)));

        DispenserBlock.registerBehavior(CeramicShearsItems.CERAMIC_SHEARS.asItem(), new BeehiveDispenseBehavior());
    }

    private static Item registerItem(String name, Item item) {
        item.setRegistryName(name);
        ForgeRegistries.ITEMS.register(item);
        return item;
    }

}
