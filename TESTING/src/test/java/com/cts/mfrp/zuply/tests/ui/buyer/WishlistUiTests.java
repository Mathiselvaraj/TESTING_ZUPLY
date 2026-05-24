package com.cts.mfrp.zuply.tests.ui.buyer;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.ProductsPage;
import com.cts.mfrp.zuply.pages.WishlistPage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Wishlist — FRD §2.8. Maps to TC010, TC011, TC012 + bug-confirmation TC020, TC021. */
@Test(groups = {"regression", "ui", "wishlist"})
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
        // Wishlist button lives on the product detail page, not the listing page
        new ProductsPage(driver).open();
        var cards = driver.findElements(By.cssSelector(".card-body, .card-name"));
        Assert.assertFalse(cards.isEmpty(), "No product cards found on products page");
        cards.get(0).click();
        wait.until(ExpectedConditions.urlMatches(".*/products/\\d+.*"));

        var wishlistBtns = driver.findElements(By.xpath("//button[contains(.,'Wishlist')]"));
        Assert.assertFalse(wishlistBtns.isEmpty(),
                "No 'Wishlist' button found on product detail page — application not rendering wishlist CTA");
        jsClick(wishlistBtns.get(0));
        waitAfterAction();

        new WishlistPage(driver).open();
        Assert.assertTrue(new WishlistPage(driver).itemCount() >= 1,
                "Wishlist should contain at least 1 item after clicking Wishlist button on detail page");
    }

    /** TC011 — Unauthenticated user navigating to /wishlist is blocked by the route guard. */
    @Test(description = "TC011 — WishlistNotLoggedIn")
    public void tc011_wishlistNotLoggedIn() {
        clearSession();
        navigateToRoute("/wishlist");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/login"),
                ExpectedConditions.urlContains("/wishlist")
        ));
        boolean redirectedToLogin = driver.getCurrentUrl().contains("/login");
        boolean wishlistContentVisible = !driver.findElements(
                By.xpath("//h1[normalize-space()='My Wishlist']")).isEmpty();
        Assert.assertTrue(redirectedToLogin || !wishlistContentVisible,
                "Anonymous /wishlist access should redirect to /login — route guard is not protecting this page");
    }

    /** TC012 — Move a wishlist item to the cart. */
    @Test(description = "TC012 — MoveWishlistToCart")
    public void tc012_moveWishlistToCart() {
        // Force a clean Angular boot, then re-login and hard-assert auth is established.
        // TC011 cleared the session; clearSession() here guarantees a known starting state
        // so loginViaUi always runs against a fresh Angular instance (not a stale SPA state).
        clearSession();
        loginViaUi(buyerEmail, "Test@1234");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".account-btn")));

        // Seed: navigate to detail page and add to wishlist
        new ProductsPage(driver).open();
        var cards = driver.findElements(By.cssSelector(".card-body, .card-name"));
        Assert.assertFalse(cards.isEmpty(), "No product cards found on products page");
        cards.get(0).click();
        wait.until(ExpectedConditions.urlMatches(".*/products/\\d+.*"));

        var wishlistBtns = driver.findElements(By.xpath("//button[contains(.,'Wishlist')]"));
        Assert.assertFalse(wishlistBtns.isEmpty(),
                "No 'Wishlist' button found on product detail page — application not rendering wishlist CTA");
        jsClick(wishlistBtns.get(0));
        waitAfterAction();

        WishlistPage wl = new WishlistPage(driver);
        // Use navigateToRoute (pushState) instead of wl.open() to avoid a route-guard
        // timing race after clearSession + re-login: wl.open() clicks the nav link which
        // immediately triggers canActivate before Angular's auth service Observable emits.
        navigateToRoute("/wishlist");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[normalize-space()='My Wishlist']")),
                ExpectedConditions.urlContains("/login")));
        Assert.assertFalse(driver.getCurrentUrl().contains("/login"),
                "Wishlist page not accessible after re-login — route guard still redirecting to /login (auth Observable not emitted in time)");
        Assert.assertFalse(wl.isEmpty(),
                "Wishlist should contain at least 1 item after add — 'Add to Wishlist' is not persisting (application bug)");
        wl.moveFirstToCart();
        Assert.assertTrue(driver.getCurrentUrl().contains("/wishlist") || driver.getCurrentUrl().contains("/cart"),
                "After move-to-cart the page should remain on /wishlist or navigate to /cart");
    }

    /**
     * TC020 — Re-clicking the Wishlist button on an already-wishlisted product removes it (toggle).
     * BUG-CONFIRMATION TEST: re-clicking does NOT decrease the wishlist count — the item is not
     * removed; the application is supposed to toggle (add/remove) but silently fails on the
     * second click. This test is expected to FAIL until the application bug is fixed.
     */
    @Test(description = "TC020 — WishlistToggleRemovesItem [BUG]")
    public void tc020_wishlistToggleRemovesItem() {
        clearSession();
        loginViaUi(buyerEmail, "Test@1234");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".account-btn")));

        // First click — add the item to wishlist
        new ProductsPage(driver).open();
        var cards = driver.findElements(By.cssSelector(".card-body, .card-name"));
        Assert.assertFalse(cards.isEmpty(), "No product cards found on products page");
        cards.get(0).click();
        wait.until(ExpectedConditions.urlMatches(".*/products/\\d+.*"));

        var wishlistBtns = driver.findElements(By.xpath("//button[contains(.,'Wishlist')]"));
        Assert.assertFalse(wishlistBtns.isEmpty(), "No Wishlist button found on product detail page");
        jsClick(wishlistBtns.get(0));
        waitAfterAction();

        // Record wishlist count after first click
        navigateToRoute("/wishlist");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[normalize-space()='My Wishlist']")),
                ExpectedConditions.urlContains("/login")));
        Assert.assertFalse(driver.getCurrentUrl().contains("/login"), "Wishlist not accessible");
        int countAfterFirstClick = new WishlistPage(driver).itemCount();
        Assert.assertTrue(countAfterFirstClick >= 1,
                "Precondition: wishlist should have at least 1 item after first add");

        // Second click on the same product — should toggle-remove the item
        new ProductsPage(driver).open();
        cards = driver.findElements(By.cssSelector(".card-body, .card-name"));
        Assert.assertFalse(cards.isEmpty(), "No product cards found (second visit)");
        cards.get(0).click();
        wait.until(ExpectedConditions.urlMatches(".*/products/\\d+.*"));

        wishlistBtns = driver.findElements(By.xpath("//button[contains(.,'Wishlist')]"));
        Assert.assertFalse(wishlistBtns.isEmpty(), "No Wishlist button found on detail page (second visit)");
        jsClick(wishlistBtns.get(0));
        waitAfterAction();

        // Verify item count decreased — the toggle should have removed the item
        navigateToRoute("/wishlist");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[normalize-space()='My Wishlist']")),
                ExpectedConditions.urlContains("/login")));
        Assert.assertFalse(driver.getCurrentUrl().contains("/login"), "Wishlist not accessible");
        int countAfterToggle = new WishlistPage(driver).itemCount();

        Assert.assertTrue(countAfterToggle < countAfterFirstClick,
                "Re-clicking the Wishlist button should remove the item (toggle behavior) — " +
                "count was " + countAfterFirstClick + " after add, still " + countAfterToggle + " after re-click " +
                "(application bug: re-clicking shows error / silently fails instead of removing)");
    }

    /**
     * TC021 — Wishlist button changes its visual state (class or text) after the product is added.
     * BUG-CONFIRMATION TEST: the button always shows class='btn-secondary btn-pill' and
     * text='heart Wishlist' — it never updates to reflect the added state (e.g. filled heart,
     * btn-primary, 'Remove' text). This test is expected to FAIL until the application bug is fixed.
     */
    @Test(description = "TC021 — WishlistButtonChangesVisualState [BUG]")
    public void tc021_wishlistButtonChangesVisualState() {
        clearSession();
        loginViaUi(buyerEmail, "Test@1234");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".account-btn")));

        new ProductsPage(driver).open();
        var cards = driver.findElements(By.cssSelector(".card-body, .card-name"));
        Assert.assertFalse(cards.isEmpty(), "No product cards found on products page");
        cards.get(0).click();
        wait.until(ExpectedConditions.urlMatches(".*/products/\\d+.*"));

        // Capture the wishlist button's initial visual state
        var wishlistBtns = driver.findElements(By.xpath("//button[contains(.,'Wishlist')]"));
        Assert.assertFalse(wishlistBtns.isEmpty(), "No Wishlist button found on product detail page");
        String classBefore = wishlistBtns.get(0).getAttribute("class");
        String textBefore  = wishlistBtns.get(0).getText().trim();

        // Click the Wishlist button — it should transition to an "added" state
        jsClick(wishlistBtns.get(0));
        waitAfterAction();

        // Re-query: look for the button by current text OR a state-change variant ("Remove", "Added")
        var btnsAfter = driver.findElements(By.xpath(
                "//button[contains(.,'Wishlist') or contains(.,'Remove') or contains(.,'Added')]"));
        boolean stateChanged = false;
        if (!btnsAfter.isEmpty()) {
            String classAfter = btnsAfter.get(0).getAttribute("class");
            String textAfter  = btnsAfter.get(0).getText().trim();
            stateChanged = !classBefore.equals(classAfter) || !textBefore.equals(textAfter);
        }

        Assert.assertTrue(stateChanged,
                "Wishlist button should change its visual state after adding a product " +
                "(e.g. filled heart icon, btn-primary class, or 'Remove from Wishlist' text) — " +
                "class before: '" + classBefore + "', text before: '" + textBefore + "' — no change detected " +
                "(application bug: wishlist button gives no 'added' visual feedback)");
    }
}
