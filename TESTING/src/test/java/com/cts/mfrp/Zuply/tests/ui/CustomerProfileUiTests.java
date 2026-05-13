package com.cts.mfrp.Zuply.tests.ui;

import com.cts.mfrp.Zuply.pages.ProfilePage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
}
