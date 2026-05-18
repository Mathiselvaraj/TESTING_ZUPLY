package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Admin landing page at {@code /admin/dashboard}.
 * All locators verified from DevTools screenshots.
 *
 * Key DevTools findings:
 * - Stat cards: div.stat-card.stat-card-link with title attribute
 * - Shortcut cards: a.nav-card with routerlink inside div.grid-4.nav-cards
 * - Sellers Pending: div#pending-section, cards are div.approval-card.seller-card
 * - Products Pending: div#pending-products-section
 * - Count badge: span.count-badge inside div.section-title
 * - Add Admin: button.btn-primary.btn-sm.btn-pill inside div.section.create-admin-section
 * - Form: div.create-admin-form with confirmed placeholders
 */
public class AdminDashboardPage extends BasePage {

    // ── Ready marker ──────────────────────────────────────────────────────────
    // DevTools: div.admin-banner confirmed in DOM
    private static final By HERO_HEADING = By.cssSelector("div.admin-banner");

    // ── Top nav links ─────────────────────────────────────────────────────────
    private static final By SELLERS_LINK  = By.cssSelector("a[routerlink='/admin/sellers']");
    private static final By PRODUCTS_LINK = By.cssSelector("a[routerlink='/admin/products']");
    private static final By ORDERS_LINK   = By.cssSelector("a[routerlink='/admin/orders']");
    private static final By REPORTS_LINK  = By.cssSelector("a[routerlink='/admin/reports']");

    // ── Stat cards ────────────────────────────────────────────────────────────
    // DevTools: div.stat-card.stat-card-link with title attribute confirmed
    private static final By CARD_TOTAL_SELLERS     = By.cssSelector("div[title='View approved sellers']");
    private static final By CARD_TOTAL_PRODUCTS    = By.cssSelector("div[title='View approved products']");
    private static final By CARD_TOTAL_ORDERS      = By.cssSelector("div[title='View all orders']");
    private static final By CARD_PENDING_APPROVALS = By.cssSelector("div[title='View pending approvals']");

    // Stat values — DevTools: div.stat-value inside each stat card
    private static final By STAT_SELLERS_VALUE  = By.cssSelector("div[title='View approved sellers'] div.stat-value");
    private static final By STAT_PRODUCTS_VALUE = By.cssSelector("div[title='View approved products'] div.stat-value");
    private static final By STAT_ORDERS_VALUE   = By.cssSelector("div[title='View all orders'] div.stat-value");
    private static final By STAT_PENDING_VALUE  = By.cssSelector("div[title='View pending approvals'] div.stat-value");

    // ── Shortcut nav cards ────────────────────────────────────────────────────
    // DevTools: a.nav-card with routerlink inside div.grid-4.nav-cards
    private static final By SHORTCUT_MANAGE_SELLERS  = By.cssSelector("a.nav-card[routerlink='/admin/sellers']");
    private static final By SHORTCUT_MANAGE_PRODUCTS = By.cssSelector("a.nav-card[routerlink='/admin/products']");
    private static final By SHORTCUT_REPORTS         = By.cssSelector("a.nav-card[routerlink='/admin/reports']");
    private static final By SHORTCUT_ALL_ORDERS      = By.cssSelector("a.nav-card[routerlink='/admin/orders']");

    // ── Sellers Pending Approval section ──────────────────────────────────────
    // DevTools: div#pending-section.section, title in div.section-title
    // Count badge: span.count-badge inside div.section-title
    // Seller cards: div.approval-card.seller-card inside div.approval-cards
    private static final By SELLERS_PENDING_SECTION  = By.cssSelector("div#pending-section");
    private static final By SELLERS_PENDING_BADGE    = By.cssSelector("div#pending-section div.section-title span.count-badge");
    private static final By SELLERS_PENDING_CARDS    = By.cssSelector("div#pending-section div.approval-card.seller-card");
    private static final By SELLERS_PENDING_APPROVE  = By.cssSelector("div#pending-section div.approval-card button.btn-primary");
    private static final By SELLERS_PENDING_REJECT   = By.cssSelector("div#pending-section div.approval-card button.btn-danger");
    private static final By SELLERS_VIEW_ALL         = By.cssSelector("div#pending-section a.section-link");

    // ── Products Pending Review section ───────────────────────────────────────
    // DevTools: div#pending-products-section.section
    private static final By PRODUCTS_PENDING_SECTION = By.cssSelector("div#pending-products-section");
    private static final By PRODUCTS_PENDING_BADGE   = By.cssSelector("div#pending-products-section div.section-title span.count-badge");
    private static final By PRODUCTS_PENDING_APPROVE = By.cssSelector("div#pending-products-section div.approval-card button.btn-primary");
    private static final By PRODUCTS_PENDING_REJECT  = By.cssSelector("div#pending-products-section div.approval-card button.btn-danger");
    private static final By PRODUCTS_VIEW_ALL        = By.cssSelector("div#pending-products-section a.section-link");

