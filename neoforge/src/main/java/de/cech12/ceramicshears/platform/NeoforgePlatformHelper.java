package de.cech12.ceramicshears.platform;

import de.cech12.ceramicshears.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

/**
 * The platform service implementation for Neoforge.
 */
public class NeoforgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

}
