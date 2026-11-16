package de.cech12.ceramicshears.gametest;

import com.mojang.serialization.MapCodec;
import de.cech12.ceramicshears.Constants;
import de.cech12.ceramicshears.gametest.tests.BlockTests;
import de.cech12.ceramicshears.gametest.tests.DispenserTests;
import de.cech12.ceramicshears.gametest.tests.DurabilityTests;
import de.cech12.ceramicshears.gametest.tests.EntityTests;
import de.cech12.ceramicshears.gametest.tests.ItemTests;
import de.cech12.ceramicshears.gametest.tests.LootTests;
import de.cech12.ceramicshears.gametest.tests.MetaTests;
import de.cech12.ceramicshears.gametest.tests.RecipeTests;
import de.cech12.ceramicshears.gametest.tests.ResourceTests;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Central entry point of all game tests. The loader adapters only call the register methods of this class.
 * <p>
 * Every test function registered here needs a test instance json in
 * {@code common/src/gametest/resources/data/ceramicshears/test_instance/<name>.json} (checked by
 * {@link MetaTests#everyTestFunctionHasAnInstance}).
 */
public final class CeramicShearsGameTests {

    private static Map<Identifier, Consumer<GameTestHelper>> tests;

    private CeramicShearsGameTests() {}

    /**
     * @return all test functions of this mod, keyed by id
     */
    public static synchronized Map<Identifier, Consumer<GameTestHelper>> getTests() {
        if (tests == null) {
            Map<Identifier, Consumer<GameTestHelper>> map = new LinkedHashMap<>();
            TestCollector collector = (name, test) -> {
                Identifier id = Constants.id(name);
                if (map.put(id, test) != null) {
                    throw new IllegalStateException("Duplicate game test " + id);
                }
            };
            MetaTests.register(collector);
            ItemTests.register(collector);
            DurabilityTests.register(collector);
            RecipeTests.register(collector);
            EntityTests.register(collector);
            BlockTests.register(collector);
            DispenserTests.register(collector);
            LootTests.register(collector);
            ResourceTests.register(collector);
            tests = Collections.unmodifiableMap(map);
        }
        return tests;
    }

    /**
     * Registers all test functions. Must be called while the test function registry is writable.
     *
     * @param registrar loader specific registration callback
     */
    public static void registerTestFunctions(BiConsumer<Identifier, Consumer<GameTestHelper>> registrar) {
        getTests().forEach(registrar);
    }

    /**
     * Registers all test environment types. Must be called while the test environment definition type registry is
     * writable.
     *
     * @param registrar loader specific registration callback
     */
    public static void registerEnvironmentTypes(BiConsumer<Identifier, MapCodec<? extends TestEnvironmentDefinition<?>>> registrar) {
        registrar.accept(Constants.id("durability_config"), DurabilityConfigEnvironment.CODEC);
    }

    /**
     * Collects test functions of a test class.
     */
    @FunctionalInterface
    public interface TestCollector {
        /**
         * @param name test name (path of the test id, must match the test_instance json file name)
         * @param test test function
         */
        void add(String name, Consumer<GameTestHelper> test);
    }

}
