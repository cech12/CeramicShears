package de.cech12.ceramicshears.platform;

import de.cech12.ceramicshears.platform.services.IConfigHelper;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The config service implementation for Neoforge.
 */
public class NeoforgeConfigHelper implements IConfigHelper {

    private static final ModConfigSpec SERVER_CONFIG;

    private static final ModConfigSpec.IntValue DURABILITY;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("Balance Options");

        DURABILITY = builder
                .comment(DURABILITY_DESCRIPTION)
                .defineInRange("durability", DURABILITY_DEFAULT, DURABILITY_MIN, DURABILITY_MAX);

        builder.pop();

        SERVER_CONFIG = builder.build();
    }

    @Override
    public void init() {
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG);
    }

    @Override
    public int getDurability() {
        try {
            return DURABILITY.get();
        } catch (IllegalStateException ex) {
            return DURABILITY_DEFAULT;
        }
    }

}
