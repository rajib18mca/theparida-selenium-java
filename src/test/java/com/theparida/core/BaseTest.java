package com.theparida.core;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.edge.*;
import org.openqa.selenium.support.ui.*;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.*;

public abstract class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        // A new browser for every case prevents state leaking between tests.
        String browser = Config.get("browser").toLowerCase();
        if (browser.equals("chrome")) {
            ChromeOptions options = new ChromeOptions();
            if (Config.flag("headless")) options.addArguments("--headless=new");
            if (Config.flag("noSandbox")) options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
            if (!Config.get("browserBinary").isBlank()) options.setBinary(Config.get("browserBinary"));
            driver = new ChromeDriver(options); // Selenium Manager resolves the driver.
        } else if (browser.equals("edge")) {
            EdgeOptions options = new EdgeOptions();
            if (Config.flag("headless")) options.addArguments("--headless=new");
            if (Config.flag("noSandbox")) options.addArguments("--no-sandbox");
            driver = new EdgeDriver(options);
        } else throw new IllegalArgumentException("Use -Dbrowser=chrome or -Dbrowser=edge");
        driver.manage().window().setSize(new Dimension(1440, 1000));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(Config.timeout()));
        wait = new WebDriverWait(driver, Duration.ofSeconds(Config.timeout()));
        Reporter.log("Target: " + Config.baseUrl() + "; browser: " + browser, true);
    }

    protected void open(String path) {
        driver.get(Config.baseUrl() + path);
        wait.until(d -> "complete".equals(js("return document.readyState")));
        // Preserve actual validation handlers, but block outgoing form writes in this tab.
        js("""
            window.__blockedWrites = 0;
            const realFetch = window.fetch.bind(window);
            window.fetch = (input, init = {}) => {
              const method = String(init.method || (input && input.method) || 'GET').toUpperCase();
              if (!['GET', 'HEAD'].includes(method)) {
                window.__blockedWrites++;
                return Promise.reject(new Error('Automation blocked a write request'));
              }
              return realFetch(input, init);
            };
            const realOpen = XMLHttpRequest.prototype.open;
            const realSend = XMLHttpRequest.prototype.send;
            XMLHttpRequest.prototype.open = function(method, ...args) {
              this.__automationMethod = String(method).toUpperCase();
              return realOpen.call(this, method, ...args);
            };
            XMLHttpRequest.prototype.send = function(...args) {
              if (!['GET', 'HEAD'].includes(this.__automationMethod)) {
                window.__blockedWrites++;
                throw new Error('Automation blocked an XHR write');
              }
              return realSend.apply(this, args);
            };
            document.addEventListener('submit', event => event.preventDefault(), true);
            HTMLFormElement.prototype.submit = function() { window.__blockedWrites++; };
            navigator.sendBeacon = () => { window.__blockedWrites++; return false; };
            """);
    }
    protected Object js(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }
    protected WebElement visible(By by) { return wait.until(ExpectedConditions.visibilityOfElementLocated(by)); }
    protected void click(By by) {
        WebElement element = visible(by);
        js("arguments[0].scrollIntoView({block:'center',behavior:'instant'})", element);
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }
    protected void type(String id, String value) {
        WebElement field = visible(By.id(id));
        field.clear();
        if (!value.isEmpty()) field.sendKeys(value);
    }
    protected boolean valid(String id) {
        return Boolean.TRUE.equals(js("return arguments[0].checkValidity()", driver.findElement(By.id(id))));
    }

    @AfterMethod(alwaysRun = true)
    public void finish(ITestResult result) {
        if (driver == null) return;
        try {
            if (!result.isSuccess()) {
                Path dir = Path.of("target", "screenshots");
                Files.createDirectories(dir);
                String name = result.getTestClass().getRealClass().getSimpleName() + "-"
                        + result.getMethod().getMethodName() + "-" + System.nanoTime() + ".png";
                Files.write(dir.resolve(name), ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
                Reporter.log("Failure screenshot: " + dir.resolve(name), true);
            }
        } catch (IOException | WebDriverException e) {
            Reporter.log("Could not save failure screenshot: " + e.getMessage(), true);
        } finally { driver.quit(); driver = null; }
    }
}
