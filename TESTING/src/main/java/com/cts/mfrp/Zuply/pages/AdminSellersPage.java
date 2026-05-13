package com.cts.mfrp.Zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** Admin → Manage Sellers at {@code /admin/sellers}. */
public class AdminSellersPage extends BasePage {

    private static final By HEADING       = By.xpath("//h1[contains(normalize-space(), 'Manage Sellers')]");
    private static final By SEARCH_INPUT  = By.cssSelector("input.search-input");
    private static final By FILTER_TABS   = By.cssSelector("button.filter-tab");
    private static final By APPROVE_BTNS  = By.xpath("//button[contains(@class,'btn-primary') and normalize-space()='Approve']");
    private static final By REJECT_BTNS   = By.xpath("//button[contains(@class,'btn-warning') and normalize-space()='Reject']");
    private static final By SUSPEND_BTNS  = By.xpath("//button[contains(@class,'btn-danger') and normalize-space()='Suspend']");

    public enum Filter { ALL, APPROVED, PENDING, REJECTED }

    public AdminSellersPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/admin/sellers"; }
    @Override protected By readyMarker() { return HEADING; }

    public AdminSellersPage search(String text) { type(SEARCH_INPUT, text); return this; }

    public void selectFilter(Filter f) {
        for (WebElement tab : driver.findElements(FILTER_TABS)) {
            String t = tab.getText().toUpperCase();
            if (t.contains(f.name())) { tab.click(); return; }
        }
        throw new IllegalStateException("Filter tab not found: " + f);
    }

    public int approvableCount() { return driver.findElements(APPROVE_BTNS).size(); }
    public int suspendableCount() { return driver.findElements(SUSPEND_BTNS).size(); }

    public void approveFirst() { firstOf(APPROVE_BTNS, "Approve").click(); }
    public void rejectFirst()  { firstOf(REJECT_BTNS, "Reject").click(); }
    public void suspendFirst() { firstOf(SUSPEND_BTNS, "Suspend").click(); }

    private WebElement firstOf(By by, String label) {
        List<WebElement> els = driver.findElements(by);
        if (els.isEmpty()) throw new IllegalStateException("No " + label + " button visible");
        return els.get(0);
    }
}
