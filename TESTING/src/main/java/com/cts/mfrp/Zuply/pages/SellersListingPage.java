package com.cts.mfrp.Zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Public listing of all sellers at {@code /sellers}. */
public class SellersListingPage extends BasePage {

    private static final By HEADING       = By.xpath("//h1[contains(normalize-space(), 'Local Sellers')]");
    private static final By SELLER_CARDS  = By.cssSelector(".seller-card");
    private static final By SELLER_NAMES  = By.cssSelector(".seller-name");
    private static final By GRID          = By.cssSelector(".sellers-grid");

    public SellersListingPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/sellers"; }
    @Override protected By readyMarker() { return HEADING; }

    public int sellerCount()        { return driver.findElements(SELLER_CARDS).size(); }
    public boolean gridVisible()    { return driver.findElement(GRID).isDisplayed(); }
    public String firstSellerName() {
        var els = driver.findElements(SELLER_NAMES);
        return els.isEmpty() ? null : els.get(0).getText().trim();
    }
}
