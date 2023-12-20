package de.cech12.ceramicshears.platform.services;

/**
 * Common configuration helper service interface.
 */
public interface IConfigHelper {

    /** Default value of the shear's durability */
    int DURABILITY_DEFAULT = 179;
    /** Config description of the shear's durability */
    String DURABILITY_DESCRIPTION = "Defines the maximum durability of Ceramic Shears. (" + DURABILITY_DEFAULT + " - default value, 0 - deactivates the durability)";
    /** Minimal value of the shear's durability */
    int DURABILITY_MIN = 0;
    /** Maximal value of the shear's durability */
    int DURABILITY_MAX = 10000;

    /**
     * Initialization method for the Service implementations.
     */
    void init();

    /**
     * Gets the configured durability value.
     *
     * @return configured durability value
     */
    int getDurability();

}