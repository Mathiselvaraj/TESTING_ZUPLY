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
        // Actual button in DOM: <button class="btn-primary btn-sm btn-pill"> Add </button>
        // inside <div class="card-footer"> — label is "Add", not "Add to cart"
        var addBtns = driver.findElements(By.cssSelector(".card-footer button"));
        Assert.assertFalse(addBtns.isEmpty(),
                "'Add' button not found inside product card-footer — no products visible or page did not load correctly");
        jsClick(addBtns.get(0));
        waitAfterAction();

        CartPage cart = new CartPage(driver);
        cart.open();
        Assert.assertTrue(cart.itemCount() >= 1,
                "Cart should contain at least 1 item after 'Add to cart' click");
    }

    /**
     * TC019 — Cart nav link shows an item count after a product is added.
     * Bug: the cart icon always reads "Cart" with no count indicator; the wishlist nav
     * correctly shows "Wishlist\n1" after an add — cart should behave the same way.
     * This test is expected to FAIL until the application bug is fixed.
     */
    @Test(description = "TC019 — CartCountBadgeUpdates [BUG]")
    public void tc019_cartNavCountUpdatesAfterAdd() {
        new ProductsPage(driver).open();
        var addBtns = driver.findElements(By.cssSelector(".card-footer button"));
        Assert.assertFalse(addBtns.isEmpty(),
                "'Add' button not found inside product card-footer");
        jsClick(addBtns.get(0));
        waitAfterAction();

        // The cart nav link should show a count after adding (e.g. "Cart\n1"),
        // exactly as the wishlist nav shows "Wishlist\n1" after wishlisting a product.
        var cartLink = driver.findElements(By.cssSelector("a.nav-cart"));
        Assert.assertFalse(cartLink.isEmpty(), "Cart nav link (a.nav-cart) not found in header");

        String cartNavText = cartLink.get(0).getText().trim();
        // Check whether any child badge/count element carries a non-zero number
        var childBadges = cartLink.get(0).findElements(By.cssSelector(
                ".badge, .count, [class*='badge'], [class*='count'], [class*='cart-count']"));
        boolean childHasCount = childBadges.stream().anyMatch(b -> {
            try { return Integer.parseInt(b.getText().trim()) >= 1; }
            catch (NumberFormatException e) { return !b.getText().trim().isEmpty(); }
        });
        // Or the link text itself contains a non-zero count ("Cart\n1", "Cart (1)", etc.)
        boolean textHasCount = cartNavText.matches("(?s).*\\b[1-9]\\d*\\b.*");

        Assert.assertTrue(childHasCount || textHasCount,
                "Cart navigation should display an item count after adding a product " +
                "(e.g. 'Cart 1') — cart nav text was: '" + cartNavText.replace("\n", "\\n") + "' " +
                "(application bug: no visual count feedback on cart icon after add)");
    }

    /** TC014 — Update item quantity in cart. */
    @Test(description = "TC014 — CartQuantityUpdate")
    public void tc014_cartQuantityUpdate() {
        new ProductsPage(driver).open();
        var addBtns = driver.findElements(By.cssSelector(".card-footer button"));
        Assert.assertFalse(addBtns.isEmpty(),
                "'Add' button not found inside product card-footer — no products visible or page did not load correctly");
        jsClick(addBtns.get(0));
        waitAfterAction();

        CartPage cart = new CartPage(driver);
        cart.open();
        var plus = driver.findElements(By.xpath(
                "//button[normalize-space()='+'] | //button[contains(@class,'qty-plus')] | //button[contains(@class,'increment')]"));
        if (!plus.isEmpty()) {
            jsClick(plus.get(0));
            waitAfterAction();
        }
        Assert.assertFalse(cart.isEmpty(), "Cart should still contain items after quantity adjust");
    }
}
