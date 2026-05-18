package com.cts.mfrp.Zuply.tests.api;

import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.utils.TestDataHelper;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.AdminClient;
import com.cts.mfrp.zuply.clients.ProductClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Test(groups = {"regression", "api", "products"})
public class ProductTests extends BaseTest {

    private ProductClient client;
    private Integer createdProductId;

    @BeforeClass
    public void setUp() {
        client = new ProductClient();
        AdminClient adminClient = new AdminClient();
        Response sellersResp = adminClient.getSellers(adminToken());
        if (sellersResp.statusCode() == 200) {
            List<Map<String, Object>> sellers = ResponseUtils.body(sellersResp).getList("$");
            if (sellers != null) {
                for (Map<String, Object> s : sellers) {
                    if ("PENDING".equals(s.get("verificationStatus"))) {
                        Object sid = s.get("id");
                        if (sid != null) adminClient.approveSeller(adminToken(), sid);
                    }
                }
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "ProductTests Fixture " + System.currentTimeMillis());
        body.put("description", "fixture product for read/update tests");
        body.put("categoryId", 5);
        body.put("price", 1999);
        body.put("stock", 10);
        Response r = client.create(sellerToken(), body);
        if (r.statusCode() == 201 || r.statusCode() == 200) {
            JsonPath data = ResponseUtils.body(r);
            Object id = data.get("id");
            if (id == null) id = data.get("productId");
            if (id != null) createdProductId = ((Number) id).intValue();
        }
    }

    @DataProvider(name = "createProduct")
    public Object[][] createProduct() throws IOException {
        return TestDataHelper.read("ProductData.xlsx", "Create");
    }

    @Test(description = "GET /api/products returns 200 + array")
    public void testSearchAll() {
        Response r = client.searchAll();
        Assert.assertEquals(r.statusCode(), 200);
        List<Object> products = ResponseUtils.body(r).getList("$");
        Assert.assertNotNull(products, "products list missing in response: " + r.asString());
    }

    @Test(description = "GET /api/products/{id} valid -> 200 with full details")
    public void testGetProductById() {
        if (createdProductId == null) {
            throw new org.testng.SkipException("Setup failed to create a product fixture; check seller registration");
        }
        Response r = client.getById(createdProductId);
        Assert.assertEquals(r.statusCode(), 200, "body=" + r.asString());
        JsonPath data = ResponseUtils.body(r);
        Assert.assertEquals(((Number) data.get("id")).intValue(), createdProductId.intValue());
        Assert.assertNotNull(data.getString("name"));
    }

    @Test(description = "GET /api/products/{id} non-existent -> 404")
    public void testGetProductByIdNotFound() {
        Response r = client.getById(99999);
        Assert.assertEquals(r.statusCode(), 404);
    }

    @Test(dataProvider = "createProduct", description = "POST /api/products - data-driven")
    public void testCreateProduct(String name, String desc, String categoryId, String price,
                                  String stock, String expectedStatus, String description_tc) {
        log("Scenario: " + description_tc);
        Map<String, Object> body = new LinkedHashMap<>();
        if (!name.isBlank()) body.put("name", name);
        body.put("description", desc);
        body.put("categoryId", Integer.parseInt(categoryId));
        body.put("price", Double.parseDouble(price));
        body.put("stock", Integer.parseInt(stock));

        Response r = client.create(sellerToken(), body);
        Assert.assertEquals(r.statusCode(), Integer.parseInt(expectedStatus),
                "create: " + description_tc + " body=" + r.asString());
    }

    @Test(description = "POST /api/products as buyer -> 403")
    public void testCreateProductAsBuyerForbidden() {
        Map<String, Object> body = Map.of(
                "name", "Buyer Attempt", "description", "x",
                "category", "Electronics", "price", 100, "stock", 1);
        Response r = client.create(buyerToken(), body);
        Assert.assertEquals(r.statusCode(), 403);
    }

    @Test(description = "PUT /api/products/{id} as owning seller -> 200")
    public void testUpdateProduct() {
        if (createdProductId == null) {
            throw new org.testng.SkipException("Setup failed to create a product fixture");
        }
        Map<String, Object> body = Map.of(
                "name", "ProductTests Fixture (Updated)",
                "price", 2499, "stock", 5,
                "description", "updated description");
        Response r = client.update(sellerToken(), createdProductId, body);
        Assert.assertEquals(r.statusCode(), 200);
    }

    @Test(description = "GET /api/products/seller/{id} -> 200 with array")
    public void testGetProductsBySeller() {
        Response r = client.getBySeller(1);
        Assert.assertEquals(r.statusCode(), 200);
        List<Object> products = ResponseUtils.body(r).getList("$");
        Assert.assertNotNull(products, "products list missing in response: " + r.asString());
    }
}
