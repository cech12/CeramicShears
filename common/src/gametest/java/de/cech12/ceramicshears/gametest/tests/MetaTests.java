package de.cech12.ceramicshears.gametest.tests;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import de.cech12.ceramicshears.gametest.CeramicShearsGameTests;
import de.cech12.ceramicshears.gametest.Platforms;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests that check the test setup itself.
 */
public final class MetaTests {

    private MetaTests() {}

    public static void register(CeramicShearsGameTests.TestCollector tests) {
        tests.add("meta/every_test_function_has_an_instance", MetaTests::everyTestFunctionHasAnInstance);
        tests.add("meta/platform_name_matches_the_loader", MetaTests::platformNameMatchesTheLoader);
    }

    /**
     * Every test function has a test_instance json with the same id that runs exactly this function. A test function
     * without one would silently never run, and a copied json that names another function would run that one twice.
     */
    public static void everyTestFunctionHasAnInstance(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var instances = registries.lookupOrThrow(Registries.TEST_INSTANCE);
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        List<Identifier> missing = new ArrayList<>();
        List<String> wrong = new ArrayList<>();
        for (Identifier id : CeramicShearsGameTests.getTests().keySet()) {
            var instance = instances.get(ResourceKey.create(Registries.TEST_INSTANCE, id));
            if (instance.isEmpty()) {
                missing.add(id);
                continue;
            }
            GameTestInstance test = instance.get().value();
            String function = test instanceof FunctionGameTestInstance
                    ? GameTestInstance.DIRECT_CODEC.encodeStart(ops, test).result()
                            .filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject)
                            .map(json -> json.get("function")).map(JsonElement::getAsString).orElse("?")
                    : "(no function instance)";
            if (!id.toString().equals(function)) {
                wrong.add(id + " runs " + function);
            }
        }
        helper.assertTrue(missing.isEmpty(), "Test functions without test_instance json: " + missing);
        helper.assertTrue(wrong.isEmpty(), "test_instance jsons that do not run their own test function: " + wrong);
        helper.succeed();
    }

    /** Loader classes that identify the running loader, keyed by the expected platform name. */
    private static final Map<String, String> LOADER_CLASSES = Map.of(
            Platforms.FABRIC, "net.fabricmc.loader.api.FabricLoader",
            Platforms.FORGE, "net.minecraftforge.fml.loading.FMLLoader",
            Platforms.NEOFORGE, "net.neoforged.fml.loading.FMLLoader");

    /**
     * The platform name of the mod (IPlatformHelper#getPlatformName) names the loader that actually runs. Tests with
     * intended loader differences and {@link de.cech12.ceramicshears.gametest.KnownIssues} rely on it. Regression: the
     * Fabric version reported "NeoForge".
     */
    public static void platformNameMatchesTheLoader(GameTestHelper helper) {
        List<String> running = LOADER_CLASSES.entrySet().stream()
                .filter(entry -> classExists(entry.getValue())).map(Map.Entry::getKey).toList();
        helper.assertValueEqual(running.size(), 1, "number of detected loaders " + running);
        helper.assertValueEqual(Platforms.current(), running.getFirst(), "platform name");
        helper.succeed();
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, MetaTests.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

}
