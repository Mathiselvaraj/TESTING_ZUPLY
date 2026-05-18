package com.cts.mfrp.Zuply.tests.ui.buyer;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Home page UI scenarios — FRD §2.2. Maps to TC006. */
public class HomePageUiTests extends UiBaseTest {

    /** TC006 — Validate required elements are displayed on the home page. */
    @Test(description = "TC006 — HomePageElements")
    public void tc006_homePageElements() {
        HomePage home = new HomePage(driver);
        home.open();

        Assert.assertTrue(home.hasNavBrand(),     "Zuply logo / brand should be visible");
        Assert.assertTrue(home.hasLoginLink(),    "Login link should appear in top nav");
        Assert.assertTrue(home.hasRegisterLink(), "Register link should appear in top nav");
        Assert.assertTrue(home.hasProductsLink(), "Products link should be in the nav");
        Assert.assertTrue(home.hasSellersLink(),  "Sellers link should be in the nav");

        Assert.assertFalse(home.title().contains("Page not found"),
                "Home page should not be Netlify's 404 shell");
    }
}
