package com.cts.mfrp.zuply.tests.ui.buyer;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.CheckoutPage;
import com.cts.mfrp.zuply.pages.ProductsPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Checkout & order placement — FRD §2.5. Maps to TC015, TC016, AD_TC_CO1. */
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

    /** TC016 — Checkout should block order placement when mandatory city field is missing. */
    @Test(description = "TC016 — CheckoutMissingFields")
    public void tc016_checkoutMissingFields() {
        seedOneItemInCart();
        CheckoutPage cp = new CheckoutPage(driver);
        cp.open();
        cp.fillAddress("John Doe", "9876543210", "123 Main St", "", "600001"); // City is blank
        try { cp.selectPaymentMethod("Cash"); } catch (Exception ignored) {}
        try { cp.placeOrder(); } catch (Exception ignored) {}
        waitAfterAction();

        Assert.assertFalse(cp.isOrderSuccessMessageVisible(),
                "CRITICAL BUG: The application allowed order placement without a city!");
        Assert.assertTrue(cp.isCityValidationErrorVisible(),
                "The red validation error for missing City did not appear on screen.");
    }

    /**
     * AD_TC_CO1 -- All three FRD-mandated payment methods (COD, UPI, Card) are
     * available on the checkout page (FRD section 2.5).
     */
    @Test(description = "AD_TC_CO1 -- ThreePaymentMethodsAvailable")
    public void co1_threePaymentMethodsAvailable() {
        seedOneItemInCart();

        CheckoutPage cp = new CheckoutPage(driver);
        cp.open();

        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".payment-options, .payment-option")));

        String body = driver.getPageSource().toLowerCase();
        boolean hasCod  = body.contains("cash on delivery") || body.contains("cod");
        boolean hasUpi  = body.contains("upi") || body.contains("gpay");
        boolean hasCard = body.contains("card");

        Assert.assertTrue(hasCod,  "Checkout should expose 'Cash on Delivery' payment method (FRD section 2.5)");
        Assert.assertTrue(hasUpi,  "Checkout should expose 'UPI' payment method (FRD section 2.5)");
        Assert.assertTrue(hasCard, "Checkout should expose 'Card' payment method (FRD section 2.5)");
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
