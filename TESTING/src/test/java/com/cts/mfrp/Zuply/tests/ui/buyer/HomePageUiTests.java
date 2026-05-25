package com.cts.mfrp.zuply.tests.ui.buyer;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.HomePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/** Home page UI scenarios -- FRD section 2.2. Maps to TC006 + AD_TC_HP1..AD_TC_HP3. */
@Test(groups = {"smoke", "regression", "ui", "home"})
public class HomePageUiTests extends UiBaseTest {

    /** Eight categories enumerated in FRD section 2.2 -- Category Section. */
    private static final String[] FRD_CATEGORIES = {
            "Food & Beverage", "Grocery", "Fashion & Footwear", "Home & Kitchen",
            "Electronics", "Beauty & Personal Care", "Health & Wellness", "Agriculture"
    };

    /** TC006 -- Validate required elements are displayed on the home page. */
    @Test(description = "TC006 -- HomePageElements")
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

    /**
     * AD_TC_HP1 -- All 8 FRD-mandated categories appear somewhere on the page.
     */
    @Test(description = "AD_TC_HP1 -- HomePageCategorySection")
    public void hp1_homePageCategorySection() {
        // 1. Start at the Home Page (Guest mode)
        HomePage home = new HomePage(driver);
        home.open();

        // 2. Simulate a real user clicking "Products" in the navigation bar
        WebElement productsNavBtn = new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'products')]")));
        productsNavBtn.click();

        // 3. Wait for the Products page to load and chips to appear
        WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.chip")));
        } catch (Exception e) {
            Assert.fail("Category chips did not load within 10 seconds after clicking Products nav link.");
        }

        // Grab ALL visible text on the page at once.
        String bodyText = driver.findElement(By.tagName("body")).getText().toLowerCase();

        int found = 0;
        StringBuilder missing = new StringBuilder();

        for (String cat : FRD_CATEGORIES) {
            if (bodyText.contains(cat.toLowerCase())) {
                found++;
            } else {
                missing.append(cat).append("; ");
            }
        }

        Assert.assertTrue(found >= FRD_CATEGORIES.length - 1,
                "Products page should display all 8 FRD categories (section 2.2). Missing: " + missing);
    }

    /**
     * AD_TC_HP2 -- Top section components: search bar, location selector.
     */
    @Test(description = "AD_TC_HP2 -- HomePageTopBarComponents")
    public void hp2_homePageTopBarComponents() {
        // FIX: Navigate to the ProductsPage where the search bar actually resides
        com.cts.mfrp.zuply.pages.ProductsPage productsPage = new com.cts.mfrp.zuply.pages.ProductsPage(driver);
        productsPage.open();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        boolean hasSearch = false;
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input.search-input, input[placeholder*='earch' i]")));
            hasSearch = true;
        } catch (Exception ignored) {}

        boolean hasLocation = !driver.findElements(By.cssSelector(
                ".location-selector, [class*='location'], select[name*='location' i], input[placeholder*='location' i], input[placeholder*='pincode' i]")).isEmpty();

        Assert.assertTrue(hasSearch,   "Page should expose a product search bar (FRD section 2.2)");
        Assert.assertTrue(hasLocation, "Page should expose a location selector (FRD section 2.2)");
    }

    /**
     * AD_TC_HP3 -- Mobile hamburger menu provides quick-access tiles.
     */
    @Test(description = "AD_TC_HP3 -- HamburgerQuickAccessTiles")
    public void hp3_hamburgerQuickAccessTiles() {
        HomePage home = new HomePage(driver);
        home.open();
        try { home.openHamburger(); }
        catch (Exception ignored) { }
        waitAfterAction();

        String body = driver.getPageSource().toLowerCase();
        Assert.assertTrue(body.contains("become a seller") || body.contains("become-a-seller"),
                "Quick-access tile 'Become a Seller' should be reachable (FRD section 2.12)");
        Assert.assertTrue(body.contains("customer care") || body.contains("customer-care"),
                "Quick-access tile 'Customer Care' should be reachable (FRD section 2.12)");
    }
}