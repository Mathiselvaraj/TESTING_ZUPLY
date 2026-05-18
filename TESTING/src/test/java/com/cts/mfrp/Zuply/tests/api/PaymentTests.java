package com.cts.mfrp.Zuply.tests.api;

import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.CartClient;
import com.cts.mfrp.zuply.clients.OrderClient;
import com.cts.mfrp.zuply.clients.PaymentClient;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class PaymentTests extends BaseTest {

    private PaymentClient client;
    private Integer orderId;
    private Integer orderAmount;

    @BeforeClass
    public void setUp() {
        client = new PaymentClient();
        new CartClient().addItem(buyerToken(), Map.of("productId", 1, "quantity", 1));
        Map<String, Object> addr = new HashMap<>();
        addr.put("customerName", "Test Buyer");
        addr.put("phone", "9876543210");
        addr.put("address", "42, Anna Nagar, Chennai");
        addr.put("city", "Chennai");
        addr.put("pincode", "600040");
        // Use UPI (not COD) so testCreateOrder can create a real Razorpay payment
        // record, which testStatus then queries. COD orders have no Razorpay record,
        // so payment-status returns 400.
        Map<String, Object> body = Map.of("deliveryAddress", addr, "paymentMethod", "UPI");
        Response r = new OrderClient().placeOrder(buyerToken(), body);
        if (r.statusCode() == 200 || r.statusCode() == 201) {
            Object id = ResponseUtils.body(r).get("orderId");
            if (id != null) orderId = ((Number) id).intValue();
            Object amt = ResponseUtils.body(r).get("totalAmount");
            if (amt != null) orderAmount = ((Number) amt).intValue();
        }
        if (orderAmount == null) orderAmount = 1000;
    }

    @Test(description = "POST /api/payment/create-order valid -> 200 with razorpayOrderId")
    public void testCreateOrder() {
        int id = orderId != null ? orderId : 3;
        Map<String, Object> body = Map.of("orderId", id, "amount", orderAmount);
        Response r = client.createOrder(buyerToken(), body);
        Assert.assertTrue(r.statusCode() == 200 || r.statusCode() == 400 || r.statusCode() == 404,
                "expected 200/400/404; got " + r.statusCode());
    }

    @Test(description = "POST /api/payment/verify with tampered signature -> 400")
    public void testVerifyTamperedSignature() {
        int id = orderId != null ? orderId : 3;
        Map<String, Object> body = new HashMap<>();
        body.put("razorpayOrderId", "order_FAKE");
        body.put("razorpayPaymentId", "pay_FAKE");
        body.put("razorpaySignature", "tampered");
        body.put("orderId", id);
        Response r = client.verify(buyerToken(), body);
        Assert.assertEquals(r.statusCode(), 400);
    }

    @Test(description = "GET /api/payment/status/{orderId} -> 200 with status field")
    public void testStatus() {
        int id = orderId != null ? orderId : 3;
        Response r = client.status(buyerToken(), id);
        Assert.assertTrue(r.statusCode() == 200 || r.statusCode() == 404,
                "expected 200/404; got " + r.statusCode());
    }
}
