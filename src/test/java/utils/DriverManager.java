package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public class DriverManager {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void initDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");

        // Hepsiburada gibi siteler navigator.webdriver=true ve otomasyon banner'ini
        // kontrol ederek bot tespiti yapabiliyor (login sirasinda "N1E2" hata kodu gibi).
        // Bu bayraklari gizleyerek gercek bir kullanici tarayicisina daha yakin davraniyoruz.
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        // Kalici Chrome profili: SMS/2FA dogrulamasi bir kere manuel yapildiktan sonra
        // Hepsiburada bu tarayici profilini "guvenilir cihaz" olarak hatirlar, boylece
        // sonraki testlerde her calistirmada tekrar SMS kodu istenmez.
        options.addArguments("--user-data-dir=" + ConfigReader.getChromeProfileDir());
        options.addArguments("--profile-directory=Default");

        // headless=true config.properties/env ile ac -> CI ortaminda kullanilir.
        if (ConfigReader.isHeadless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getPageLoadTimeoutSeconds()));
        driverThreadLocal.set(driver);
    }

    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver henuz baslatilmadi. BeforeScenario hook'unu kontrol edin.");
        }
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
        }
    }
}
