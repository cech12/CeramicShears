package de.cech12.ceramicshears.gametest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.server.level.ServerLevel;

/**
 * Test environment that sets the durability config option for all tests of a batch and restores it afterward.
 * Tests are grouped into batches by environment and batches run one after another, so tests with different
 * config values never run at the same time.
 * <p>
 * Usage in a test_environment json: {@code {"type": "ceramicshears:durability_config", "durability": 0}}
 * <p>
 * In Minecraft 1.21.11 setup returns nothing, so the previous config value is kept in this instance (one instance per
 * test_environment json, its batches never run at the same time).
 */
public final class DurabilityConfigEnvironment implements TestEnvironmentDefinition {

    /** codec of this environment type */
    public static final MapCodec<DurabilityConfigEnvironment> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(Codec.INT.fieldOf("durability").forGetter(DurabilityConfigEnvironment::durability))
                    .apply(i, DurabilityConfigEnvironment::new)
    );

    private final int durability;
    private Integer previous;

    /**
     * @param durability raw config value that is set during the batch
     */
    public DurabilityConfigEnvironment(int durability) {
        this.durability = durability;
    }

    /** @return raw config value that is set during the batch */
    public int durability() {
        return this.durability;
    }

    @Override
    public void setup(ServerLevel level) {
        this.previous = GameTestPlatform.INSTANCE.setDurability(this.durability);
    }

    @Override
    public void teardown(ServerLevel level) {
        if (this.previous != null) {
            GameTestPlatform.INSTANCE.setDurability(this.previous);
            this.previous = null;
        }
    }

    @Override
    public MapCodec<DurabilityConfigEnvironment> codec() {
        return CODEC;
    }

}
