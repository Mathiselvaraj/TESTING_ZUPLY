package com.cts.mfrp.zuply.clients;

import com.cts.mfrp.zuply.utils.RequestBuilder;
import com.cts.mfrp.zuply.constants.Endpoints;
import io.restassured.response.Response;

import java.io.File;

public class UploadClient {

    public Response uploadFile(String token, File file) {
        return RequestBuilder.multipartSpec(token)
                .multiPart("file", file, "image/jpeg")
                .post(Endpoints.UPLOAD);
    }

    public Response uploadFileNoAuth(File file) {
        return RequestBuilder.spec()
                .multiPart("file", file, "image/jpeg")
                .post(Endpoints.UPLOAD);
    }

    /** Upload an empty multipart request (no file part). */
    public Response uploadEmpty(String token) {
        return RequestBuilder.multipartSpec(token).post(Endpoints.UPLOAD);
    }
}
