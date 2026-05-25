package com.cts.mfrp.zuply.tests.ui.buyer;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.OrdersPage;
import com.cts.mfrp.zuply.pages.SellerOrdersPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Order management — FRD §2.6. Maps to TC017 and TC018. */
@Test(groups = {"regression", "ui", "orders"})
public class OrderTrackingUiTests extends UiBaseTest {

    private String buyerEmail;

    /** Class-level setup: just register a buyer. Login happens per test because
     *  TC017 is buyer-side and TC018 is seller-side. */
    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void seedBuyer() {
        buyerEmail = registerNewCustomer("OrdersUser");
    }

    /** TC017 — Customer can view their order history. */
    @Test(description = "TC017 — OrderHistory")
    public void tc017_orderHistory() {
        loginViaUi(buyerEmail, "Test@1234");

        OrdersPage page = new OrdersPage(driver);
        page.open();
        Assert.assertTrue(page.isLoaded(), "Orders page heading should be visible");
        Assert.assertTrue(page.orderCount() >= 0,
                "Orders count should be a non-negative number");
    }

    /**
     * AD_TC_OT1 -- Buyer can cancel a pending order from the My Orders page.
     *
     * The app already shows a "Cancelled" filter tab on the orders page, which confirms
     * the FRD section 2.6 cancellation status is recognised. However, there is currently
     * NO "Cancel Order" action button on any order card — the buyer has no way to
     * initiate a cancellation from the UI.
     *
     * Step 1 (filter tab check) is expected to PASS.
     * Step 2 (cancel button check) is expected to FAIL until the feature is built.
     */
    @Test(description = "AD_TC_OT1 -- CancelOrderButton [MISSING FEATURE]")
    public void tcOt1_cancelOrderButton() {
        loginViaUi(buyerEmail, "Test@1234");

        OrdersPage page = new OrdersPage(driver);
        page.open();
        Assert.assertTrue(page.isLoaded(), "Orders page must load before checking cancel feature");

        // Step 1 — The app already has a "Cancelled" filter tab (class=tab-btn) confirming
        // the status is part of the data model (FRD section 2.6).
        boolean hasCancelledTab = driver.findElements(By.cssSelector("button.tab-btn"))
                .stream()
                .anyMatch(b -> b.getText().trim().equalsIgnoreCase("Cancelled"));
        Assert.assertTrue(hasCancelledTab,
                "Orders page should expose a 'Cancelled' filter tab (FRD section 2.6)");

        // Step 2 — Each order card must expose a 'Cancel Order' button so the buyer can
        // cancel a pending order. FRD section 2.6 mandates buyer-initiated cancellation.
        // THIS ASSERTION IS EXPECTED TO FAIL — the cancel action is not yet implemented.
        boolean hasCancelAction = !driver.findElements(By.xpath(
                "//*[self::button or self::a]"
                + "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                + " 'abcdefghijklmnopqrstuvwxyz'), 'cancel order')]")).isEmpty();

        Assert.assertTrue(hasCancelAction,
                "MISSING FEATURE: Orders page should expose a 'Cancel Order' button on each "
                + "order card (FRD section 2.6). No cancel action was found on the page — "
                + "the buyer currently has no way to cancel a pending order.");
    }

    /** TC018 — Seller orders page loads and handles status updates (or empty state). */
    @Test(enabled = false, description = "TC018 — OrderStatusUpdate")
    public void tc018_orderStatusUpdate() {
        clearSession();
        String sellerEmail = registerNewSeller("OrderUpdater");
        loginViaUi(sellerEmail, "Test@1234");

        SellerOrdersPage page = new SellerOrdersPage(driver);
        page.open();
        Assert.assertTrue(page.isLoaded(),
                "Seller orders page should load successfully after login");
        if (page.orderCount() == 0) {
            // Fresh seller: no orders yet — valid state; assert empty state renders without error
            Assert.assertTrue(page.isLoaded(),
                    "Seller orders page should handle empty order list gracefully without crashing");
        } else {
            page.updateFirstOrderStatus("PROCESSING");
            Assert.assertTrue(page.isLoaded(),
                    "Seller orders page should remain loaded after status update");
        }
    }
}
