package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

public class UserClient {

    public Response getProfile(String token) {
        return RequestBuilder.authSpec(token).get(Endpoints.USERS_PROFILE);
    }

    public Response getProfileNoAuth() {
        return RequestBuilder.spec().get(Endpoints.USERS_PROFILE);
    }

    public Response updateProfile(String token, Map<String, Object> body) {
        return RequestBuilder.authSpec(token).body(body).put(Endpoints.USERS_PROFILE);
    }

    public Response updateProfileNoAuth(Map<String, Object> body) {
        return RequestBuilder.spec().body(body).put(Endpoints.USERS_PROFILE);
    }
}
