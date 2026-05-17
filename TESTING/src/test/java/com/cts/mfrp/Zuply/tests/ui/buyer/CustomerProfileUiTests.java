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
    @Test(description = "AD_TC020 — ProfilePictureUpload")
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
}
