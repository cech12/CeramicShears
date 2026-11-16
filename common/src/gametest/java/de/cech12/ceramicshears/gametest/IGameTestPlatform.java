package de.cech12.ceramicshears.gametest;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * Loader specific operations that the game tests need, but that cannot be done with vanilla code alone.
 * Each loader provides an implementation in its gametest source set (see META-INF/services).
 */
public interface IGameTestPlatform {

    /**
     * Changes the durability config value of the mod at runtime, as if a server admin changed the config file.
     * Loaders with a validating config system (Forge, NeoForge) must apply their range correction here.
     *
     * @param durability raw durability value
     * @return previous stored config value, to restore it exactly (on Fabric it can be outside the valid range, Forge
     *         and NeoForge only store corrected values)
     */
    int setDurability(int durability);

    /**
     * Reads the default of the durability config option from the config definition of the loader (Forge/NeoForge: the
     * config spec, Fabric: the initial value of the config class), not from the current config value.
     *
     * @return default durability of the config definition
     */
    int defaultDurability();

    /**
     * Burn time of a stack in a furnace, as the furnace of the loader determines it. Vanilla and Forge use the fuel
     * values of the level, NeoForge asks the item itself (IItemExtension#getBurnTime).
     *
     * @param level level whose fuel values are used
     * @param stack fuel stack
     * @return burn time in ticks, 0 if the stack is no fuel
     */
    default int burnTime(ServerLevel level, ItemStack stack) {
        return level.fuelValues().burnDuration(stack);
    }

}
