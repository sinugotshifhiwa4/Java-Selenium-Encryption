package com.encryption.tests;

import com.encryption.utils.ConfigUat;
import com.encryption.utils.SecretKeyReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.crypto.SecretKey;

import static com.encryption.config.EncryptionUtil.decrypt;


/**
 * Utility class for reading secret keys from properties files.
 *
 * <p>
 * Before running this test, create a file named 'secret_key.properties' under the main resources directory.
 * Then you need to encrypt your credentials before you run the test.
 * </p>
 */

public class DemoTest {

    WebDriver driver;

    @BeforeMethod
    public void setup(){

        ChromeOptions co = new ChromeOptions();
        co.addArguments("--incognito");
        driver = new ChromeDriver(co);
    }

    @Test
    public void login(){

        SecretKey secretKey = SecretKeyReader.getPropertyByKey("SECRET_KEY");


        driver.get(decrypt(ConfigUat.getPropertyByKey("URL"), secretKey));

        WebElement usernameTextBox = driver.findElement(By.cssSelector("#username"));
        usernameTextBox.sendKeys(decrypt(ConfigUat.getPropertyByKey("USERNAME"), secretKey));
        WebElement passwordTextBox = driver.findElement(By.cssSelector("#password"));
        //passwordTextBox.sendKeys(decrypt(ConfigUat.getPropertyByKey("PASSWORD"), secretKey)); //login will pass, password is decrypted
        passwordTextBox.sendKeys(ConfigUat.getPropertyByKey("PSSWORD")); // Login will fail, password not decrypted
        WebElement loginButton = driver.findElement(By.cssSelector("#login"));
        loginButton.click();
    }

    @AfterMethod
    public void tearDown() throws InterruptedException {
        Thread.sleep(5000);
        driver.quit();
    }
}
