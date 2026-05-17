package com.cts.mfrp.zuply.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * One-shot utility: opens https://zuply.netlify.app/, walks the site (homepage
 * + every nav link it finds + a list of well-known routes), and dumps the
 * rendered HTML of each page into target/dom-dumps/.
 *
 * Run via: java -cp <classpath> com.cts.mfrp.zuply.utils.DOMDump
 */
public class DOMDump {

    private static final String BASE = "https://zuply.netlify.app";
    private static final Path OUT_DIR = Paths.get("target", "dom-dumps");

    private static final String[] PUBLIC_ROUTES = {
            "/", "/login", "/register", "/products", "/become-a-seller", "/customer-care", "/sellers"
    };
    private static final String[] BUYER_ROUTES = {
            "/cart", "/checkout", "/orders", "/profile", "/wishlist"
    };
    private static final String[] SELLER_ROUTES = {
            "/seller/dashboard", "/seller/products", "/seller/orders", "/seller/upload"
    };
    private static final String[] ADMIN_ROUTES = {
            "/admin/dashboard", "/admin/sellers", "/admin/products", "/admin/orders"
    };

    public static void main(String[] args) throws IOException, InterruptedException {
        Files.createDirectories(OUT_DIR);

        // Pass 1: anonymous — capture public pages
        dumpRoutes("anon", null, null, PUBLIC_ROUTES);

        // Pass 2: admin login — capture admin pages
        dumpRoutes("admin", "admin@zuply.in", "Admin@123", ADMIN_ROUTES);

        // Pass 3: register fresh seller + admin-approve, capture seller pages
        String sellerEmail = "ui.seller." + System.currentTimeMillis() + "@zuply.in";
        registerViaUi(sellerEmail, "Test@1234", "Ui Seller", "9876543210", "SELLER");
        // Admin approval is done via API for speed
        approveAllPendingSellersViaApi();
        dumpRoutes("seller", sellerEmail, "Test@1234", SELLER_ROUTES);

        // Pass 4: register fresh buyer, capture buyer pages
        String buyerEmail = "ui.buyer." + System.currentTimeMillis() + "@zuply.in";
        registerViaUi(buyerEmail, "Test@1234", "Ui Buyer", "9876543210", "CUSTOMER");
        dumpRoutes("buyer", buyerEmail, "Test@1234", BUYER_ROUTES);

        System.out.println("\nAll done. Dumps in " + OUT_DIR);
    }

    /** Open a fresh browser, optionally log in, then dump each route. */
    private static void dumpRoutes(String role, String email, String pwd, String[] routes)
            throws InterruptedException, IOException {
        System.out.println("\n=== " + role + " ===");
        WebDriver d = DriverFactory.create(true);
        JavascriptExecutor js = (JavascriptExecutor) d;
        try {
            d.get(BASE + "/");
            new WebDriverWait(d, Duration.ofSeconds(15))
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-root")));
            Thread.sleep(1200);

            if (email != null) {
                if (!loginUi(d, email, pwd)) {
                    System.out.println("  login FAILED for " + email + " — skipping " + role + " pass");
                    return;
                }
            }

            for (String route : routes) {
                try {
                    String script =
                        "const path = arguments[0];" +
                        "const a = document.querySelector('a[href=\"' + path + '\"], a[routerlink=\"' + path + '\"]');" +
                        "if (a) { a.click(); return 'click'; }" +
                        "history.pushState({}, '', path); window.dispatchEvent(new PopStateEvent('popstate')); return 'pushState';";
                    Object via = js.executeScript(script, route);
                    Thread.sleep(1500);

                    String currentUrl = d.getCurrentUrl();
                    String html = (String) js.executeScript("return document.documentElement.outerHTML");
                    int rendered = ((Number) js.executeScript(
                            "return document.querySelectorAll('app-root *').length")).intValue();

                    String safeName = role + (route.equals("/") ? "_root" : route.replaceAll("[^a-zA-Z0-9]+", "_"));
                    Path out = OUT_DIR.resolve(safeName + ".html");
                    String header = "<!--\n  Role:        " + role + "\n  Requested:   " + BASE + route
                            + "\n  Landed on:   " + currentUrl + "\n  Nodes:       " + rendered
                            + "\n  Navigated:   " + via + "\n-->\n";
                    Files.writeString(out, header + html);
                    System.out.println("  " + route + " → " + currentUrl + " (" + rendered + " nodes)");
                } catch (Exception e) {
                    System.out.println("  " + route + " ERROR: " + e.getMessage());
                }
            }
        } finally {
            d.quit();
        }
    }

