package de.cech12.ceramicshears.mixin;

import de.cech12.ceramicshears.item.CeramicShearsItem;
import de.cech12.ceramicshears.platform.Services;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for the Item class to enable the possibility to configure the item's durability.
 */
@Mixin(Item.class)
public class MixinItem {

    @Mutable
    @Final
    @Shadow
    private int maxDamage;

    /**
     * Updates the max damage value (by using reflection) if it is a CeramicShearsItem and the maxDamage is different.
     */
    @Unique
    private void ceramicShears$updateMaxDamage() {
        if (!((Object)this instanceof CeramicShearsItem)) {
            return;
        }
        int durability = Services.CONFIG.getDurability();
        if (maxDamage == durability) {
            return;
        }
        maxDamage = durability;
    }

    /**
     * Injection method to initialize the maxDamage value.
     * @param cir CallbackInfoReturnable
     */
    @Inject(at = @At("HEAD"), method = "getMaxDamage")
    public void getMaxDamageProxy(CallbackInfoReturnable<Integer> cir) {
        ceramicShears$updateMaxDamage();
    }

    /**
     * Injection method to initialize the maxDamage value.
     * @param cir CallbackInfoReturnable
     */
    @Inject(at = @At("HEAD"), method = "canBeDepleted")
    public void canBeDepletedProxy(CallbackInfoReturnable<Boolean> cir) {
        ceramicShears$updateMaxDamage();
    }

    /**
     * Injection method to initialize the maxDamage value.
     * @param stack ItemStack
     * @param cir CallbackInfoReturnable
     */
    @Inject(at = @At("HEAD"), method = "getBarWidth")
    public void getBarWidthProxy(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        ceramicShears$updateMaxDamage();
    }

    /**
     * Injection method to initialize the maxDamage value.
     * @param stack ItemStack
     * @param cir CallbackInfoReturnable
     */
    @Inject(at = @At("HEAD"), method = "getBarColor")
    public void getBarColorProxy(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        ceramicShears$updateMaxDamage();
    }

}
