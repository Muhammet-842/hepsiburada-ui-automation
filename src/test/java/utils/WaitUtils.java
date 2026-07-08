package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class WaitUtils {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public WaitUtils(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getExplicitTimeoutSeconds()));
        // Sayfa bir yonlendirme (redirect) ortasindayken Selenium'un o anki execution
        // context'i gecici olarak kaybolabiliyor ("no such execution context" hatasi).
        // Bu, gercek bir hata degil, gecici bir zamanlama sorunu oldugu icin, wait bunu
        // görmezden gelip element gorunene kadar denemeye devam eder.
        this.wait.ignoring(WebDriverException.class).ignoring(NoSuchSessionException.class);
    }

    public WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public List<WebElement> waitForAllVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public boolean waitForInvisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public boolean isDisplayedQuick(By locator, int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cerez/KVKK onay banner'i varsa kapatir. Banner cikmazsa sessizce devam eder,
     * cunku her oturumda cikmayabilir.
     */
    public void dismissCookieBannerIfPresent(By cookieAcceptButton) {
        if (isDisplayedQuick(cookieAcceptButton, 10)) {
            driver.findElement(cookieAcceptButton).click();
            waitForInvisible(cookieAcceptButton);
        }
    }

    /**
     * dismissCookieBannerIfPresent normal DOM'da bulamadiginda kullanilir.
     * Bazi KVKK/cerez widget'lari shadow DOM icinde render edilir, By.id bunlari goremez.
     * checkout.hepsiburada.com gibi farkli alt alan adlari, anasayfadan farkli bir cerez onay
     * widget'i (ör. OneTrust) kullanabiliyor, dolayisiyla farkli bir id'ye sahip olabilir.
     * Bilinen tum olasi id'ler sirayla denenir, ilk bulunan tiklanir.
     */
    public void dismissCookieBannerInShadowDomIfPresent(String... possibleElementIds) {
        for (String elementId : possibleElementIds) {
            if (clickInsideShadowDomById(elementId)) {
                return;
            }
        }
        // Hic bir id eslesmedi - farkli alt alan adlari farkli widget/id kullanabiliyor.
        // Son care olarak, butonun goruntulenen metnine gore ariyoruz (ör. "Kabul Et"),
        // id ne olursa olsun butonun uzerindeki yazi genelde ayni kaliyor.
        clickInsideShadowDomByText("Kabul Et");
    }

    private boolean clickInsideShadowDomById(String elementId) {
        return clickInsideShadowDom(
                "root.querySelector('#' + CSS.escape(id))",
                elementId);
    }

    private boolean clickInsideShadowDomByText(String buttonText) {
        return clickInsideShadowDom(
                "Array.from(root.querySelectorAll('button, a, div[role=\"button\"], span'))" +
                "  .find(el => el.textContent && el.textContent.trim() === id)",
                buttonText);
    }

    private boolean clickInsideShadowDom(String matchExpression, String matchValue) {
        String script =
                "function deepFind(root, id) {" +
                "  const direct = " + matchExpression + ";" +
                "  if (direct) return direct;" +
                "  const all = root.querySelectorAll('*');" +
                "  for (const node of all) {" +
                "    if (node.shadowRoot) {" +
                "      const found = deepFind(node.shadowRoot, id);" +
                "      if (found) return found;" +
                "    }" +
                "  }" +
                "  return null;" +
                "}" +
                "const el = deepFind(document, arguments[0]);" +
                "if (el) { el.click(); return true; }" +
                "return false;";
        try {
            // Kalici profil sayesinde banner genelde ilk calistirmadan sonra hic cikmiyor;
            // JS kontrolu kendisi ani oldugu icin kisa bir timeout yeterli, gereksiz yere
            // her calistirmada uzun sure beklenmesini onler.
            return new WebDriverWait(driver, Duration.ofSeconds(3)).until(d ->
                    (Boolean) ((JavascriptExecutor) d).executeScript(script, matchValue));
        } catch (Exception e) {
            // Banner hic cikmadiysa sessizce devam edilir.
            return false;
        }
    }
}
