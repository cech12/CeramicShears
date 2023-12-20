package de.cech12.ceramicshears.platform;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import de.cech12.ceramicshears.Constants;
import de.cech12.ceramicshears.platform.services.IConfigHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * The config service implementation for Forge.
 */
public class ForgeConfigHelper implements IConfigHelper {

    private static final ForgeConfigSpec SERVER_CONFIG;

    private static final ForgeConfigSpec.IntValue DURABILITY;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Balance Options");

        DURABILITY = builder
                .comment(DURABILITY_DESCRIPTION)
                .defineInRange("durability", DURABILITY_DEFAULT, DURABILITY_MIN, DURABILITY_MAX);

        builder.pop();

        SERVER_CONFIG = builder.build();
    }

    @Override
    public void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG);
        Path path = FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve(Constants.MOD_ID + "-server.toml");
        final CommentedFileConfig configData = CommentedFileConfig.builder(path).sync().autosave().writingMode(WritingMode.REPLACE).build();
        configData.load();
        SERVER_CONFIG.setConfig(configData);
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
