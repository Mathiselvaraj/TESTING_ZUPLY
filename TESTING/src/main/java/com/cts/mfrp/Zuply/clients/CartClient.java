package com.cts.mfrp.Zuply.clients;

import com.cts.mfrp.Zuply.Utils.RequestBuilder;
import com.cts.mfrp.Zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

public class CartClient {

    public Response getCart(String token) {
        return RequestBuilder.authSpec(token).get(Endpoints.CART);
    }

    public Response getCartNoAuth() {
        return RequestBuilder.spec().get(Endpoints.CART);
    }

    public Response addItem(String token, Map<String, Object> body) {
        return RequestBuilder.authSpec(token).body(body).post(Endpoints.CART);
    }

    public Response updateItem(String token, Object itemId, Map<String, Object> body) {
        return RequestBuilder.authSpec(token)
                .pathParam("itemId", itemId)
                .queryParams(body)
                .put(Endpoints.CART_ITEM);
    }

    public Response deleteItem(String token, Object itemId) {
        return RequestBuilder.authSpec(token)
                .pathParam("itemId", itemId)
                .delete(Endpoints.CART_ITEM);
    }
}