    // ── Admin Accounts section ────────────────────────────────────────────────
    // DevTools: div.section.create-admin-section
    // Add Admin button: button.btn-primary.btn-sm.btn-pill text "+ Add Admin"
    // Form: div.create-admin-form (only present after clicking Add Admin)
    private static final By ADMIN_ACCOUNTS_SECTION = By.cssSelector("div.section.create-admin-section");
    private static final By ADD_ADMIN_BTN           = By.cssSelector("div.section.create-admin-section div.section-header button.btn-primary");
    private static final By ADD_ADMIN_FORM          = By.cssSelector("div.create-admin-form");

    // DevTools confirmed exact placeholders from Image 4
    private static final By ADD_ADMIN_FORM_NAME     = By.cssSelector("input[placeholder='Admin name']");
    private static final By ADD_ADMIN_FORM_EMAIL    = By.cssSelector("input[placeholder='admin@example.com']");
    private static final By ADD_ADMIN_FORM_PASSWORD = By.cssSelector("input[placeholder='Strong password']");
    private static final By ADD_ADMIN_FORM_PHONE    = By.cssSelector("input[placeholder='Phone number'][type='tel']");
    private static final By ADD_ADMIN_SUBMIT        = By.cssSelector("div.create-admin-form button.btn-primary");
    private static final By ADD_ADMIN_CANCEL        = By.cssSelector("div.section.create-admin-section div.section-header button:not(.active)");
    // FIX: confirmed class from DevTools screenshot — div.create-admin-msg.create-admin-msg-error
    private static final By ADD_ADMIN_ERROR = By.cssSelector(
            "div.create-admin-msg.create-admin-msg-error");

    public AdminDashboardPage(WebDriver driver) { super(driver); }

    @Override public String route()       { return "/admin/dashboard"; }
    @Override protected By readyMarker() { return HERO_HEADING; }

