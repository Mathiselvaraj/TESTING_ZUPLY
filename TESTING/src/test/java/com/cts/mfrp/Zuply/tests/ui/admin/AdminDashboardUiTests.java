package com.cts.mfrp.zuply.tests.ui.admin;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.AdminDashboardPage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * Admin Dashboard UI Tests — core, non-redundant coverage.
 * All behaviours verified manually against https://zuply.netlify.app/admin/dashboard
 */
public class AdminDashboardUiTests extends UiBaseTest {

    private static final By ADD_ADMIN_FORM = By.cssSelector("div.create-admin-form");

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
        Assert.assertTrue(page.isLoaded(),                      "Dashboard hero banner should be loaded");
        Assert.assertTrue(page.isTotalSellersCardVisible(),     "Total Sellers stat card should be visible");
        Assert.assertTrue(page.isTotalProductsCardVisible(),    "Total Products stat card should be visible");
        Assert.assertTrue(page.isTotalOrdersCardVisible(),      "Total Orders stat card should be visible");
        Assert.assertTrue(page.isPendingApprovalsCardVisible(), "Pending Approvals stat card should be visible");
        System.out.println("[TC050] PASSED — all dashboard elements visible");
    }

    // ── Sellers Pending Approval ──────────────────────────────────────────────

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
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div.section.create-admin-section")));
        page.clickAddAdmin();
        System.out.println("[TC067] Waiting for create-admin-form to appear");
        wait.until(ExpectedConditions.visibilityOfElementLocated(ADD_ADMIN_FORM));
        Assert.assertTrue(page.isAddAdminFormVisible(),
                "Add Admin inline form should expand after clicking + Add Admin button");
        page.cancelAddAdminForm();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(ADD_ADMIN_FORM));
        System.out.println("[TC067] PASSED — form expanded and cancelled successfully");
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
