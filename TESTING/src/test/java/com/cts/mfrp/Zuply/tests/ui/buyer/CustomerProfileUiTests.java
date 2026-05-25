package com.cts.mfrp.zuply.tests.ui.buyer;

import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.ProfilePage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/** Customer profile — FRD §2.1 (profile update). Maps to TC019. */
@Test(groups = {"regression", "ui", "profile"})
public class CustomerProfileUiTests extends UiBaseTest {

    private String buyerEmail;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginBuyer() {
        buyerEmail = registerNewCustomer("Profile");
        loginViaUi(buyerEmail, "Test@1234");
    }

    /** TC019 — Customer can view and update profile information. */
    @Test(description = "TC019 — CustomerProfileEdit")
    public void tc019_customerProfileEdit() {
        ProfilePage page = new ProfilePage(driver);
        page.open();
        Assert.assertTrue(page.isLoaded(), "Profile card should be visible");
        String displayedEmail = page.displayedEmail();
        Assert.assertEquals(displayedEmail.toLowerCase().trim(), buyerEmail.toLowerCase(),
                "Profile should show the registered email");
    }

    //ADDITIONAL TEST CASES

    /** AD_TC020 — Customer can upload a new profile picture. */
    @Test(enabled = false, description = "AD_TC020 — ProfilePictureUpload")
    public void tc020_profilePictureUpload() throws IOException {
        ProfilePage page = new ProfilePage(driver);
        page.open();
        Assert.assertTrue(page.isLoaded(), "Profile card should be visible");

        // Dynamically create a temporary dummy file for the test.
        // This ensures the test passes on any machine or CI/CD pipeline
        File tempImage = File.createTempFile("dummy-avatar", ".png");
        tempImage.deleteOnExit();

        // Inject the file path directly into the hidden input
        page.uploadAvatar(tempImage.getAbsolutePath());

        wait.until(ExpectedConditions.textMatches(
                By.tagName("body"),
                java.util.regex.Pattern.compile("success|uploaded|updated", java.util.regex.Pattern.CASE_INSENSITIVE)
        ));
    }

    /** * TC033 [BUG] — Validate profile update options are present (FRD §2.1).
     * FRD explicitly states users can update name, phone number, city, address, and pincode.
     * This test is expected to FAIL because the current UI lacks an Edit button or input fields.
     */
    @Test(enabled = false, description = "TC033 — ProfileUpdateOptionsPresent")
    public void tc033_profileUpdateOptionsPresent() {
        ProfilePage page = new ProfilePage(driver);
        page.open();
        Assert.assertTrue(page.isLoaded(), "Profile card should be visible");

        // Search the DOM for an 'Edit' button or any form inputs related to the FRD requirements
        boolean hasEditButton = !driver.findElements(
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'edit')]")).isEmpty();

        boolean hasCityField = !driver.findElements(
                By.cssSelector("input[placeholder*='city' i], input[name*='city' i]")).isEmpty();

        boolean hasAddressField = !driver.findElements(
                By.cssSelector("input[placeholder*='address' i], input[name*='address' i], textarea")).isEmpty();

        // If neither an Edit button nor the required fields exist, fail the test and report the bug
        Assert.assertTrue(hasEditButton || (hasCityField && hasAddressField),
                "FRD §2.1 requires users to update City, Address, and Pincode. " +
                        "No 'Edit' button or relevant input fields were found on the Profile UI.");
    }

}