    /**
     * Opens dashboard and waits for pending badges to load real data.
     * Badge renders with "0" initially then updates after API call — this
     * method waits for the badge to show a non-zero value before returning.
     * Use instead of open() for tests that check pending seller/product counts.
     */
    public void openAndWaitForData() {
        open();
        // Wait for pending-section to be present first
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions
                            .presenceOfElementLocated(SELLERS_PENDING_SECTION));
        } catch (Exception ignored) {}
        // Then wait for badge to show non-zero value
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30))
                    .until(d -> {
                        try {
                            String t = d.findElement(SELLERS_PENDING_BADGE).getText().trim();
                            return !t.isEmpty() && !t.equals("0");
                        } catch (Exception e) { return false; }
                    });
        } catch (Exception ignored) {}
        // Same for products
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30))
                    .until(d -> {
                        try {
                            String t = d.findElement(PRODUCTS_PENDING_BADGE).getText().trim();
                            return !t.isEmpty() && !t.equals("0");
                        } catch (Exception e) { return false; }
                    });
        } catch (Exception ignored) {}
    }

    // ── Top nav ───────────────────────────────────────────────────────────────
    public void goToSellers()  { click(SELLERS_LINK); }
    public void goToProducts() { click(PRODUCTS_LINK); }
    public void goToOrders()   { click(ORDERS_LINK); }
    public void goToReports()  { click(REPORTS_LINK); }

    // ── Stat cards ────────────────────────────────────────────────────────────
    public boolean isTotalSellersCardVisible()    { return isVisible(CARD_TOTAL_SELLERS); }
    public boolean isTotalProductsCardVisible()   { return isVisible(CARD_TOTAL_PRODUCTS); }
    public boolean isTotalOrdersCardVisible()     { return isVisible(CARD_TOTAL_ORDERS); }
    public boolean isPendingApprovalsCardVisible() { return isVisible(CARD_PENDING_APPROVALS); }

    public void clickTotalSellersCard()     { scrollAndClick(driver.findElement(CARD_TOTAL_SELLERS)); }
    public void clickTotalProductsCard()    { scrollAndClick(driver.findElement(CARD_TOTAL_PRODUCTS)); }
    public void clickTotalOrdersCard()      { scrollAndClick(driver.findElement(CARD_TOTAL_ORDERS)); }
    public void clickPendingApprovalsCard() { scrollAndClick(driver.findElement(CARD_PENDING_APPROVALS)); }

    public String statSellersValue()  { return text(STAT_SELLERS_VALUE); }
    public String statProductsValue() { return text(STAT_PRODUCTS_VALUE); }
    public String statOrdersValue()   { return text(STAT_ORDERS_VALUE); }
    public String statPendingValue()  { return text(STAT_PENDING_VALUE); }

    // ── Shortcut cards ────────────────────────────────────────────────────────
    public void clickManageSellers()  { click(SHORTCUT_MANAGE_SELLERS); }
    public void clickManageProducts() { click(SHORTCUT_MANAGE_PRODUCTS); }
    public void clickReports()        { click(SHORTCUT_REPORTS); }
    public void clickAllOrders()      { click(SHORTCUT_ALL_ORDERS); }

    // ── Sellers Pending Approval ──────────────────────────────────────────────
    public boolean isSellersPendingSectionVisible() { return isVisible(SELLERS_PENDING_SECTION); }

    /** Reads count from span.count-badge — confirmed DevTools: "16"
     *  Badge renders with 0 first then updates — wait for non-zero or stable value */
    public int sellersPendingCount() {
        try {
            // Wait for badge to appear
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions
                            .presenceOfElementLocated(SELLERS_PENDING_BADGE));
            // Wait for badge to show non-zero (real data loaded) or stabilise after 10s
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30))
                    .until(d -> {
                        try {
                            String t = d.findElement(SELLERS_PENDING_BADGE).getText().trim();
                            return !t.isEmpty() && !t.equals("0");
                        } catch (Exception e) { return false; }
                    });
            return Integer.parseInt(driver.findElement(SELLERS_PENDING_BADGE).getText().trim());
        } catch (Exception e) { return 0; }
    }

    public int sellersPendingCardCount()          { return driver.findElements(SELLERS_PENDING_CARDS).size(); }
    public int sellersPendingApproveButtonCount() { return driver.findElements(SELLERS_PENDING_APPROVE).size(); }

    /** UI confirmed: Approve on dashboard has NO browser dialog */
    public void approveFirstPendingSeller() {
        scrollAndClick(firstOf(SELLERS_PENDING_APPROVE, "Seller Approve"));
    }

    /** UI confirmed: Reject on dashboard triggers browser dialog */
    public void rejectFirstPendingSeller() {
        scrollAndClick(firstOf(SELLERS_PENDING_REJECT, "Seller Reject"));
    }

    public void clickSellersViewAll() { click(SELLERS_VIEW_ALL); }

    // ── Products Pending Review ───────────────────────────────────────────────
    public boolean isProductsPendingSectionVisible() { return isVisible(PRODUCTS_PENDING_SECTION); }

    /** Reads count from span.count-badge
     *  Badge renders with 0 first then updates — wait for non-zero or stable value */
    public int productsPendingCount() {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions
                            .presenceOfElementLocated(PRODUCTS_PENDING_BADGE));
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30))
                    .until(d -> {
                        try {
                            String t = d.findElement(PRODUCTS_PENDING_BADGE).getText().trim();
                            return !t.isEmpty() && !t.equals("0");
                        } catch (Exception e) { return false; }
                    });
            return Integer.parseInt(driver.findElement(PRODUCTS_PENDING_BADGE).getText().trim());
        } catch (Exception e) { return 0; }
    }

    /** UI confirmed: Approve on dashboard has NO browser dialog */
    public void approveFirstPendingProduct() {
        scrollAndClick(firstOf(PRODUCTS_PENDING_APPROVE, "Product Approve"));
    }

    /** UI confirmed: Reject on dashboard triggers browser dialog */
    public void rejectFirstPendingProduct() {
        scrollAndClick(firstOf(PRODUCTS_PENDING_REJECT, "Product Reject"));
    }

    public void clickProductsViewAll() { click(PRODUCTS_VIEW_ALL); }

    // ── Admin Accounts ────────────────────────────────────────────────────────
    public boolean isAdminAccountsSectionVisible() { return isVisible(ADMIN_ACCOUNTS_SECTION); }
    public boolean isAddAdminFormVisible()          { return isVisible(ADD_ADMIN_FORM); }
    public boolean isAddAdminErrorVisible()         { return isVisible(ADD_ADMIN_ERROR); }

    public void clickAddAdmin() {
        // FIX: use pure JS click instead of scroll + native click
        // In headless mode window.scrollTo does not reliably trigger Angular
        // to render the Admin Accounts section — JS click works regardless of
        // viewport position in both headless and non-headless mode
        ((JavascriptExecutor) driver).executeScript(
                "document.querySelector(" +
                        "'div.section.create-admin-section div.section-header button.btn-primary'" +
                        ").click();"
        );
    }

    public void fillAddAdminForm(String name, String email, String password, String phone) {
        type(ADD_ADMIN_FORM_NAME,     name);
        type(ADD_ADMIN_FORM_EMAIL,    email);
        type(ADD_ADMIN_FORM_PASSWORD, password);
        if (phone != null && !phone.isEmpty()) type(ADD_ADMIN_FORM_PHONE, phone);
    }

    public void submitAddAdminForm() {
        // FIX: use JS click — same reason as clickAddAdmin()
        // scrollAndClick doesn't reliably trigger form submission in headless mode
        ((JavascriptExecutor) driver).executeScript(
                "document.querySelector('div.create-admin-form button.btn-primary').click();"
        );
    }

    public void cancelAddAdminForm() {
        scrollAndClick(driver.findElement(ADD_ADMIN_CANCEL));
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private boolean isVisible(By by) {
        try { return driver.findElement(by).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    private WebElement firstOf(By by, String label) {
        List<WebElement> els = driver.findElements(by);
        if (els.isEmpty()) throw new IllegalStateException("No '" + label + "' element visible");
        return els.get(0);
    }

    private void scrollAndClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", el);
    }
}