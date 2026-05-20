package com.cts.mfrp.Zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

/** Login page at {@code /login}. */
public class LoginPage extends BasePage {

    private static final By EMAIL    = By.cssSelector("input[type='email'].input");
    private static final By PASSWORD = By.cssSelector("input[type='password'].input");
    private static final By LOGIN_BTN = By.cssSelector("button.login-btn");
    private static final By REGISTER_LINK = By.cssSelector("a[routerlink='/register'], a[href='/register']");

    public LoginPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/login"; }
    @Override protected By readyMarker() { return LOGIN_BTN; }

    public LoginPage enterEmail(String email)       { type(EMAIL, email); return this; }
    public LoginPage enterPassword(String password) { type(PASSWORD, password); return this; }

    /**
     * Submit the login form. Waits up to 20s for the [disabled] binding on
     * button.login-btn to flip — Angular's form async validators can be slow on
     * Render cold-starts. Falls back to a JS click if a stray overlay intercepts.
     */
    public void submit() {
        WebElement btn = waitClickable(LOGIN_BTN, Duration.ofSeconds(20));
        try { btn.click(); }
        catch (org.openqa.selenium.ElementClickInterceptedException e) { jsClick(btn); }
    }

    /** Convenience: fill + submit + wait for navigation away from /login. */
    public void loginAs(String email, String password) {
        enterEmail(email).enterPassword(password).submit();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
    }

    public void goToRegister() { click(REGISTER_LINK); }
}
