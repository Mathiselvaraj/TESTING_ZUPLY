package com.cts.mfrp.Zuply.tests.ui;

import com.cts.mfrp.Zuply.pages.HomePage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Home page UI scenarios — FRD §2.2. Maps to TC006. */
public class HomePageUiTests extends UiBaseTest {

    /** TC006 — Validate required elements are displayed on the home page. */
    @Test(description = "TC006 — HomePageElements")
    public void tc006_homePageElements() {
        HomePage home = new HomePage(driver);
        home.open();

        // Top section: brand, login, register links
        Assert.assertTrue(driver.findElements(By.cssSelector("a.nav-brand")).size() >= 1,
                "Zuply logo / brand should be visible");
        Assert.assertTrue(driver.findElements(By.cssSelector("a[href='/login'], a[routerlink='/login']")).size() >= 1,
                "Login link should appear in top nav");
        Assert.assertTrue(driver.findElements(By.cssSelector("a[href='/register'], a[routerlink='/register']")).size() >= 1,
                "Register link should appear in top nav");

        // Navigation links to other major sections
        Assert.assertTrue(driver.findElements(By.cssSelector("a[href='/products']")).size() >= 1,
                "Products link should be in the nav");
        Assert.assertTrue(driver.findElements(By.cssSelector("a[href='/sellers']")).size() >= 1,
                "Sellers link should be in the nav");

        // Page should not be the Netlify 404 shell
        Assert.assertFalse(driver.getTitle().contains("Page not found"),
                "Home page should not be Netlify's 404 shell");
    }
}
