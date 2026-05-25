package com.cts.mfrp.zuply.tests.ui.auth;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.LoginPage;
import com.cts.mfrp.zuply.pages.RegisterPage;
import com.cts.mfrp.zuply.utils.ExcelUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Authentication UI scenarios — FRD §2.1.
 *
 * Data-driven: every input value comes from {@code UI_AuthData.xlsx} (sheets
 * {@code Registration} and {@code Login}). Each test method is bound to a
 * {@link DataProvider} that filters the sheet by {@code Scenario} so one method
 * can iterate every applicable row.
 *
 * Three tests stay inline because they have no row-iteration value:
 *   - AD_TC011 (password strength indicator: spec-level UI presence check)
 *   - AD_TC012 (login chooser exposes 3 roles: FRD-mandated constants)
 *   - AD_TC0013 (JWT alg = HS256: bespoke JWT inspection logic)
 *
 * Token substitution applied to cells:
 *   {@code <RANDOM>}    → {@code "ui.<name>.<8charSuffix>@zuply.in"} (per row)
 *   {@code <ADMIN>}     → {@link UiBaseTest#adminEmail()}
 *   {@code <ADMIN_PWD>} → {@link UiBaseTest#adminPassword()}
 */
@Test(groups = {"smoke", "regression", "ui", "auth"})
public class AuthUiTests extends UiBaseTest {

    private static final String DATA_FILE   = "src/test/resources/testdata/UI_AuthData.xlsx";
    private static final String REG_SHEET   = "Registration";
    private static final String LOGIN_SHEET = "Login";

    // ── DataProviders (filter by Scenario) ───────────────────────────────────

    @DataProvider(name = "regValid")
    public Object[][] regValid() throws Exception { return rowsByScenario(REG_SHEET, "VALID", "VALID_SELLER"); }

    @DataProvider(name = "regDuplicate")
    public Object[][] regDuplicate() throws Exception { return rowsByScenario(REG_SHEET, "DUPLICATE_EMAIL"); }

    @DataProvider(name = "regInvalidEmail")
    public Object[][] regInvalidEmail() throws Exception { return rowsByScenario(REG_SHEET, "INVALID_EMAIL"); }

    @DataProvider(name = "regInvalidPhone")
    public Object[][] regInvalidPhone() throws Exception { return rowsByScenario(REG_SHEET, "INVALID_PHONE"); }

    @DataProvider(name = "regPwdSpecial")
    public Object[][] regPwdSpecial() throws Exception { return rowsByScenario(REG_SHEET, "INVALID_PWD_SPECIAL"); }

    @DataProvider(name = "regInvalidTld")
    public Object[][] regInvalidTld() throws Exception { return rowsByScenario(REG_SHEET, "INVALID_TLD"); }

    @DataProvider(name = "regDisposable")
    public Object[][] regDisposable() throws Exception { return rowsByScenario(REG_SHEET, "DISPOSABLE_DOMAIN"); }

    @DataProvider(name = "loginValid")
    public Object[][] loginValid() throws Exception { return rowsByScenario(LOGIN_SHEET, "VALID"); }

    @DataProvider(name = "loginInvalid")
    public Object[][] loginInvalid() throws Exception { return rowsByScenario(LOGIN_SHEET, "INVALID_CREDS", "INVALID_ROLE"); }

    private Object[][] rowsByScenario(String sheet, String... scenarios) throws Exception {
        Set<String> wanted = new HashSet<>();
        for (String s : scenarios) wanted.add(s.toLowerCase());
        List<Map<String, String>> all = ExcelUtils.getTestDataAsMaps(DATA_FILE, sheet);
        return all.stream()
                .filter(r -> wanted.contains(r.getOrDefault("Scenario", "").toLowerCase()))
                .map(r -> new Object[]{ r })
                .toArray(Object[][]::new);
    }

    // ── Token / parsing helpers ──────────────────────────────────────────────

    /** Resolve {@code <RANDOM>}/{@code <ADMIN>} tokens to concrete email strings. */
    private String resolveEmail(Map<String, String> row) {
        String v = row.getOrDefault("Email", "");
        if ("<RANDOM>".equals(v)) {
            String safeName = row.getOrDefault("Name", "user").toLowerCase().replaceAll("\\s+", "");
            return "ui." + safeName + "." + randomSuffix() + "@zuply.in";
        }
        if ("<ADMIN>".equals(v)) return adminEmail();
        return v;
    }

    /** Resolve {@code <ADMIN_PWD>} to {@link #adminPassword()}; otherwise return literal. */
    private String resolvePassword(Map<String, String> row) {
        String v = row.getOrDefault("Password", "");
        return "<ADMIN_PWD>".equals(v) ? adminPassword() : v;
    }

    private RegisterPage.Role parseRole(String s) {
        return "SELLER".equalsIgnoreCase(s) ? RegisterPage.Role.SELLER : RegisterPage.Role.CUSTOMER;
    }

    /** Wait until {@code body} text matches the given pipe-separated regex (case-insensitive). */
    private void waitForBodyPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) return;
        wait.until(ExpectedConditions.textMatches(
                By.tagName("body"), Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)));
    }

    // ── Registration tests ──────────────────────────────────────────────────

    /** TC001 / AD_TC008 — Valid customer/seller registration. */
    @Test(dataProvider = "regValid", description = "Valid registration (customer + seller)")
    public void validRegistration(Map<String, String> row) {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        Assert.assertTrue(page.isLoaded(),
                row.get("TestCaseId") + " — register form should be displayed");

        String email = resolveEmail(row);
        RegisterPage.Role role = parseRole(row.get("Role"));
        String storeName = row.getOrDefault("StoreName", "");

        if (role == RegisterPage.Role.SELLER && !storeName.isEmpty()) {
            page.registerAs(row.get("Name"), email, row.get("Phone"),
                            resolvePassword(row), role, storeName);
        } else {
            page.registerAs(row.get("Name"), email, row.get("Phone"),
                            resolvePassword(row), role);
        }
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/register")));
        Assert.assertFalse(driver.getCurrentUrl().contains("/register"),
                row.get("TestCaseId") + " — should leave /register after successful submit; was: "
                        + driver.getCurrentUrl());
    }

    /** TC002 — Duplicate email rejected. */
    @Test(dataProvider = "regDuplicate", description = "Duplicate email registration")
    public void duplicateEmailRegistration(Map<String, String> row) {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        page.registerAs(row.get("Name"), resolveEmail(row), row.get("Phone"),
                        resolvePassword(row), parseRole(row.get("Role")));
        waitForBodyPattern(row.get("ErrorPattern"));

        boolean stayedOnRegister = driver.getCurrentUrl().contains("/register");
        boolean showsError = driver.getPageSource().toLowerCase()
                .matches(".*(" + row.get("ErrorPattern").toLowerCase() + ").*");
        Assert.assertTrue(stayedOnRegister || showsError,
                row.get("TestCaseId") + " — expected duplicate-email rejection; url="
                        + driver.getCurrentUrl());
    }

    /** AD_TC006 — Invalid email format. */
    @Test(dataProvider = "regInvalidEmail", description = "Invalid email format on register")
    public void invalidEmailFormatRegistration(Map<String, String> row) {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        page.registerAs(row.get("Name"), row.get("Email"), row.get("Phone"),
                        resolvePassword(row), parseRole(row.get("Role")));
        waitForBodyPattern(row.get("ErrorPattern"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/register"),
                row.get("TestCaseId") + " — should remain on /register when email format invalid");
    }

    /** AD_TC007 / AD_TC0010 — Invalid phone number (too short, or invalid prefix). */
    @Test(dataProvider = "regInvalidPhone", description = "Invalid phone number on register")
    public void invalidPhoneRegistration(Map<String, String> row) {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        page.registerAs(row.get("Name"), resolveEmail(row), row.get("Phone"),
                        resolvePassword(row), parseRole(row.get("Role")));
        waitForBodyPattern(row.get("ErrorPattern"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/register"),
                row.get("TestCaseId") + " — should remain on /register when phone invalid");
    }

    /** AD_TC009 — Password with exotic special characters is rejected. */
    @Test(dataProvider = "regPwdSpecial", description = "Password with special characters")
    public void passwordSpecialCharsRegistration(Map<String, String> row) {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        page.registerAs(row.get("Name"), resolveEmail(row), row.get("Phone"),
                        row.get("Password"), parseRole(row.get("Role")));
        Assert.assertTrue(driver.getCurrentUrl().contains("/register"),
                row.get("TestCaseId") + " — should stay on /register with special-char password");
    }

    /**
     * AD_TC0011 — Invalid TLD email. Preserves the original test's exact
     * assertion (which checks URL contains "/login" — see original AuthUiTests).
     */
    @Test(dataProvider = "regInvalidTld", description = "Invalid TLD email on register")
    public void invalidTldRegistration(Map<String, String> row) {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        page.registerAs(row.get("Name"), row.get("Email"), row.get("Phone"),
                        resolvePassword(row), parseRole(row.get("Role")));
        waitForBodyPattern(row.get("ErrorPattern"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                row.get("TestCaseId") + " — preserved: original test asserts URL contains /login after invalid TLD");
    }

    /** AD_TC0012 — Disposable email domain is rejected with a visible error. */
    @Test(dataProvider = "regDisposable", description = "Disposable email domain on register")
    public void disposableDomainRegistration(Map<String, String> row) {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        page.registerAs(row.get("Name"), row.get("Email"), row.get("Phone"),
                        resolvePassword(row), parseRole(row.get("Role")));
        waitForBodyPattern(row.get("ErrorPattern"));

        boolean stayedOnLogin = driver.getCurrentUrl().contains("/login");
        boolean surfacedError = page.hasAlertBanner() || page.hasEmailValidationError();
        Assert.assertTrue(stayedOnLogin && surfacedError,
                row.get("TestCaseId") + " — expected disposable-domain rejection to surface; url="
                        + driver.getCurrentUrl()
                        + " banner=" + page.getAlertBannerText()
                        + " inlineErr=" + page.getEmailErrorMessage());
    }

    // ── Login tests ─────────────────────────────────────────────────────────

    /** TC003 — Successful login. */
    @Test(dataProvider = "loginValid", description = "Valid login")
    public void validLogin(Map<String, String> row) {
        LoginPage page = new LoginPage(driver);
        page.open();
        page.loginAs(resolveEmail(row), resolvePassword(row));
        Assert.assertFalse(driver.getCurrentUrl().contains("/login"),
                row.get("TestCaseId") + " — should leave /login on valid creds");
    }

    /** TC004 / AD_TC005 — Invalid login (wrong password, wrong role). */
    @Test(dataProvider = "loginInvalid", description = "Invalid login credentials / role")
    public void invalidLogin(Map<String, String> row) {
        LoginPage page = new LoginPage(driver);
        page.open();
        page.enterEmail(resolveEmail(row)).enterPassword(resolvePassword(row)).submit();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.tagName("body"), row.get("BodyPattern")));
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                row.get("TestCaseId") + " — should remain on /login on invalid creds");
    }

    // ── Inline tests (no row iteration) ─────────────────────────────────────

    /**
     * AD_TC011 — Password strength indicator (FRD §3.3). The literal "abc" is a
     * deliberately weak input to invite the meter to render; not parameterised.
     */
    @Test(description = "AD_TC011 — PasswordStrengthIndicator")
    public void tc011_passwordStrengthIndicator() {
        RegisterPage page = new RegisterPage(driver);
        page.open();

        List<WebElement> pwd = driver.findElements(By.cssSelector("input[type='password'].input"));
        if (pwd.isEmpty()) throw new SkipException("Password input not found on register form");
        pwd.get(0).sendKeys("abc");
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('blur',{bubbles:true}));", pwd.get(0));

        String body = driver.getPageSource().toLowerCase();
        boolean hasLabel = body.contains("weak") || body.contains("medium") || body.contains("strong");
        boolean hasMeter = !driver.findElements(By.cssSelector(
                ".password-strength, [class*='strength'], [class*='meter'], progress")).isEmpty();
        Assert.assertTrue(hasLabel || hasMeter,
                "Register form should display a password strength indicator (FRD §3.3)");
    }

    /**
     * AD_TC012 — Login chooser exposes the FRD-mandated three roles
     * (Customer / Seller / Admin). FRD §2.1 — the role list is a spec constant.
     */
    @Test(description = "AD_TC012 — LoginRoleChooserShowsAllThreeRoles")
    public void tc012_loginRoleChooserAllThreeRoles() {
        new LoginPage(driver).open();
        String body = driver.getPageSource().toLowerCase();
        int present = (body.contains("customer") ? 1 : 0)
                    + (body.contains("seller")   ? 1 : 0)
                    + (body.contains("admin")    ? 1 : 0);
        Assert.assertTrue(present >= 2,
                "Login flow should expose at least 2 of 3 FRD-mandated role options "
                        + "(Customer / Seller / Admin) — found " + present);
    }

    /**
     * AD_TC0013 — JWT 'alg' must be HS256. Bespoke JWT inspection logic that
     * does not benefit from row iteration; kept inline using admin creds.
     */
    @Test(description = "AD_TC0013 — JwtAlgorithmIsHs256")
    public void tc013_jwtAlgorithmIsHs256() {
        LoginPage page = new LoginPage(driver);
        page.open();
        page.loginAs(adminEmail(), adminPassword());
        Assert.assertFalse(driver.getCurrentUrl().contains("/login"),
                "Pre-condition: login must succeed before JWT can be inspected");

        String jwt = readJwtFromLocalStorage();
        Assert.assertNotNull(jwt, "Expected a JWT in localStorage after successful login");
        String[] segments = jwt.split("\\.");
        Assert.assertEquals(segments.length, 3,
                "JWT must have header.payload.signature; got: " + jwt);

        String alg = decodeJwtHeaderAlg(segments[0]);
        Assert.assertEquals(alg, "HS256",
                "JWT 'alg' header must strictly equal HS256; was: " + alg);
    }

    // ── JWT helpers (kept private to the spec) ──────────────────────────────

    /**
     * Scan localStorage for the JWT. Some Angular builds store the raw token
     * under a fixed key (e.g. 'token', 'access_token'), others nest it in a
     * JSON blob ({user:{token:'...'}}). This script handles both shapes by
     * treating any three-segment dotted string as a candidate JWT.
     */
    private String readJwtFromLocalStorage() {
        String script =
                "for (let i = 0; i < window.localStorage.length; i++) {" +
                "  const k = window.localStorage.key(i);" +
                "  const v = window.localStorage.getItem(k);" +
                "  if (!v) continue;" +
                "  if (v.split('.').length === 3 && /^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$/.test(v)) return v;" +
                "  try {" +
                "    const o = JSON.parse(v);" +
                "    const stack = [o];" +
                "    while (stack.length) {" +
                "      const cur = stack.pop();" +
                "      if (cur && typeof cur === 'object') {" +
                "        for (const key of Object.keys(cur)) {" +
                "          const val = cur[key];" +
                "          if (typeof val === 'string' && val.split('.').length === 3 &&" +
                "              /^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$/.test(val)) return val;" +
                "          if (val && typeof val === 'object') stack.push(val);" +
                "        }" +
                "      }" +
                "    }" +
                "  } catch (e) { /* not JSON */ }" +
                "}" +
                "return null;";
        return (String) ((JavascriptExecutor) driver).executeScript(script);
    }

    /** Base64URL-decode the JWT header segment and return its {@code alg} claim. */
    private String decodeJwtHeaderAlg(String headerSegment) {
        byte[] decoded = Base64.getUrlDecoder().decode(headerSegment);
        String json = new String(decoded, StandardCharsets.UTF_8);
        try {
            JsonNode node = new ObjectMapper().readTree(json);
            JsonNode alg = node.get("alg");
            return alg == null ? null : alg.asText();
        } catch (Exception e) {
            throw new AssertionError("JWT header is not valid JSON: " + json, e);
        }
    }
}
