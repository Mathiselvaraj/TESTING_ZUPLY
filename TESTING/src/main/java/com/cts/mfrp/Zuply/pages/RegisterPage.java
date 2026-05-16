package com.cts.mfrp.Zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** Registration page at {@code /register}. */
public class RegisterPage extends BasePage {

    private static final By NAME      = By.cssSelector("input[type='text'].input");
    private static final By EMAIL     = By.cssSelector("input[type='email'].input");
    private static final By PHONE     = By.cssSelector("input[type='tel'].input");
    private static final By PASSWORD  = By.cssSelector("input[type='password'].input");
    private static final By ROLE_BTNS = By.cssSelector("button.role-btn");
    private static final By REGISTER_BTN = By.cssSelector("button.register-btn");
    private static final By LOGIN_LINK = By.cssSelector("a[routerlink='/login'], a[href='/login']");

    private static final By STORE_NAME = By.xpath("//input[@placeholder='Enter your store name']");

    public enum Role { CUSTOMER, SELLER }

    public RegisterPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/register"; }
    @Override protected By readyMarker() { return REGISTER_BTN; }

    public RegisterPage enterName(String name)       { type(NAME, name); return this; }
    public RegisterPage enterEmail(String email)     { type(EMAIL, email); return this; }
    public RegisterPage enterPhone(String phone)     { type(PHONE, phone); return this; }
    public RegisterPage enterPassword(String pwd)    { type(PASSWORD, pwd); return this; }
    public RegisterPage enterStoreName(String store) { type(STORE_NAME, store); return this; }
    public RegisterPage selectRole(Role role) {
        List<WebElement> btns = driver.findElements(ROLE_BTNS);
        for (WebElement b : btns) {
            if (b.getText().trim().equalsIgnoreCase(role.name())) { b.click(); return this; }
        }
        throw new IllegalStateException("Role button not found: " + role);
    }

    public void submit() { click(REGISTER_BTN); }

    public void registerAs(String name, String email, String phone, String password, Role role) {
        enterName(name).enterEmail(email).enterPhone(phone).enterPassword(password).selectRole(role).submit();
    }

    public void registerAs(String name, String email, String phone, String password, Role role, String storeName) {
        enterName(name).enterEmail(email).enterPhone(phone).enterPassword(password);
        selectRole(role);
        enterStoreName(storeName); // Fill this after clicking Seller exposes the field
        submit();
    }

    public void goToLogin() { click(LOGIN_LINK); }
}
