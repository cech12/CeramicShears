package de.cech12.ceramicshears;

import de.cech12.ceramicshears.item.CeramicShearsItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.function.Function;

/**
 * Mod class for the Fabric loader.
 */
@SuppressWarnings("unused")
public class FabricCeramicShearsMod implements ModInitializer {

    /** clay shears part item registry object */
    public static final Item CLAY_SHEARS_PART = registerItem("clay_shears_part", Item::new);
    /** ceramic shears part item registry object */
    public static final Item CERAMIC_SHEARS_PART = registerItem("ceramic_shears_part", Item::new);
    /** ceramic shears item registry object */
    public static final Item CERAMIC_SHEARS = registerItem("ceramic_shears", CeramicShearsItem::new);

    private static Item registerItem(String name, Function<Item.Properties, Item> itemConstructor) {
        ResourceKey<Item> resourceKey = ResourceKey.create(BuiltInRegistries.ITEM.key(), Constants.id(name));
        return Registry.register(BuiltInRegistries.ITEM, resourceKey, itemConstructor.apply(new Item.Properties().setId(resourceKey)));
    }

    /**
     * Initialization of a mod instance.
     */
    @Override
    public void onInitialize() {
        CommonLoader.init();
        //Register item in the creative tab.
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(content -> {
            content.accept(CLAY_SHEARS_PART);
            content.accept(CERAMIC_SHEARS_PART);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.accept(CERAMIC_SHEARS);
        });
        //register dispense behavior
        DispenserBlock.registerBehavior(CERAMIC_SHEARS, new ShearsDispenseItemBehavior());
    }

}
