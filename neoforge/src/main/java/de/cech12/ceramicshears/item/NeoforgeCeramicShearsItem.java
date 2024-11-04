package de.cech12.ceramicshears.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class NeoforgeCeramicShearsItem extends CeramicShearsItem {

    public NeoforgeCeramicShearsItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return stack.isDamageableItem(); //references to mixed in method
    }

    @Override
    public int getMaxDamage(@NotNull ItemStack stack) {
        return stack.getMaxDamage(); //references to mixed in method
    }

}
