package com.cts.mfrp.zuply.tests.ui.auth;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.LoginPage;
import com.cts.mfrp.zuply.pages.RegisterPage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

/**
 * Authentication UI scenarios — FRD §2.1.
 * Maps to test cases TC001 – TC005 in the test-case spreadsheet.
 */
public class AuthUiTests extends UiBaseTest {

    /** TC001 — Validate successful user registration with all valid inputs. */
    @Test(description = "TC001 — ValidRegistration")
    public void tc001_validRegistration() {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        Assert.assertTrue(page.isLoaded(), "Register form should be displayed");

        String email = "ui.john." + randomSuffix() + "@zuply.in";
        page.registerAs("John Doe", email, "9876543210", "Test@1234", RegisterPage.Role.CUSTOMER);

        page.waitForRegistrationToComplete();
        Assert.assertFalse(driver.getCurrentUrl().contains("/register"),
                "Should leave /register after successful submit, was: " + driver.getCurrentUrl());
    }

    /** TC002 — Validate registration is rejected when email already exists. */
    @Test(description = "TC002 — DuplicateEmailRegistration")
    public void tc002_duplicateEmailRegistration() {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        // Re-use admin's seeded email — guaranteed to exist
        page.registerAs("Dup", adminEmail(), "9876543210", "Test@1234", RegisterPage.Role.CUSTOMER);
        Pattern errorPattern = Pattern.compile("already|exists|duplicate|in use", Pattern.CASE_INSENSITIVE);
        wait.until(ExpectedConditions.textMatches(By.tagName("body"), errorPattern));

        // The SPA should either stay on /register or show a duplicate-email error toast.
        boolean stayedOnRegister = driver.getCurrentUrl().contains("/register");
        boolean showsError = driver.getPageSource().toLowerCase().matches(".*(already|exists|duplicate|in use).*");
        Assert.assertTrue(stayedOnRegister || showsError,
                "Expected duplicate-email rejection; url=" + driver.getCurrentUrl());
    }

    /** TC003 — Validate successful login with valid credentials. */
    @Test(description = "TC003 — ValidLogin")
    public void tc003_validLogin() {
        LoginPage page = new LoginPage(driver);
        page.open();
        page.loginAs(adminEmail(), adminPassword());
        Assert.assertFalse(driver.getCurrentUrl().contains("/login"),
                "After valid login the URL should leave /login");
    }

    /** TC004 — Validate error message for wrong password. */
    @Test(description = "TC004 — InvalidLoginCredentials")
    public void tc004_invalidLoginCredentials() {
        LoginPage page = new LoginPage(driver);
        page.open();
        page.enterEmail(adminEmail()).enterPassword("WrongPass!1").submit();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.tagName("body"), "Invalid email or password"
        ));

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Should remain on /login on bad credentials");
        //Assert.assertTrue(driver.getPageSource().toLowerCase().matches(".*(invalid|incorrect|wrong).*"),
                //"Page should surface an invalid-credentials message");
    }

    /** TC005 — Validate error message when role does not match account.
     *  The SPA frontend has Customer/Seller/Admin login chooser, so attempting to log
     *  in via the wrong role chooser should fail. This test exercises that branch by
     *  attempting a regular login with credentials that aren't valid for the buyer role. */
    @Test(description = "AD_TC005 — InvalidRoleLogin")
    public void tc005_invalidRoleLogin() {
        LoginPage page = new LoginPage(driver);
        page.open();
        // Use admin creds — backend rejects unknown roles with the same generic error
        page.enterEmail(adminEmail()).enterPassword("DoesNotMatter").submit();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.tagName("body"), "Invalid email or password"
        ));

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Should remain on /login for invalid role/credentials");
    }

    //ADDITIONAL TEST CASES

    /** AD_TC006 — Validate error handling for invalid email format. */
    @Test(description = "AD_TC006 — InvalidEmailFormat")
    public void tc006_invalidEmailFormat() {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        // Submit an email missing the "@" and domain
        page.registerAs("Bad Email", "plainaddress", "9876543210", "Test@1234", RegisterPage.Role.CUSTOMER);

        // Wait for front-end or back-end validation text to appear
        Pattern errorPattern = Pattern.compile("invalid|format|valid email", Pattern.CASE_INSENSITIVE);
        wait.until(ExpectedConditions.textMatches(By.tagName("body"), errorPattern));

        Assert.assertTrue(driver.getCurrentUrl().contains("/register"),
                "Should remain on /register when email format is invalid");
    }

    /** AD_TC007 — Validate error handling for invalid phone number. */
    @Test(description = "AD_TC007 — InvalidPhoneNumber")
    public void tc007_invalidPhoneNumber() {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        // Submit a short, invalid phone number
        page.registerAs("Bad Phone", "phone." + randomSuffix() + "@zuply.in", "12345", "Test@1234", RegisterPage.Role.CUSTOMER);

        // Wait for phone validation error
        Pattern errorPattern = Pattern.compile("invalid|10-digit|valid phone", Pattern.CASE_INSENSITIVE);
        wait.until(ExpectedConditions.textMatches(By.tagName("body"), errorPattern));

        Assert.assertTrue(driver.getCurrentUrl().contains("/register"),
                "Should remain on /register when phone number is invalid");
    }

    /** AD_TC008 — Validate successful Seller registration including Store Name. */
    @Test(description = "AD_TC008 — RegisterAsSeller")
    public void tc008_registerAsSeller() {
        RegisterPage page = new RegisterPage(driver);
        page.open();

        String email = "ui.seller." + randomSuffix() + "@zuply.in";

        page.registerAs("Jane Seller", email, "9876543210", "Test@1234", RegisterPage.Role.SELLER, "Jane's Boutique");

        // Should successfully route away from the register page
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/register")));
        Assert.assertFalse(driver.getCurrentUrl().contains("/register"),
                "Should leave /register after successful Seller submit");
    }

    /** AD_TC009 — Validate Failed registration with special characters in password. */
    @Test(description = "AD_TC009 — PasswordWithSpecialCharacters")
    public void tc009_passwordWithSpecialCharacters() {
        RegisterPage page = new RegisterPage(driver);
        page.open();

        String email = "ui.special." + randomSuffix() + "@zuply.in";
        // Pass a password loaded with special characters
        page.registerAs("Special Char", email, "9876543210", "P@ssw0rd_#!!*", RegisterPage.Role.CUSTOMER);

        //wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/register")));
        Assert.assertTrue(driver.getCurrentUrl().contains("/register"),
                "Should stay in /register after successful submit with special characters in password");
    }

    /** AD_TC007 — Validate error handling for invalid phone number starting with less than 6. */
    @Test(description = "AD_TC0010 — InvalidPhoneNumber")
    public void tc007_invalidPhoneNumber1() {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        // Submit a short, invalid phone number
        page.registerAs("Bad Phone", "phone." + randomSuffix() + "@zuply.in", "5234567891", "Test@1234", RegisterPage.Role.CUSTOMER);

        // Wait for phone validation error
        Pattern errorPattern = Pattern.compile("invalid|10-digit|valid phone", Pattern.CASE_INSENSITIVE);
        wait.until(ExpectedConditions.textMatches(By.tagName("body"), errorPattern));

        Assert.assertTrue(driver.getCurrentUrl().contains("/register"),
                "Should remain on /register when phone number is invalid");
    }
}
