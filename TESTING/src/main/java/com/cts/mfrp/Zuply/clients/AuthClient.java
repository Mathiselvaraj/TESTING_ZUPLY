package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.util.Map;

public class AuthClient {

    public Response register(Map<String, Object> body) {
        return RequestBuilder.spec().body(body).post(Endpoints.AUTH_REGISTER);
    }

    public Response login(Map<String, Object> body) {
        return RequestBuilder.spec().body(body).post(Endpoints.AUTH_LOGIN);
    }
}
