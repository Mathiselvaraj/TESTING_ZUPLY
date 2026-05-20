package com.cts.mfrp.Zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
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
        // Wait for at least one role button to render so we don't iterate an empty list.
        wait.until(ExpectedConditions.presenceOfElementLocated(ROLE_BTNS));
        List<WebElement> btns = driver.findElements(ROLE_BTNS);
        for (WebElement b : btns) {
            if (b.getText().trim().equalsIgnoreCase(role.name())) {
                // Scroll the role button into view then JS-click. The Zuply register page
                // has a hero section above the form, so role buttons sit below the fold
                // on smaller viewports -- a raw .click() lands at off-screen coordinates
                // and throws ElementClickInterceptedException at e.g. (573, -13).
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", b);
                return this;
            }
        }
        throw new IllegalStateException("Role button not found: " + role);
    }

    /**
     * Submit the register form. Waits up to 20s for the [disabled] binding on
     * button.register-btn to flip — Angular's async email-uniqueness validator
     * exceeds the default 10s wait on Render cold-starts. Falls back to a JS
     * click if an overlay intercepts the native click.
     */
    public void submit() {
        WebElement btn = waitClickable(REGISTER_BTN, Duration.ofSeconds(20));
        try { btn.click(); }
        catch (org.openqa.selenium.ElementClickInterceptedException e) { jsClick(btn); }
    }

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

    /**
     * Wait until the SPA finishes post-register routing — the URL must leave
     * {@code /register}. After a successful submit the SPA either auto-logs the
     * user in or redirects to {@code /login}; either way the path changes.
     */
    public void waitForRegistrationToComplete() {
        wait.until(d -> {
            String url = d.getCurrentUrl();
            return url != null && !url.contains("/register");
        });
    }
}
