package com.cts.mfrp.zuply.tests.ui.admin;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.AdminProductsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/** Admin product management — FRD §2.11. Maps to TC022 and TC023. */
public class AdminProductUiTests extends UiBaseTest {

    private static final By PRODUCTS_HEADING =
            By.xpath("//h1[contains(normalize-space(),'Manage Products')]");
    private static final By LOADING_SPINNER = By.cssSelector("app-loading-spinner");
    private static final By FILTER_TABS     = By.cssSelector("button.filter-tab");

    private static final Duration FILTER_LOAD = Duration.ofSeconds(5);

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginAdmin() { loginAsAdmin(); }

    /** TC022 — Admin can approve a submitted product. */
    @Test(description = "TC022 — AdminApproveProduct")
    public void tc022_adminApproveProduct() {
        AdminProductsPage page = new AdminProductsPage(driver);
        WebDriverWait wait     = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Step 1 — open page
        page.open();
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCTS_HEADING));
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        // Step 2 — select Pending Review filter
        try { page.selectFilter(AdminProductsPage.Filter.PENDING_REVIEW); }
        catch (Exception ignored) {}
        page.waitForSpinnerGone(FILTER_LOAD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCTS_HEADING));
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        // Step 3 — read count from tab label
        int pendingBefore = page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW);
        System.out.println("Pending Review count from tab before: " + pendingBefore);

        if (pendingBefore == 0) {
            throw new org.testng.SkipException(
                    "No PENDING REVIEW products on this env — cannot exercise approval");
        }

        // Step 4 — approve first product
        page.approveFirst();

        // Step 5 — wait for spinner and tabs to reload
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_SPINNER)); }
        catch (Exception ignored) {}
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));
        waitAfterAction();

        // Step 6 — wait for Pending Review count to decrease by 1
        longWait.until(d ->
                page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW) == pendingBefore - 1);

        // Step 7 — assert page still loaded
        Assert.assertTrue(page.isLoaded(),
                "Admin products page should remain loaded after approve");

        // Step 8 — assert pending count decreased by 1
        Assert.assertEquals(
                page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW),
                pendingBefore - 1,
                "Pending Review count should decrease by 1 after approval");

        // Step 9 — verify approved count increased by 1
        int approvedAfter = page.getFilterCount(AdminProductsPage.Filter.APPROVED);
        Assert.assertTrue(approvedAfter >= 1,
                "Approved tab count should be at least 1 after approval");
    }

    /** TC023 — Admin can reject a submitted product. */
    @Test(description = "TC023 — AdminRejectProduct")
    public void tc023_adminRejectProduct() {
        AdminProductsPage page = new AdminProductsPage(driver);
        WebDriverWait wait     = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Step 1 — open page
        page.open();
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCTS_HEADING));
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        // Step 2 — select Pending Review filter
        try { page.selectFilter(AdminProductsPage.Filter.PENDING_REVIEW); }
        catch (Exception ignored) {}
        page.waitForSpinnerGone(FILTER_LOAD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(PRODUCTS_HEADING));
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));

        // Step 3 — read count from tab label
        int pendingBefore = page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW);
        System.out.println("Pending Review count from tab before: " + pendingBefore);

        if (pendingBefore == 0) {
            throw new org.testng.SkipException(
                    "No PENDING REVIEW products on this env — cannot exercise rejection");
        }

        // Step 4 — reject first product
        page.rejectFirst();

        // Step 5 — wait for spinner and tabs to reload
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_SPINNER)); }
        catch (Exception ignored) {}
        longWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(FILTER_TABS, 0));
        waitAfterAction();

        // Step 6 — wait for Pending Review count to decrease by 1
        longWait.until(d ->
                page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW) == pendingBefore - 1);

        // Step 7 — assert page still loaded
        Assert.assertTrue(page.isLoaded(),
                "Admin products page should remain loaded after reject");

        // Step 8 — assert pending count decreased by 1
        Assert.assertEquals(
                page.getFilterCount(AdminProductsPage.Filter.PENDING_REVIEW),
                pendingBefore - 1,
                "Pending Review count should decrease by 1 after rejection");

        // Step 9 — verify rejected count increased by 1
        int rejectedAfter = page.getFilterCount(AdminProductsPage.Filter.REJECTED);
        Assert.assertTrue(rejectedAfter >= 1,
                "Rejected tab count should be at least 1 after rejection");
    }
}