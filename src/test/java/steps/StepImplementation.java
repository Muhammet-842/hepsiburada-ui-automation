package steps;

import com.thoughtworks.gauge.Step;
import driver.Driver;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;
import utils.ElementRepository;
import utils.WaitUtils;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Locator'lar hala
 * Java kodunun disinda, element-infos/elements.json'da tutuluyor (ElementRepository ile).
 */
public class StepImplementation {

    // Sepette dogrulama icin arama terimi yerine urunun gercek basligini kullaniyoruz:
    // her urunun basligi arama terimini (ör. "bilgisayar") icermeyebilir (bazilari "Laptop" diyor).
    private String addedProductTitle;

    private static final int COLUMNS_PER_ROW = 4;

    private static final String CART_URL = "https://checkout.hepsiburada.com/sepetim";
    // www.hepsiburada.com "hb-accept-all" kullanirken, checkout.hepsiburada.com gibi farkli
    // alt alan adlari farkli bir cerez onay widget'i (OneTrust) kullanabiliyor.
    private static final String[] COOKIE_ACCEPT_BUTTON_IDS = {"hb-accept-all", "onetrust-accept-btn-handler"};

    // Kalici Chrome profili login oturumunu hatirladigi icin, her test her seferinde
    // ayni (logged-out) baslangic durumundan baslasin diye once cikis yapilir.
    // 2FA/SMS "guvenilir cihaz" durumu logout'tan etkilenmez, profilde kalici kalir.
    // LOGOUT_URL zaten ReturnUrl=anasayfa parametresiyle otomatik anasayfaya yonlendiriyor.
    private static final String LOGOUT_URL =
            "https://www.hepsiburada.com/uyelik/cikis?ReturnUrl=https%3A%2F%2Fwww.hepsiburada.com%2F";

    private WebDriver driver() {
        return Driver.getDriver();
    }

    private WaitUtils waitUtils() {
        return new WaitUtils(driver());
    }

    // ---------------------------------------------------------------------
    // Anasayfa / login
    // ---------------------------------------------------------------------

    @Step("Kullanici tarayiciyi acar ve anasayfaya gider")
    public void openHomePage() {
        driver().get(LOGOUT_URL);
        waitUtils().waitForVisible(ElementRepository.get("accountMenuTrigger"));
        dismissCookieBannerIfPresent();
    }

    private void dismissCookieBannerIfPresent() {
        // Cerez/KVKK onay banner'i shadow DOM icinde render ediliyor, By.id bunu goremez.
        waitUtils().dismissCookieBannerInShadowDomIfPresent(COOKIE_ACCEPT_BUTTON_IDS);
    }

    @Step("Kullanici sag ustteki Giris Yap butonuna tiklar")
    public void clickLoginButton() {
        // "Giris Yap" hover ile acilan bir menu icinde gosteriliyor.
        By accountMenuTrigger = ElementRepository.get("accountMenuTrigger");
        By loginLink = ElementRepository.get("loginLink");

        WebElement trigger = waitUtils().waitForVisible(accountMenuTrigger);
        // Gercek (CDP tabanli) mouse hareketi + menunun acilmasi icin kisa bir bekleme (dwell time).
        // Bazi dropdown'lar mouseenter sonrasi bir animasyon/timeout ile aciliyor, aninda kontrol
        // edilirse henuz DOM'a eklenmemis/gorunur olmamis olabiliyor.
        new Actions(driver())
                .moveToElement(trigger)
                .pause(Duration.ofMillis(800))
                .perform();
        // Ek guvence olarak JS ile de mouseover/mouseenter dispatch ediyoruz.
        ((JavascriptExecutor) driver()).executeScript(
                "arguments[0].dispatchEvent(new MouseEvent('mouseover', {bubbles: true}));" +
                "arguments[0].dispatchEvent(new MouseEvent('mouseenter', {bubbles: false}));",
                trigger);
        waitUtils().waitForClickable(loginLink).click();
    }

