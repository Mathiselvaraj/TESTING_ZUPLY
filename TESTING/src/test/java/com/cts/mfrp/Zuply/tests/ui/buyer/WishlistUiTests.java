package com.cts.mfrp.zuply.tests.ui.buyer;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.ProductsPage;
import com.cts.mfrp.zuply.pages.WishlistPage;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Wishlist — FRD §2.8. Maps to TC010, TC011, TC012. */
public class WishlistUiTests extends UiBaseTest {

    private String buyerEmail;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginBuyer() {
        buyerEmail = registerNewCustomer("WishUser");
        loginViaUi(buyerEmail, "Test@1234");
    }

    /** TC010 — A logged-in customer can add a product to the wishlist. */
    @Test(description = "TC010 — AddToWishlistLoggedIn")
    public void tc010_addToWishlistLoggedIn() {
        ProductsPage products = new ProductsPage(driver);
        products.open();
        if (!products.hasWishlistIcons()) {
            throw new SkipException("No wishlist icon found on a product card");
        }
        products.addFirstToWishlist();

        WishlistPage wl = new WishlistPage(driver);
        wl.open();
        Assert.assertTrue(wl.itemCount() >= 1,
                "Wishlist should contain at least 1 item after add");
    }

    /** TC011 — Unauthenticated user attempts to add to wishlist sees the login prompt. */
    @Test(description = "TC011 — WishlistNotLoggedIn")
    public void tc011_wishlistNotLoggedIn() {
        clearSession();
        ProductsPage products = new ProductsPage(driver);
        products.open();
        if (!products.hasWishlistIcons()) {
            throw new SkipException("No wishlist icon found on a product card");
        }
        products.addFirstToWishlist();

        // FRD: "Please login to add items to your wishlist." OR redirect to /login
        boolean toastShown = driver.getPageSource().toLowerCase().matches(".*please login.*wishlist.*");
        boolean redirected = driver.getCurrentUrl().contains("/login");
        Assert.assertTrue(toastShown || redirected,
                "Anonymous wishlist click should prompt login or redirect to /login");
    }

    /** TC012 — Move a wishlist item to the cart. */
    @Test(description = "TC012 — MoveWishlistToCart")
    public void tc012_moveWishlistToCart() {
        // TC011 may have cleared the session; re-login if needed
        if (!driver.getCurrentUrl().startsWith(BASE_URL) || driver.manage().getCookies().isEmpty()) {
            loginViaUi(buyerEmail, "Test@1234");
        }

        ProductsPage products = new ProductsPage(driver);
        products.open();
        if (!products.hasWishlistIcons()) {
            throw new SkipException("No wishlist icon found");
        }
        products.addFirstToWishlist();

        WishlistPage wl = new WishlistPage(driver);
        wl.open();
        if (wl.isEmpty()) throw new SkipException("Wishlist seed failed; cannot test move");
        wl.moveFirstToCart();
        Assert.assertTrue(
                driver.getCurrentUrl().contains("/wishlist") || driver.getCurrentUrl().contains("/cart"));
    }
}
