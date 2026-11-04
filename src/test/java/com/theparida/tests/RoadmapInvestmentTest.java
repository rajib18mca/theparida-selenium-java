package com.theparida.tests;

import com.theparida.core.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class RoadmapInvestmentTest extends BaseTest {
    @DataProvider(name = "progress")
    public Object[][] progress() { return new Object[][] {
        {"foundation", "The foundational business of The Parida", "partnership"},
        {"lathe", "Lathe Workshop & Automobile Refurbishment and Resale", "lathe"}
    }; }
    @Test(dataProvider = "progress")
    public void progressDetailsAndOpportunityLink(String key, String title, String topic) {
        open("growth-roadmap.html"); click(By.cssSelector("[data-progress='" + key + "']"));
        Assert.assertEquals(visible(By.id("progressModalTitle")).getText(), title);
        Assert.assertTrue(driver.findElements(By.cssSelector("#progressModalList li")).size() >= 3);
        click(By.id("modalOpportunity")); wait.until(ExpectedConditions.urlContains("topic=" + topic));
        String selected = new Select(visible(By.id("topic"))).getFirstSelectedOption().getAttribute("data-topic");
        Assert.assertEquals(selected, topic);
    }
    @Test
    public void progressPopupCanBeCancelled() {
        open("growth-roadmap.html"); click(By.cssSelector("[data-progress='lathe']")); click(By.id("progressCancel"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("progressBackdrop")));
        Assert.assertEquals(driver.findElement(By.id("progressBackdrop")).getAttribute("aria-hidden"), "true");
    }
    @Test
    public void investmentContentImagesAndSessionLink() {
        open("invest.html");
        Assert.assertEquals(driver.findElements(By.cssSelector(".pattern-card img")).size(), 6);
        for (WebElement image : driver.findElements(By.cssSelector(".pattern-card img"))) {
            js("arguments[0].scrollIntoView({block:'center',behavior:'instant'})", image);
            wait.until(d -> Boolean.TRUE.equals(js("return arguments[0].complete && arguments[0].naturalWidth>0", image)));
        }
        Assert.assertTrue(driver.findElement(By.id("disclaimer-title")).getText().contains("disclaimer"));
        click(By.cssSelector("a[href='index.html?topic=investment#contact']"));
        Assert.assertEquals(new Select(visible(By.id("topic"))).getFirstSelectedOption().getText(), "Investment session");
    }
}
