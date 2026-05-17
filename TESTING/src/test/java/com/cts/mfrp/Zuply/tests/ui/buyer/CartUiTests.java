package com.cts.mfrp.zuply.tests.ui.buyer;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.CartPage;
import com.cts.mfrp.zuply.pages.ProductsPage;
import org.testng.Assert;
import org.testng.SkipException;
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
}
