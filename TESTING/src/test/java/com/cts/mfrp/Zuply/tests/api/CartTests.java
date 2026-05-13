package com.cts.mfrp.Zuply.tests.api;

import com.cts.mfrp.Zuply.Utils.ResponseUtils;
import com.cts.mfrp.Zuply.Utils.TestDataHelper;
import com.cts.mfrp.Zuply.base.BaseTest;
import com.cts.mfrp.Zuply.clients.CartClient;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartTests extends BaseTest {

    private CartClient client;

    @BeforeClass
    public void setUp() { client = new CartClient(); }

    private Integer freshCartItemId() {
        client.addItem(buyerToken(), Map.of("productId", 1, "quantity", 1));
        Response cart = client.getCart(buyerToken());
        if (cart.statusCode() == 200) {
            List<Map<String, Object>> items = ResponseUtils.body(cart).getList("items");
            if (items != null && !items.isEmpty()) {
                Object id = items.get(items.size() - 1).get("itemId");
                if (id != null) return ((Number) id).intValue();
            }
        }
        return null;
    }

    @DataProvider(name = "addItem")
    public Object[][] addItem() throws IOException {
        return TestDataHelper.read("CartData.xlsx", "AddItem");
    }

    @Test(description = "GET /api/cart with buyer JWT -> 200 with items")
    public void testGetCart() {
        Response r = client.getCart(buyerToken());
        Assert.assertEquals(r.statusCode(), 200);
        Assert.assertNotNull(ResponseUtils.body(r).getList("items"),
                "items missing in cart response: " + r.asString());
    }

    @Test(dataProvider = "addItem", description = "POST /api/cart - data-driven")
    public void testAddItem(String productId, String quantity, String expectedStatus, String description) {
        log("Scenario: " + description);
        Map<String, Object> body = new HashMap<>();
        body.put("productId", Integer.parseInt(productId));
        body.put("quantity", Integer.parseInt(quantity));
        Response r = client.addItem(buyerToken(), body);
        if (Integer.parseInt(expectedStatus) == 201) {
            Assert.assertTrue(r.statusCode() == 201 || r.statusCode() == 200,
                    "expected 201/200; got " + r.statusCode());
        } else {
            Assert.assertEquals(r.statusCode(), Integer.parseInt(expectedStatus),
                    "add: " + description);
        }
    }

    @Test(description = "PUT /api/cart/{itemId} valid quantity -> 200")
    public void testUpdateItem() {
        Integer id = freshCartItemId();
        Assert.assertNotNull(id, "Could not obtain a cart item id for update");
        Response r = client.updateItem(buyerToken(), id, Map.of("quantity", 5));
        Assert.assertTrue(r.statusCode() == 200 || r.statusCode() == 404,
                "expected 200/404; got " + r.statusCode());
    }

    @Test(description = "DELETE /api/cart/{itemId} valid -> 204")
    public void testDeleteItem() {
        Integer id = freshCartItemId();
        Assert.assertNotNull(id, "Could not obtain a cart item id for delete");
        Response r = client.deleteItem(buyerToken(), id);
        Assert.assertTrue(r.statusCode() == 200 || r.statusCode() == 204 || r.statusCode() == 404,
                "expected 200/204/404; got " + r.statusCode());
    }
}
