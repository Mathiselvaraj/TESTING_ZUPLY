package com.cts.mfrp.Zuply.tests.api;

import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.UploadClient;
import io.restassured.response.Response;
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

@Test(groups = {"regression", "api", "upload"})
public class UploadTests extends BaseTest {

    private UploadClient client;
    private File sampleImage;

    @BeforeClass
    public void setUp() throws IOException {
        client = new UploadClient();
        Path p = Files.createTempFile("zuply_sample_", ".jpg");
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(33, 150, 243));
        g.fillRect(0, 0, 200, 200);
        g.setColor(Color.WHITE);
        g.drawString("ZUPLY", 80, 100);
        g.dispose();
        ImageIO.write(img, "jpg", p.toFile());
        sampleImage = p.toFile();
        sampleImage.deleteOnExit();
    }

    @Test(description = "POST /api/upload with valid JPEG and seller JWT -> 200 with imageId")
    public void testUploadValidImage() {
        Response r = client.uploadFile(sellerToken(), sampleImage);
        Assert.assertEquals(r.statusCode(), 200, "body=" + r.asString());
        Assert.assertNotNull(ResponseUtils.body(r).get("imageId"),
                "imageId missing in upload response: " + r.asString());
    }

    @Test(description = "POST /api/upload as buyer -> 403")
    public void testUploadAsBuyerForbidden() {
        Response r = client.uploadFile(buyerToken(), sampleImage);
        Assert.assertEquals(r.statusCode(), 403);
    }
}
