package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ElementRepository;
import utils.WaitUtils;

public class LoginPage {

    private final WebDriver driver;
    private final WaitUtils waitUtils;

    private final By usernameInput = ElementRepository.get("usernameInput");
    private final By passwordInput = ElementRepository.get("passwordInput");
    private final By submitButton = ElementRepository.get("submitButton");

    // Login sonrasi header'da "Hesabim / <ad soyad>" gosteren link.
    private final By loggedInUserIndicator = ElementRepository.get("loggedInUserIndicator");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
    }

    public void login(String username, String password) {
        waitUtils.waitForVisible(usernameInput).sendKeys(username);
        driver.findElement(passwordInput).sendKeys(password);
        waitUtils.waitForClickable(submitButton).click();
    }

    public boolean isLoginSuccessful() {
        // data-test-id="account" login oncesinde de ("Giris Yap" yazisiyla) mevcut oldugu
        // icin sadece elementin varligi degil, metninin "Hesabim" icerip icermedigi kontrol edilir.
        if (!waitUtils.isDisplayedQuick(loggedInUserIndicator, 10)) {
            return false;
        }
        return driver.findElement(loggedInUserIndicator).getText().contains("Hesabım");
    }
}
