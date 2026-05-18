package com.cts.mfrp.zuply.tests.ui.admin;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.AdminSellersPage;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

/** Admin seller management — FRD §2.11. Maps to TC024. */
@Test(groups = {"regression", "ui", "admin"})
public class AdminSellerUiTests extends UiBaseTest {

    private static final By SELLERS_HEADING =
            By.xpath("//h1[contains(normalize-space(),'Manage Sellers')]");

    private static final By SELLER_CARDS = By.cssSelector("div.sellers-table > div");
    private static final By LOADING_SPINNER = By.cssSelector("app-loading-spinner");

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginAdmin() { loginAsAdmin(); }

    /** TC024 — Admin can suspend a seller; page updates automatically after confirmation. */
    @Test(description = "TC024 — AdminSuspendSeller")
    public void tc024_adminSuspendSeller() {
        AdminSellersPage page = new AdminSellersPage(driver);

        WebDriverWait wait     = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Step 1 — open page
        page.open();
        wait.until(ExpectedConditions.visibilityOfElementLocated(SELLERS_HEADING));

        // Step 2 — click Approved filter and wait for heading
        try { page.selectFilter(AdminSellersPage.Filter.APPROVED); }
        catch (Exception ignored) {}
        wait.until(ExpectedConditions.visibilityOfElementLocated(SELLERS_HEADING));

        // Step 3 — wait for loading spinner to disappear
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_SPINNER)); }
        catch (Exception ignored) {}

        // Step 4 — wait for at least one seller card to appear
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(SELLER_CARDS, 0));

        // Step 5 — skip if no suspend buttons visible
        if (page.suspendableCount() == 0) {
            throw new org.testng.SkipException(
                    "No approved sellers available to suspend on this env");
        }

        // Step 6 — record suspend button count as proxy for approved seller count
        // FIX: Using suspendableCount() instead of rowCount() because
        // div.sellers-table > div matches more elements than just seller cards.
        // Each approved seller has exactly 1 Suspend button — reliable 1:1 mapping.
        int approvedBefore = page.suspendableCount();
        System.out.println("Suspend buttons before: " + approvedBefore);

        // Step 7 — click Suspend on first seller
        page.suspendFirst();

        // Step 8 — handle browser confirmation dialog
        // UI confirmed: "Suspend this seller?" native browser dialog appears
        try {
            WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
            System.out.println("Confirmation dialog text: " + alert.getText());
            alert.accept();
        } catch (NoAlertPresentException e) {
            System.out.println("No confirmation dialog appeared after suspend click");
        }

        // Step 9 — wait for spinner after suspend API call
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_SPINNER)); }
        catch (Exception ignored) {}

        // Step 10 — wait for suspend button count to decrease by 1
        // UI confirmed: page updates automatically but takes a few seconds
        longWait.until(d -> page.suspendableCount() == approvedBefore - 1);

        // Step 11 — assert page is still loaded
        Assert.assertTrue(page.isLoaded(),
                "Admin sellers page should remain loaded after suspend");

        // Step 12 — assert suspend button count decreased by 1
        Assert.assertEquals(page.suspendableCount(), approvedBefore - 1,
                "Approved seller count should decrease by 1 after suspend");

        // Step 13 — verify seller moved to Suspended tab
        page.selectFilter(AdminSellersPage.Filter.SUSPENDED);
        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_SPINNER)); }
        catch (Exception ignored) {}
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(SELLER_CARDS, 0));
        Assert.assertTrue(page.suspendableCount() == 0,
                "Suspended tab should show no Suspend buttons — sellers here get Approve instead");
        Assert.assertTrue(page.approvableCount() >= 1,
                "Suspended tab should show at least 1 Approve button after suspend action");
    }
}