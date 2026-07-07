package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import utils.ElementRepository;
import utils.WaitUtils;

import java.time.Duration;

public class HomePage {

    private final WebDriver driver;
    private final WaitUtils waitUtils;

    // "Giris Yap" hover ile acilan bir menu icinde gosteriliyor.
    private final By accountMenuTrigger = ElementRepository.get("accountMenuTrigger");
    private final By loginLink = ElementRepository.get("loginLink");

    private final By searchInput = ElementRepository.get("searchInput");

    private static final String CART_URL = "https://checkout.hepsiburada.com/sepetim";

    // Cerez/KVKK onay banner'i shadow DOM icinde render ediliyor - cikarsa kapatilir, cikmazsa hata vermez.
    private static final String COOKIE_ACCEPT_BUTTON_ID = "hb-accept-all";

    // Kalici Chrome profili login oturumunu hatirladigi icin, her test her seferinde
    // ayni (logged-out) baslangic durumundan baslasin diye once cikis yapilir.
    // 2FA/SMS "guvenilir cihaz" durumu logout'tan etkilenmez, profilde kalici kalir.
    private static final String LOGOUT_URL =
            "https://www.hepsiburada.com/uyelik/cikis?ReturnUrl=https%3A%2F%2Fwww.hepsiburada.com%2F";

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
    }

    public void open(String baseUrl) {
        // LOGOUT_URL zaten ReturnUrl=anasayfa parametresiyle otomatik anasayfaya yonlendiriyor,
        // bu yuzden ayrica driver.get(baseUrl) cagirmak gereksiz bir ikinci tam sayfa yuklemesiydi.
        driver.get(LOGOUT_URL);
        waitUtils.waitForVisible(accountMenuTrigger);
        dismissCookieBannerIfPresent();
    }

    public void dismissCookieBannerIfPresent() {
        waitUtils.dismissCookieBannerInShadowDomIfPresent(COOKIE_ACCEPT_BUTTON_ID);
    }

    public LoginPage clickLoginButton() {
        WebElement trigger = waitUtils.waitForVisible(accountMenuTrigger);
        // Gercek (CDP tabanli) mouse hareketi + menunun acilmasi icin kisa bir bekleme (dwell time).
        // Bazi dropdown'lar mouseenter sonrasi bir animasyon/timeout ile aciliyor, aninda kontrol
        // edilirse henuz DOM'a eklenmemis/gorunur olmamis olabiliyor.
        new Actions(driver)
                .moveToElement(trigger)
                .pause(Duration.ofMillis(800))
                .perform();
        // Ek guvence olarak JS ile de mouseover/mouseenter dispatch ediyoruz.
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new MouseEvent('mouseover', {bubbles: true}));" +
                "arguments[0].dispatchEvent(new MouseEvent('mouseenter', {bubbles: false}));",
                trigger);
        waitUtils.waitForClickable(loginLink).click();
        return new LoginPage(driver);
    }

    public void searchFor(String searchTerm) {
        // Arama ikonu sitede bugli/calismiyor - arama kutusuna yazip Enter ile aratiyoruz.
        // Sayfa acilirken React input'u kisa bir sure sonra yeniden render edebiliyor
        // (StaleElementReferenceException), bu yuzden birkac deneme yapiyoruz.
        StaleElementReferenceException lastError = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                WebElement input = waitUtils.waitForVisible(searchInput);
                ((JavascriptExecutor) driver).executeScript("arguments[0].focus();", input);
                input.sendKeys(searchTerm);
                input.sendKeys(Keys.ENTER);
                return;
            } catch (StaleElementReferenceException e) {
                lastError = e;
            }
        }
        throw lastError;
    }

    public CartPage openCart() {
        driver.get(CART_URL);
        return new CartPage(driver);
    }
}
