package de.cech12.ceramicshears;

import de.cech12.ceramicshears.item.CeramicShearsItem;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

/**
 * Mod class for the Forge loader.
 */
@SuppressWarnings("unused")
@Mod(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid= Constants.MOD_ID)
public class ForgeCeramicShearsMod {

    /** mod specific item registry */
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    /** clay shears part item registry object */
    public static final RegistryObject<Item> CLAY_SHEARS_PART = registerItem("clay_shears_part", Item::new);
    /** ceramic shears part item registry object */
    public static final RegistryObject<Item> CERAMIC_SHEARS_PART = registerItem("ceramic_shears_part", Item::new);
    /** ceramic shears item registry object */
    public static final RegistryObject<Item> CERAMIC_SHEARS = registerItem("ceramic_shears", CeramicShearsItem::new);

    private static RegistryObject<Item> registerItem(String name, Function<Item.Properties, Item> itemConstructor) {
        return ITEMS.register(name, () -> itemConstructor.apply(new Item.Properties().setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), Constants.id(name)))));
    }

    /**
     * Constructor of a mod instance.
     */
    public ForgeCeramicShearsMod(FMLJavaModLoadingContext context) {
        ITEMS.register(context.getModBusGroup());
        CommonLoader.init();
    }

    /**
     * Registers the dispense behaviour of the ceramic shears.
     * @param event FMLCommonSetupEvent
     */
    @SubscribeEvent
    public static void registerDispenseBehavior(FMLCommonSetupEvent event) {
        DispenserBlock.registerBehavior(CERAMIC_SHEARS.get(), new ShearsDispenseItemBehavior());
    }

    /**
     * Registers the creative tabs for all items.
     * @param event BuildCreativeModeTabContentsEvent
     */
    @SubscribeEvent
    public static void addItemsToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(CLAY_SHEARS_PART);
            event.accept(CERAMIC_SHEARS_PART);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CERAMIC_SHEARS);
        }
    }

}
