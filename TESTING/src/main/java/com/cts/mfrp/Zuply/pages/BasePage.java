package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Shared base for all Page Objects. Wraps a WebDriver + WebDriverWait and exposes
 * common helpers (visit, fill, click, isDisplayed) so subclasses stay declarative.
 *
 * The Zuply site is an Angular SPA hosted at https://zuply.netlify.app/. Netlify
 * does NOT have an SPA fallback configured, so direct URL navigation to any route
 * other than "/" returns Netlify's 404 page. Always start at "/" and let the
 * Angular router handle in-app navigation (this base class does that for you).
 */
public abstract class BasePage {

    public static final String BASE_URL = "https://zuply.netlify.app";

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final WebDriverWait longWait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /** Subclasses declare the route they live at (e.g. "/login", "/admin/dashboard"). */
    public abstract String route();

    /** Subclasses declare the CSS selector for a top-level element that proves the page rendered. */
    protected abstract By readyMarker();

    /**
     * Boots the SPA at "/" if needed, then navigates to {@link #route()} via
     * the Angular router (history.pushState + popstate event). Waits for the
     * page's ready marker to appear.
     */
    public void open() {
        String currentUrl = driver.getCurrentUrl();
        if (currentUrl == null || !currentUrl.startsWith(BASE_URL)) {
            driver.get(BASE_URL + "/");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-root")));
        }
        if (!route().equals("/")) {
            ((JavascriptExecutor) driver).executeScript(
                    "const p = arguments[0];" +
                    "const a = document.querySelector('a[href=\"' + p + '\"], a[routerlink=\"' + p + '\"]');" +
                    "if (a) { a.click(); } else { history.pushState({}, '', p); window.dispatchEvent(new PopStateEvent('popstate')); }",
                    route());
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(readyMarker()));
    }

    public boolean isLoaded() {
        try { return driver.findElement(readyMarker()).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    protected WebElement waitVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    protected WebElement waitClickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    protected void type(By by, String text) {
        WebElement el = waitVisible(by);
        el.clear();
        el.sendKeys(text);
    }

    /**
     * Click an element. Tries a native click first, then falls back to a
     * JavaScript click if the SPA's chat FAB (or any other overlay) intercepts
     * the native click — a chronic problem on the Zuply SPA.
     */
    protected void click(By by) {
        WebElement el = waitClickable(by);
        try {
            el.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
        }
    }

    protected String text(By by) { return waitVisible(by).getText().trim(); }

    /** True when at least one element matching {@code by} is in the DOM (no wait). */
    public boolean exists(By by) { return !driver.findElements(by).isEmpty(); }

    /** Count of elements currently matching {@code by} (no wait). */
    public int count(By by) { return driver.findElements(by).size(); }

    public String currentUrl() { return driver.getCurrentUrl(); }
    public String title() { return driver.getTitle(); }
}
