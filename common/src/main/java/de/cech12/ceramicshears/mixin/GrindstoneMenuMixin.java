package de.cech12.ceramicshears.mixin;

import de.cech12.ceramicshears.item.CeramicShearsItem;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Combining two ceramic shears in a grindstone must not store a fixed max damage, so the durability config still
 * applies to the result (like the crafting repair, see {@link RepairItemRecipeMixin}). #51
 */
@Mixin(GrindstoneMenu.class)
public class GrindstoneMenuMixin {

    @Inject(at = @At("RETURN"), method = "mergeItems")
    private void ceramicshears$removeStoredMaxDamage(ItemStack first, ItemStack second, CallbackInfoReturnable<ItemStack> cir) {
        CeramicShearsItem.removeStoredMaxDamage(cir.getReturnValue());
    }

}
