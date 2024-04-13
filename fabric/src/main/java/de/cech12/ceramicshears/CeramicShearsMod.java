package de.cech12.ceramicshears;

import de.cech12.ceramicshears.item.CeramicShearsItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

/**
 * Mod class for the Fabric loader.
 */
public class CeramicShearsMod implements ModInitializer {

    /** clay shears part item registry object */
    public static final Item CLAY_SHEARS_PART = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Constants.MOD_ID, "clay_shears_part"), new Item(new Item.Properties()));
    /** ceramic shears part item registry object */
    public static final Item CERAMIC_SHEARS_PART = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Constants.MOD_ID, "ceramic_shears_part"), new Item(new Item.Properties()));
    /** ceramic shears item registry object */
    public static final Item CERAMIC_SHEARS = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Constants.MOD_ID, "ceramic_shears"), new CeramicShearsItem());

    /**
     * Initialization of a mod instance.
     */
    @Override
    public void onInitialize() {
        CommonLoader.init();
        //Register item in the creative tab.
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(content -> {
            content.accept(CLAY_SHEARS_PART);
            content.accept(CERAMIC_SHEARS_PART);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.accept(CERAMIC_SHEARS);
        });
        //register dispense behavior (is done by wsmlmb)
        //DispenserBlock.registerBehavior(CERAMIC_SHEARS, new ShearsDispenseItemBehavior());
    }

}
