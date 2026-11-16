package de.cech12.ceramicshears.gametest;

import de.cech12.ceramicshears.platform.Services;

/**
 * Platform names of the loaders (see IPlatformHelper#getPlatformName), for tests that intentionally expect different
 * behavior per loader and for {@link KnownIssues}.
 */
public final class Platforms {

    /** platform name of Fabric */
    public static final String FABRIC = "Fabric";
    /** platform name of Forge */
    public static final String FORGE = "Forge";
    /** platform name of NeoForge */
    public static final String NEOFORGE = "NeoForge";

    private Platforms() {}

    /** @return platform name of the running loader */
    public static String current() {
        return Services.PLATFORM.getPlatformName();
    }

    /** @return true if the tests run on Forge */
    public static boolean isForge() {
        return FORGE.equals(current());
    }

    /** @return true if the tests run on Fabric */
    public static boolean isFabric() {
        return FABRIC.equals(current());
    }

}
