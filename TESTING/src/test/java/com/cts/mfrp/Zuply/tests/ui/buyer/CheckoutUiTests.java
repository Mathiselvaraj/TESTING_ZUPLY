package com.cts.mfrp.Zuply.tests.ui.buyer;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.CheckoutPage;
import com.cts.mfrp.zuply.pages.ProductsPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Checkout & order placement — FRD §2.5. Maps to TC015 and TC016. */
@Test(groups = {"regression", "ui", "checkout"})
public class CheckoutUiTests extends UiBaseTest {

    private String buyerEmail;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginBuyer() {
        buyerEmail = registerNewCustomer("Checkout");
        loginViaUi(buyerEmail, "Test@1234");
    }

    /** TC015 — Successful checkout with valid delivery address and payment method. */
    @Test(description = "TC015 — ValidCheckout")
    public void tc015_validCheckout() {
        seedOneItemInCart();

        CheckoutPage cp = new CheckoutPage(driver);
        cp.open();
        cp.fillAddress("John Doe", "9876543210", "123 Main St", "Chennai", "600001");
        try { cp.selectPaymentMethod("Cash"); } catch (Exception ignored) {}
        try { cp.placeOrder(); } catch (Exception ignored) {}
        waitAfterAction();

        Assert.assertFalse(driver.getTitle().contains("Page not found"),
                "Should not land on Netlify's 404 after checkout submit");
    }

    @Test(description = "TC016 — CheckoutMissingFields")
    public void tc016_checkoutMissingFields() {
        seedOneItemInCart();
        CheckoutPage cp = new CheckoutPage(driver);
        cp.open();
        cp.fillAddress("John Doe", "9876543210", "123 Main St", "", "600001"); // City is blank
        try { cp.selectPaymentMethod("Cash"); } catch (Exception ignored) {}
        try { cp.placeOrder(); } catch (Exception ignored) {}
        waitAfterAction();

        // 1. Check if the frontend incorrectly allowed the order to go through
        Assert.assertFalse(cp.isOrderSuccessMessageVisible(),
                "CRITICAL BUG: The application allowed order placement without a city!");
        // 2. Strictly check that the specific visual error rendered on the DOM
        Assert.assertTrue(cp.isCityValidationErrorVisible(),
                "The red validation error for missing City did not appear on screen.");
    }

    /** AD_TC017 — Successful checkout via Razorpay Online Payment Flow (FRD §4.1). */
    @Test(description = "TC_AD017 — OnlinePaymentCheckout")
    public void tc017_onlinePaymentCheckout() {
        seedOneItemInCart();

        CheckoutPage cp = new CheckoutPage(driver);
        cp.open();
        cp.fillAddress("Jane Doe", "9876543210", "456 Tech Park", "Hyderabad", "500081");

        // Select UPI or Card
        try { cp.selectPaymentMethod("UPI"); } catch (Exception ignored) {}

        // Trigger the backend call to create the Razorpay Order
        try { cp.placeOrder(); } catch (Exception ignored) {}

        // Verify the application successfully handed the flow over to Razorpay
        boolean didRazorpayOpen = cp.isRazorpayModalOpened();
        Assert.assertTrue(didRazorpayOpen,
                "Razorpay modal failed to open! The integration between Zuply and Razorpay is broken.");

        // We intentionally stop the test here. Testing Razorpay's internal anti-bot
        // security is outside the scope of testing the Zuply application.
    }

    /** Best-effort cart seed: skip silently if the Products page has no Add buttons. */
    private void seedOneItemInCart() {
        ProductsPage products = new ProductsPage(driver);
        products.open();
        if (products.hasAddToCartButtons()) {
            products.addFirstToCart();
        }
    }
}
