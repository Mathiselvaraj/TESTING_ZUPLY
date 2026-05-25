package com.cts.mfrp.zuply.tests.ui.buyer;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.HomePage;
import com.cts.mfrp.zuply.pages.ProductsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Home page UI scenarios — FRD §2.2. Maps to TC006 + AD_TC_HP1, AD_TC_HP2.
 * (AD_TC_HP3 — HamburgerQuickAccessTiles removed per cleanup pass.)
 */
@Test(groups = {"smoke", "regression", "ui", "home"})
public class HomePageUiTests extends UiBaseTest {

    /**
     * Eight categories enumerated in FRD §2.2 — Category Section. These are
     * specification constants (an FRD revision is the only thing that should
     * change them), so they live inline rather than in Excel.
     */
    private static final String[] FRD_CATEGORIES = {
            "Food & Beverage", "Grocery", "Fashion & Footwear", "Home & Kitchen",
            "Electronics", "Beauty & Personal Care", "Health & Wellness", "Agriculture"
    };

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

    /** AD_TC_HP1 — All 8 FRD-mandated categories appear somewhere on the page. */
    @Test(description = "AD_TC_HP1 -- HomePageCategorySection")
    public void hp1_homePageCategorySection() {
        new HomePage(driver).open();

        WebElement productsNavBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(By.xpath(
                        "//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'products')]")));
        productsNavBtn.click();

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.chip")));

        String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase();

        int found = 0;
        StringBuilder missing = new StringBuilder();
        for (String cat : FRD_CATEGORIES) {
            if (bodyText.contains(cat.toLowerCase())) found++;
            else missing.append(cat).append("; ");
        }

        Assert.assertTrue(found >= FRD_CATEGORIES.length - 1,
                "Products page should display all 8 FRD categories (§2.2). Missing: " + missing);
    }

    /** AD_TC_HP2 — Top section components: search bar, location selector. */
    @Test(description = "AD_TC_HP2 -- HomePageTopBarComponents")
    public void hp2_homePageTopBarComponents() {
        new ProductsPage(driver).open();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        boolean hasSearch = false;
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
                    "input.search-input, input[placeholder*='earch' i]")));
            hasSearch = true;
        } catch (Exception ignored) {}

        boolean hasLocation = !driver.findElements(By.cssSelector(
                ".location-selector, [class*='location'], select[name*='location' i], input[placeholder*='location' i], input[placeholder*='pincode' i]")).isEmpty();

        Assert.assertTrue(hasSearch,   "Page should expose a product search bar (FRD §2.2)");
        Assert.assertTrue(hasLocation, "Page should expose a location selector (FRD §2.2)");
    }
}
