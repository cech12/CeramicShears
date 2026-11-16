package de.cech12.ceramicshears.item;

import de.cech12.ceramicshears.platform.Services;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
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

    /**
     * Repairing (crafting grid, grindstone) stores the current max damage as a fixed component in the result. Ceramic
     * shears read their max damage from the config (DataComponentHolderMixin), so the stored value is removed again
     * to let config changes still apply. Damage 0 is removed as well, so fully repaired shears equal new shears.
     *
     * @param stack result of the repair, changed in place (other items are ignored)
     */
    public static void removeStoredMaxDamage(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof CeramicShearsItem && Services.CONFIG.getDurability() > 0) {
            stack.remove(DataComponents.MAX_DAMAGE);
            if (stack.getOrDefault(DataComponents.DAMAGE, 0) == 0) {
                stack.remove(DataComponents.DAMAGE);
            }
        }
    }

}
