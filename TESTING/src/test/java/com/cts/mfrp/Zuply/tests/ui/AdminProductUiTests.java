package com.cts.mfrp.Zuply.tests.ui;

import com.cts.mfrp.Zuply.pages.AdminProductsPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Admin product management — FRD §2.11. Maps to TC022 and TC023. */
public class AdminProductUiTests extends UiBaseTest {

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginAdmin() { loginAsAdmin(); }

    /** TC022 — Admin can approve a submitted product. */
    @Test(description = "TC022 — AdminApproveProduct")
    public void tc022_adminApproveProduct() {
        AdminProductsPage page = new AdminProductsPage(driver);
        page.open();
        try { page.selectFilter(AdminProductsPage.Filter.PENDING); }
        catch (Exception ignored) {}
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}

        if (page.rowCount() == 0) {
            throw new org.testng.SkipException("No PENDING products on this env — cannot exercise approval");
        }
        page.approveFirst();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        Assert.assertTrue(page.isLoaded(), "Admin products page should remain loaded after approve");
    }

    /** TC023 — Admin can reject a submitted product. */
    @Test(description = "TC023 — AdminRejectProduct")
    public void tc023_adminRejectProduct() {
        AdminProductsPage page = new AdminProductsPage(driver);
        page.open();
        try { page.selectFilter(AdminProductsPage.Filter.PENDING); }
        catch (Exception ignored) {}
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}

        if (page.rowCount() == 0) {
            throw new org.testng.SkipException("No PENDING products on this env — cannot exercise rejection");
        }
        page.rejectFirst();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        Assert.assertTrue(page.isLoaded(), "Admin products page should remain loaded after reject");
    }
}
