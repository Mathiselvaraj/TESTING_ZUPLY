package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Authenticated buyer checkout at {@code /checkout}. Fields are
 * placeholder-driven (no name attributes). Backend's DeliveryAddressDto =
 * customerName / phone / address / city / pincode.
 */
public class CheckoutPage extends BasePage {

    private static final By HEADING       = By.xpath("//h1[normalize-space()='Checkout']");
    // The form puts customer name in the first text input; address in the textarea (placeholder
    // "House no..."), then city, pincode, phone (all .input class with distinct placeholders).
    private static final By NAME_INPUT    = By.cssSelector("input[type='text'].input:not([placeholder='City']):not([placeholder='600001'])");
    private static final By ADDRESS_AREA  = By.cssSelector("textarea.input[placeholder^='House no']");
    private static final By CITY_INPUT    = By.cssSelector("input.input[placeholder='City']");
    private static final By PINCODE_INPUT = By.cssSelector("input.input[placeholder='600001']");
    private static final By PHONE_INPUT   = By.cssSelector("input[type='tel'].input[placeholder='9876543210']");
    private static final By PAYMENT_RADIOS = By.cssSelector("input[type='radio']");
    private static final By PAYMENT_OPTIONS = By.cssSelector(".payment-option, .payment-method, label.payment");
    private static final By PLACE_ORDER_BTN = By.xpath("//button[contains(., 'Place Order')]");

    public CheckoutPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/checkout"; }
    @Override protected By readyMarker() { return HEADING; }

    public CheckoutPage fillAddress(String name, String phone, String address, String city, String pincode) {
        // Customer name is the first plain-text input
        var nameEls = driver.findElements(By.cssSelector("input[type='text'].input"));
        if (!nameEls.isEmpty()) { nameEls.get(0).clear(); nameEls.get(0).sendKeys(name); }
        type(ADDRESS_AREA, address);
        type(CITY_INPUT, city);
        type(PINCODE_INPUT, pincode);
        type(PHONE_INPUT, phone);
        return this;
    }

    public CheckoutPage selectPaymentMethod(String method) { // COD, UPI, CARD
        for (WebElement r : driver.findElements(PAYMENT_RADIOS)) {
            String v = r.getAttribute("value");
            if (method.equalsIgnoreCase(v)) { r.click(); return this; }
        }
        // fallback: click a label/option containing the method text
        for (WebElement opt : driver.findElements(PAYMENT_OPTIONS)) {
            if (opt.getText().toUpperCase().contains(method.toUpperCase())) { opt.click(); return this; }
        }
        throw new IllegalStateException("Payment method not found: " + method);
    }

    public void placeOrder() { click(PLACE_ORDER_BTN); }
}
