package com.cts.mfrp.Zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.util.List;

/**
 * Seller's "Add Product" page at {@code /seller/upload}. The form supports
 * both AI-generated and manual product creation. Fields are placeholder-driven
 * (no name attributes).
 */
public class SellerUploadPage extends BasePage {

    private static final By HEADING       = By.xpath("//h1[normalize-space()='Add Product']");
    private static final By UPLOAD_ZONE   = By.cssSelector(".upload-zone");
    private static final By FILE_INPUT    = By.cssSelector("input[type='file'][accept='image/jpeg,image/png']");
    private static final By TITLE_INPUT   = By.cssSelector("input[type='text'].input[placeholder*='Handmade']");
    private static final By DESC_AREA     = By.cssSelector("textarea.input[placeholder*='Describe your product']");
    private static final By PRICE_INPUT   = By.cssSelector("input[type='number'].input[placeholder='e.g. 299']");
    private static final By STOCK_INPUT   = By.cssSelector("input[type='number'].input[placeholder='e.g. 50']");
    private static final By VARIATIONS_INPUT = By.cssSelector("input[type='text'].input[placeholder*='Red, Blue']");
    private static final By SELECTS       = By.cssSelector(".manual-form select.select");
    private static final By SUBMIT_BTN    = By.cssSelector("button.submit-btn");

    public SellerUploadPage(WebDriver driver) { super(driver); }

    @Override public String route() { return "/seller/upload"; }
    @Override protected By readyMarker() { return HEADING; }

    /** Drop or browse: the hidden file input takes a path directly via sendKeys. */
    public SellerUploadPage uploadImage(File img) {
        driver.findElement(FILE_INPUT).sendKeys(img.getAbsolutePath());
        return this;
    }

    public SellerUploadPage enterTitle(String title)       { type(TITLE_INPUT, title); return this; }
    public SellerUploadPage enterDescription(String desc)  { type(DESC_AREA, desc); return this; }
    public SellerUploadPage enterPrice(String price)       { type(PRICE_INPUT, price); return this; }
    public SellerUploadPage enterStock(String stock)       { type(STOCK_INPUT, stock); return this; }
    public SellerUploadPage enterVariations(String vars)   { type(VARIATIONS_INPUT, vars); return this; }

    /** The form has multiple selects (category, delivery method, etc.) — select by visible text in any of them. */
    public SellerUploadPage selectOptionByText(String visibleText) {
        for (WebElement sel : driver.findElements(SELECTS)) {
            for (WebElement opt : sel.findElements(By.tagName("option"))) {
                if (opt.getText().trim().equalsIgnoreCase(visibleText)) {
                    new Select(sel).selectByVisibleText(opt.getText());
                    return this;
                }
            }
        }
        throw new IllegalStateException("Option not found in any select: " + visibleText);
    }

    public List<WebElement> selects() { return driver.findElements(SELECTS); }

    public void submitForReview() { click(SUBMIT_BTN); }
}