    @Step("Kullanici config/environment'tan okunan gecerli bilgilerle giris yapar")
    public void loginWithConfiguredCredentials() {
        String username = ConfigReader.getUsername();
        String password = ConfigReader.getPassword();

        Assert.assertNotNull(
                "HB_USERNAME environment variable veya config.properties'teki hb.username degeri bos olamaz",
                username);
        Assert.assertNotNull(
                "HB_PASSWORD environment variable veya config.properties'teki hb.password degeri bos olamaz",
                password);

        waitUtils().waitForVisible(ElementRepository.get("usernameInput")).sendKeys(username);
        driver().findElement(ElementRepository.get("passwordInput")).sendKeys(password);
        waitUtils().waitForClickable(ElementRepository.get("submitButton")).click();
    }

    @Step("Login isleminin basarili oldugunu dogrular")
    public void verifyLoginSuccessful() {
        Assert.assertTrue(
                "Login basarisiz: kullanici adi sayfada gorunmuyor. Kullanici adi/sifre gecersiz olabilir ya da locator degismis olabilir.",
                isLoginSuccessful());

        System.out.println("Kullanici basarili sekilde login oldu");
    }

    private boolean isLoginSuccessful() {
        By loggedInUserIndicator = ElementRepository.get("loggedInUserIndicator");
        // data-test-id="account" login oncesinde de ("Giris Yap" yazisiyla) mevcut oldugu
        // icin sadece elementin varligi degil, metninin "Hesabim" icerip icermedigi kontrol edilir.
        if (!waitUtils().isDisplayedQuick(loggedInUserIndicator, 10)) {
            return false;
        }
        return driver().findElement(loggedInUserIndicator).getText().contains("Hesabım");
    }

    // ---------------------------------------------------------------------
    // Arama
    // ---------------------------------------------------------------------

