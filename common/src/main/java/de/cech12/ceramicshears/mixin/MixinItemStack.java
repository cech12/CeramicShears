package de.cech12.ceramicshears.mixin;

import de.cech12.ceramicshears.item.CeramicShearsItem;
import de.cech12.ceramicshears.platform.Services;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for the Item class to enable the possibility to configure the item's durability.
 */
@Mixin(ItemStack.class)
public abstract class MixinItemStack implements DataComponentHolder {

    @Shadow public abstract Item getItem();

    @Inject(at = @At("HEAD"), method = "getMaxDamage", cancellable = true)
    public void getMaxDamageProxy(CallbackInfoReturnable<Integer> cir) {
        if (this.getItem() instanceof CeramicShearsItem) {
            cir.setReturnValue(Services.CONFIG.getDurability());
            cir.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "isDamageableItem", cancellable = true)
    public void isDamageableItemProxy(CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem() instanceof CeramicShearsItem) {
            cir.setReturnValue(Services.CONFIG.getDurability() > 0 && !this.has(DataComponents.UNBREAKABLE));
            cir.cancel();
        }
    }

}
