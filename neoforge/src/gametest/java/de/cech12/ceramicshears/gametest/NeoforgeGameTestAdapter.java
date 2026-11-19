package de.cech12.ceramicshears.gametest;

import de.cech12.ceramicshears.Constants;
import de.cech12.ceramicshears.platform.NeoforgeConfigHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.lang.reflect.Field;

/**
 * Thin NeoForge adapter for the common game tests. Only part of the gametest source set.
 */
@EventBusSubscriber(modid = Constants.MOD_ID)
public final class NeoforgeGameTestAdapter implements IGameTestPlatform {

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, helper -> CeramicShearsGameTests.registerTestFunctions(helper::register));
        event.register(Registries.TEST_ENVIRONMENT_DEFINITION_TYPE, helper -> CeramicShearsGameTests.registerEnvironmentTypes(helper::register));
    }

    @Override
    @SuppressWarnings("unchecked")
    public int setDurability(int durability) {
        try {
            ModConfigSpec.IntValue value = durabilityValue();
            int previous = value.get();
            // like loading a config file: NeoForge corrects values outside the range (the mod itself does not clamp here)
            value.set((Integer) value.getSpec().correct(durability));
            return previous;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot change durability config", e);
        }
    }

    @Override
    public int defaultDurability() {
        try {
            return durabilityValue().getDefault();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read durability config", e);
        }
    }

    @Override
    public int burnTime(ServerLevel level, ItemStack stack) {
        return stack.getBurnTime(null, level.fuelValues());
    }

    private static ModConfigSpec.IntValue durabilityValue() throws ReflectiveOperationException {
        Field field = NeoforgeConfigHelper.class.getDeclaredField("DURABILITY");
        field.setAccessible(true);
        return (ModConfigSpec.IntValue) field.get(null);
    }

}
