package com.cts.mfrp.Zuply.clients;

import com.cts.mfrp.Zuply.Utils.RequestBuilder;
import com.cts.mfrp.Zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

public class ListingClient {

    public Response generate(String token, Object imageId) {
        return RequestBuilder.authSpec(token)
                .pathParam("imageId", imageId)
                .post(Endpoints.LISTING_GENERATE);
    }

    public Response generateNoAuth(Object imageId) {
        return RequestBuilder.spec()
                .pathParam("imageId", imageId)
                .post(Endpoints.LISTING_GENERATE);
    }

    public Response getDraft(String token, Object imageId) {
        return RequestBuilder.authSpec(token)
                .pathParam("imageId", imageId)
                .get(Endpoints.LISTING_BY_IMAGE);
    }

    public Response editDraft(String token, Object productId, Map<String, Object> body) {
        return RequestBuilder.authSpec(token)
                .pathParam("productId", productId)
                .body(body)
                .put(Endpoints.LISTING_BY_PRODUCT);
    }

    public Response publish(String token, Object productId) {
        return RequestBuilder.authSpec(token)
                .pathParam("productId", productId)
                .post(Endpoints.LISTING_PUBLISH);
    }

    public Response saveImages(String token, Object productId, Map<String, Object> body) {
        return RequestBuilder.authSpec(token)
                .pathParam("productId", productId)
                .body(body)
                .put(Endpoints.LISTING_IMAGES);
    }
}
