package de.cech12.ceramicshears.gametest;

import de.cech12.ceramicshears.Constants;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Set;

/**
 * Marks tests that are known to fail on some loaders because of a real bug or missing loader support.
 * On these loaders the test is still executed: it passes while the issue is present and FAILS as soon as the issue is
 * gone, so the marker cannot be forgotten. On all other loaders the test runs normally.
 */
public final class KnownIssues {

    private KnownIssues() {}

    /**
     * Runs a synchronous test body and succeeds the test. On the affected platforms every failed assertion of the body
     * counts as "issue still present", so the body should only contain the assertion of the issue itself. Setup and
     * plausibility checks belong before this call.
     *
     * @param helper game test helper
     * @param issue link or short description of the known issue
     * @param affectedPlatforms platforms where the issue is known (see {@link Platforms})
     * @param test test body with assertions
     */
    public static void run(GameTestHelper helper, String issue, Set<String> affectedPlatforms, Runnable test) {
        String platform = Platforms.current();
        if (!affectedPlatforms.contains(platform)) {
            test.run();
            helper.succeed();
            return;
        }
        try {
            test.run();
        } catch (GameTestAssertException e) {
            Constants.LOG.warn("Known issue still present on {} ({}): {}", platform, issue, e.getMessage());
            helper.succeed();
            return;
        }
        helper.fail("Known issue seems to be fixed on " + platform + " (" + issue + "). Remove the KnownIssues marker of this test.");
    }

}
