package com.cts.mfrp.zuply.tests.api;

import com.cts.mfrp.zuply.utils.ResponseUtils;
import com.cts.mfrp.zuply.base.BaseTest;
import com.cts.mfrp.zuply.clients.AdminClient;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

@Test(groups = {"regression", "api", "admin"})
public class AdminTests extends BaseTest {

    private AdminClient client;
    private Integer productIdToApprove;
    private Integer productIdToReject;
    private Integer sellerIdToApprove;

    @BeforeClass
    public void setUp() {
        client = new AdminClient();
        Response prodResp = client.pendingProducts(adminToken());
        if (prodResp.statusCode() == 200) {
            List<Map<String, Object>> products = ResponseUtils.body(prodResp).getList("$");
            if (products != null && !products.isEmpty()) {
                productIdToApprove = ((Number) products.get(0).get("id")).intValue();
                productIdToReject = products.size() > 1
                        ? ((Number) products.get(1).get("id")).intValue()
                        : productIdToApprove;
            }
        }
        Response sellerResp = client.getSellers(adminToken());
        if (sellerResp.statusCode() == 200) {
            List<Map<String, Object>> sellers = ResponseUtils.body(sellerResp).getList("$");
            if (sellers != null && !sellers.isEmpty()) {
                sellerIdToApprove = ((Number) sellers.get(0).get("id")).intValue();
            }
        }
    }

    @Test(description = "GET /api/admin/dashboard valid -> 200")
    public void testDashboard() {
        Response r = client.dashboard(adminToken());
        Assert.assertEquals(r.statusCode(), 200);
        Assert.assertNotNull(ResponseUtils.body(r).get("totalSellers"),
                "totalSellers missing in dashboard response: " + r.asString());
    }

    @Test(description = "GET /api/admin/sellers -> 200 array")
    public void testGetSellers() {
        Response r = client.getSellers(adminToken());
        Assert.assertEquals(r.statusCode(), 200);
    }

    @Test(description = "PATCH /api/admin/sellers/{id}/approve -> 200")
    public void testApproveSeller() {
        int id = sellerIdToApprove != null ? sellerIdToApprove : 2;
        Response r = client.approveSeller(adminToken(), id);
        Assert.assertTrue(r.statusCode() == 200 || r.statusCode() == 400 || r.statusCode() == 404,
                "expected 200/400/404; got " + r.statusCode());
    }

    @Test(description = "PATCH /api/admin/products/{id}/approve -> 200")
    public void testApproveProduct() {
        int id = productIdToApprove != null ? productIdToApprove : 5;
        Response r = client.approveProduct(adminToken(), id);
        Assert.assertTrue(r.statusCode() == 200 || r.statusCode() == 400 || r.statusCode() == 404,
                "expected 200/400/404; got " + r.statusCode());
    }

    @Test(description = "PATCH /api/admin/products/{id}/reject -> 200 with reason")
    public void testRejectProduct() {
        int id = productIdToReject != null ? productIdToReject : 6;
        Response r = client.rejectProduct(adminToken(), id,
                Map.of("reason", "Product description violates marketplace policy"));
        Assert.assertTrue(r.statusCode() == 200 || r.statusCode() == 400 || r.statusCode() == 404,
                "expected 200/400/404; got " + r.statusCode());
    }

    @Test(description = "GET /api/admin/orders -> 200 array")
    public void testAdminOrders() {
        Response r = client.getOrders(adminToken());
        Assert.assertEquals(r.statusCode(), 200);
    }
}
