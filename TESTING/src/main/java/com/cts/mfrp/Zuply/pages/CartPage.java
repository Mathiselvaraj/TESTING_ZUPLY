package com.cts.mfrp.Zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Authenticated buyer cart at {@code /cart}. */
public class CartPage extends BasePage {

    private static final By HEADING      = By.xpath("//h1[normalize-space()='Shopping Cart']");
    private static final By CART_ITEMS   = By.cssSelector(".cart-item, [class*='cart-item']");
    private static final By QTY_INPUTS   = By.cssSelector("input[type='number'], .qty-input");
    private static final By REMOVE_BTNS  = By.xpath("//button[contains(translate(.,'REMOVEDEL','removedel'),'remove')]");
    private static final By GRAND_TOTAL  = By.cssSelector(".grand-total, [class*='total']");
    private static final By CHECKOUT_BTN = By.xpath("//button[contains(translate(.,'CHECKOUT','checkout'),'checkout')]");
    private static final By EMPTY_STATE  = By.xpath("//*[contains(.,'empty') or contains(.,'no items')]");

    public CartPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/cart"; }
    @Override protected By readyMarker() { return HEADING; }

    public int itemCount()      { return driver.findElements(CART_ITEMS).size(); }
    public boolean isEmpty()    { return itemCount() == 0; }
    public String grandTotal()  { return text(GRAND_TOTAL); }

    public void removeFirst() {
        var els = driver.findElements(REMOVE_BTNS);
        if (els.isEmpty()) throw new IllegalStateException("No Remove button visible");
        els.get(0).click();
    }

    public void setFirstQuantity(String quantity) {
        var els = driver.findElements(QTY_INPUTS);
        if (els.isEmpty()) throw new IllegalStateException("No quantity input visible");
        WebElement el = els.get(0);
        el.clear();
        el.sendKeys(quantity);
    }

    public void proceedToCheckout() { click(CHECKOUT_BTN); }
}
