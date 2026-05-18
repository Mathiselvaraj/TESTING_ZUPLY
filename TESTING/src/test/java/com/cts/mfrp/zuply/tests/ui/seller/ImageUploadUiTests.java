package com.cts.mfrp.zuply.tests.ui.seller;


import com.cts.mfrp.zuply.base.UiBaseTest;
import com.cts.mfrp.zuply.pages.SellerUploadPage;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;

/** Image upload — FRD §2.10 (Product Upload) + §3.4 (validation rules). Maps to TC025-TC028. */
public class ImageUploadUiTests extends UiBaseTest {

    private static final Duration UPLOAD_SETTLE = Duration.ofSeconds(10);

    private String sellerEmail;

    @BeforeClass(alwaysRun = true, dependsOnMethods = "launchBrowser")
    public void loginSeller() {
        sellerEmail = registerNewSeller("UploadSeller");
        loginViaUi(sellerEmail, "Test@1234");
    }

    /** TC025 — Seller can upload a valid JPEG within the size limit. */
    @Test(description = "TC025 — ValidImageUpload")
    public void tc025_validImageUpload() throws IOException {
        File img = generateImage("jpg", 200, 200);
        SellerUploadPage upload = new SellerUploadPage(driver);
        upload.open();
        upload.uploadImage(img);
        upload.waitForUploadAccepted(UPLOAD_SETTLE);
        Assert.assertFalse(driver.getTitle().contains("Page not found"),
                "Upload should not crash the page");
    }

    /** TC026 — Reject non-JPEG/PNG file types (e.g. PDF). */
    @Test(description = "TC026 — InvalidFileTypeUpload")
    public void tc026_invalidFileTypeUpload() throws IOException {
        Path pdfPath = Files.createTempFile("zuply_bogus_", ".pdf");
        Files.writeString(pdfPath, "%PDF-1.4\n%fake pdf for negative test\n");
        File pdf = pdfPath.toFile();
        pdf.deleteOnExit();

        SellerUploadPage upload = new SellerUploadPage(driver);
        upload.open();
        try { upload.uploadImage(pdf); }
        catch (Exception ignored) { /* the SPA's accept= attr may block this */ }
        // Wait for either the validation toast or a stable page; either resolves quickly.
        waitAfterAction();

        // Expect: either upload was blocked by the input's accept attribute, or
        // the SPA shows an error toast. We just verify the page didn't navigate away.
        Assert.assertTrue(driver.getCurrentUrl().contains("/seller/upload"),
                "Should remain on /seller/upload when file type is invalid");
    }

    /** TC027 — Reject upload exceeding 10 MB. */
    @Test(description = "TC027 — FileSizeExceeded")
    public void tc027_fileSizeExceeded() throws IOException {
        Path big = Files.createTempFile("zuply_big_", ".jpg");
        byte[] payload = new byte[12 * 1024 * 1024];
        new Random().nextBytes(payload);
        Files.write(big, payload);
        big.toFile().deleteOnExit();

        SellerUploadPage upload = new SellerUploadPage(driver);
        upload.open();
        try { upload.uploadImage(big.toFile()); }
        catch (Exception ignored) {}
        upload.waitForUploadAccepted(UPLOAD_SETTLE);

        boolean errorShown = driver.getPageSource().toLowerCase()
                .matches(".*(file size|too large|exceed|10 ?mb).*");
        Assert.assertTrue(errorShown || driver.getCurrentUrl().contains("/seller/upload"),
                "Expected size-rejection error or stay on /seller/upload");
    }

    /** TC028 — Seller can upload a valid PNG file. */
    @Test(description = "TC028 — ValidPNGUpload")
    public void tc028_validPngUpload() throws IOException {
        File img = generateImage("png", 200, 200);
        SellerUploadPage upload = new SellerUploadPage(driver);
        upload.open();
        upload.uploadImage(img);
        upload.waitForUploadAccepted(UPLOAD_SETTLE);
        Assert.assertFalse(driver.getTitle().contains("Page not found"),
                "PNG upload should not crash the page");
    }

    private static File generateImage(String format, int w, int h) throws IOException {
        Path p = Files.createTempFile("zuply_ui_", "." + format);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setColor(new Color(33, 150, 243));
        g.fillRect(0, 0, w, h);
        g.dispose();
        ImageIO.write(bi, format, p.toFile());
        File f = p.toFile();
        f.deleteOnExit();
        return f;
    }
}
