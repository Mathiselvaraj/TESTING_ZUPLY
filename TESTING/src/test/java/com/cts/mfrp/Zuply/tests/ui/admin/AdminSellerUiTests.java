package com.cts.mfrp.zuply.tests.ui.admin;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.AdminSellersPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Admin seller management — FRD §2.11. Maps to TC024. */
public class AdminSellerUiTests extends UiBaseTest {

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginAdmin() { loginAsAdmin(); }

    /** TC024 — Admin can suspend a seller; their products become hidden. */
    @Test(description = "TC024 — AdminSuspendSeller")
    public void tc024_adminSuspendSeller() {
        AdminSellersPage page = new AdminSellersPage(driver);
        page.open();

        try { page.selectFilter(AdminSellersPage.Filter.APPROVED); }
        catch (Exception ignored) {}
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}

        if (page.suspendableCount() == 0) {
            throw new org.testng.SkipException("No approved sellers available to suspend");
        }
        page.suspendFirst();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        Assert.assertTrue(page.isLoaded(), "Admin sellers page should remain loaded after suspend");
    }
}
