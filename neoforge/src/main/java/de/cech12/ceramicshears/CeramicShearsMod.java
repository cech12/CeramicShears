package de.cech12.ceramicshears;

import de.cech12.ceramicshears.item.CeramicShearsItem;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Mod class for the Neoforge loader.
 */
@Mod(Constants.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CeramicShearsMod {

    /** mod specific item registry */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);

    /** clay shears part item registry object */
    public static final DeferredItem<Item> CLAY_SHEARS_PART = ITEMS.register("clay_shears_part", () -> new Item(new Item.Properties()));
    /** ceramic shears part item registry object */
    public static final DeferredItem<Item> CERAMIC_SHEARS_PART = ITEMS.register("ceramic_shears_part", () -> new Item(new Item.Properties()));
    /** ceramic shears item registry object */
    public static final DeferredItem<Item> CERAMIC_SHEARS = ITEMS.register("ceramic_shears", CeramicShearsItem::new);

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
