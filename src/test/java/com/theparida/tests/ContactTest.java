package com.theparida.tests;

import com.theparida.core.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.*;

public class ContactTest extends BaseTest {
    @Test
    public void contactInformationAndRequiredFields() {
        open("index.html#contact");
        Assert.assertTrue(visible(By.cssSelector(".contactinfo")).getText().contains("Odisha"));
        Assert.assertEquals(driver.findElement(By.cssSelector(".contactinfo a")).getAttribute("href"), "mailto:info@theparida.com");
        for (String id : new String[] {"name", "email", "message"}) Assert.assertFalse(valid(id), id + " must reject empty input");
        Assert.assertFalse(Boolean.TRUE.equals(js("return document.getElementById('contactForm').checkValidity()")));
    }
    @DataProvider(name = "emails")
    public Object[][] emails() { return new Object[][] {{"not-an-email", false}, {"rajib@", false}, {"qa@example.com", true}}; }
    @Test(dataProvider = "emails")
    public void emailValidation(String email, boolean expected) {
        open("index.html#contact"); type("email", email); Assert.assertEquals(valid("email"), expected);
    }
    @Test
    public void completeEnquiryIsValidWithoutSubmitting() {
        open("index.html#contact");
        type("name", "Automation Test User"); type("email", "qa@example.com"); type("message", "TEST ONLY - validation check");
        Assert.assertTrue(Boolean.TRUE.equals(js("return document.getElementById('contactForm').checkValidity()")));
        // Deliberately do not click Send: this test must not send an email.
    }
    @Test
    public void investmentDeepLinkSelectsCorrectTopic() {
        open("index.html?topic=investment#contact");
        Assert.assertEquals(new Select(visible(By.id("topic"))).getFirstSelectedOption().getText(), "Investment session");
    }
}
