package com.cts.mfrp.Zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/** Public product listing at {@code /products}. */
public class ProductsPage extends BasePage {

    private static final By HEADING       = By.xpath("//h1[normalize-space()='All Products']");
    private static final By SEARCH_INPUT  = By.cssSelector("input.search-input");
    private static final By SEARCH_BTN    = By.cssSelector("button.search-btn");
    private static final By SORT_SELECT   = By.cssSelector("select.sort-select");
    private static final By PRODUCT_CARDS = By.cssSelector("[class*='product-card'], .card, .grid > *");
    private static final By ADD_TO_CART_BTNS = By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'add to cart')]");

    public ProductsPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/products"; }
    @Override protected By readyMarker() { return HEADING; }

    public ProductsPage search(String keyword) {
        type(SEARCH_INPUT, keyword);
        click(SEARCH_BTN);
        return this;
    }

    public ProductsPage sortBy(String visibleOption) {
        new Select(waitVisible(SORT_SELECT)).selectByVisibleText(visibleOption);
        return this;
    }

    public int productCount() { return driver.findElements(PRODUCT_CARDS).size(); }

    public void addFirstToCart() {
        driver.findElements(ADD_TO_CART_BTNS).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No 'Add to cart' button visible"))
                .click();
    }
}
