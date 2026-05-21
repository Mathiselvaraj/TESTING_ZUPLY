package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/** Login page at {@code /login}. */
public class LoginPage extends BasePage {

    private static final By EMAIL         = By.cssSelector("input[type='email'].input");
    private static final By PASSWORD      = By.cssSelector("input[type='password'].input");
    private static final By LOGIN_BTN     = By.cssSelector("button.login-btn");
    private static final By REGISTER_LINK = By.cssSelector("a[routerlink='/register'], a[href='/register']");

    public LoginPage(WebDriver driver) { super(driver); }

    @Override public String route()       { return "/login"; }
    @Override protected By readyMarker() { return LOGIN_BTN; }

    public LoginPage enterEmail(String email)       { type(EMAIL, email); return this; }
    public LoginPage enterPassword(String password) { type(PASSWORD, password); return this; }
    public void submit()                            { click(LOGIN_BTN); }

    /** Convenience: fill + submit + wait for navigation away from /login. */
    public void loginAs(String email, String password) {
        // FIX: increased to 15s — button renders but stays disabled while Angular
        // initialises the form; 10s is not enough under load in the merged suite
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.elementToBeClickable(LOGIN_BTN));
        enterEmail(email).enterPassword(password);
        // FIX: second wait after typing — Angular re-validates on input and briefly
        // disables the button; submit() must not fire until it is enabled again
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(LOGIN_BTN));
        submit();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
    }

    public void goToRegister() { click(REGISTER_LINK); }
}