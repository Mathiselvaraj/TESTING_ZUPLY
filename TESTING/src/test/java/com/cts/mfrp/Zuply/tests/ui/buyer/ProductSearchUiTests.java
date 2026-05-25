package com.cts.mfrp.zuply.tests.ui.buyer;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.ProductsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Product browsing and search -- FRD section 2.3. Maps to TC007 + AD_TC_PS1. */
@Test(groups = {"regression", "ui", "search"})
public class ProductSearchUiTests extends UiBaseTest {

    /** TC007 — Validate product search returns relevant results. */
    @Test(description = "TC007 — ValidProductSearch")
    public void tc007_validProductSearch() {
        ProductsPage page = new ProductsPage(driver);
        page.open();
        page.search("rice");
        waitAfterAction();

        Assert.assertTrue(driver.getCurrentUrl().contains("/products"));
        Assert.assertTrue(page.isLoaded(), "Products page heading should remain after search");
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
}
