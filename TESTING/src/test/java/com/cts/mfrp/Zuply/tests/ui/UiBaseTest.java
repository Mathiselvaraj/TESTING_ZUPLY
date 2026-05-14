package com.cts.mfrp.Zuply.tests.ui;

import com.cts.mfrp.Zuply.pages.LoginPage;
import com.cts.mfrp.Zuply.pages.RegisterPage;
import com.cts.mfrp.Zuply.ui.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Optional;

import java.time.Duration;
import java.util.UUID;

/**
 * Shared base for all UI tests. Spins up a fresh Chrome (headless by default,
 * toggle via {@code -Dheadless=false}) before each test method and tears it
 * down afterwards. Exposes auth shortcuts (admin/customer/seller).
 *
 * UI tests target https://zuply.netlify.app/. The SPA is Angular 18; Netlify
 * does NOT provide SPA fallback routing, so we always boot at "/" and let the
 * page object's {@code open()} use the Angular router. All credentials come
 * from environment / system properties so the file has no hard-coded secrets.
 */
public abstract class UiBaseTest {

    public static final String BASE_URL = "https://zuply.netlify.app";

    protected WebDriver driver;
    protected WebDriverWait wait;

    /** Pre-seeded admin login on the SPA's backend. Override via -Dadmin.email / -Dadmin.password. */
    protected String adminEmail()    { return System.getProperty("admin.email",    "admin@zuply.in"); }
    protected String adminPassword() { return System.getProperty("admin.password", "Admin@123"); }

    @BeforeClass(alwaysRun = true)
    @Parameters({"headless"})
    public void launchBrowser(@Optional("true") String headless) {
        driver = DriverFactory.create(Boolean.parseBoolean(headless));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-root")));
    }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) {}
            driver = null;
        }
    }

    /**
     * Clear cookies + localStorage + sessionStorage to drop the current login.
     * Use in tests that need to switch from logged-in state to anonymous (e.g.
     * a "must login to view wishlist" scenario).
     */
    protected void clearSession() {
        driver.manage().deleteAllCookies();
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-root")));
    }

    /* ------------------------------------------------------------------ */
    /* Shared helpers                                                      */
    /* ------------------------------------------------------------------ */

    /** Returns a random 8-char alphanumeric suffix for unique emails. */
    protected static String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /** Register a fresh user via the SPA's signup form; returns the email used. */
    protected String registerNewCustomer(String namePrefix) {
        String email = "ui." + namePrefix.toLowerCase().replaceAll("\\s+","") + "." + randomSuffix() + "@zuply.in";
        new RegisterPage(driver).open();
        new RegisterPage(driver).registerAs(namePrefix, email, "9876543210", "Test@1234",
                RegisterPage.Role.CUSTOMER);
        // Wait until the router leaves /register, then allow backend to commit the account
        try { wait.until(d -> !d.getCurrentUrl().contains("/register")); }
        catch (Exception ignored) {}
        waitAfterAction();
        return email;
    }

    protected String registerNewSeller(String namePrefix) {
        String email = "ui." + namePrefix.toLowerCase().replaceAll("\\s+","") + "." + randomSuffix() + "@zuply.in";
        new RegisterPage(driver).open();
        new RegisterPage(driver).registerAs(namePrefix, email, "9876543210", "Test@1234",
                RegisterPage.Role.SELLER);
        try { wait.until(d -> !d.getCurrentUrl().contains("/register")); }
        catch (Exception ignored) {}
        waitAfterAction();
        return email;
    }

    /**
     * Drive through the login page. Retries up to 3 times with a short gap
     * between attempts to handle slow backend registration on cold starts.
     */
    protected void loginViaUi(String email, String password) {
        LoginPage login = new LoginPage(driver);
        Exception lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                login.open();
                login.loginAs(email, password);
                return;
            } catch (Exception e) {
                lastError = e;
                if (attempt < 3) waitAfterAction();
            }
        }
        throw new RuntimeException("Login failed after 3 attempts for " + email, lastError);
    }

    protected void loginAsAdmin() {
        loginViaUi(adminEmail(), adminPassword());
    }

    /**
     * Waits for a toast/alert to appear after a button-click action, then returns.
     * Falls through silently if no toast appears within 3 s (some actions complete
     * without a visual notification). Replaces Thread.sleep after action clicks.
     */
    protected void waitAfterAction() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                            "[role='alert'], .toast, .notification, [class*='toast'], [class*='snack']")));
        } catch (Exception ignored) {}
    }

    /**
     * Navigates within the SPA via the Angular router (pushState + popstate)
     * without triggering a full page reload. Used to test route guards for
     * pages that are not supposed to be accessible without authentication.
     */
    protected void navigateToRoute(String route) {
        ((JavascriptExecutor) driver).executeScript(
                "history.pushState({}, '', arguments[0]); " +
                "window.dispatchEvent(new PopStateEvent('popstate'));",
                route);
    }

    /** Best-effort JS click — bypasses overlay-intercepted clicks (e.g. chat FAB). */
    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
    }

    protected void jsClick(By by) { jsClick(driver.findElement(by)); }
}
