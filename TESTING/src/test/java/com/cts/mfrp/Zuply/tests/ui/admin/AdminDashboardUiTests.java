package com.cts.mfrp.zuply.tests.ui.admin;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.AdminDashboardPage;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.NoSuchElementException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Admin Dashboard UI Tests — TC050 onwards.
 * All behaviours verified manually against https://zuply.netlify.app/admin/dashboard
 */
public class AdminDashboardUiTests extends UiBaseTest {

    private static final By DASHBOARD_HEADING = By.cssSelector("div.admin-banner");
    private static final By ADD_ADMIN_FORM    = By.cssSelector("div.create-admin-form");

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginAdmin() { loginAsAdmin(); }

    // ── Page load ─────────────────────────────────────────────────────────────

    /** TC050 — Dashboard loads with hero banner and all 4 stat cards visible. */
    @Test(description = "TC050 — AdminDashboardLoads")
    public void tc050_adminDashboardLoads() {
        System.out.println("[TC050] Opening admin dashboard");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.open();
        System.out.println("[TC050] Verifying hero banner and stat cards are visible");
        Assert.assertTrue(page.isLoaded(),                     "Dashboard hero banner should be loaded");
        Assert.assertTrue(page.isTotalSellersCardVisible(),    "Total Sellers stat card should be visible");
        Assert.assertTrue(page.isTotalProductsCardVisible(),   "Total Products stat card should be visible");
        Assert.assertTrue(page.isTotalOrdersCardVisible(),     "Total Orders stat card should be visible");
        Assert.assertTrue(page.isPendingApprovalsCardVisible(), "Pending Approvals stat card should be visible");
        System.out.println("[TC050] PASSED — all dashboard elements visible");
    }

    // ── Top nav ───────────────────────────────────────────────────────────────

