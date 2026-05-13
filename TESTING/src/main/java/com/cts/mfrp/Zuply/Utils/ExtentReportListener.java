package com.cts.mfrp.Zuply.Utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ExtentReportListener implements ITestListener, ISuiteListener {

    private final ExtentReports extent = ExtentManager.get();

    @Override
    public void onStart(ITestContext context) {
        // ensure singleton initialized
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(
                result.getTestClass().getRealClass().getSimpleName() + " :: " + result.getMethod().getMethodName(),
                result.getMethod().getDescription());
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
        if (ExtentManager.test() != null) {
            ExtentManager.test().fail(result.getThrowable());
        }
        ExtentManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (ExtentManager.test() != null) {
            ExtentManager.test().skip(result.getThrowable() != null ? result.getThrowable().getMessage() : "Skipped");
        }
        ExtentManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }
}
