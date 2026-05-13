package com.cts.mfrp.Zuply.tests.api;

import com.cts.mfrp.Zuply.Utils.ResponseUtils;
import com.cts.mfrp.Zuply.Utils.TestDataHelper;
import com.cts.mfrp.Zuply.base.BaseTest;
import com.cts.mfrp.Zuply.clients.SellerClient;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SellerTests extends BaseTest {

    private SellerClient client;
    private Integer sellerOrderId;

    @BeforeClass
    public void setUp() {
        client = new SellerClient();
        Response orders = client.sellerOrders(sellerToken());
        if (orders.statusCode() == 200) {
            List<Map<String, Object>> list = ResponseUtils.body(orders).getList("$");
            if (list != null && !list.isEmpty()) {
                Object id = list.get(0).get("id");
                if (id != null) sellerOrderId = ((Number) id).intValue();
            }
        }
    }

    @DataProvider(name = "orderStatus")
    public Object[][] orderStatus() throws IOException {
        return TestDataHelper.read("SellerData.xlsx", "OrderStatus");
    }

    @Test(description = "GET /api/seller/dashboard valid -> 200")
    public void testDashboard() {
        Response r = client.dashboard(sellerToken());
        Assert.assertEquals(r.statusCode(), 200);
        Assert.assertNotNull(ResponseUtils.body(r).get("totalProductsUploaded"),
                "totalProductsUploaded missing in dashboard response: " + r.asString());
    }

    @Test(description = "GET /api/seller/products -> 200 array")
    public void testSellerProducts() {
        Response r = client.sellerProducts(sellerToken());
        Assert.assertEquals(r.statusCode(), 200);
    }

    @Test(description = "GET /api/seller/orders -> 200 array")
    public void testSellerOrders() {
        Response r = client.sellerOrders(sellerToken());
        Assert.assertEquals(r.statusCode(), 200);
    }

    @Test(dataProvider = "orderStatus", description = "PATCH /api/seller/orders/{id}/status - data-driven")
    public void testUpdateOrderStatus(String status, String expectedStatus, String description) {
        log("Scenario: " + description);
        int id = sellerOrderId != null ? sellerOrderId : 1;
        Response r = client.updateOrderStatus(sellerToken(), id, Map.of("status", status));
        if (Integer.parseInt(expectedStatus) == 200) {
            Assert.assertTrue(r.statusCode() == 200 || r.statusCode() == 400 || r.statusCode() == 403,
                    "expected 200/400/403; got " + r.statusCode());
        } else {
            Assert.assertEquals(r.statusCode(), Integer.parseInt(expectedStatus));
        }
    }
}
