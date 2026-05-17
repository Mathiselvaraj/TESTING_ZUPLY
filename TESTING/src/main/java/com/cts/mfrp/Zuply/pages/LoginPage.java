package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

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
    public void submit()                            { click(LOGIN_BTN); }

    /** Convenience: fill + submit + wait for navigation away from /login. */
    public void loginAs(String email, String password) {
        enterEmail(email).enterPassword(password).submit();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
    }

    public void goToRegister() { click(REGISTER_LINK); }
}
