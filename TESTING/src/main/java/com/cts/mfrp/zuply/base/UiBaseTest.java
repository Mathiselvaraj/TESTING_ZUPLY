package com.cts.mfrp.zuply.base;

import com.cts.mfrp.zuply.pages.LoginPage;
import com.cts.mfrp.zuply.pages.RegisterPage;
import com.cts.mfrp.zuply.utils.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
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

public abstract class UiBaseTest {

    public static final String BASE_URL = "https://zuply.netlify.app";

    protected WebDriver driver;
    protected WebDriverWait wait;

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

    protected void clearSession() {
        driver.manage().deleteAllCookies();
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-root")));
    }

    protected static String randomSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    protected String registerNewCustomer(String namePrefix) {
        String email = "ui." + namePrefix.toLowerCase().replaceAll("\\s+","") + "." + randomSuffix() + "@zuply.in";
        new RegisterPage(driver).open();
        new RegisterPage(driver).registerAs(namePrefix, email, "9876543210", "Test@1234",
                RegisterPage.Role.CUSTOMER);
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
        // FIX: wait for admin dashboard to fully stabilise after login
        // Without this, page.open() fires history.pushState before Angular has
        // finished bootstrapping, causing filter tabs and other components to
        // not render in the target page
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("div.admin-banner")));
        } catch (Exception ignored) {
            // Dashboard may render differently — continue regardless
        }
    }

    protected void ensureLoggedIn(String email, String password) {
        String url = driver.getCurrentUrl();
        if (url != null && (url.contains("/seller/") || url.contains("/admin/"))) {
            return;
        }
        try {
            loginViaUi(email, password);
        } catch (TimeoutException ignored) {}
        try {
            new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                    .until(d -> {
                        String u = d.getCurrentUrl();
                        return u != null && !u.contains("/login");
                    });
        } catch (TimeoutException ignored) {}
    }

    protected void waitAfterAction() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                            "[role='alert'], .toast, .notification, [class*='toast'], [class*='snack']")));
        } catch (Exception ignored) {}
    }

    protected void navigateToRoute(String route) {
        navigateRoute(route);
    }

    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
    }

    protected void jsClick(By by) { jsClick(driver.findElement(by)); }

    protected void navigateRoute(String route) {
        ((JavascriptExecutor) driver).executeScript(
                "const p = arguments[0];" +
                        "const a = document.querySelector('a[href=\"'+p+'\"], a[routerlink=\"'+p+'\"]');" +
                        "if (a) a.click(); else { history.pushState({}, '', p); window.dispatchEvent(new PopStateEvent('popstate')); }",
                route);
    }
}