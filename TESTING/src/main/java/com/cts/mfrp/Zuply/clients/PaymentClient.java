package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

public class PaymentClient {

    public Response createOrder(String token, Map<String, Object> body) {
        return RequestBuilder.authSpec(token).body(body).post(Endpoints.PAYMENT_CREATE_ORDER);
    }

    public Response verify(String token, Map<String, Object> body) {
        return RequestBuilder.authSpec(token).body(body).post(Endpoints.PAYMENT_VERIFY);
    }

    public Response status(String token, Object orderId) {
        return RequestBuilder.authSpec(token)
                .pathParam("orderId", orderId)
                .get(Endpoints.PAYMENT_STATUS);
    }
}
