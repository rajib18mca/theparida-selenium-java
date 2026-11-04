package com.theparida.pages;

import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

public final class CareersPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    public CareersPage(WebDriver driver, int timeout) {
        this.driver = driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
    }
    public void expand() {
        WebElement toggle = driver.findElement(By.id("formToggle"));
        if (!"true".equals(toggle.getAttribute("aria-expanded"))) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center',behavior:'instant'})", toggle);
            toggle.click();
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("applicationForm")));
    }
    public void selectPosition(String role) { new Select(driver.findElement(By.id("position"))).selectByVisibleText(role); }
    public String selectedPosition() { return new Select(driver.findElement(By.id("position"))).getFirstSelectedOption().getText(); }
}
