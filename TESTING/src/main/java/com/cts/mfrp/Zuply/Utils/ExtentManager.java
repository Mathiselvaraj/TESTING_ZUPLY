package com.cts.mfrp.Zuply.Utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

/** Singleton ExtentReports + per-thread ExtentTest holder. */
public final class ExtentManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();

    private ExtentManager() {}

    public static synchronized ExtentReports get() {
        if (extent == null) {
            String path = ConfigReader.get("report.path");
            new File(path).getParentFile().mkdirs();
            ExtentSparkReporter spark = new ExtentSparkReporter(path);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("Zuply API Test Report");
            spark.config().setReportName("Zuply API Automation");
            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Base URL", ConfigReader.get("base.url"));
            extent.setSystemInfo("Environment", ConfigReader.get("env"));
        }
        return extent;
    }

    public static void setTest(ExtentTest t) { currentTest.set(t); }
    public static ExtentTest test()           { return currentTest.get(); }
    public static void removeTest()           { currentTest.remove(); }
}
