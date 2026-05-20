package com.cts.mfrp.Zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Admin landing page at {@code /admin/dashboard}. */
public class AdminDashboardPage extends BasePage {

    private static final By ROLE_HEADER     = By.cssSelector(".dh-role");
    private static final By NAME_HEADER     = By.cssSelector(".dh-name");
    private static final By SELLERS_LINK    = By.cssSelector("a[routerlink='/admin/sellers']");
    private static final By PRODUCTS_LINK   = By.cssSelector("a[routerlink='/admin/products']");
    private static final By ORDERS_LINK     = By.cssSelector("a[routerlink='/admin/orders']");
    private static final By REPORTS_LINK    = By.cssSelector("a[routerlink='/admin/reports']");
    private static final By PLATFORM_SECTION = By.xpath("//div[@class='mm-section-label' and normalize-space()='Platform Management']");

    public AdminDashboardPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/admin/dashboard"; }
    @Override protected By readyMarker() { return ROLE_HEADER; }

    public String adminName() { return text(NAME_HEADER); }
    public String roleLabel() { return text(ROLE_HEADER); }

    public void goToSellers()  { click(SELLERS_LINK); }
    public void goToProducts() { click(PRODUCTS_LINK); }
    public void goToOrders()   { click(ORDERS_LINK); }
    public void goToReports()  { click(REPORTS_LINK); }
}
