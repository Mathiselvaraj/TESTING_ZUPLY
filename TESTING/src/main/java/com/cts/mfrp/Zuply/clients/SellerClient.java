package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

public class SellerClient {

    public Response register(String token, Map<String, Object> body) {
        return RequestBuilder.authSpec(token).body(body).post(Endpoints.SELLER_REGISTER);
    }

    public Response registerNoAuth(Map<String, Object> body) {
        return RequestBuilder.spec().body(body).post(Endpoints.SELLER_REGISTER);
    }

    public Response dashboard(String token) {
        return RequestBuilder.authSpec(token).get(Endpoints.SELLER_DASHBOARD);
    }

    public Response dashboardNoAuth() {
        return RequestBuilder.spec().get(Endpoints.SELLER_DASHBOARD);
    }

    public Response sellerProducts(String token) {
        return RequestBuilder.authSpec(token).get(Endpoints.SELLER_PRODUCTS);
    }

    public Response sellerOrders(String token) {
        return RequestBuilder.authSpec(token).get(Endpoints.SELLER_ORDERS);
    }

    public Response updateOrderStatus(String token, Object orderId, Map<String, Object> body) {
        return RequestBuilder.authSpec(token)
                .pathParam("id", orderId)
                .queryParams(body)
                .patch(Endpoints.SELLER_ORDER_STATUS);
    }

    public Response getPublicProfile(Object sellerId) {
        return RequestBuilder.spec()
                .pathParam("id", sellerId)
                .get(Endpoints.SELLER_PUBLIC);
    }
}
