package de.cech12.ceramicshears;

import de.cech12.ceramicshears.item.CeramicShearsItem;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Mod class for the Forge loader.
 */
@Mod(Constants.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CeramicShearsMod {

    /** mod specific item registry */
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    /** clay shears part item registry object */
    public static final RegistryObject<Item> CLAY_SHEARS_PART = ITEMS.register("clay_shears_part", () -> new Item(new Item.Properties()));
    /** ceramic shears part item registry object */
    public static final RegistryObject<Item> CERAMIC_SHEARS_PART = ITEMS.register("ceramic_shears_part", () -> new Item(new Item.Properties()));
    /** ceramic shears item registry object */
    public static final RegistryObject<Item> CERAMIC_SHEARS = ITEMS.register("ceramic_shears", CeramicShearsItem::new);

    /**
     * Constructor of a mod instance.
     */
    public CeramicShearsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
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
