package com.cts.mfrp.zuply.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.openqa.selenium.WebDriver;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExtentReportListener implements ITestListener, ISuiteListener {

    private final ExtentReports extent = ExtentManager.get();

    @Override
    public void onStart(ITestContext context) {}

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getTestClass().getRealClass().getSimpleName()
                + " :: " + result.getMethod().getMethodName();
        ExtentTest test = extent.createTest(testName, result.getMethod().getDescription());

        // Assign category based on package (api vs ui) and groups
        String pkg = result.getTestClass().getRealClass().getPackage().getName();
        if (pkg.contains(".ui.")) {
            test.assignCategory("UI");
        } else if (pkg.contains(".api.")) {
            test.assignCategory("API");
        }
        String[] groups = result.getMethod().getGroups();
        for (String g : groups) {
            test.assignCategory(g);
        }

        ExtentManager.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (ExtentManager.test() != null) {
            ExtentManager.test().pass("Passed");
        }
        ExtentManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentManager.test();
        if (test != null) {
            test.fail(result.getThrowable());
            // Capture screenshot for UI tests (driver is stored in DriverFactory ThreadLocal by UiBaseTest)
            WebDriver driver = DriverFactory.current();
            if (driver != null) {
                try {
                    String base64 = ScreenshotUtils.captureBase64(driver);
                    test.fail("Failure screenshot",
                            MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
                } catch (Exception e) {
                    test.warning("Could not capture screenshot: " + e.getMessage());
                }
            }
        }
        ExtentManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (ExtentManager.test() != null) {
            String reason = result.getThrowable() != null
                    ? result.getThrowable().getMessage()
                    : "Skipped — dependency failed or SkipException thrown";
            ExtentManager.test().skip(reason);
        }
        ExtentManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
}
