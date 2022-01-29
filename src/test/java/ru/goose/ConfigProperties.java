package ru.goose;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigProperties
{
    protected static FileInputStream fileInputStream;
    protected static Properties PROPERTIES;
    static
    {
        try
        {
            //указание пути до файла с настройками
            fileInputStream = new FileInputStream("src/test/resources/config.properties");
            PROPERTIES = new Properties();
            PROPERTIES.load(fileInputStream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            //обработка возможного исключения (нет файла и т.п.)
        }
        finally
        {
            if (fileInputStream != null)
            {
                try
                {
                    fileInputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * метод для возврата строки со значением из файла с настройками
     */
    public static String getProperty(String key)
    {
        return PROPERTIES.getProperty(key);
    }

    public static RemoteWebDriver getDriver()
    {
        switch (PROPERTIES.getProperty("driverName"))
        {
            case "webdriver.opera.driver" ->
                    {
                        return new OperaDriver();
                    }
            case "webdriver.chrome.driver" ->
                    {
                        return new ChromeDriver();
                    }

            default -> throw new IllegalStateException("Could not determine browser type");
        }
    }
}