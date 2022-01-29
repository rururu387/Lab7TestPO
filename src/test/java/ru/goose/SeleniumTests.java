package ru.goose;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SeleniumTests
{
    private static WebDriver driver;

    @BeforeEach
    public void setup()
    {
        System.setProperty(ConfigProperties.getProperty("driverName"), ConfigProperties.getProperty("driverPath"));
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://owa.miet.ru/owa/");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
    }

    @Test
    public void errorLogin()
    {
        login("12345"); // Password is incorrect
        String error_message = driver.findElement(By.id("trInvCrd")).getText();
        Assertions.assertTrue(error_message.contains("Введено неправильное имя пользователя или пароль. Повторите ввод."));
    }

    @Test
    public void correctLogin()
    {
        login();
        Assertions.assertTrue(driver.findElement(By.id("lo")).getText().equals("Выйти"));
    }

    private void login()
    {
        login(ConfigProperties.getProperty("password"));
    }

    private void login(String password)
    {
        driver.findElement(By.id("username")).sendKeys(ConfigProperties.getProperty("login"));
        driver.findElement(By.id("password")).sendKeys(password);
        if ( !driver.findElement(By.id("chkBsc")).isSelected() )
        {
            driver.findElement(By.id("chkBsc")).click();
        }
        driver.findElement(By.xpath("//input[@type='submit']")).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
    }

    private void navigateToMessage(int messageNumber)
    {
        int messagesOnPageAmount = Integer.parseInt(ConfigProperties.getProperty("messagesOnPage"));
        int pageNumber = messageNumber / messagesOnPageAmount;

        for (int i = 0; i < pageNumber; i++)
        {
            navigateToNextPage();
        }

        int messageOnPageNumber = messageNumber % messagesOnPageAmount;

        WebElement wantedMessage = driver.findElement(
                By.xpath("*[@id=\"frm\"]/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/table/tbody/tr["
                        + 3 + messageOnPageNumber +"]/td[6]/h1/a"));

        wantedMessage.click();
    }

    @Test
    public void tempTest()
    {
        login();
        //navigateToNextPage(0);
    }

    @Test
    public void logout()
    {
        login();
        driver.findElement(By.id("lo")).click();
        Assertions.assertTrue(driver.getCurrentUrl().startsWith("https://owa.miet.ru/owa/auth/logoff.aspx"));
    }

    @Test
    public void checkMessage()
    {
        String assertion_str  = "Гуси лучшие! Гуси имеют лапки, клювы и даже крылья. " +
                "Вы могли не знать, но у гусей есть еще и уши с зубами, так что тебе от них нигде не скрыться. " +
                "Гуси хорошо плавают и ныряют, так что подводная база - не вариант. " +
                "Нет ни одного существа, более универсального, чем гусь: ныряющего, плавающего, бегающего и летающего. " +
                "Даже утки не такие универсальные. А лебеди не такие гордые и жесткие, как гуси. " +
                "Если лебедь нападает, то шансов выжить > 0, в отличие от гусиного нападения (0). " +
                "Гуси лучше всех! \n";
        login();
        driver.findElement(By.linkText("Гуси лучшие!")).click();
        Assertions.assertEquals("Гуси лучшие!", driver.findElement(By.className("sub")).getText());
        Assertions.assertEquals("Лаврентьев Олег Евгеньевич", driver.findElement(By.className("frm")).getText());
        Assertions.assertEquals(assertion_str, driver.findElement(By.className("bdy")).getText());
    }

    private boolean navigateToPrevPage()
    {
        // findElements() called to prevent throwing error if not found
        List<WebElement> secondPageButtonList = driver.findElements(By.id("#lnkPrvPg"));

        if (secondPageButtonList.size() == 0)
        {
            return false;
        }

        secondPageButtonList.get(0).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        return true;
    }

    private boolean navigateToNextPage()
    {
        // findElements() called to prevent throwing error if not found
        List<WebElement> secondPageButtonList = driver.findElements(By.id("#lnkNxtPg"));

        if (secondPageButtonList.size() == 0)
        {
            return false;
        }

        secondPageButtonList.get(0).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        return true;
    }

    @Test
    public void viewSecondPage()
    {
        correctLogin();
        WebElement firstPageFirstMessage = driver.findElement(By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/table/tbody/tr[3]/td[6]/h1/a"));
        firstPageFirstMessage.click();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        String firstPageFirstMessageId = driver.getCurrentUrl();
        driver.navigate().back();

        navigateToNextPage();

        WebElement firstMessageSecondPage = driver.findElement(By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/table/tbody/tr[3]/td[6]/h1/a"));
        firstMessageSecondPage.click();
        String secondPageFirstMessageId = driver.getCurrentUrl();
        Assertions.assertNotEquals(firstPageFirstMessage, firstMessageSecondPage);
    }

    @Test
    public void sendMessage()
    {
        login();

        driver.findElement(By.id("lnkHdrnewmsg")).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        driver.findElement(By.id("txtto")).sendKeys("8181033@edu.miet.ru");
        String msgTitle = "testTitle"+ LocalDateTime.now().toString();
        driver.findElement(By.id("txtsbj")).sendKeys(msgTitle);
        driver.findElement(By.name("txtbdy")).sendKeys("testMessage: sportorgs are cool, geese too");
        driver.findElement(By.id("lnkHdrsend")).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));

        driver.findElement(By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[1]/table/tbody/tr[2]/td/table/tbody/tr/td/table[1]/tbody/tr[3]/td/a")).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));

        //go to first msg

        Assertions.assertTrue(driver.findElement(By.className("sub")).getText().contains(msgTitle));
    }

    /*@AfterEach
    public void finish()
    {
        driver.quit();
    }*/
}
