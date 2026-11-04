package com.theparida.tests;

import com.theparida.core.*;
import com.theparida.pages.BillingPage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;

public class BillingEnquiryTest extends BaseTest {
    private void requireHostedTarget() {
        String host = java.net.URI.create(driver.getCurrentUrl()).getHost();
        if (java.util.Set.of("localhost", "127.0.0.1").contains(host)) {
            throw new org.testng.SkipException("forms.js intentionally blocks local submissions; use a hosted preview for simulated-response checks");
        }
    }
    @DataProvider(name = "products")
    public Object[][] products() { return new Object[][] {
        {0, "Bakery Shop Billing Software"}, {1, "Spa & Salon Management Software"},
        {2, "CRM Software Solutions"}, {3, "Pharmacy Billing Software"},
        {4, "Retail & Supermarket Billing"}, {5, "Restaurant & Cafe Billing"}
    }; }
    @Test(dataProvider = "products")
    public void correctProductAndPriceInEnquiry(int index, String name) {
        open("billing.html");
        Assert.assertEquals(driver.findElements(By.cssSelector("article.product")).size(), 6);
        WebElement card = driver.findElements(By.cssSelector("article.product")).get(index);
        Assert.assertEquals(card.findElement(By.tagName("h2")).getText(), name);
        Assert.assertTrue(card.getText().contains("₹4,999"));
        BillingPage page = new BillingPage(driver, Config.timeout()); page.chooseProduct(index);
        Assert.assertEquals(page.selectedProduct(), name); page.close();
    }
    @DataProvider(name = "phones")
    public Object[][] phones() { return new Object[][] {
        {"", "", false}, {"98765", "98765", false}, {"1234567890", "1234567890", false},
        {"abcdef", "", false}, {"98ab76543210", "9876543210", true},
        {"9876543210123", "9876543210", true}, {"9876543210", "9876543210", true},
        {"6123456789", "6123456789", true}
    }; }
    @Test(dataProvider = "phones")
    public void phoneSanitizationAndValidation(String input, String retained, boolean expected) {
        open("billing.html"); BillingPage page = new BillingPage(driver, Config.timeout()); page.chooseProduct(0);
        page.enterPhone(input);
        Assert.assertEquals(driver.findElement(By.id("phone")).getAttribute("value"), retained);
        Assert.assertEquals(valid("phone"), expected);
    }
    @Test
    public void escapeClosesDialogAndReturnsFocus() {
        open("billing.html"); new BillingPage(driver, Config.timeout()).chooseProduct(0);
        driver.switchTo().activeElement().sendKeys(Keys.ESCAPE);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("modal")));
        Assert.assertEquals(driver.switchTo().activeElement().getAttribute("data-name"), "Bakery Shop Billing Software");
    }
    @Test
    public void reopeningClearsOldPhone() {
        open("billing.html"); BillingPage page = new BillingPage(driver, Config.timeout());
        page.chooseProduct(0); page.enterPhone("9876543210"); page.close(); page.chooseProduct(1);
        Assert.assertEquals(driver.findElement(By.id("phone")).getAttribute("value"), "");
        Assert.assertEquals(page.selectedProduct(), "Spa & Salon Management Software");
    }

    @Test
    public void simulatedSuccessShowsThanksAndAutoCloses() {
        open("billing.html"); BillingPage page = new BillingPage(driver, Config.timeout());
        page.chooseProduct(0); page.enterPhone("9876543210");
        requireHostedTarget();
        // A controlled browser response, NOT a Netlify or inbox delivery test.
        js("window.fetch = () => Promise.resolve(new Response('', {status:200}))");
        click(By.cssSelector("#form button[type='submit']"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("status"), "Thank you!"));
        Assert.assertFalse(driver.findElement(By.id("phone")).isDisplayed());
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("modal")));
        Assert.assertEquals(driver.switchTo().activeElement().getAttribute("data-name"), "Bakery Shop Billing Software");
    }
    @Test
    public void simulatedServerFailureKeepsPhoneForRetry() {
        open("billing.html"); BillingPage page = new BillingPage(driver, Config.timeout());
        page.chooseProduct(0); page.enterPhone("9876543210");
        requireHostedTarget();
        js("window.fetch = () => Promise.resolve(new Response('', {status:500}))");
        click(By.cssSelector("#form button[type='submit']"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("status"), "Unable to submit"));
        Assert.assertEquals(driver.findElement(By.id("phone")).getAttribute("value"), "9876543210");
        Assert.assertTrue(driver.findElement(By.cssSelector("#form button[type='submit']")).isEnabled());
    }
}
