package de.cech12.ceramicshears.mixin;

import com.google.common.collect.ImmutableList;
import de.cech12.ceramicshears.FabricCeramicShearsMod;
import de.cech12.ceramicshears.mixin.accessor.HolderSetDirectAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(MatchTool.class)
public abstract class MatchToolMixin implements LootItemCondition {

    @Unique
    private static final List<ItemPredicate> ITEM_PREDICATES = new ArrayList<>();

    @Inject(at = @At("RETURN"), method = "<init>")
    private void initProxy(CallbackInfo ci) {
        //collect all MatchTool predicates
        ((MatchTool)(Object)this).predicate().ifPresent(ITEM_PREDICATES::add);
    }

    static {
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            Holder<Item> ceramicShearsHolder = BuiltInRegistries.ITEM.wrapAsHolder(FabricCeramicShearsMod.CERAMIC_SHEARS);
            Holder<Item> shearsHolder = BuiltInRegistries.ITEM.wrapAsHolder(Items.SHEARS);
            //add ceramic shears to all MatchTool predicates that contains vanilla shears
            for (ItemPredicate itemPredicate : ITEM_PREDICATES) {
                itemPredicate.items().ifPresent(holders -> {
                    if (holders instanceof HolderSet.Direct && holders.contains(shearsHolder) && !holders.contains(ceramicShearsHolder)) {
                        HolderSetDirectAccessor<Item> accessor = ((HolderSetDirectAccessor<Item>) holders);
                        ArrayList<Holder<Item>> newList = new ArrayList<>(accessor.getContents());
                        newList.add(ceramicShearsHolder);
                        accessor.setContents(ImmutableList.copyOf(newList));
                        accessor.setContentsSet(null); //reset contents set
                    }
                });
            }
        });
    }

}
