package com.cts.mfrp.zuply.tests.ui.buyer;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.CartPage;
import com.cts.mfrp.zuply.pages.ProductsPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Shopping cart — FRD §2.4. Maps to TC013, TC014, TC019. */
@Test(groups = {"regression", "ui", "cart"})
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
        ProductsPage products = new ProductsPage(driver);
        products.open();
        if (!products.hasAddToCartButtons()) {
            throw new SkipException("No 'Add to cart' button visible on products page");
        }
        products.addFirstToCart();

        CartPage cart = new CartPage(driver);
        cart.open();
        Assert.assertTrue(cart.itemCount() >= 1,
                "Cart should contain at least 1 item after Add to cart click");
    }

    /** TC014 — Update item quantity in cart. */
    @Test(description = "TC014 — CartQuantityUpdate")
    public void tc014_cartQuantityUpdate() {
        ProductsPage products = new ProductsPage(driver);
        products.open();
        if (!products.hasAddToCartButtons()) {
            throw new SkipException("No 'Add to cart' button visible");
        }
        products.addFirstToCart();

        CartPage cart = new CartPage(driver);
        cart.open();
        cart.incrementFirstQuantity();
        Assert.assertFalse(cart.isEmpty(), "Cart should still contain items after quantity adjust");
    }

    /**
     * TC019 — Cart nav link shows an item count after a product is added.
     * BUG-CONFIRMATION TEST (from Likitha): the cart icon always reads "Cart" with no
     * count indicator; the wishlist nav correctly shows "Wishlist\n1" after an add — cart
     * should behave the same way. Expected to FAIL until the application bug is fixed.
     */
    @Test(description = "TC019 — CartCountBadgeUpdates [BUG]")
    public void tc019_cartNavCountUpdatesAfterAdd() {
        ProductsPage products = new ProductsPage(driver);
        products.open();
        if (!products.hasAddToCartButtons()) {
            throw new SkipException("No 'Add to cart' button visible on products page");
        }
        products.addFirstToCart();
        waitAfterAction();

        // The cart nav link should show a count after adding (e.g. "Cart\n1"),
        // exactly as the wishlist nav shows "Wishlist\n1" after wishlisting a product.
        // TODO: lift these locators onto HomePage once the bug is fixed.
        var cartLink = driver.findElements(By.cssSelector("a.nav-cart"));
        Assert.assertFalse(cartLink.isEmpty(), "Cart nav link (a.nav-cart) not found in header");

        String cartNavText = cartLink.get(0).getText().trim();
        var childBadges = cartLink.get(0).findElements(By.cssSelector(
                ".badge, .count, [class*='badge'], [class*='count'], [class*='cart-count']"));
        boolean childHasCount = childBadges.stream().anyMatch(b -> {
            try { return Integer.parseInt(b.getText().trim()) >= 1; }
            catch (NumberFormatException e) { return !b.getText().trim().isEmpty(); }
        });
        boolean textHasCount = cartNavText.matches("(?s).*\\b[1-9]\\d*\\b.*");

        Assert.assertTrue(childHasCount || textHasCount,
                "Cart navigation should display an item count after adding a product " +
                "(e.g. 'Cart 1') — cart nav text was: '" + cartNavText.replace("\n", "\\n") + "' " +
                "(application bug: no visual count feedback on cart icon after add)");
    }
}
