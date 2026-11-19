package de.cech12.ceramicshears.mixin;

import de.cech12.ceramicshears.item.CeramicShearsItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RepairItemRecipe.class)
public class RepairItemRecipeMixin {

    @Inject(at = @At("RETURN"), method = "assemble", cancellable = true)
    public void getProxy(CraftingInput input, CallbackInfoReturnable<ItemStack> cir) {
        CeramicShearsItem.removeStoredMaxDamage(cir.getReturnValue());
    }

}
