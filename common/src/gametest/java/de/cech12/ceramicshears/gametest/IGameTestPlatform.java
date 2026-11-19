package de.cech12.ceramicshears.gametest;

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

}
