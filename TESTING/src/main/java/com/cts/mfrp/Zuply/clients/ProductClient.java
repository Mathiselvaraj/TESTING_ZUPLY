package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

public class ProductClient {

    public Response search(Map<String, Object> queryParams) {
        return RequestBuilder.spec().queryParams(queryParams).get(Endpoints.PRODUCTS);
    }

    public Response searchAll() {
        return RequestBuilder.spec().get(Endpoints.PRODUCTS);
    }

    public Response getById(Object id) {
        return RequestBuilder.spec().pathParam("id", id).get(Endpoints.PRODUCT_BY_ID);
    }

    public Response create(String token, Map<String, Object> body) {
        return RequestBuilder.authSpec(token).body(body).post(Endpoints.PRODUCTS);
    }

    public Response createNoAuth(Map<String, Object> body) {
        return RequestBuilder.spec().body(body).post(Endpoints.PRODUCTS);
    }

    public Response update(String token, Object id, Map<String, Object> body) {
        return RequestBuilder.authSpec(token).pathParam("id", id).body(body).put(Endpoints.PRODUCT_BY_ID);
    }

    public Response delete(String token, Object id) {
        return RequestBuilder.authSpec(token).pathParam("id", id).delete(Endpoints.PRODUCT_BY_ID);
    }

    public Response getBySeller(Object sellerId) {
        return RequestBuilder.spec().pathParam("sellerId", sellerId).get(Endpoints.PRODUCTS_BY_SELLER);
    }
}
