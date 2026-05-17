package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Admin → Manage Products at {@code /admin/products}. */
public class AdminProductsPage extends BasePage {

    private static final By HEADING      = By.xpath("//h1[contains(normalize-space(), 'Manage Products')]");
    private static final By SEARCH_INPUT = By.cssSelector("input.search-input");
    private static final By FILTER_TABS  = By.cssSelector("button.filter-tab");
    private static final By APPROVE_BTNS = By.xpath("//button[contains(@class,'btn-primary') and normalize-space()='Approve']");
    private static final By REJECT_BTNS  = By.xpath("//button[contains(@class,'btn-warning') and normalize-space()='Reject']");

    public enum Filter { ALL, APPROVED, PENDING, REJECTED }

    public AdminProductsPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/admin/products"; }
    @Override protected By readyMarker() { return HEADING; }

    public AdminProductsPage search(String text) { type(SEARCH_INPUT, text); return this; }

    public void selectFilter(Filter f) {
        for (WebElement tab : driver.findElements(FILTER_TABS)) {
            if (tab.getText().toUpperCase().contains(f.name())) { tab.click(); return; }
        }
        throw new IllegalStateException("Filter tab not found: " + f);
    }

    public int rowCount() {
        return driver.findElements(By.cssSelector("table tr, .product-row, .card")).size();
    }

    public void approveFirst() {
        var els = driver.findElements(APPROVE_BTNS);
        if (els.isEmpty()) throw new IllegalStateException("No Approve button visible");
        els.get(0).click();
    }

    public void rejectFirst() {
        var els = driver.findElements(REJECT_BTNS);
        if (els.isEmpty()) throw new IllegalStateException("No Reject button visible");
        els.get(0).click();
    }
}
