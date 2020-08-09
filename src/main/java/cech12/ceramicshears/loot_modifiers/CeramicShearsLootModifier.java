package cech12.ceramicshears.loot_modifiers;

import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.List;

public class CeramicShearsLootModifier extends LootModifier {

    public CeramicShearsLootModifier(ILootCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        //only called when ceramic shears are used
        BlockState blockState = context.get(LootParameters.BLOCK_STATE);
        if (blockState != null) {
            //generate loot with vanilla shears
            LootContext ctx = new LootContext.Builder(context)
                    .withParameter(LootParameters.TOOL, new ItemStack(Items.SHEARS))
                    .build(LootParameterSets.BLOCK);
            LootTable loottable = context.getWorld().getServer().getLootTableManager()
                    .getLootTableFromLocation(blockState.getBlock().getLootTable());
            return loottable.generate(ctx);
        }
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<CeramicShearsLootModifier> {
        @Override
        public CeramicShearsLootModifier read(ResourceLocation location, JsonObject object, ILootCondition[] conditions) {
            return new CeramicShearsLootModifier(conditions);
        }
    }

}
