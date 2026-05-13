package com.cts.mfrp.Zuply.clients;

import com.cts.mfrp.Zuply.Utils.RequestBuilder;
import com.cts.mfrp.Zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

public class OrderClient {

    public Response placeOrder(String token, Map<String, Object> body) {
        return RequestBuilder.authSpec(token).body(body).post(Endpoints.ORDERS);
    }

    public Response placeOrderNoAuth(Map<String, Object> body) {
        return RequestBuilder.spec().body(body).post(Endpoints.ORDERS);
    }

    public Response getOrders(String token) {
        return RequestBuilder.authSpec(token).get(Endpoints.ORDERS);
    }

    public Response getOrdersNoAuth() {
        return RequestBuilder.spec().get(Endpoints.ORDERS);
    }

    public Response getOrderById(String token, Object id) {
        return RequestBuilder.authSpec(token)
                .pathParam("id", id)
                .get(Endpoints.ORDER_BY_ID);
    }
}
