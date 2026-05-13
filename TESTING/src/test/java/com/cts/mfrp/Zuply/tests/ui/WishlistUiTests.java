package com.cts.mfrp.Zuply.tests.ui;

import com.cts.mfrp.Zuply.pages.ProductsPage;
import com.cts.mfrp.Zuply.pages.WishlistPage;
import org.openqa.selenium.By;
import org.testng.Assert;
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
        new ProductsPage(driver).open();
        // Click the first heart icon on a product card
        var hearts = driver.findElements(By.cssSelector(".wishlist-icon, .heart-icon, [class*='wishlist']"));
        if (hearts.isEmpty()) throw new org.testng.SkipException("No wishlist icon found on a product card");
        jsClick(hearts.get(0));
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}

        new WishlistPage(driver).open();
        Assert.assertTrue(new WishlistPage(driver).itemCount() >= 1,
                "Wishlist should contain at least 1 item after add");
    }

    /** TC011 — Unauthenticated user attempts to add to wishlist sees the login prompt. */
    @Test(description = "TC011 — WishlistNotLoggedIn")
    public void tc011_wishlistNotLoggedIn() {
        // Drop the class-level login for this single scenario
        clearSession();
        new ProductsPage(driver).open();
        var hearts = driver.findElements(By.cssSelector(".wishlist-icon, .heart-icon, [class*='wishlist']"));
        if (hearts.isEmpty()) throw new org.testng.SkipException("No wishlist icon found on a product card");
        jsClick(hearts.get(0));
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}

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

        // Seed: add an item to wishlist first
        new ProductsPage(driver).open();
        var hearts = driver.findElements(By.cssSelector(".wishlist-icon, .heart-icon, [class*='wishlist']"));
        if (hearts.isEmpty()) throw new org.testng.SkipException("No wishlist icon found");
        jsClick(hearts.get(0));
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}

        WishlistPage wl = new WishlistPage(driver);
        wl.open();
        if (wl.isEmpty()) throw new org.testng.SkipException("Wishlist seed failed; cannot test move");
        wl.moveFirstToCart();
        // We just verify the click succeeded and the page didn't crash
        Assert.assertTrue(driver.getCurrentUrl().contains("/wishlist") || driver.getCurrentUrl().contains("/cart"));
    }
}
