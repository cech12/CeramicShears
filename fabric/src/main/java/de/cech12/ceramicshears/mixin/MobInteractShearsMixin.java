package de.cech12.ceramicshears.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.cech12.ceramicshears.item.CeramicShearsItem;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({Bogged.class, CopperGolem.class, MushroomCow.class, Sheep.class, SnowGolem.class})
public abstract class MobInteractShearsMixin {

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"), method = "mobInteract")
	private boolean isShears(ItemStack stack, Object item, Operation<Boolean> original) {
		return original.call(stack, item) || (item == Items.SHEARS && stack.getItem() instanceof CeramicShearsItem);
	}

}
