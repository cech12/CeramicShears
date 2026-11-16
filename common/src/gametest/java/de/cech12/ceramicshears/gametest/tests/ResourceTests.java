package de.cech12.ceramicshears.gametest.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.cech12.ceramicshears.Constants;
import de.cech12.ceramicshears.gametest.CeramicShearsGameTests;
import de.cech12.ceramicshears.platform.Services;
import de.cech12.ceramicshears.platform.services.IConfigHelper;
import net.minecraft.gametest.framework.GameTestHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Client resources (translations, item model, texture). A dedicated server does not load them, so the tests read them
 * directly from the mod resources. How they look in game must be checked manually (see TESTING.md).
 */
public final class ResourceTests {

    /** Item names (path of the item id) of all items of this mod with their English name. */
    private static final Map<String, String> ITEMS = Map.of(
            "ceramic_shears", "Ceramic Shears",
            "ceramic_shears_part", "Ceramic Shears Part",
            "clay_shears_part", "Clay Shears Part");
    private static final String LANG_DIR = "assets/ceramicshears/lang/";
    /** Languages that must always be found, so that a broken directory listing does not check nothing. */
    private static final List<String> REQUIRED_LANGUAGES = List.of("en_us", "de_de");

    private ResourceTests() {}

    public static void register(CeramicShearsGameTests.TestCollector tests) {
        tests.add("resources/item_name_is_translated", ResourceTests::itemNameIsTranslated);
        tests.add("resources/item_model_and_texture_exist", ResourceTests::itemModelAndTextureExist);
        tests.add("resources/config_tooltip_shows_default_and_range", ResourceTests::configTooltipShowsDefaultAndRange);
    }

    private static InputStream open(String path) {
        return Services.class.getClassLoader().getResourceAsStream(path);
    }

    private static JsonObject readJson(GameTestHelper helper, String path) {
        try (InputStream stream = open(path)) {
            helper.assertTrue(stream != null, "resource " + path + " is missing");
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            helper.fail("resource " + path + " cannot be read: " + e.getMessage());
            return null;
        }
    }

    /** All shipped languages: the json files next to en_us.json (in a directory or a jar). */
    private static List<String> shippedLanguages(GameTestHelper helper) {
        URL english = Services.class.getClassLoader().getResource(LANG_DIR + "en_us.json");
        helper.assertTrue(english != null, "resource " + LANG_DIR + "en_us.json is missing");
        try {
            URI uri = english.toURI();
            try {
                return listLanguages(Path.of(uri).getParent());
            } catch (FileSystemNotFoundException e) {
                // resources in a jar that is not opened as file system yet
                FileSystem jar;
                try {
                    jar = FileSystems.newFileSystem(uri, Map.of());
                } catch (FileSystemAlreadyExistsException opened) {
                    // opened by someone else in the meantime: use it, but do not close it
                    return listLanguages(FileSystems.getFileSystem(uri).provider().getPath(uri).getParent());
                }
                try (jar) {
                    return listLanguages(jar.provider().getPath(uri).getParent());
                }
            }
        } catch (URISyntaxException | IOException | RuntimeException e) {
            helper.fail("cannot list the language files in " + english + ": " + e);
            return List.of();
        }
    }

    private static List<String> listLanguages(Path dir) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(".json"))
                    .map(name -> name.substring(0, name.length() - ".json".length()))
                    .sorted().toList();
        }
    }

    /** Every shipped language translates the names of all items, English is "Ceramic Shears" (and the parts). */
    public static void itemNameIsTranslated(GameTestHelper helper) {
        List<String> languages = shippedLanguages(helper);
        helper.assertTrue(languages.containsAll(REQUIRED_LANGUAGES), "language files " + REQUIRED_LANGUAGES + " not found, found only " + languages);
        JsonObject english = readJson(helper, LANG_DIR + "en_us.json");
        for (var item : ITEMS.entrySet()) {
            String key = "item.ceramicshears." + item.getKey();
            for (String language : languages) {
                JsonObject lang = readJson(helper, LANG_DIR + language + ".json");
                helper.assertTrue(lang.has(key) && !lang.get(key).getAsString().isBlank(), language + " does not translate " + key);
            }
            helper.assertValueEqual(english.get(key).getAsString(), item.getValue(), "English name of " + item.getKey());
        }
        Constants.LOG.info("Checked the translations of {} languages: {}", languages.size(), languages);
        helper.succeed();
    }

    /** For every item the item model definition points to an existing model, which points to an existing texture. */
    public static void itemModelAndTextureExist(GameTestHelper helper) {
        for (String item : ITEMS.keySet()) {
            JsonObject definition = readJson(helper, "assets/ceramicshears/items/" + item + ".json");
            String model = definition.getAsJsonObject("model").get("model").getAsString();
            helper.assertValueEqual(model, "ceramicshears:item/" + item, "model of item definition " + item);
            JsonObject modelJson = readJson(helper, "assets/ceramicshears/models/item/" + item + ".json");
            String texture = modelJson.getAsJsonObject("textures").get("layer0").getAsString();
            helper.assertValueEqual(texture, "ceramicshears:item/" + item, "texture of item model " + item);
            try (InputStream stream = open("assets/ceramicshears/textures/item/" + item + ".png")) {
                helper.assertTrue(stream != null && stream.read() != -1, "texture of " + item + " is missing or empty");
            } catch (IOException e) {
                helper.fail("texture of " + item + " cannot be read: " + e.getMessage());
            }
        }
        helper.succeed();
    }

    /**
     * The tooltip of the durability option in the config screen (Cloth Config, Fabric) shows the real default and range
     * in every language that translates it.
     */
    public static void configTooltipShowsDefaultAndRange(GameTestHelper helper) {
        String option = "text.autoconfig.ceramicshears.option.DURABILITY.@Tooltip";
        int checked = 0;
        for (String language : shippedLanguages(helper)) {
            JsonObject lang = readJson(helper, LANG_DIR + language + ".json");
            if (lang.has(option + "[3]")) {
                String range = lang.get(option + "[3]").getAsString();
                helper.assertTrue(containsNumber(range, IConfigHelper.DURABILITY_MIN) && containsNumber(range, IConfigHelper.DURABILITY_MAX),
                        language + ": range tooltip \"" + range + "\" does not show " + IConfigHelper.DURABILITY_MIN + " - " + IConfigHelper.DURABILITY_MAX);
            }
            if (lang.has(option + "[4]")) {
                String defaultValue = lang.get(option + "[4]").getAsString();
                helper.assertTrue(containsNumber(defaultValue, IConfigHelper.DURABILITY_DEFAULT),
                        language + ": default tooltip \"" + defaultValue + "\" does not show " + IConfigHelper.DURABILITY_DEFAULT);
                checked++;
            }
        }
        helper.assertTrue(checked > 0, "no language translates the config tooltip");
        helper.succeed();
    }

    /** @return true if the text contains the number as a whole number (not only as part of another number) */
    private static boolean containsNumber(String text, int number) {
        return Pattern.compile("(?<!\\d)" + number + "(?!\\d)").matcher(text).find();
    }

}
