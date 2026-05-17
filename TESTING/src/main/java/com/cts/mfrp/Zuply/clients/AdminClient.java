package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

public class AdminClient {

    public Response dashboard(String token) {
        return RequestBuilder.authSpec(token).get(Endpoints.ADMIN_DASHBOARD);
    }

    public Response dashboardNoAuth() {
        return RequestBuilder.spec().get(Endpoints.ADMIN_DASHBOARD);
    }

    public Response getSellers(String token) {
        return RequestBuilder.authSpec(token).get(Endpoints.ADMIN_SELLERS);
    }

    public Response approveSeller(String token, Object sellerId) {
        return RequestBuilder.authSpec(token)
                .pathParam("id", sellerId)
                .patch(Endpoints.ADMIN_SELLER_APPROVE);
    }

    public Response suspendSeller(String token, Object sellerId) {
        return RequestBuilder.authSpec(token)
                .pathParam("id", sellerId)
                .patch(Endpoints.ADMIN_SELLER_SUSPEND);
    }

    public Response deleteSeller(String token, Object sellerId) {
        return RequestBuilder.authSpec(token)
                .pathParam("id", sellerId)
                .delete(Endpoints.ADMIN_SELLER_BY_ID);
    }

    public Response pendingProducts(String token) {
        return RequestBuilder.authSpec(token).get(Endpoints.ADMIN_PRODUCTS);
    }

    public Response approveProduct(String token, Object productId) {
        return RequestBuilder.authSpec(token)
                .pathParam("id", productId)
                .patch(Endpoints.ADMIN_PRODUCT_APPROVE);
    }

    public Response rejectProduct(String token, Object productId, Map<String, Object> body) {
        return RequestBuilder.authSpec(token)
                .pathParam("id", productId)
                .body(body)
                .patch(Endpoints.ADMIN_PRODUCT_REJECT);
    }

    public Response updateProduct(String token, Object productId, Map<String, Object> body) {
        return RequestBuilder.authSpec(token)
                .pathParam("id", productId)
                .body(body)
                .put(Endpoints.ADMIN_PRODUCT_BY_ID);
    }

    public Response deleteProduct(String token, Object productId) {
        return RequestBuilder.authSpec(token)
                .pathParam("id", productId)
                .delete(Endpoints.ADMIN_PRODUCT_BY_ID);
    }

    public Response getOrders(String token) {
        return RequestBuilder.authSpec(token).get(Endpoints.ADMIN_ORDERS);
    }

    public Response getReports(String token) {
        return RequestBuilder.authSpec(token).get(Endpoints.ADMIN_REPORTS);
    }
}
