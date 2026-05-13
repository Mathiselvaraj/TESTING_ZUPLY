package com.cts.mfrp.Zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

/**
 * Seller's customer-orders page at {@code /seller/orders}.
 * Order status enum: PROCESSING, SHIPPED, DELIVERED.
 */
public class SellerOrdersPage extends BasePage {

    private static final By HEADING       = By.xpath("//h1[normalize-space()='Customer Orders']");
    private static final By ORDER_ROWS    = By.cssSelector(".order-card, .order-row, table tbody tr");
    private static final By STATUS_DROPDOWNS = By.cssSelector("select.status-select, select[name='status'], select.select");
    private static final By UPDATE_BTNS   = By.xpath("//button[contains(translate(.,'UPDATE','update'),'update')]");

    public SellerOrdersPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/seller/orders"; }
    @Override protected By readyMarker() { return HEADING; }

    public int orderCount() { return driver.findElements(ORDER_ROWS).size(); }

    public void updateFirstOrderStatus(String status) {
        List<WebElement> dropdowns = driver.findElements(STATUS_DROPDOWNS);
        if (dropdowns.isEmpty()) throw new IllegalStateException("No status dropdown visible");
        new Select(dropdowns.get(0)).selectByVisibleText(status);
        List<WebElement> updateBtns = driver.findElements(UPDATE_BTNS);
        if (!updateBtns.isEmpty()) updateBtns.get(0).click();
    }
}
