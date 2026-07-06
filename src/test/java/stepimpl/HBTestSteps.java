package stepimpl;

import com.thoughtworks.gauge.Step;
import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import pages.CartPage;
import pages.HomePage;
import pages.LoginPage;
import pages.ProductDetailPage;
import pages.SearchResultsPage;
import utils.ConfigReader;
import utils.DriverManager;

public class HBTestSteps {

    private HomePage homePage;
    private LoginPage loginPage;
    private SearchResultsPage searchResultsPage;
    private ProductDetailPage productDetailPage;
    private CartPage cartPage;
    private String addedProductTitle;

    private WebDriver driver() {
        return DriverManager.getDriver();
    }

    @Step("Kullanici tarayiciyi acar ve anasayfaya gider")
    public void openHomePage() {
        homePage = new HomePage(driver());
        homePage.open(ConfigReader.getBaseUrl());
    }

    @Step("Kullanici sag ustteki Giris Yap butonuna tiklar")
    public void clickLoginButton() {
        loginPage = homePage.clickLoginButton();
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

        loginPage.login(username, password);
    }

    @Step("Login isleminin basarili oldugunu dogrular")
    public void verifyLoginSuccessful() {
        Assert.assertTrue(
                "Login basarisiz: kullanici adi sayfada gorunmuyor. Kullanici adi/sifre gecersiz olabilir ya da locator degismis olabilir.",
                loginPage.isLoginSuccessful());
    }

    @Step("Kullanici arama kutusuna <aramaTerimi> yazip arar")
    public void searchFor(String aramaTerimi) {
        homePage.searchFor(aramaTerimi);
    }

    @Step("Arama sonuclarinin goruntulendigini dogrular")
    public void verifySearchResultsDisplayed() {
        searchResultsPage = new SearchResultsPage(driver());
        Assert.assertTrue(
                "Arama sonuc listesi bos geldi veya sayfa beklenen surede yuklenmedi.",
                searchResultsPage.areResultsDisplayed());
    }

    @Step("Kullanici ikinci satirdaki ilk urune tiklar")
    public void clickSecondRowFirstProduct() {
        productDetailPage = searchResultsPage.getProductAt(2, 1);
        // Sepette dogrulama icin arama terimi yerine urunun gercek basligini kullaniyoruz:
        // her urunun basligi arama terimini (ör. "bilgisayar") icermeyebilir (bazilari "Laptop" diyor).
        addedProductTitle = productDetailPage.getProductTitle();
    }

    @Step("Kullanici Sepete Ekle butonuna tiklar ve onay mesajini bekler")
    public void addProductToCart() {
        productDetailPage.addToCart();
    }

    @Step("Kullanici sepeti acar")
    public void openCart() {
        cartPage = homePage.openCart();
    }

    @Step("Sepette eklenen urunun goruntulendigini dogrular <aramaTerimi>")
    public void verifyProductInCart(String aramaTerimi) {
        // Arama terimi (ör. "bilgisayar") her urun basliginda gecmeyebilir (bazilari "Laptop" diyor),
        // bu yuzden sepette gercekten eklenen urunun basligini ariyoruz.
        Assert.assertTrue(
                "Sepette eklenen urun ('" + addedProductTitle + "') bulunamadi. "
                        + "Sepete ekleme basarisiz olmus olabilir veya sepet locator'i degismis olabilir.",
                cartPage.containsProduct(addedProductTitle));

        System.out.println("Sepete eklenen urun: " + addedProductTitle);
    }
}
