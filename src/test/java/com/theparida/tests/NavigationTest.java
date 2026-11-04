package com.theparida.tests;

import com.theparida.core.BaseTest;
import java.util.List;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;

public class NavigationTest extends BaseTest {
    @DataProvider(name = "pages")
    public Object[][] pages() { return new Object[][] {
        {"index.html", "The Parida", "Ideas into enterprise."},
        {"billing.html", "Billing Software", "Billing & Business Software"},
        {"careers.html", "Careers", "Careers at The Parida"},
        {"growth-roadmap.html", "Growth Roadmap", "Our Growth Roadmap"},
        {"invest.html", "Investment", "Investment"}
    }; }

    @Test(dataProvider = "pages")
    public void pagesHaveTitleHeadingLogoAndNineNavigationLinks(String path, String title, String heading) {
        open(path);
        Assert.assertTrue(driver.getTitle().contains(title), "Unexpected title: " + driver.getTitle());
        Assert.assertTrue(visible(By.tagName("h1")).getText().contains(heading));
        Assert.assertEquals(driver.findElements(By.cssSelector("#navlinks a")).size(), 9);
        WebElement logo = driver.findElement(By.cssSelector("header img"));
        Assert.assertTrue(logo.isDisplayed());
        Assert.assertTrue(Boolean.TRUE.equals(js("return arguments[0].complete && arguments[0].naturalWidth>0", logo)), "Logo failed to load");
    }

    @DataProvider(name = "destinations")
    public Object[][] destinations() { return new Object[][] {
        {"Home", "index.html#home"}, {"Our Venture", "index.html#ventures"},
        {"About", "index.html#about"}, {"Our Approach", "index.html#approach"},
        {"Growth Roadmap", "growth-roadmap.html"}, {"Billing software", "billing.html"},
        {"Investment", "invest.html"}, {"Careers", "careers.html"}, {"Contact", "index.html#contact"}
    }; }
    @Test(dataProvider = "destinations")
    public void navigationOpensExpectedDestinationAndMarksItActive(String label, String destination) {
        open("index.html");
        click(By.xpath("//*[@id='navlinks']/a[normalize-space()='" + label + "']"));
        wait.until(ExpectedConditions.urlContains(destination));
        wait.until(d -> {
            List<WebElement> active = d.findElements(By.cssSelector("#navlinks a[aria-current='page']"));
            return active.size() == 1 && active.get(0).getText().equals(label);
        });
    }

    @Test(dataProvider = "pages")
    public void narrowScreenMenuOpensAndCloses(String path, String title, String heading) {
        driver.manage().window().setSize(new Dimension(390, 844));
        open(path);
        WebElement toggle = visible(By.id("menuBtn"));
        Assert.assertEquals(toggle.getAttribute("aria-expanded"), "false");
        click(By.id("menuBtn"));
        Assert.assertEquals(toggle.getAttribute("aria-expanded"), "true");
        Assert.assertTrue(visible(By.cssSelector("#navlinks a")).isDisplayed());
        click(By.id("menuBtn"));
        Assert.assertEquals(toggle.getAttribute("aria-expanded"), "false");
        Assert.assertTrue(Boolean.TRUE.equals(js("return document.documentElement.scrollWidth <= innerWidth+1")), "Horizontal overflow at 390px");
    }
}
