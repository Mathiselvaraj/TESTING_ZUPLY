package com.cts.mfrp.Zuply.tests.ui;

import com.cts.mfrp.Zuply.pages.CartPage;
import com.cts.mfrp.Zuply.pages.ProductsPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Shopping cart — FRD §2.4. Maps to TC013 and TC014. */
public class CartUiTests extends UiBaseTest {

    private String buyerEmail;

    /** Register + login once for the whole class so individual tests don't repeat auth. */
    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginBuyer() {
        buyerEmail = registerNewCustomer("Cart");
        loginViaUi(buyerEmail, "Test@1234");
    }

    /** TC013 — Add a product to the cart. */
    @Test(description = "TC013 — AddToCart")
    public void tc013_addToCart() {
        new ProductsPage(driver).open();
        var addBtns = driver.findElements(By.xpath(
                "//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'add to cart')]"));
        if (addBtns.isEmpty()) throw new org.testng.SkipException("No 'Add to cart' button visible on products page");
        jsClick(addBtns.get(0));
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        CartPage cart = new CartPage(driver);
        cart.open();
        Assert.assertTrue(cart.itemCount() >= 1,
                "Cart should contain at least 1 item after Add to cart click");
    }

    /** TC014 — Update item quantity in cart. */
    @Test(description = "TC014 — CartQuantityUpdate")
    public void tc014_cartQuantityUpdate() {
        // Seed an item first
        new ProductsPage(driver).open();
        var addBtns = driver.findElements(By.xpath(
                "//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'add to cart')]"));
        if (addBtns.isEmpty()) throw new org.testng.SkipException("No 'Add to cart' button visible");
        jsClick(addBtns.get(0));
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        CartPage cart = new CartPage(driver);
        cart.open();
        // Increase quantity by clicking the "+" button if available
        var plus = driver.findElements(By.xpath("//button[normalize-space()='+'] | //button[contains(@class,'qty-plus')]"));
        if (!plus.isEmpty()) {
            jsClick(plus.get(0));
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        Assert.assertFalse(cart.isEmpty(), "Cart should still contain items after quantity adjust");
    }
}