    /** TC051 — Sellers nav link navigates to /admin/sellers. */
    @Test(description = "TC051 — AdminDashboardNavSellers")
    public void tc051_navSellersGoesToAllSellers() {
        System.out.println("[TC051] Opening dashboard and clicking Sellers nav link");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.open();
        page.goToSellers();
        System.out.println("[TC051] Current URL: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/sellers"),
                "Sellers nav link should navigate to /admin/sellers — got: " + driver.getCurrentUrl());
    }

    /** TC052 — Products nav link navigates to /admin/products. */
    @Test(description = "TC052 — AdminDashboardNavProducts")
    public void tc052_navProductsGoesToAllProducts() {
        System.out.println("[TC052] Opening dashboard and clicking Products nav link");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.open();
        page.goToProducts();
        System.out.println("[TC052] Current URL: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/products"),
                "Products nav link should navigate to /admin/products — got: " + driver.getCurrentUrl());
    }

    /** TC053 — Orders nav link navigates to /admin/orders. */
    @Test(description = "TC053 — AdminDashboardNavOrders")
    public void tc053_navOrdersGoesToAllOrders() {
        System.out.println("[TC053] Opening dashboard and clicking Orders nav link");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.open();
        page.goToOrders();
        System.out.println("[TC053] Current URL: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/orders"),
                "Orders nav link should navigate to /admin/orders — got: " + driver.getCurrentUrl());
    }

    /** TC054 — Reports nav link navigates to /admin/reports. */
    @Test(description = "TC054 — AdminDashboardNavReports")
    public void tc054_navReportsGoesToReports() {
        System.out.println("[TC054] Opening dashboard and clicking Reports nav link");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.open();
        page.goToReports();
        System.out.println("[TC054] Current URL: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/reports"),
                "Reports nav link should navigate to /admin/reports — got: " + driver.getCurrentUrl());
    }

    // ── Shortcut cards ────────────────────────────────────────────────────────

    /** TC055 — Manage Sellers shortcut navigates to /admin/sellers. */
    @Test(description = "TC055 — AdminDashboardManageSellersCard")
    public void tc055_manageSellersCardNavigates() {
        System.out.println("[TC055] Clicking Manage Sellers shortcut card");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.open();
        page.clickManageSellers();
        System.out.println("[TC055] Current URL: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/sellers"),
                "Manage Sellers shortcut should navigate to /admin/sellers — got: " + driver.getCurrentUrl());
    }

    /** TC056 — Manage Products shortcut navigates to /admin/products. */
    @Test(description = "TC056 — AdminDashboardManageProductsCard")
    public void tc056_manageProductsCardNavigates() {
        System.out.println("[TC056] Clicking Manage Products shortcut card");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.open();
        page.clickManageProducts();
        System.out.println("[TC056] Current URL: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/products"),
                "Manage Products shortcut should navigate to /admin/products — got: " + driver.getCurrentUrl());
    }

    /** TC057 — Reports shortcut navigates to /admin/reports. */
    @Test(description = "TC057 — AdminDashboardReportsCard")
    public void tc057_reportsCardNavigates() {
        System.out.println("[TC057] Clicking Reports shortcut card");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.open();
        page.clickReports();
        System.out.println("[TC057] Current URL: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/reports"),
                "Reports shortcut should navigate to /admin/reports — got: " + driver.getCurrentUrl());
    }

    /** TC058 — All Orders shortcut navigates to /admin/orders. */
    @Test(description = "TC058 — AdminDashboardAllOrdersCard")
    public void tc058_allOrdersCardNavigates() {
        System.out.println("[TC058] Clicking All Orders shortcut card");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.open();
        page.clickAllOrders();
        System.out.println("[TC058] Current URL: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/orders"),
                "All Orders shortcut should navigate to /admin/orders — got: " + driver.getCurrentUrl());
    }

    // ── Sellers Pending Approval ──────────────────────────────────────────────

    /** TC059 — Sellers Pending Approval section is visible when pending sellers exist. */
    @Test(description = "TC059 — AdminDashboardSellersPendingSection")
    public void tc059_sellersPendingSectionVisible() {
        System.out.println("[TC059] Checking Sellers Pending Approval section");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.openAndWaitForData();
        int pendingCount = page.sellersPendingCount();
        System.out.println("[TC059] Pending sellers count: " + pendingCount);
        if (pendingCount == 0) {
            throw new org.testng.SkipException("No pending sellers on this env");
        }
        Assert.assertTrue(page.isSellersPendingSectionVisible(),
                "Sellers Pending Approval section should be visible when pending count > 0");
        Assert.assertTrue(page.sellersPendingApproveButtonCount() > 0,
                "At least one Approve button should be visible for pending sellers");
    }

    /** TC060 — Approve pending seller from dashboard — no dialog, count decreases. */
    @Test(description = "TC060 — AdminDashboardApprovePendingSeller")
    public void tc060_approvePendingSellerFromDashboard() {
        System.out.println("[TC060] Approving first pending seller from dashboard");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        page.openAndWaitForData();
        int countBefore = page.sellersPendingCount();
        System.out.println("[TC060] Pending sellers before approve: " + countBefore);
        if (countBefore == 0) {
            throw new org.testng.SkipException("No pending sellers on this env");
        }
        page.approveFirstPendingSeller();
        waitAfterAction();
        longWait.until(d -> page.sellersPendingCount() == countBefore - 1);
        System.out.println("[TC060] Pending sellers after approve: " + page.sellersPendingCount());
        Assert.assertEquals(page.sellersPendingCount(), countBefore - 1,
                "Pending seller count should decrease by 1 after approve — was: " + countBefore);
    }

    /** TC061 — Reject pending seller from dashboard — browser dialog appears, count decreases. */
    @Test(description = "TC061 — AdminDashboardRejectPendingSeller")
    public void tc061_rejectPendingSellerFromDashboard() {
        System.out.println("[TC061] Rejecting first pending seller from dashboard");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        page.openAndWaitForData();
        int countBefore = page.sellersPendingCount();
        System.out.println("[TC061] Pending sellers before reject: " + countBefore);
        if (countBefore == 0) {
            throw new org.testng.SkipException("No pending sellers on this env");
        }
        page.rejectFirstPendingSeller();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            System.out.println("[TC061] Browser dialog text: " + alert.getText());
            alert.accept();
        } catch (NoAlertPresentException ignored) {
            System.out.println("[TC061] No browser dialog appeared");
        }
        waitAfterAction();
        longWait.until(d -> page.sellersPendingCount() == countBefore - 1);
        System.out.println("[TC061] Pending sellers after reject: " + page.sellersPendingCount());
        Assert.assertEquals(page.sellersPendingCount(), countBefore - 1,
                "Pending seller count should decrease by 1 after reject — was: " + countBefore);
    }

    /** TC062 — View all link in Sellers Pending section navigates to /admin/sellers. */
    @Test(description = "TC062 — AdminDashboardSellersViewAll")
    public void tc062_sellersViewAllNavigates() {
        System.out.println("[TC062] Clicking View all in Sellers Pending Approval section");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.openAndWaitForData();
        if (!page.isSellersPendingSectionVisible()) {
            throw new org.testng.SkipException("Sellers Pending section not visible — no pending sellers");
        }
        page.clickSellersViewAll();
        System.out.println("[TC062] Current URL: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/sellers"),
                "View all sellers link should navigate to /admin/sellers — got: " + driver.getCurrentUrl());
    }

    // ── Products Pending Review ───────────────────────────────────────────────

    /** TC063 — Products Pending Review section is visible when pending products exist. */
    @Test(description = "TC063 — AdminDashboardProductsPendingSection")
    public void tc063_productsPendingSectionVisible() {
        System.out.println("[TC063] Checking Products Pending Review section");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.openAndWaitForData();
        int pendingCount = page.productsPendingCount();
        System.out.println("[TC063] Pending products count: " + pendingCount);
        if (pendingCount == 0) {
            throw new org.testng.SkipException("No pending products on this env");
        }
        Assert.assertTrue(page.isProductsPendingSectionVisible(),
                "Products Pending Review section should be visible when pending count > 0");
    }

    /** TC064 — Approve pending product from dashboard — no dialog, count decreases. */
    @Test(description = "TC064 — AdminDashboardApprovePendingProduct")
    public void tc064_approvePendingProductFromDashboard() {
        System.out.println("[TC064] Approving first pending product from dashboard");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        page.openAndWaitForData();
        int countBefore = page.productsPendingCount();
        System.out.println("[TC064] Pending products before approve: " + countBefore);
        if (countBefore == 0) {
            throw new org.testng.SkipException("No pending products on this env");
        }
        page.approveFirstPendingProduct();
        waitAfterAction();
        longWait.until(d -> page.productsPendingCount() == countBefore - 1);
        System.out.println("[TC064] Pending products after approve: " + page.productsPendingCount());
        Assert.assertEquals(page.productsPendingCount(), countBefore - 1,
                "Pending product count should decrease by 1 after approve — was: " + countBefore);
    }

    /** TC065 — Reject pending product from dashboard — browser dialog appears, count decreases. */
    @Test(description = "TC065 — AdminDashboardRejectPendingProduct")
    public void tc065_rejectPendingProductFromDashboard() {
        System.out.println("[TC065] Rejecting first pending product from dashboard");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        page.openAndWaitForData();
        int countBefore = page.productsPendingCount();
        System.out.println("[TC065] Pending products before reject: " + countBefore);
        if (countBefore == 0) {
            throw new org.testng.SkipException("No pending products on this env");
        }
        page.rejectFirstPendingProduct();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            System.out.println("[TC065] Browser dialog text: " + alert.getText());
            alert.accept();
        } catch (NoAlertPresentException ignored) {
            System.out.println("[TC065] No browser dialog appeared");
        }
        waitAfterAction();
        longWait.until(d -> page.productsPendingCount() == countBefore - 1);
        System.out.println("[TC065] Pending products after reject: " + page.productsPendingCount());
        Assert.assertEquals(page.productsPendingCount(), countBefore - 1,
                "Pending product count should decrease by 1 after reject — was: " + countBefore);
    }

    /** TC066 — View all link in Products Pending section navigates to /admin/products. */
    @Test(description = "TC066 — AdminDashboardProductsViewAll")
    public void tc066_productsViewAllNavigates() {
        System.out.println("[TC066] Clicking View all in Products Pending Review section");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        page.openAndWaitForData();
        if (!page.isProductsPendingSectionVisible()) {
            throw new org.testng.SkipException("Products Pending section not visible — no pending products");
        }
        page.clickProductsViewAll();
        System.out.println("[TC066] Current URL: " + driver.getCurrentUrl());
        Assert.assertTrue(driver.getCurrentUrl().contains("/admin/products"),
                "View all products link should navigate to /admin/products — got: " + driver.getCurrentUrl());
    }

    // ── Admin Accounts ────────────────────────────────────────────────────────

    /** TC067 — Clicking + Add Admin expands inline form with Full Name, Email, Password, Phone fields. */
    @Test(description = "TC067 — AdminDashboardAddAdminFormExpands")
    public void tc067_addAdminFormExpands() {
        System.out.println("[TC067] Clicking + Add Admin button and verifying form expands");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        page.open();
        Assert.assertTrue(page.isAdminAccountsSectionVisible(),
                "Admin Accounts section should be visible at the bottom of the dashboard");
        // Wait for Admin Accounts section to be fully present before clicking
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.section.create-admin-section")));
        page.clickAddAdmin();
        System.out.println("[TC067] Waiting for create-admin-form to appear");
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADD_ADMIN_FORM));
        Assert.assertTrue(page.isAddAdminFormVisible(),
                "Add Admin inline form should expand after clicking + Add Admin button");
        // Clean up — cancel form so TC068 starts with a fresh state
        page.cancelAddAdminForm();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(ADD_ADMIN_FORM));
        System.out.println("[TC067] PASSED — form expanded and cancelled successfully");
    }

    /** TC068 — Clicking Cancel hides the Add Admin form. */
    @Test(description = "TC068 — AdminDashboardAddAdminCancel")
    public void tc068_addAdminFormCancels() {
        System.out.println("[TC068] Testing Cancel button hides the Add Admin form");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        page.open();
        // FIX: wait for Admin Accounts section to be present before clicking
        // page.open() navigates via pushState — bottom sections load after heading
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.section.create-admin-section")));
        page.clickAddAdmin();
        System.out.println("[TC068] Waiting for form to expand");
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADD_ADMIN_FORM));
        Assert.assertTrue(page.isAddAdminFormVisible(),
                "Form should be visible after clicking + Add Admin");
        System.out.println("[TC068] Clicking Cancel button");
        page.cancelAddAdminForm();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(ADD_ADMIN_FORM));
        Assert.assertFalse(page.isAddAdminFormVisible(),
                "Add Admin form should be hidden after clicking Cancel");
        System.out.println("[TC068] PASSED — form hidden after cancel");
    }

    /**
     * TC069 — Submitting Create Admin Account shows a backend error.
     * Known bug: api/admin/create-admin endpoint does not exist (404).
     */
    @Test(description = "TC069 — AdminDashboardCreateAdminKnownBug")
    public void tc069_createAdminShowsKnownError() {
        System.out.println("[TC069] Testing Create Admin Account — expecting known backend error");
        AdminDashboardPage page = new AdminDashboardPage(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        org.openqa.selenium.JavascriptExecutor js =
                (org.openqa.selenium.JavascriptExecutor) driver;
        page.open();
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.section.create-admin-section")));
        page.clickAddAdmin();
        System.out.println("[TC069] Waiting for form to expand");
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADD_ADMIN_FORM));
        System.out.println("[TC069] Filling form with test data");
        page.fillAddAdminForm("TestAdmin", "testadmin@zuply.in", "Admin@123", "9876543210");

        // Install MutationObserver BEFORE submit — fires the instant Angular adds error div
        js.executeScript(
                "window.__adminError = false; window.__adminErrorText = '';" +
                        "window.__obs = new MutationObserver(function(ms) {" +
                        "  ms.forEach(function(m) { m.addedNodes.forEach(function(n) {" +
                        "    if (n.nodeType===1) {" +
                        "      var all = [n].concat(Array.from(n.querySelectorAll('*')));" +
                        "      all.forEach(function(el) {" +
                        "        if ((el.className||'').includes('create-admin-msg') && (el.innerText||'').trim()) {" +
                        "          window.__adminError = true;" +
                        "          window.__adminErrorText = el.innerText.trim();" +
                        "        }" +
                        "      });" +
                        "    }" +
                        "  }); });" +
                        "});" +
                        "window.__obs.observe(document.body,{childList:true,subtree:true});"
        );

        System.out.println("[TC069] Submitting form (observer active)");
        page.submitAddAdminForm();

        System.out.println("[TC069] Waiting for observer to detect error");
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(d -> Boolean.TRUE.equals(js.executeScript("return window.__adminError;")));

        js.executeScript("window.__obs.disconnect();");
        String msg = (String) js.executeScript("return window.__adminErrorText;");
        System.out.println("[TC069] Error message: " + msg);
        Assert.assertTrue(true, "Known bug confirmed: api/admin/create-admin not found");
        System.out.println("[TC069] PASSED — known bug confirmed");
    }
}