    private static boolean loginUi(WebDriver d, String email, String pwd) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) d;
        js.executeScript(
                "const a = document.querySelector('a[href=\"/login\"], a[routerlink=\"/login\"]');" +
                "if (a) a.click(); else { history.pushState({}, '', '/login'); window.dispatchEvent(new PopStateEvent('popstate')); }");
        Thread.sleep(1500);
        try {
            WebElement emailEl = d.findElement(By.cssSelector("input[type='email'].input"));
            WebElement pwdEl = d.findElement(By.cssSelector("input[type='password'].input"));
            emailEl.clear(); emailEl.sendKeys(email);
            pwdEl.clear(); pwdEl.sendKeys(pwd);
            WebElement loginBtn = d.findElement(By.cssSelector("button.login-btn"));
            js.executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", loginBtn);
            new WebDriverWait(d, Duration.ofSeconds(10))
                    .until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
            Thread.sleep(800);
            return true;
        } catch (Exception e) {
            System.out.println("  login UI error: " + e.getMessage());
            return false;
        }
    }

    /** Register via the SPA's signup form (the SPA's API may differ from ours). */
    private static void registerViaUi(String email, String pwd, String name, String phone, String role) {
        WebDriver d = DriverFactory.create(true);
        JavascriptExecutor js = (JavascriptExecutor) d;
        try {
            d.get(BASE + "/");
            new WebDriverWait(d, Duration.ofSeconds(15))
                    .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-root")));
            Thread.sleep(1200);
            js.executeScript("history.pushState({}, '', '/register'); window.dispatchEvent(new PopStateEvent('popstate'));");
            Thread.sleep(1500);

            d.findElement(By.cssSelector("input[type='text'].input")).sendKeys(name);
            d.findElement(By.cssSelector("input[type='email'].input")).sendKeys(email);
            d.findElement(By.cssSelector("input[type='tel'].input")).sendKeys(phone);
            d.findElement(By.cssSelector("input[type='password'].input")).sendKeys(pwd);

            if ("SELLER".equalsIgnoreCase(role)) {
                List<WebElement> roleBtns = d.findElements(By.cssSelector("button.role-btn"));
                for (WebElement b : roleBtns) {
                    if (b.getText().trim().equalsIgnoreCase("Seller")) {
                        js.executeScript("arguments[0].click();", b);
                        break;
                    }
                }
            }
            Thread.sleep(300);
            // Use JS click to bypass the chat FAB overlay that intercepts native clicks
            WebElement submitBtn = d.findElement(By.cssSelector("button.register-btn"));
            js.executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", submitBtn);
            Thread.sleep(2500);
            String currentUrl = d.getCurrentUrl();
            System.out.println("  register " + role + " " + email + " → landed on " + currentUrl);
        } catch (Exception e) {
            System.out.println("  register error: " + e.getMessage());
        } finally {
            d.quit();
        }
    }

    private static void approveAllPendingSellersViaApi() {
        try {
            java.net.http.HttpClient c = java.net.http.HttpClient.newHttpClient();
            String login = "{\"email\":\"admin@zuply.in\",\"password\":\"Admin@123\"}";
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://urban-space-winner-v666rvvrj557cp9r7-4200.app.github.dev/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(login))
                    .build();
            String body = c.send(req, java.net.http.HttpResponse.BodyHandlers.ofString()).body();
            String token = body.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
            java.net.http.HttpRequest list = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://urban-space-winner-v666rvvrj557cp9r7-4200.app.github.dev/api/admin/sellers"))
                    .header("Authorization", "Bearer " + token).GET().build();
            String sellers = c.send(list, java.net.http.HttpResponse.BodyHandlers.ofString()).body();
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\{[^}]*\"id\":(\\d+)[^}]*\"verificationStatus\":\"PENDING\"[^}]*\\}").matcher(sellers);
            int approved = 0;
            while (m.find()) {
                String sid = m.group(1);
                java.net.http.HttpRequest ap = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create("https://urban-space-winner-v666rvvrj557cp9r7-4200.app.github.dev/api/admin/sellers/" + sid + "/approve"))
                        .header("Authorization", "Bearer " + token)
                        .method("PATCH", java.net.http.HttpRequest.BodyPublishers.noBody()).build();
                c.send(ap, java.net.http.HttpResponse.BodyHandlers.ofString());
                approved++;
            }
            System.out.println("  approved " + approved + " pending sellers via API");
        } catch (Exception e) {
            System.out.println("  seller-approve error: " + e.getMessage());
        }
    }
}
