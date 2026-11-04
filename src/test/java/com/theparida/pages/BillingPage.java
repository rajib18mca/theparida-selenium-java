package com.theparida.pages;

import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

public final class BillingPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    public BillingPage(WebDriver driver, int timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
    }
    public void chooseProduct(int index) {
        WebElement button = driver.findElements(By.cssSelector("button.price[data-name]")).get(index);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center',behavior:'instant'})", button);
        wait.until(ExpectedConditions.elementToBeClickable(button)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modal")));
    }
    public String selectedProduct() { return driver.findElement(By.id("software")).getAttribute("value"); }
    public void enterPhone(String value) {
        WebElement phone = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("phone")));
        phone.clear();
        if (!value.isEmpty()) phone.sendKeys(value);
    }
    public void close() {
        driver.findElement(By.id("close")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("modal")));
    }
}
