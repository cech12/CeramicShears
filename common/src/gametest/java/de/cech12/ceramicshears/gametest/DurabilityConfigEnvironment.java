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
 *
 * @param durability raw config value that is set during the batch
 */
public record DurabilityConfigEnvironment(int durability) implements TestEnvironmentDefinition<Integer> {

    /** codec of this environment type */
    public static final MapCodec<DurabilityConfigEnvironment> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(Codec.INT.fieldOf("durability").forGetter(DurabilityConfigEnvironment::durability))
                    .apply(i, DurabilityConfigEnvironment::new)
    );

    @Override
    public Integer setup(ServerLevel level) {
        return GameTestPlatform.INSTANCE.setDurability(this.durability);
    }

    @Override
    public void teardown(ServerLevel level, Integer previous) {
        GameTestPlatform.INSTANCE.setDurability(previous);
    }

    @Override
    public MapCodec<DurabilityConfigEnvironment> codec() {
        return CODEC;
    }

}
