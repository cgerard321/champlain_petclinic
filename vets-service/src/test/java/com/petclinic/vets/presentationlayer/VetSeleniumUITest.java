package com.petclinic.vets.presentationlayer;

import io.github.bonigarcia.seljup.SeleniumExtension;
import org.apache.commons.io.FileUtils;
import org.aspectj.util.FileUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.event.annotation.AfterTestMethod;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(SeleniumExtension.class)
public class VetSeleniumUITest {
    @LocalServerPort
    int randomServerPort;

    ChromeDriver driver;
    private final String SCREENSHOTS = "./src/test/screenshots/";

    public VetSeleniumUITest(ChromeDriver driver){
        this.driver = driver;

        DesiredCapabilities dc = new DesiredCapabilities();
        dc.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
        System.setProperty("sel.jup.screenshot.at.the.end.of.tests","whenfailure");
        System.setProperty("sel.jup.screenshot.format", "png");
        System.setProperty("sel.jup.output.folder","./src/test/failureScreenshots/");
    }

    public static void takeSnapShot(WebDriver webDriver, String fileWithPath) throws Exception{

        //Convert web driver object to TakeScreenshot
        TakesScreenshot scrShot = ((TakesScreenshot) webDriver);
        //call get Screenshot method to create actual image file
        File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
        //Move image file to new destination
        File DestFile = new File(fileWithPath);
        //Copy file at destination
        FileUtils.copyFile(SrcFile,DestFile);
    }

    @BeforeEach
    public void setup() throws InterruptedException {
        driver.get("http://localhost:8080/#!/vets");
        driver.manage().window().maximize();

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @AfterTestMethod
    public void screenShot() throws Exception{
        driver.quit();
    }

    @Test
    @DisplayName("Test_Vet_Modal_Information_Presence")
    public void testModalAppear(TestInfo testInfo) throws Exception{
        String method = testInfo.getDisplayName();
        boolean error = false;
        //assert
        //open web page
        try {
            Actions actions = new Actions(driver);
            WebElement info = driver.findElement(By.className("info"));
            actions.moveToElement(info);
            actions.click().build().perform();
            Thread.sleep(1000);
            WebElement modal = driver.findElement(By.className("modalOn"));
            assertThat(modal.isDisplayed(), is(true));
        }catch (AssertionError e){
            e.printStackTrace();
            error = true;
            throw new AssertionError(e);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        finally {
            if(error == false) {
                takeSnapShot(driver, SCREENSHOTS + "/pass/" + method + "_" + System.currentTimeMillis() + ".png");
            }
            else{
                takeSnapShot(driver, SCREENSHOTS+"/fail/"+method+"_"+System.currentTimeMillis()+".png");
            }
        }
    }
}
