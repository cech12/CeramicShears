package de.cech12.ceramicshears.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.cech12.ceramicshears.item.CeramicShearsItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.TripWireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TripWireBlock.class)
public abstract class TripwireBlockMixin {

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"), method = "playerWillDestroy")
	private boolean isShears(ItemStack stack, Item item, Operation<Boolean> original) {
		return original.call(stack, item) || (item == Items.SHEARS && stack.getItem() instanceof CeramicShearsItem);
	}

}
