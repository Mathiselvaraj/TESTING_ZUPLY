package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.utils.TestDataHelper;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.UserClient;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserTests extends BaseTest {

    private UserClient client;

    @BeforeClass
    public void setUp() { client = new UserClient(); }

    @DataProvider(name = "profileUpdate")
    public Object[][] profileUpdate() throws IOException {
        return TestDataHelper.read("UserData.xlsx", "ProfileUpdate");
    }

    @Test(description = "GET /api/users/profile with valid JWT returns 200 and profile")
    public void testGetProfileValid() {
        Response r = client.getProfile(buyerToken());
        Assert.assertEquals(r.statusCode(), 200);
        Assert.assertNotNull(ResponseUtils.body(r).getString("email"));
    }

    @Test(dataProvider = "profileUpdate", description = "PUT /api/users/profile - data-driven")
    public void testUpdateProfile(String name, String email, String expectedStatus, String description) {
        log("Scenario: " + description);
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("phone", "9876543210");
        body.put("address", "42, Anna Nagar, Chennai");
        body.put("pincode", "600040");
        Response r = client.updateProfile(buyerToken(), body);
        Assert.assertEquals(r.statusCode(), Integer.parseInt(expectedStatus),
                "update profile: " + description + " body=" + r.asString());
    }
}
