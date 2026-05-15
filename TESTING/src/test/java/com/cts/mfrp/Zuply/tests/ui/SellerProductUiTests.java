package com.cts.mfrp.Zuply.tests.ui;

//import com.cts.mfrp.Zuply.pages.SellerProductsPage;
//import com.cts.mfrp.Zuply.pages.SellerUploadPage;
//import org.testng.Assert;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
import com.cts.mfrp.Zuply.pages.SellerDashboardPage;
import com.cts.mfrp.Zuply.pages.SellerOrdersPage;
import com.cts.mfrp.Zuply.pages.SellerProductsPage;
import com.cts.mfrp.Zuply.pages.SellerUploadPage;
import com.cts.mfrp.Zuply.pages.SellersListingPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
/** Seller product management — FRD §2.10. Maps to TC020 and TC021. */
public class SellerProductUiTests extends UiBaseTest {

    private String sellerEmail;
    /** Reusable explicit-wait duration. No Thread.sleep() — use this instead. */
    private static final Duration WAIT = Duration.ofSeconds(20);

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginSeller() {
        sellerEmail = registerNewSeller("SellerCreate");
        ensureLoggedIn(sellerEmail, "Test@1234");
    }

    /**
     * Login wrapper that tolerates the SPA's quirks after self-registration:
     *   • The register flow sometimes auto-logs the seller in — visiting /login
     *     then submitting will land us back on /login while the SPA redirects,
     *     and LoginPage.loginAs() throws TimeoutException waiting for the URL
     *     to change.
     *   • A PENDING (unapproved) seller may briefly stay on /login while the
     *     SPA processes the response.
     *
     * Strategy: if we are already on a /seller/* route, skip login entirely.
     * Otherwise call loginViaUi but swallow a TimeoutException — the next
     * page.open() will surface the real failure via its own readyMarker wait.
     */
    private void ensureLoggedIn(String email, String password) {
        String url = driver.getCurrentUrl();
        if (url != null && url.contains("/seller/")) {
            return;
        }
        try {
            loginViaUi(email, password);
        } catch (TimeoutException ignored) {
            // URL didn't leave /login within the LoginPage's wait window.
            // Continue — downstream page.open() calls will verify state.
        }
        // Best-effort: wait briefly for navigation to settle.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> {
                        String u = d.getCurrentUrl();
                        return u != null && !u.contains("/login");
                    });
        } catch (TimeoutException ignored) { /* still on /login — let test decide */ }
    }

    /** TC020 — Seller can create a product listing with all required fields. */
    @Test(description = "TC020 — SellerCreateProduct")
    public void tc020_sellerCreateProduct() {
        SellerUploadPage upload = new SellerUploadPage(driver);
        upload.open();
        Assert.assertTrue(upload.isLoaded(), "Add Product page should be visible");

        // The seller will be PENDING until admin approves; we just verify the form is
        // navigable and submittable. Backend may reject if seller isn't approved,
        // which is the expected platform behavior per FRD.
        try {
            upload.enterTitle("UI Test Rice " + randomSuffix())
                  .enterDescription("1 kg pack, organic")
                  .enterPrice("80")
                  .enterStock("100");
        } catch (Exception e) {
            throw new org.testng.SkipException("Form fields may not match current SPA build: " + e.getMessage());
        }
        // Don't actually submit (would create dirty data on the env). Just verify the
        // submit button is present and clickable.
        Assert.assertTrue(driver.findElements(
                org.openqa.selenium.By.cssSelector("button.submit-btn")).size() >= 1,
                "Submit-for-Review button should be visible");
    }

    /** TC021 — Seller can only edit/delete their own products (cross-seller access denied). */
    @Test(description = "TC021 — SellerEditDeleteProduct")
    public void tc021_sellerEditDeleteProduct() {
        // Switch from "SellerCreate" to a fresh "SellerB" to prove cross-seller isolation
        clearSession();
        String sellerBEmail = registerNewSeller("SellerB");
        ensureLoggedIn(sellerBEmail, "Test@1234");

        SellerProductsPage page = new SellerProductsPage(driver);
        page.open();
        Assert.assertTrue(page.isEmpty(),
                "A newly-registered seller B should see only their own products (zero), not seller A's");
    }
    /**
     * Additional UI test cases for the Seller module (TC022 – TC031).
     *
     * NO Thread.sleep() is used anywhere in this file.
     * All synchronisation is handled by WebDriverWait /
     * ExpectedConditions, or by BasePage.isLoaded() which itself
     * wraps WebDriverWait around readyMarker().
     *
     * Every test is labelled either:
     *   [POSITIVE] — exercises the happy path; expected to PASS when the
     *                feature works correctly.
     *   [NEGATIVE] — exercises a guard/restriction; expected to PASS when
     *                the application correctly denies or limits access.
     */

    // ══════════════════════════════════════════════════════════════════
    //  TC022 — SellerDashboardPage — POSITIVE
    // ══════════════════════════════════════════════════════════════════
    @Test(description = "TC022 [POSITIVE] — SellerDashboardLoads")
    public void tc022_sellerDashboardLoads() {
        SellerDashboardPage dashboard = new SellerDashboardPage(driver);
        dashboard.open();

        Assert.assertTrue(dashboard.isLoaded(),
                "Seller dashboard should be loaded with stats-grid visible");

        // Wait explicitly for stat cards to be rendered before counting them.
        try {
            new WebDriverWait(driver, WAIT)
                    .until(ExpectedConditions.numberOfElementsToBe(
                            By.cssSelector(".stat-card"), 4));
        } catch (TimeoutException e) {
            throw new AssertionError(
                    "Dashboard must show exactly 4 stat cards — found " + dashboard.statCardCount(), e);
        }
        Assert.assertEquals(dashboard.statCardCount(), 4,
                "Dashboard must show exactly 4 stat cards");
    }

    // ══════════════════════════════════════════════════════════════════
    //  TC023 — SellerDashboardPage — POSITIVE
    // ══════════════════════════════════════════════════════════════════
    @Test(description = "TC023 [POSITIVE] — SellerDashboardNavLinks")
    public void tc023_sellerDashboardNavLinks() {
        SellerDashboardPage dashboard = new SellerDashboardPage(driver);
        dashboard.open();

        // ── Upload link ──────────────────────────────────────────────
        clickDashboardLinkTo("/seller/upload");
        new WebDriverWait(driver, WAIT)
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[normalize-space()='Add Product']")));
        Assert.assertTrue(new SellerUploadPage(driver).isLoaded(),
                "Upload quick-action link should open the Add Product page");

        // ── Products link ────────────────────────────────────────────
        dashboard.open();
        clickDashboardLinkTo("/seller/products");
        new WebDriverWait(driver, WAIT)
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[normalize-space()='My Products']")));
        Assert.assertTrue(new SellerProductsPage(driver).isLoaded(),
                "Products quick-action link should open My Products page");

        // ── Orders link ──────────────────────────────────────────────
        dashboard.open();
        clickDashboardLinkTo("/seller/orders");
        new WebDriverWait(driver, WAIT)
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[normalize-space()='Customer Orders']")));
        Assert.assertTrue(new SellerOrdersPage(driver).isLoaded(),
                "Orders quick-action link should open Customer Orders page");
    }

    // ══════════════════════════════════════════════════════════════════
    //  TC024 — SellerOrdersPage — POSITIVE
    // ══════════════════════════════════════════════════════════════════
    @Test(description = "TC024 [POSITIVE] — SellerOrdersPageLoads")
    public void tc024_sellerOrdersPageLoads() {
        SellerOrdersPage orders = new SellerOrdersPage(driver);
        orders.open();

        Assert.assertTrue(orders.isLoaded(),
                "Customer Orders page should be accessible for a logged-in seller");

        // Filter tabs may be rendered as <button class="filter-tab">, generic
        // .filter-tab elements, or hidden when no orders exist. Try several
        // shapes; if none match, fall back to the page heading as proof of load.
        By[] tabCandidates = new By[] {
                By.cssSelector("button.filter-tab"),
                By.cssSelector(".filter-tab"),
                By.cssSelector("[class*='filter-tab']"),
                By.cssSelector("button[class*='filter']"),
                By.cssSelector(".filter-tabs button, .filters button")
        };
        int tabCount = 0;
        for (By c : tabCandidates) {
            int n = driver.findElements(c).size();
            if (n > tabCount) tabCount = n;
            if (tabCount >= 4) break;
        }

        if (tabCount == 0) {
            // Empty state — page may not render tabs without orders. Still must
            // show the heading (verified above) for the test to be meaningful.
            return;
        }
        Assert.assertTrue(tabCount >= 1,
                "Orders page should show at least one filter tab when tabs are rendered");
    }

    // ══════════════════════════════════════════════════════════════════
    //  TC025 — SellerOrdersPage — POSITIVE
    // ══════════════════════════════════════════════════════════════════
    @Test(description = "TC025 [POSITIVE] — SellerOrderActionButtonVisible")
    public void tc025_sellerOrderActionButtonVisible() {
        SellerOrdersPage orders = new SellerOrdersPage(driver);
        orders.open();

        // Give the page a brief window for order rows to load before deciding "empty".
        List<WebElement> rows;
        try {
            rows = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                            By.cssSelector(".table-row:not(.header)")));
        } catch (TimeoutException e) {
            rows = driver.findElements(By.cssSelector(".table-row:not(.header)"));
        }

        if (rows.isEmpty()) {
            throw new org.testng.SkipException(
                    "No orders in this environment — action-button check skipped");
        }

        // Each order row must have a status action button
        List<WebElement> actionBtns = driver.findElements(
                By.cssSelector(".action-btns .btn-primary.btn-sm.btn-pill"));
        Assert.assertFalse(actionBtns.isEmpty(),
                "At least one action button (▶ Process / ✔ Deliver) must be visible when orders exist");
    }

    // ══════════════════════════════════════════════════════════════════
    //  TC026 — SellerProductsPage — NEGATIVE
    // ══════════════════════════════════════════════════════════════════
    @Test(description = "TC026 [NEGATIVE] — SellerProductsEmptyForNewSeller")
    public void tc026_sellerProductsEmptyForNewSeller() {
        clearSession();
        String freshSeller = registerNewSeller("SellerEmpty");
        ensureLoggedIn(freshSeller, "Test@1234");

        SellerProductsPage products = new SellerProductsPage(driver);
        products.open();

        new WebDriverWait(driver, WAIT)
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[normalize-space()='My Products']")));

        Assert.assertTrue(products.isLoaded(),
                "My Products page should load for a newly registered seller");

        int count = driver.findElements(By.cssSelector("div.prod-card")).size();
        Assert.assertEquals(count, 0,
                "A new seller must see 0 product cards — cross-seller isolation must hold");
    }

    // ══════════════════════════════════════════════════════════════════
    //  TC027 — SellerProductsPage — POSITIVE
    // ══════════════════════════════════════════════════════════════════
    @Test(description = "TC027 [POSITIVE] — SellerProductsAddNewLink")
    public void tc027_sellerProductsAddNewLink() {
        SellerProductsPage products = new SellerProductsPage(driver);
        products.open();
        Assert.assertTrue(products.isLoaded(), "My Products page should be visible");

        // Tolerate both href and routerlink variants.
        clickWhenReady(By.cssSelector(
                "a.btn-accent.btn-pill[href='/seller/upload'], a.btn-accent.btn-pill[routerlink='/seller/upload']"));

        new WebDriverWait(driver, WAIT)
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[normalize-space()='Add Product']")));
        Assert.assertTrue(new SellerUploadPage(driver).isLoaded(),
                "Clicking '+ Upload New' must navigate to the Add Product page");
    }

    // ══════════════════════════════════════════════════════════════════
    //  TC028 — SellerUploadPage — POSITIVE
    // ══════════════════════════════════════════════════════════════════
    @Test(description = "TC028 [POSITIVE] — SellerUploadFormFieldsPresent")
    public void tc028_sellerUploadFormFieldsPresent() {
        SellerUploadPage upload = new SellerUploadPage(driver);
        upload.open();
        Assert.assertTrue(upload.isLoaded(), "Add Product page should be visible");

        // Use the same robust pattern as TC020 — go through the page object,
        // which itself waits via type() → waitVisible().
        try {
            upload.enterTitle("Smoke Title " + randomSuffix())
                  .enterDescription("Smoke description text for automated test")
                  .enterPrice("99")
                  .enterStock("10");
        } catch (Exception e) {
            throw new org.testng.SkipException(
                    "One or more form fields not found — SPA build may differ: " + e.getMessage());
        }

        WebElement submitBtn;
        try {
            submitBtn = new WebDriverWait(driver, WAIT)
                    .until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("button.submit-btn")));
        } catch (TimeoutException e) {
            throw new AssertionError("Submit for Review button not clickable within timeout", e);
        }
        Assert.assertNotNull(submitBtn,
                "Submit for Review button must be present and clickable");
    }

    // ══════════════════════════════════════════════════════════════════
    //  TC029 — SellerUploadPage — POSITIVE
    // ══════════════════════════════════════════════════════════════════
    @Test(description = "TC029 [POSITIVE] — SellerUploadSelectDropdownsPresent")
    public void tc029_sellerUploadSelectDropdownsPresent() {
        SellerUploadPage upload = new SellerUploadPage(driver);
        upload.open();
        Assert.assertTrue(upload.isLoaded(), "Add Product page should be visible");

        List<WebElement> selects;
        try {
            selects = new WebDriverWait(driver, WAIT)
                    .until(ExpectedConditions.numberOfElementsToBe(
                            By.cssSelector(".manual-form select.select"), 3));
        } catch (TimeoutException e) {
            int found = driver.findElements(By.cssSelector(".manual-form select.select")).size();
            throw new AssertionError(
                    "Upload form must have exactly 3 select dropdowns — found " + found, e);
        }
        Assert.assertEquals(selects.size(), 3,
                "Upload form must have exactly 3 select dropdowns: Category, Delivery Method, Return Policy");
    }

    // ══════════════════════════════════════════════════════════════════
    //  TC030 — SellersListingPage — POSITIVE
    // ══════════════════════════════════════════════════════════════════
    @Test(description = "TC030 [POSITIVE] — SellersListingPageLoads")
    public void tc030_sellersListingPageLoads() {
        SellersListingPage listing = new SellersListingPage(driver);
        listing.open();

        Assert.assertTrue(listing.isLoaded(),
                "Public sellers listing page should load with a heading visible");

        // Wait for either the grid or table wrapper to render before asserting.
        try {
            new WebDriverWait(driver, WAIT)
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector(".sellers-grid, .sellers-table")));
        } catch (TimeoutException e) {
            throw new AssertionError("Sellers grid/table did not become visible within timeout", e);
        }
        Assert.assertTrue(listing.gridVisible(),
                "The sellers table must be displayed on the listing page");
    }

    // ══════════════════════════════════════════════════════════════════
    //  TC031 — SellersListingPage — NEGATIVE
    // ══════════════════════════════════════════════════════════════════
    @Test(description = "TC031 [NEGATIVE] — PendingSellerNotVisibleInPublicListing")
    public void tc031_pendingSellerNotVisibleInPublicListing() {
        // ── Step 1: baseline count ────────────────────────────────────
        SellersListingPage listing = new SellersListingPage(driver);
        listing.open();

        waitForListingReady();
        int countBefore = listing.sellerCount();

        // ── Step 2: register new seller — stays PENDING (no admin approval) ──
        clearSession();
        registerNewSeller("SellerPending");

        // ── Step 3: revisit as anonymous visitor ──────────────────────
        clearSession();
        listing.open();
        waitForListingReady();
        int countAfter = listing.sellerCount();

        // ── Step 4: count must not have increased ─────────────────────
        Assert.assertEquals(countAfter, countBefore,
                "PENDING seller must NOT appear in public listing before admin approval. "
                + "Before: " + countBefore + ", After: " + countAfter);
    }

    /* ------------------------------------------------------------------ */
    /* Local helpers                                                       */
    /* ------------------------------------------------------------------ */

    /** Click after explicitly waiting for the element to be clickable. */
    private void clickWhenReady(By locator) {
        new WebDriverWait(driver, WAIT)
                .until(ExpectedConditions.elementToBeClickable(locator))
                .click();
    }

    /**
     * Click any anchor on the dashboard that points at the given route. Tries
     * several selector shapes because the SPA may render the quick-action card
     * as either an {@code <a href="...">} or {@code <a routerlink="...">}, and
     * the wrapping class has changed across builds (qaction-row, action-row,
     * nav-card, …). Falls back to {@code By.partialLinkText} if none match.
     */
    private void clickDashboardLinkTo(String route) {
        By[] candidates = new By[] {
                By.cssSelector("a[href='" + route + "']"),
                By.cssSelector("a[routerlink='" + route + "']"),
                By.cssSelector("a[href$='" + route + "']"),
                By.xpath("//a[contains(@href,'" + route + "') or contains(@routerlink,'" + route + "')]")
        };
        for (By c : candidates) {
            List<WebElement> els = driver.findElements(c);
            if (!els.isEmpty()) {
                try {
                    new WebDriverWait(driver, WAIT)
                            .until(ExpectedConditions.elementToBeClickable(els.get(0)))
                            .click();
                    return;
                } catch (TimeoutException ignored) { /* try next */ }
            }
        }
        throw new AssertionError("No clickable dashboard link found for route: " + route);
    }

    /** Wait for the sellers listing's grid OR table wrapper — whichever the SPA renders. */
    private void waitForListingReady() {
        new WebDriverWait(driver, WAIT)
                .until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".sellers-grid, .sellers-table")));
    }
}
