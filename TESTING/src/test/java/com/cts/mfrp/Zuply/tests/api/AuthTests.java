package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.utils.TestDataHelper;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.AuthClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Test(groups = {"smoke", "regression", "api", "auth"})
public class AuthTests extends BaseTest {

    private AuthClient client;

    @BeforeClass
    public void setUp() { client = new AuthClient(); }

    @DataProvider(name = "registerData")
    public Object[][] registerData() throws IOException {
        return TestDataHelper.read("AuthData.xlsx", "Register");
    }

    @DataProvider(name = "loginData")
    public Object[][] loginData() throws IOException {
        return TestDataHelper.read("AuthData.xlsx", "Login");
    }

    @Test(dataProvider = "registerData", description = "POST /api/auth/register - data-driven")
    public void testRegister(String name, String email, String password, String role,
                             String phone, String expectedStatus, String description) {
        log("Scenario: " + description);
        Map<String, Object> body = new HashMap<>();
        if (!name.isBlank())  body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        body.put("role", role);
        if (phone != null && !phone.isBlank()) body.put("phone", phone);

        Response r = client.register(body);
        Assert.assertEquals(r.statusCode(), Integer.parseInt(expectedStatus),
                "register: " + description + " body=" + r.asString());
        if (r.statusCode() == 201) {
            JsonPath data = ResponseUtils.body(r);
            Assert.assertEquals(data.getString("email"), email);
            Assert.assertEquals(data.getString("role"),  role);
            Assert.assertNotNull(data.get("userId"));
        }
    }

    @Test(dataProvider = "loginData", description = "POST /api/auth/login - data-driven")
    public void testLogin(String email, String password, String expectedStatus, String description) {
        log("Scenario: " + description);
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Response r = client.login(body);
        Assert.assertEquals(r.statusCode(), Integer.parseInt(expectedStatus),
                "login: " + description + " body=" + r.asString());
        if (r.statusCode() == 200) {
            JsonPath data = ResponseUtils.body(r);
            Assert.assertNotNull(data.getString("token"));
            Assert.assertNotNull(data.getString("role"));
        }
    }

    @Test(description = "Bootstrapped buyer login produces a non-empty JWT")
    public void testLoginReturnsJwt() {
        String token = buyerToken();
        Assert.assertNotNull(token);
        Assert.assertTrue(token.length() > 20, "JWT token suspiciously short: " + token);
    }
}
