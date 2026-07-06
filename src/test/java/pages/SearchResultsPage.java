package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ElementRepository;
import utils.WaitUtils;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class SearchResultsPage {

    private final WebDriver driver;
    private final WaitUtils waitUtils;

    // Sonuc listesinde ayri bir "satir" DOM elementi yok: tum urunler tek bir <ul> icinde
    // duz (flat) siralanmis <li> elemanlari, satir gorunumu sadece CSS grid ile saglaniyor.
    // Reklam/sponsorlu <li> farkli class kullaniyor, bu selector sadece gercek urun kartlarini secer.
    private final By productCard = ElementRepository.get("productCard");
    private final By productLink = ElementRepository.get("productLink");

    // Ekranda gorunen sutun sayisi (satir/sutun hesaplamasi icin).
    private static final int COLUMNS_PER_ROW = 4;

    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
    }

    public boolean areResultsDisplayed() {
        return !waitUtils.waitForAllVisible(productCard).isEmpty();
    }

    /**
     * 1-index'li satir/sutun ile urun secer (row=2, column=1 -> ikinci satirdaki ilk urun).
     * Duz listede bu, ((row-1) * COLUMNS_PER_ROW + (column-1)). index'teki karta karsilik gelir.
     */
    public ProductDetailPage getProductAt(int row, int column) {
        List<WebElement> products = waitUtils.waitForAllVisible(productCard);

        int index = (row - 1) * COLUMNS_PER_ROW + (column - 1);
        if (index >= products.size()) {
            throw new IllegalStateException(
                    row + ". satir " + column + ". sutun (index " + index + ") sonuc listesinde yok. "
                            + "Toplam urun sayisi: " + products.size());
        }

        Set<String> handlesBeforeClick = driver.getWindowHandles();
        products.get(index).findElement(productLink).click();
        switchToNewTabIfOpened(handlesBeforeClick);
        return new ProductDetailPage(driver);
    }

    /**
     * Bazi urun linkleri target="_blank" ile yeni sekmede aciliyor; Selenium tiklama sonrasi
     * varsayilan olarak eski sekmede kalir, bu yuzden yeni acilan sekmeye gecis yapmak gerekir.
     */
    private void switchToNewTabIfOpened(Set<String> handlesBeforeClick) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> d.getWindowHandles().size() > handlesBeforeClick.size());
        } catch (Exception e) {
            return; // Yeni sekme acilmadi, ayni sekmede navigasyon olmus demektir.
        }

        Set<String> handlesAfterClick = driver.getWindowHandles();
        handlesAfterClick.removeAll(handlesBeforeClick);
        if (!handlesAfterClick.isEmpty()) {
            driver.switchTo().window(handlesAfterClick.iterator().next());
        }
    }
}
