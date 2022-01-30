package ru.goose;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.platform.commons.util.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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
        try
        {
            login("12345"); // Password is incorrect
            String error_message = driver.findElement(By.id("trInvCrd")).getText();
            Assertions.assertTrue(error_message.contains("Введено неправильное имя пользователя или пароль. Повторите ввод."));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    public void correctLogin()
    {
        try
        {
            login();
            Assertions.assertTrue(driver.findElement(By.id("lo")).getText().equals("Выйти"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assertions.fail();
        }
        finally
        {
            logout();
        }
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

    public void logout()
    {
        driver.findElement(By.id("lo")).click();
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void logoutTest()
    {
        try
        {
            login();
            logout();
            Assertions.assertTrue(driver.getCurrentUrl().startsWith("https://owa.miet.ru/owa/auth/logoff.aspx"));
        }
        catch (Exception e)
        {
            logout();
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    public void checkMessageInbox()
    {
        try
        {
            String assertion_str = "Гуси лучшие! Гуси имеют лапки, клювы и даже крылья. " +
                    "Вы могли не знать, но у гусей есть еще и уши с зубами, так что тебе от них нигде не скрыться. " +
                    "Гуси хорошо плавают и ныряют, так что подводная база - не вариант. " +
                    "Нет ни одного существа, более универсального, чем гусь: ныряющего, плавающего, бегающего и летающего. " +
                    "Даже утки не такие универсальные. А лебеди не такие гордые и жесткие, как гуси. " +
                    "Если лебедь нападает, то шансов выжить > 0, в отличие от гусиного нападения (0). " +
                    "Гуси лучше всех! ";
            login();
            driver.findElement(By.linkText("Гуси лучшие!")).click();
            Assertions.assertEquals("Гуси лучшие!", driver.findElement(By.className("sub")).getText());
            Assertions.assertEquals("Лаврентьев Олег Евгеньевич", driver.findElement(By.className("frm")).getText());
            Assertions.assertEquals(assertion_str, driver.findElement(By.className("bdy")).getText());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assertions.fail();
        }
        finally
        {
            logout();
        }
    }

    @Test
    public void deleteMessage()
    {
        login();
        sendMessage();

        driver.findElement(
                By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[1]/table/tbody/tr[2]/td/table/tbody/tr/td/table[1]/tbody/tr[3]/td/a"))
                .click();

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.findElement(By.id("lnkHdrcheckmessages")).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));

        openMessageOnCurrentPage(0);
        String sub = driver.findElement(By.className("sub")).getText();
        String frm = driver.findElement(By.className("frm")).getText();
        String bdy = driver.findElement(By.className("bdy")).getText();
        driver.findElement(By.id("lnkHdrdelete")).click();
        driver.findElement(
                By.xpath("/html/body/form/table/tbody/tr[2]/td[1]/table/tbody/tr[2]/td/table/tbody/tr/td/table[1]/tbody/tr[4]/td/a"))
                .click();
        openMessage(0);
        Assertions.assertEquals(sub, driver.findElement(By.className("sub")).getText());
        Assertions.assertEquals(frm, driver.findElement(By.className("frm")).getText());
        Assertions.assertEquals(bdy, driver.findElement(By.className("bdy")).getText());
        logout();
}

private boolean navigateToPrevPage()
    {
        // findElements() called to prevent throwing error if not found
        List<WebElement> secondPageButtonList = driver.findElements(By.id("lnkPrvPg"));

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
        List<WebElement> secondPageButtonList = driver.findElements(By.id("lnkNxtPg"));

        if (secondPageButtonList.size() == 0)
        {
            return false;
        }

        secondPageButtonList.get(0).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        return true;
    }

    private void checkMessageOnCurrentPage(int messageNumber)
    {
        messageNumber = messageNumber + 3;
        driver.findElement(
                By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/table/tbody/tr["
                        + messageNumber + "]/td[4]/input"))
                .click();
    }

    private void openMessageOnCurrentPage(int messageNumber)
    {
        messageNumber = messageNumber + 3;
        WebElement wantedMessage = driver.findElement(
                By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/table/tbody/tr["
                        + messageNumber +"]/td[6]/h1/a"));

        wantedMessage.click();

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
    }

    private void openMessage(int messageNumber)
    {
        List<WebElement> pageRefs = driver.findElements(By.id("lnkPgNm1"));

        if (pageRefs.size() != 0)
        {
            pageRefs.get(0).click();
        }

        int messagesPerPageAmount = Integer.parseInt(ConfigProperties.getProperty("messagesPerPage"));
        int pageNumber = messageNumber / messagesPerPageAmount;

        for (int i = 0; i < pageNumber; i++)
        {
            navigateToNextPage();
        }

        int messageOnPageNumber = messageNumber % messagesPerPageAmount;

        openMessageOnCurrentPage(messageOnPageNumber);
    }

    @Test
    public void checkDifferentPagesShowDifferentMessages()
    {
        try
        {
            login();

            List<String> messageUrls = new ArrayList<>();

            do
            {
                openMessageOnCurrentPage(0);
                messageUrls.add(driver.getCurrentUrl());
                driver.navigate().back();
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
            }
            while (navigateToNextPage());

            if (driver.findElements(By.xpath("//*[@id=\"lnkLstPg\"]")).size() != 0)
            {
                Assertions.fail();
            }
            else
            {
                Set<String> messageUrlsSet = new HashSet<>(messageUrls);
                Assertions.assertEquals(messageUrls.size(), messageUrlsSet.size());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assertions.fail();
        }
        finally
        {
            logout();
        }
    }

    public String sendMessage()
    {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        driver.findElement(By.id("lnkHdrnewmsg")).click();
        driver.findElement(By.id("txtto")).sendKeys("8181033@edu.miet.ru");
        String msgTitle = "testTitle" + LocalDateTime.now().toString();
        driver.findElement(By.id("txtsbj")).sendKeys(msgTitle);
        driver.findElement(By.name("txtbdy"))
                .sendKeys("testMessage: sportorgs are cool, geese are the coolest");
        driver.findElement(By.id("lnkHdrsend")).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        return msgTitle;
    }

    public void refreshInbox() throws InterruptedException
    {
        Thread.sleep(5000);
        driver.findElement(By.id("lnkHdrcheckmessages")).click();
    }

    @Test
    public void sendMessageTest()
    {
        login();
        try
        {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
            String msgTitle = sendMessage();

            driver.findElement(By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[1]/table/tbody/tr[2]/td/table/tbody/tr/td/table[1]/tbody/tr[3]/td/a")).click();

            refreshInbox();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));

            openMessage(0);

            Assertions.assertEquals(msgTitle, driver.findElement(By.className("sub")).getText());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage() + "<----");
            e.printStackTrace();
            Assertions.fail();
        }
        finally
        {
            logout();
        }
    }

    @Test
    public void testMarkMessageAsUnread()
    {
        login();
        try
        {
            checkMessageOnCurrentPage(0);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
            driver.findElement(By.id("lnkHdrmarkunread")).click();
            WebElement messageRow = driver.findElement(By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/table/tbody/tr[3]"));
            WebElement messageIcon = driver.findElement(By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/table/tbody/tr[3]/td[2]/img"));

            Assertions.assertEquals("font-weight: bold;", messageRow.getAttribute("style"));
            Assertions.assertEquals("https://owa.miet.ru/owa/14.3.513.0/themes/basic/msg-unrd.png", messageIcon.getAttribute("src"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assertions.fail();
        }
        finally
        {
            logout();
        }
    }

    @Test
    public void testMarkMessageAsRead()
    {
        login();
        try
        {
            checkMessageOnCurrentPage(0);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
            driver.findElement(By.id("lnkHdrmarkread")).click();
            WebElement messageRow = driver.findElement(By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/table/tbody/tr[3]"));
            WebElement messageIcon = driver.findElement(By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/div/table/tbody/tr[3]/td[2]/img"));

            Assertions.assertNotEquals("font-weight: bold;", messageRow.getAttribute("style"));
            Assertions.assertEquals("https://owa.miet.ru/owa/14.3.513.0/themes/basic/msg-rd.png", messageIcon.getAttribute("src"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assertions.fail();
        }
        finally
        {
            logout();
        }
    }

    @Test
    public void testFilterMessageContainsGoose()
    {
        login();
        try
        {
            String gooseStr = "Гусь";
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));

            WebElement searchField = driver.findElement(By.id("txtSch"));
            searchField.sendKeys(gooseStr);

            WebElement searchButton = driver.findElement(By.id("schBtn"));
            searchButton.click();

            List<WebElement> messageButtons = driver.findElements(
                    By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[3]/table/tbody/tr[3]/td/div/table/tbody/tr/td/h1/a"));

            for (int i = 0; i < messageButtons.size(); i++)
            {
                WebElement messageButton = messageButtons.get(i);

                if (messageButton.getText().toLowerCase(Locale.ROOT).contains("Гусь".toLowerCase(Locale.ROOT)))
                {
                    continue;
                }

                messageButton.click();

                WebElement title = driver.findElement(
                        By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[3]/table/tbody/tr[2]/td/table/tbody/tr[1]/td/table/tbody/tr[1]/td"));

                WebElement messageText = driver.findElement(
                        By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr[3]/td/div/div/div"));
                                  //*[@id=\"frm\"]/table/tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr[3]/td/div/div/div
                WebElement dateSent = driver.findElement(
                        By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr[1]/td/table/tbody/tr[4]/td[2]"));
                                  //*[@id="frm"]/table/tbody/tr[2]/td/table/tbody/tr[2]/td/table/tbody/tr[1]/td/table/tbody/tr[4]/td[2]

                if (!title.getText().toLowerCase(Locale.ROOT).contains("Гусь".toLowerCase(Locale.ROOT))
                        && !messageText.getText().toLowerCase(Locale.ROOT).contains("Гусь".toLowerCase(Locale.ROOT)))
                {
                    System.out.println(title.getText());
                    System.out.println(dateSent.getText());
                    Assertions.fail();
                }

                driver.navigate().back();

                messageButtons = driver.findElements(
                        By.xpath("//*[@id=\"frm\"]/table/tbody/tr[2]/td[3]/table/tbody/tr[3]/td/div/table/tbody/tr/td/h1/a"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assertions.fail();
        }
        finally
        {
            logout();
        }
    }

    @AfterEach
    public void finish()
    {
        driver.quit();
    }
}
