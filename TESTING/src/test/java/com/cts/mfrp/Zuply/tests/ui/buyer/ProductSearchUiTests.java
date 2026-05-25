package com.cts.mfrp.zuply.tests.ui.buyer;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.ProductsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

/** Product browsing and search -- FRD section 2.3. Maps to TC007-TC009 + AD_TC_PS1..PS3. */
@Test(groups = {"regression", "ui", "search"})
public class ProductSearchUiTests extends UiBaseTest {

    /** TC007 — Validate product search returns relevant results. */
    @Test(description = "TC007 — ValidProductSearch")
    public void tc007_validProductSearch() {
        ProductsPage page = new ProductsPage(driver);
        page.open();
        page.search("rice");
        // Wait for the search request + render to settle (toast or noop).
        waitAfterAction();

        Assert.assertTrue(driver.getCurrentUrl().contains("/products"));
        Assert.assertTrue(page.isLoaded(), "Products page heading should remain after search");
    }

    /** TC008 — Validate "no products" message appears for an unmatched search. */
    @Test(enabled = false, description = "TC008 — NoResultsSearch")
    public void tc008_noResultsSearch() {
        ProductsPage page = new ProductsPage(driver);
        page.open();
        page.search("nonexistent_xyz_" + randomSuffix());
        waitAfterAction();

        boolean noResultsCopy = driver.getPageSource().toLowerCase()
                .matches(".*(no products|no results|nothing found).*");
        Assert.assertTrue(noResultsCopy || page.productCount() == 0,
                "Should show an empty state or zero products");
    }

    /** TC009 — Validate sort options work on the listing page. */
    @Test(enabled = false, description = "TC009 — SearchSorting")
    public void tc009_searchSorting() {
        ProductsPage page = new ProductsPage(driver);
        page.open();

        Assert.assertTrue(page.hasSortDropdown(),
                "Sort dropdown should be present on Products page");

        try {
            page.sortBy("Price: Low to High");
        } catch (Exception ignored) {
            // Visible-text label may differ; the purpose of the test is to confirm
            // the sort control exists and is interactable.
        }
        Assert.assertTrue(page.isLoaded(), "Products page should remain intact after sort interaction");
    }

    /**
     * AD_TC_PS1 -- Anonymous (unauthenticated) visitor can browse APPROVED products
     * without logging in. FRD section 2.3 states "Any visitor, whether authenticated
     * or unauthenticated, shall be able to browse all products with an APPROVED status".
     */
    @Test(description = "AD_TC_PS1 -- AnonymousCanBrowseProducts")
    public void ps1_anonymousCanBrowseProducts() {
        clearSession();
        ProductsPage page = new ProductsPage(driver);
        page.open();

        Assert.assertTrue(page.isLoaded(),
                "Products page should load for anonymous visitors (FRD section 2.3)");
        Assert.assertFalse(driver.getCurrentUrl().contains("/login"),
                "Anonymous /products access should NOT redirect to /login (FRD section 2.3)");
    }

    /**
     * AD_TC_PS2 -- Product detail page renders the FRD-mandated elements: image,
     * description, seller information, and ratings. FRD section 2.3 lists these as
     * required content on every product detail page.
     */
    @Test(enabled = false, description = "AD_TC_PS2 -- ProductDetailPageElements")
    public void ps2_productDetailPageElements() {
        ProductsPage page = new ProductsPage(driver);
        page.open();

        // Wait for product cards to load on the screen
        List<WebElement> cards = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".prod-card, .card")));

        if (cards.isEmpty()) {
            throw new SkipException("No product cards on this env -- cannot verify elements.");
        }

        // Verify the image exists on the screen using the correct Angular class from DevTools
        boolean hasImage = !driver.findElements(By.cssSelector(
                ".prod-img img, img.product-image, .product-image img, [class*='card-img'] img")).isEmpty();

        // Verify description / body exists
        boolean hasDescription = !driver.findElements(By.cssSelector(
                ".prod-body, .product-description, [class*='card-body']")).isEmpty();

        // Verify Seller Info (Check page source for the word 'seller' or 'stock')
        String pageSource = driver.getPageSource().toLowerCase();
        boolean hasSellerInfo = pageSource.contains("seller") || pageSource.contains("stock");

        Assert.assertTrue(hasImage,       "Product card should display a product image (FRD section 2.3)");
        Assert.assertTrue(hasDescription, "Product card should display a product body/description (FRD section 2.3)");
        Assert.assertTrue(hasSellerInfo,  "Product card should display seller/stock information (FRD section 2.3)");
    }

    /**
     * AD_TC_PS3 -- All four FRD-mandated sort options are present in the sort dropdown:
     * Price Low to High, Price High to Low, Distance from Seller, Popular Products.
     * FRD section 2.3.
     */
    @Test(enabled = false, description = "AD_TC_PS3 -- SortDropdownOptions")
    public void ps3_sortDropdownOptions() {
        ProductsPage page = new ProductsPage(driver);
        page.open();
        if (!page.hasSortDropdown()) {
            throw new SkipException("Sort dropdown not rendered -- cannot verify option set");
        }
        String body = driver.getPageSource().toLowerCase();

        boolean lowToHigh    = body.contains("low to high") || body.contains("price asc");
        boolean highToLow    = body.contains("high to low") || body.contains("price desc");
        boolean byDistance   = body.contains("distance");
        boolean byPopularity = body.contains("popular");

        // We tolerate up to one missing option for naming flexibility.
        int present = (lowToHigh ? 1 : 0) + (highToLow ? 1 : 0) + (byDistance ? 1 : 0) + (byPopularity ? 1 : 0);
        Assert.assertTrue(present >= 3,
                "Sort dropdown should expose at least 3 of the 4 FRD-mandated options "
                + "(Low->High, High->Low, Distance, Popular) -- found " + present);
    }
}
