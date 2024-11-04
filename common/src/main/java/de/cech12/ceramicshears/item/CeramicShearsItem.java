package de.cech12.ceramicshears.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ShearsItem;

/**
 * Extends the ShearsItem to have a unique class and simplify the construction.
 */
public class CeramicShearsItem extends ShearsItem {

    /**
     * Constructs a CeramicShearsItem by configure it to stack to one item.
     */
    public CeramicShearsItem(Properties properties) {
        super(properties.stacksTo(1).component(DataComponents.TOOL, ShearsItem.createToolProperties()));
    }

}
