package com.cts.mfrp.zuply.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
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
    private static final By AI_TAGS       = By.cssSelector(".tag, .chip-tag, [class*='tag-']");
    private static final By AI_HIGHLIGHTS = By.cssSelector(".highlight, .highlight-item, [class*='highlight']");

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

    /** Wait for the Submit-for-Review button to become clickable and return it. */
    public WebElement waitForSubmitClickable() {
        return longWait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BTN));
    }

    /** Wait until the manual-form select count matches {@code expected} and return them. */
    public List<WebElement> waitForSelectCount(int expected) {
        return longWait.until(ExpectedConditions.numberOfElementsToBe(SELECTS, expected));
    }

    /* ------------------------------------------------------------------ */
    /* AI pipeline accessors                                               */
    /* ------------------------------------------------------------------ */

    /**
     * Read the current value of the title input. Returns an empty string when the
     * AI pipeline hasn't populated it yet, the input isn't rendered, or the
     * attribute is missing.
     */
    public String generatedTitle() {
        try {
            String v = driver.findElement(TITLE_INPUT).getAttribute("value");
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
    }

    public int tagCount()       { return count(AI_TAGS); }
    public int highlightCount() { return count(AI_HIGHLIGHTS); }

    /** Value of the first select dropdown (typically Category). Empty when not populated. */
    public String firstSelectValue() {
        List<WebElement> sels = driver.findElements(SELECTS);
        if (sels.isEmpty()) return "";
        String v = sels.get(0).getAttribute("value");
        return v == null ? "" : v;
    }
}
