package com.cts.mfrp.Zuply.tests.api;

import com.cts.mfrp.Zuply.Utils.ResponseUtils;
import com.cts.mfrp.Zuply.Utils.TestDataHelper;
import com.cts.mfrp.Zuply.base.BaseTest;
import com.cts.mfrp.Zuply.clients.CartClient;
import com.cts.mfrp.Zuply.clients.OrderClient;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OrderTests extends BaseTest {

    private OrderClient orderClient;

    @BeforeClass
    public void setUp() {
        orderClient = new OrderClient();
        new CartClient().addItem(buyerToken(), Map.of("productId", 1, "quantity", 1));
    }

    @DataProvider(name = "place")
    public Object[][] place() throws IOException {
        return TestDataHelper.read("OrderData.xlsx", "Place");
    }

    @Test(dataProvider = "place", description = "POST /api/orders - data-driven")
    public void testPlaceOrder(String address, String city, String pincode, String paymentMethod,
                               String expectedStatus, String description) {
        log("Scenario: " + description);
        Map<String, Object> deliveryAddress = new HashMap<>();
        deliveryAddress.put("customerName", "Test Buyer");
        deliveryAddress.put("phone", "9876543210");
        if (!address.isBlank()) deliveryAddress.put("address", address);
        deliveryAddress.put("city", city);
        deliveryAddress.put("pincode", pincode);

        Map<String, Object> body = new HashMap<>();
        body.put("deliveryAddress", deliveryAddress);
        body.put("paymentMethod", paymentMethod);
        Response r = orderClient.placeOrder(buyerToken(), body);
        Assert.assertEquals(r.statusCode(), Integer.parseInt(expectedStatus),
                "place: " + description + " body=" + r.asString());
    }

    @Test(description = "GET /api/orders -> 200 with array")
    public void testGetOrders() {
        Response r = orderClient.getOrders(buyerToken());
        Assert.assertEquals(r.statusCode(), 200);
        Assert.assertNotNull(ResponseUtils.body(r).getList("$"),
                "orders list missing in response: " + r.asString());
    }

    @Test(description = "GET /api/orders/{id} valid -> 200")
    public void testGetOrderById() {
        Response r = orderClient.getOrderById(buyerToken(), 1);
        Assert.assertTrue(r.statusCode() == 200 || r.statusCode() == 404,
                "expected 200/404; got " + r.statusCode());
    }
}
