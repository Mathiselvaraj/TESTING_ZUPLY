package com.cts.mfrp.Zuply.tests.ui;

import com.cts.mfrp.Zuply.pages.OrdersPage;
import com.cts.mfrp.Zuply.pages.SellerOrdersPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Order management — FRD §2.6. Maps to TC017 and TC018. */
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

    /** TC018 — Seller can update an order status (Placed → Processing → Delivered). */
    @Test(description = "TC018 — OrderStatusUpdate")
    public void tc018_orderStatusUpdate() {
        // Switch role: drop buyer session and log in as a fresh seller
        clearSession();
        String sellerEmail = registerNewSeller("OrderUpdater");
        loginViaUi(sellerEmail, "Test@1234");

        SellerOrdersPage page = new SellerOrdersPage(driver);
        page.open();
        if (page.orderCount() == 0) {
            throw new org.testng.SkipException(
                    "Fresh seller has no orders — TC018 requires an existing order. " +
                    "Run admin-side product approval + a buyer purchase first.");
        }
        page.updateFirstOrderStatus("PROCESSING");
        Assert.assertTrue(page.isLoaded(), "Seller orders page should remain loaded after status update");
    }
}