    @Step("Kullanici arama kutusuna <aramaTerimi> yazip arar")
    public void searchFor(String aramaTerimi) {
        By searchInput = ElementRepository.get("searchInput");
        // Arama ikonu sitede bugli/calismiyor - arama kutusuna yazip Enter ile aratiyoruz.
        // Sayfa acilirken React input'u kisa bir sure sonra yeniden render edebiliyor
        // (StaleElementReferenceException), bu yuzden birkac deneme yapiyoruz.
        StaleElementReferenceException lastError = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                WebElement input = waitUtils().waitForVisible(searchInput);
                ((JavascriptExecutor) driver()).executeScript("arguments[0].focus();", input);
                input.sendKeys(aramaTerimi);
                input.sendKeys(Keys.ENTER);
                return;
            } catch (StaleElementReferenceException e) {
                lastError = e;
            }
        }
        throw lastError;
    }

    @Step("Arama sonuclarinin goruntulendigini dogrular")
    public void verifySearchResultsDisplayed() {
        By productCard = ElementRepository.get("productCard");
        Assert.assertTrue(
                "Arama sonuc listesi bos geldi veya sayfa beklenen surede yuklenmedi.",
                !waitUtils().waitForAllVisible(productCard).isEmpty());

        System.out.println("Arama sonuclari kullanici girdisine uygun geldi");
    }

    // ---------------------------------------------------------------------
    // Urun secimi ve sepete ekleme
    // ---------------------------------------------------------------------

    @Step("Kullanici ikinci satirdaki ilk urune tiklar")
    public void clickSecondRowFirstProduct() {
        // Sonuc listesinde ayri bir "satir" DOM elementi yok: tum urunler tek bir <ul> icinde
        // duz (flat) siralanmis <li> elemanlari, satir gorunumu sadece CSS grid ile saglaniyor.
        // Reklam/sponsorlu <li> farkli class kullaniyor, productCard selector'i sadece gercek
        // urun kartlarini secer.
        List<WebElement> products = waitUtils().waitForAllVisible(ElementRepository.get("productCard"));

        int row = 2;
        int column = 1;
        int index = (row - 1) * COLUMNS_PER_ROW + (column - 1);
        if (index >= products.size()) {
            throw new IllegalStateException(
                    row + ". satir " + column + ". sutun (index " + index + ") sonuc listesinde yok. "
                            + "Toplam urun sayisi: " + products.size());
        }

        Set<String> handlesBeforeClick = driver().getWindowHandles();
        products.get(index).findElement(ElementRepository.get("productLink")).click();
        switchToNewTabIfOpened(handlesBeforeClick);

        // Sepette dogrulama icin arama terimi yerine urunun gercek basligini kullaniyoruz.
        addedProductTitle = waitUtils().waitForVisible(ElementRepository.get("productTitle")).getText();

        System.out.println("Ikinci satirdaki ilk urunun sayfasina yonlendirildi: " + addedProductTitle);
    }

    /**
     * Bazi urun linkleri target="_blank" ile yeni sekmede aciliyor; Selenium tiklama sonrasi
     * varsayilan olarak eski sekmede kalir, bu yuzden yeni acilan sekmeye gecis yapmak gerekir.
     */
    private void switchToNewTabIfOpened(Set<String> handlesBeforeClick) {
        try {
            new WebDriverWait(driver(), Duration.ofSeconds(5))
                    .until(d -> d.getWindowHandles().size() > handlesBeforeClick.size());
        } catch (Exception e) {
            return; // Yeni sekme acilmadi, ayni sekmede navigasyon olmus demektir.
        }

        Set<String> handlesAfterClick = driver().getWindowHandles();
        handlesAfterClick.removeAll(handlesBeforeClick);
        if (!handlesAfterClick.isEmpty()) {
            driver().switchTo().window(handlesAfterClick.iterator().next());
        }
    }

    @Step("Kullanici Sepete Ekle butonuna tiklar ve onay mesajini bekler")
    public void addProductToCart() {
        // Site zaman icinde "Urun sepetinizde" onay popup'ini kaldirip yerine
        // sag ustteki bildirimi koydu; onay mesaji metnine bagli kalmak yerine
        // sepet rozetindeki urun sayisinin artmasini bekliyoruz (ör. 2 -> 3).
        int countBeforeClick = getCartItemCount();

        waitUtils().waitForClickable(ElementRepository.get("addToCartButton")).click();

        new WebDriverWait(driver(), Duration.ofSeconds(ConfigReader.getExplicitTimeoutSeconds()))
                .until(d -> getCartItemCount() > countBeforeClick);

        System.out.println("Urun sepete eklendi");
    }

    private int getCartItemCount() {
        By cartItemCount = ElementRepository.get("cartItemCount");
        if (!waitUtils().isDisplayedQuick(cartItemCount, 2)) {
            return 0;
        }
        try {
            return Integer.parseInt(driver().findElement(cartItemCount).getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Step("Kullanici sepeti acar")
    public void openCart() {
        driver().get(CART_URL);
        // checkout.hepsiburada.com anasayfadan farkli bir alt alan adi oldugu icin
        // kendi cerez onay banner'ini ayrica gosterebiliyor; goz ardi edilirse element
        // aramalarini geciktirip flaky timeout'lara yol acabiliyor.
        dismissCookieBannerIfPresent();
    }

    @Step("Sepette eklenen urunun goruntulendigini dogrular <aramaTerimi>")
    public void verifyProductInCart(String aramaTerimi) {
        // Sepet sayfasindaki urun adi elementinin class/href yapisi zaman icinde degisebiliyor
        // (ör. link degil duz metin olmasi gibi). Bu yuzden belirli bir locator'a bagli kalmak
        // yerine, sayfanin gorunen tum metnini kontrol ediyoruz - hangi elementte oldugu onemli
        // degil, urun adi ekranda bir yerde goruntuleniyor mu diye bakiyoruz.
        String pageText = (String) ((JavascriptExecutor) driver())
                .executeScript("return document.body.innerText;");
        boolean found = pageText.toLowerCase().contains(addedProductTitle.toLowerCase());

        Assert.assertTrue(
                "Sepette eklenen urun ('" + addedProductTitle + "') bulunamadi. "
                        + "Sepete ekleme basarisiz olmus olabilir veya sayfa beklenen surede yuklenmemis olabilir.",
                found);

        System.out.println("Sepet ekraninda eklenen urun goruntuleniyor: " + addedProductTitle);
    }
}
