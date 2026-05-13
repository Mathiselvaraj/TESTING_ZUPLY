package com.cts.mfrp.Zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Seller's home at {@code /seller/dashboard}. Shows 4 stat cards:
 * Total Products, Total Orders, Pending Orders, Approved Products.
 */
public class SellerDashboardPage extends BasePage {

    private static final By STATS_GRID    = By.cssSelector(".grid-4.stats-grid, .stats-grid");
    private static final By STAT_CARDS    = By.cssSelector(".stat-card");
    private static final By UPLOAD_LINK   = By.cssSelector("a[routerlink='/seller/upload']");
    private static final By PRODUCTS_LINK = By.cssSelector("a[routerlink='/seller/products']");
    private static final By ORDERS_LINK   = By.cssSelector("a[routerlink='/seller/orders']");

    public SellerDashboardPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/seller/dashboard"; }
    @Override protected By readyMarker() { return STATS_GRID; }

    /** Returns the numeric value (as text) shown under the given stat label. */
    public String statValue(String labelText) {
        for (WebElement card : driver.findElements(STAT_CARDS)) {
            try {
                WebElement label = card.findElement(By.cssSelector(".stat-label"));
                if (label.getText().trim().equalsIgnoreCase(labelText)) {
                    return card.findElement(By.cssSelector(".stat-value")).getText().trim();
                }
            } catch (Exception ignored) {}
        }
        throw new IllegalStateException("Stat card not found: " + labelText);
    }

    public String totalProducts()    { return statValue("Total Products"); }
    public String totalOrders()      { return statValue("Total Orders"); }
    public String pendingOrders()    { return statValue("Pending Orders"); }
    public String approvedProducts() { return statValue("Approved Products"); }

    public int statCardCount() { return driver.findElements(STAT_CARDS).size(); }

    public void goToUpload()   { click(UPLOAD_LINK); }
    public void goToProducts() { click(PRODUCTS_LINK); }
    public void goToOrders()   { click(ORDERS_LINK); }
}
