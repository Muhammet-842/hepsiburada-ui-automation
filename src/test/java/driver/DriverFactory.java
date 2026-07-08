package driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import utils.ConfigReader;

import java.io.File;

/**
 * ChromeOptions'i kurup somut bir WebDriver ornegi uretir. Driver'in kendisi (yasam dongusu,
 * ThreadLocal saklama) Driver sinifinda; burada sadece "nasil baslatilir" mantigi var.
 */
public class DriverFactory {

    public static WebDriver createDriver() {
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
        // Config'te relative bir yol verilse bile, Chrome bunu kendi calisma dizinine gore
        // yorumlayip acilamayabiliyor ("Chrome instance exited" hatasi), bu yuzden yolu
        // her zaman mutlak hale getiriyoruz.
        String profileDir = new File(ConfigReader.getChromeProfileDir()).getAbsolutePath();
        options.addArguments("--user-data-dir=" + profileDir);
        options.addArguments("--profile-directory=Default");

        // headless=true config.properties/env ile ac -> CI ortaminda kullanilir.
        if (ConfigReader.isHeadless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }

        return new ChromeDriver(options);
    }
}
