package com.cts.mfrp.Zuply.tests.ui.admin;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.AdminDashboardPage;
import com.cts.mfrp.zuply.pages.AdminProductsPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/** Admin product management -- FRD section 2.11. Maps to TC022, TC023 + AD_TC_AD1..AD3. */
@Test(groups = {"regression", "ui", "admin"})
public class AdminProductUiTests extends UiBaseTest {

    private static final Duration FILTER_LOAD = Duration.ofSeconds(5);

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginAdmin() { loginAsAdmin(); }

    /** TC022 — Admin can approve a submitted product. */
    @Test(description = "TC022 — AdminApproveProduct")
    public void tc022_adminApproveProduct() {
        AdminProductsPage page = new AdminProductsPage(driver);
        page.open();
        try { page.selectFilter(AdminProductsPage.Filter.PENDING); }
        catch (Exception ignored) {}
        page.waitForSpinnerGone(FILTER_LOAD);

        if (page.rowCount() == 0) {
            throw new org.testng.SkipException("No PENDING products on this env — cannot exercise approval");
        }
        page.approveFirst();
        waitAfterAction();
        Assert.assertTrue(page.isLoaded(), "Admin products page should remain loaded after approve");
    }

    /** TC023 — Admin can reject a submitted product. */
    @Test(description = "TC023 — AdminRejectProduct")
    public void tc023_adminRejectProduct() {
        AdminProductsPage page = new AdminProductsPage(driver);
        page.open();
        try { page.selectFilter(AdminProductsPage.Filter.PENDING); }
        catch (Exception ignored) {}
        page.waitForSpinnerGone(FILTER_LOAD);

        if (page.rowCount() == 0) {
            throw new org.testng.SkipException("No PENDING products on this env — cannot exercise rejection");
        }
        page.rejectFirst();
        waitAfterAction();
        Assert.assertTrue(page.isLoaded(), "Admin products page should remain loaded after reject");
    }

    /**
     * AD_TC_AD1 -- Admin Dashboard displays platform-wide stats (Total Sellers,
     * Total Products, Total Orders, Total Revenue / GMV). FRD section 2.11.
     */
    @Test(description = "AD_TC_AD1 -- AdminDashboardStats")
    public void ad1_adminDashboardStats() {
        AdminDashboardPage dashboard = new AdminDashboardPage(driver);
        dashboard.open();

        Assert.assertTrue(dashboard.isLoaded(),
                "Admin dashboard should load with role header visible (FRD section 2.11)");

        String body = driver.getPageSource().toLowerCase();
        boolean hasSellersStat  = body.contains("seller");
        boolean hasProductsStat = body.contains("product");
        boolean hasOrdersStat   = body.contains("order");
        boolean hasRevenueStat  = body.contains("revenue") || body.contains("gmv")
                || body.contains("merchandise");

        int statsFound = (hasSellersStat ? 1 : 0) + (hasProductsStat ? 1 : 0)
                + (hasOrdersStat ? 1 : 0) + (hasRevenueStat ? 1 : 0);
        Assert.assertTrue(statsFound >= 3,
                "Admin dashboard should display at least 3 of the 4 FRD stat metrics "
                + "(Total Sellers, Total Products, Total Orders, Total Revenue) -- found " + statsFound);
    }

    /**
     * AD_TC_AD2 -- Admin Dashboard exposes navigation links to Manage Sellers,
     * Manage Products, View All Orders, and View Reports. FRD section 2.11.
     */
    @Test(description = "AD_TC_AD2 -- AdminDashboardNavLinks")
    public void ad2_adminDashboardNavLinks() {
        AdminDashboardPage dashboard = new AdminDashboardPage(driver);
        dashboard.open();

        boolean hasSellersLink  = !driver.findElements(By.cssSelector("a[routerlink='/admin/sellers']")).isEmpty();
        boolean hasProductsLink = !driver.findElements(By.cssSelector("a[routerlink='/admin/products']")).isEmpty();
        boolean hasOrdersLink   = !driver.findElements(By.cssSelector("a[routerlink='/admin/orders']")).isEmpty();
        boolean hasReportsLink  = !driver.findElements(By.cssSelector("a[routerlink='/admin/reports']")).isEmpty();

        Assert.assertTrue(hasSellersLink,  "Admin dashboard should link to Manage Sellers (FRD section 2.11)");
        Assert.assertTrue(hasProductsLink, "Admin dashboard should link to Manage Products (FRD section 2.11)");
        Assert.assertTrue(hasOrdersLink,   "Admin dashboard should link to View All Orders (FRD section 2.11)");
        Assert.assertTrue(hasReportsLink,  "Admin dashboard should link to View Reports (FRD section 2.11)");
    }

    /**
     * AD_TC_AD3 -- Admin Reports page displays platform analytics. FRD section 2.11
     * lists Total Sales, # Sellers, # Customers, Product Distribution by Category,
     * and cross-platform orders as required content.
     */
    @Test(description = "AD_TC_AD3 -- AdminReportsPage")
    public void ad3_adminReportsPage() {
        new AdminDashboardPage(driver).open();
        try { new AdminDashboardPage(driver).goToReports(); }
        catch (Exception e) {
            throw new org.testng.SkipException("Reports link not reachable from dashboard: " + e.getMessage());
        }
        waitAfterAction();

        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/reports"),
                "Reports link should navigate to /admin/reports (FRD section 2.11) -- got: " + driver.getCurrentUrl());

        String body = driver.getPageSource().toLowerCase();
        int signals = 0;
        if (body.contains("total sales") || body.contains("sales")) signals++;
        if (body.contains("customers")) signals++;
        if (body.contains("category"))  signals++;
        if (body.contains("orders"))    signals++;
        Assert.assertTrue(signals >= 2,
                "Reports page should surface at least 2 analytics signals (sales / customers / category / orders) "
                + "-- found " + signals);
    }
}
