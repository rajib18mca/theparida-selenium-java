package com.theparida.tests;

import com.theparida.core.*;
import com.theparida.pages.CareersPage;
import java.nio.file.*;
import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class CareersTest extends BaseTest {
    private CareersPage application() {
        open("careers.html"); CareersPage page = new CareersPage(driver, Config.timeout()); page.expand(); return page;
    }
    @DataProvider(name = "roles")
    public Object[][] roles() { return new Object[][] {
        {"senior", "Senior Lathe Operator"}, {"junior", "Junior Lathe Operator"},
        {"sales-executive", "Sales Executive"}, {"assistant-manager", "Assistant Manager"}
    }; }
    @Test(dataProvider = "roles")
    public void roleButtonSelectsPosition(String key, String expected) {
        open("careers.html"); click(By.cssSelector(".select-role[data-role='" + key + "']"));
        Assert.assertEquals(new CareersPage(driver, Config.timeout()).selectedPosition(), expected);
        Assert.assertEquals(driver.findElement(By.id("formToggle")).getAttribute("aria-expanded"), "true");
    }
    @Test
    public void applicationRequiresPositionNamePhoneEmailAndResume() {
        application();
        for (String id : new String[] {"position", "fullname", "phone", "email", "resume"}) {
            Assert.assertFalse(valid(id), id + " must reject missing input");
        }
    }
    @DataProvider(name = "experience")
    public Object[][] experience() { return new Object[][] {{"0", true}, {"5", true}, {"-1", false}, {"1.5", false}}; }
    @Test(dataProvider = "experience")
    public void experienceAllowsOnlyNonNegativeWholeYears(String years, boolean expected) {
        application(); type("experience", years); Assert.assertEquals(valid("experience"), expected);
    }
    @Test
    public void careerPhoneRemovesLettersAndLimitsLength() {
        application(); type("phone", "98ab76543210123");
        Assert.assertEquals(driver.findElement(By.id("phone")).getAttribute("value"), "9876543210");
        Assert.assertTrue(valid("phone")); type("phone", "98765"); Assert.assertFalse(valid("phone"));
    }
    @Test
    public void invalidResumeIsRejectedAndCanBeReplaced() throws Exception {
        CareersPage page = application(); page.selectPosition("General / Other Application");
        type("fullname", "Automation Test User"); type("phone", "9876543210"); type("email", "qa@example.com");
        Path bad = Files.createTempFile("resume-test-", ".txt");
        Path good = Path.of("src", "test", "resources", "files", "sample-resume.pdf").toAbsolutePath();
        try {
            Files.writeString(bad, "Not an allowed resume format.");
            driver.findElement(By.id("resume")).sendKeys(bad.toAbsolutePath().toString());
            // The real handler returns before fetch for invalid files. No request is sent.
            click(By.cssSelector("#applicationForm button[type='submit']"));
            Assert.assertFalse(valid("resume"));
            Assert.assertTrue(driver.findElement(By.id("resume")).getAttribute("validationMessage").contains("PDF"));
            driver.findElement(By.id("resume")).sendKeys(good.toString());
            Assert.assertTrue(valid("resume"), "Changing file must clear previous custom error");
            Assert.assertTrue(Boolean.TRUE.equals(js("return document.getElementById('applicationForm').checkValidity()")));
            // Never submit the valid replacement.
        } finally { Files.deleteIfExists(bad); }
    }
    @Test
    public void oversizedResumeIsRejected() throws Exception {
        CareersPage page = application(); page.selectPosition("General / Other Application");
        type("fullname", "Automation Test User"); type("phone", "9876543210"); type("email", "qa@example.com");
        Path large = Files.createTempFile("resume-large-", ".pdf");
        try {
            Files.write(large, new byte[5 * 1024 * 1024 + 1]);
            driver.findElement(By.id("resume")).sendKeys(large.toAbsolutePath().toString());
            click(By.cssSelector("#applicationForm button[type='submit']"));
            Assert.assertFalse(valid("resume"));
            Assert.assertTrue(driver.findElement(By.id("resume")).getAttribute("validationMessage").contains("5 MB"));
        } finally { Files.deleteIfExists(large); }
    }
}
