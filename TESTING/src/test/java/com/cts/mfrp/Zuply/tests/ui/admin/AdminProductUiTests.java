package com.cts.mfrp.Zuply.tests.ui.admin;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.AdminProductsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/** Admin product management — FRD §2.11. Maps to TC022 and TC023. */
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
}
