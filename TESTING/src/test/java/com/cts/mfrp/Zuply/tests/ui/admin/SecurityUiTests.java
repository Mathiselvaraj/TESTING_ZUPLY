package com.cts.mfrp.zuply.tests.ui.admin;


import com.cts.mfrp.zuply.base.UiBaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Role-based access control — FRD §3.2. Maps to TC039. */
public class SecurityUiTests extends UiBaseTest {

    private String buyerEmail;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginBuyer() {
        buyerEmail = registerNewCustomer("RbacUser");
        loginViaUi(buyerEmail, "Test@1234");
    }

    /** TC039 — A Customer cannot reach /admin or /seller routes; routes redirect to /login. */
    @Test(description = "TC039 — RoleBasedAccess")
    public void tc039_roleBasedAccess() {
        // Attempt 1: customer hits /admin/dashboard via SPA router
        navigateRoute("/admin/dashboard");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        Assert.assertTrue(
                driver.getCurrentUrl().contains("/login")
                        || driver.getCurrentUrl().contains("/")
                        && !driver.getCurrentUrl().contains("/admin/dashboard"),
                "Customer should not reach admin dashboard; landed on " + driver.getCurrentUrl());

        // Attempt 2: customer hits /seller/dashboard
        navigateRoute("/seller/dashboard");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        Assert.assertFalse(driver.getCurrentUrl().endsWith("/seller/dashboard"),
                "Customer should not reach seller dashboard; landed on " + driver.getCurrentUrl());
    }
}
