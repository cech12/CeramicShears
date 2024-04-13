package de.cech12.ceramicshears.platform;

import de.cech12.ceramicshears.Constants;
import de.cech12.ceramicshears.platform.services.IConfigHelper;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;

/**
 * The config service implementation for Fabric.
 */
@Config(name = Constants.MOD_ID)
public class FabricConfigHelper implements ConfigData, IConfigHelper {

    @ConfigEntry.Gui.Tooltip(count = 4)
    @ConfigEntry.BoundedDiscrete(min = DURABILITY_MIN, max = DURABILITY_MAX)
    public int DURABILITY = DURABILITY_DEFAULT;

    @Override
    public void init() {
        AutoConfig.register(FabricConfigHelper.class, Toml4jConfigSerializer::new);
    }

    @Override
    public int getDurability() {
        return AutoConfig.getConfigHolder(FabricConfigHelper.class).getConfig().DURABILITY;
    }

}
