package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/** Admin → Manage Sellers at {@code /admin/sellers}. */
public class AdminSellersPage extends BasePage {

    private static final By HEADING      = By.xpath("//h1[contains(normalize-space(), 'Manage Sellers')]");
    private static final By SEARCH_INPUT = By.cssSelector("input.search-input");
    private static final By FILTER_TABS  = By.cssSelector("button.filter-tab");
    private static final By APPROVE_BTNS = By.xpath("//button[contains(@class,'btn-primary') and normalize-space()='Approve']");
    private static final By REJECT_BTNS  = By.xpath("//button[contains(@class,'btn-warning') and normalize-space()='Reject']");
    private static final By SUSPEND_BTNS = By.xpath("//button[contains(@class,'btn-danger') and normalize-space()='Suspend']");

    // FIX: DevTools confirmed sellers are rendered as cards inside div.sellers-table
    // There is NO <table> element — class name is misleading
    private static final By TABLE_ROWS   = By.cssSelector("div.sellers-table > div");

    // Loading spinner — present while API call is in progress after filter click
    private static final By LOADING_SPINNER = By.cssSelector("app-loading-spinner");

    // FIX: Added SUSPENDED and PENDING_APPROVAL to match actual UI tab labels
    // FIX: Each value carries a tabLabel string used in selectFilter()
    //      so matching works against "ALL SELLERS 74", "PENDING APPROVAL 17" etc.
    public enum Filter {
        ALL_SELLERS("ALL SELLERS"),
        APPROVED("APPROVED"),
        PENDING_APPROVAL("PENDING APPROVAL"),
        SUSPENDED("SUSPENDED"),
        REJECTED("REJECTED");

        public final String tabLabel;
        Filter(String tabLabel) { this.tabLabel = tabLabel; }
    }

    public AdminSellersPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/admin/sellers"; }
    @Override protected By readyMarker() { return HEADING; }

    public AdminSellersPage search(String text) { type(SEARCH_INPUT, text); return this; }

    // FIX: Uses f.tabLabel instead of f.name() so enum values match UI tab text
    public void selectFilter(Filter f) {
        for (WebElement tab : driver.findElements(FILTER_TABS)) {
            if (tab.getText().toUpperCase().contains(f.tabLabel)) {
                tab.click();
                return;
            }
        }
        throw new IllegalStateException("Filter tab not found: " + f);
    }

    // FIX: rowCount() now counts div cards inside div.sellers-table (not table rows)
    public int rowCount()         { return driver.findElements(TABLE_ROWS).size(); }
    public int approvableCount()  { return driver.findElements(APPROVE_BTNS).size(); }
    public int suspendableCount() { return driver.findElements(SUSPEND_BTNS).size(); }
    public int rejectableCount()  { return driver.findElements(REJECT_BTNS).size(); }

    public void approveFirst() { firstOf(APPROVE_BTNS, "Approve").click(); }
    public void rejectFirst()  { firstOf(REJECT_BTNS,  "Reject").click(); }
    public void suspendFirst() { firstOf(SUSPEND_BTNS, "Suspend").click(); }

    /** Returns true if any seller card contains the given name or email. */
    public boolean isSellerVisible(String nameOrEmail) {
        return driver.findElements(TABLE_ROWS).stream()
                .anyMatch(row -> row.getText().contains(nameOrEmail));
    }

    private WebElement firstOf(By by, String label) {
        List<WebElement> els = driver.findElements(by);
        if (els.isEmpty()) throw new IllegalStateException("No " + label + " button visible");
        return els.get(0);
    }
}