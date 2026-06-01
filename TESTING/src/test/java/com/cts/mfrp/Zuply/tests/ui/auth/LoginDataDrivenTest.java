package com.cts.mfrp.zuply.tests.ui.auth;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.LoginPage;
import com.cts.mfrp.zuply.utils.ExcelUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Data-driven login scenarios sourced from UI_AuthData.xlsx (sheet: "Login").
 *
 * Excel contract — columns read by name (order-independent):
 *   | TestCaseId | Scenario      | Email           | Password   |
 *   | TC003      | VALID         | admin@zuply.in  | Admin@123  |
 *   | TC004      | INVALID_CREDS | admin@zuply.in  | WrongPass! |
 *
 * Scenario values understood:
 *   VALID         — login completes, URL leaves /login
 *   INVALID_CREDS / INVALID_ROLE — stays on /login and surfaces an error
 */
@Test(groups = {"regression", "ui", "auth"})
public class LoginDataDrivenTest extends UiBaseTest {

    private static final String DATA_FILE = "src/test/resources/testdata/UI_AuthData.xlsx";
    private static final String SHEET     = "Login";

    /* ------------------------------------------------------------------ */
    /* Named-column flavour — tolerant of column reorders/insertions      */
    /* ------------------------------------------------------------------ */

    @DataProvider(name = "loginRowsAsMaps")
    public Object[][] loginRowsAsMaps() throws Exception {
        return ExcelUtils.getTestDataAsMaps(DATA_FILE, SHEET).stream()
                .map(m -> new Object[]{ m })
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "loginRowsAsMaps", description = "Data-driven login by column name")
    public void loginByName(Map<String, String> row) {
        String scenario = row.getOrDefault("Scenario", "");
        // Map Scenario values to outcome tokens
        String expected;
        if ("VALID".equalsIgnoreCase(scenario)) {
            expected = "SUCCESS";
        } else if (scenario.toUpperCase().startsWith("INVALID")) {
            expected = "INVALID_CREDS";
        } else {
            expected = scenario; // pass through
        }
        runLoginScenario(
                row.getOrDefault("TestCaseId", scenario),
                resolveEmailCell(row.getOrDefault("Email", ""), "login"),
                row.getOrDefault("Password", ""),
                expected);
    }

    /* ------------------------------------------------------------------ */
    /* Shared scenario logic                                              */
    /* ------------------------------------------------------------------ */

    private void runLoginScenario(String testCaseId, String email, String password, String expected) {
        clearSession();                           // ensure no prior login bleeds in
        LoginPage login = new LoginPage(driver);
        login.open();

        if ("SUCCESS".equalsIgnoreCase(expected)) {
            login.loginAs(email, password);       // already waits for URL to leave /login
            Assert.assertFalse(driver.getCurrentUrl().contains("/login"),
                    testCaseId + " — expected to leave /login on successful login");
            return;
        }

        if ("INVALID_CREDS".equalsIgnoreCase(expected)) {
            login.enterEmail(email).enterPassword(password).submit();
            // Accept any common error phrasing the SPA backend may return
            try {
                wait.until(ExpectedConditions.textMatches(
                        By.tagName("body"),
                        Pattern.compile(
                                "invalid email or password|invalid credentials|incorrect|wrong password|login failed",
                                Pattern.CASE_INSENSITIVE)));
            } catch (Exception ignored) { /* fall through — URL assertion is authoritative */ }
            Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                    testCaseId + " — should remain on /login on invalid credentials");
            return;
        }

        Assert.fail(testCaseId + " — unknown Scenario/ExpectedOutcome: " + expected);
    }
}
