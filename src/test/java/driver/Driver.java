package driver;

import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.ExecutionContext;
import org.openqa.selenium.WebDriver;
import utils.ConfigReader;
import utils.ScreenshotUtils;

/**
 * WebDriver'in yasam dongusunu yonetir. Her senaryodan once yeni bir tarayici acilir, sonunda
 * kapanir; bu sayede senaryolar birbirinden izole kalir (paylasilan durum sizmaz).
 */
public class Driver {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    @BeforeScenario
    public void setUp() {
        driverThreadLocal.set(DriverFactory.createDriver());
    }

    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver henuz baslatilmadi. BeforeScenario hook'unu kontrol edin.");
        }
        return driver;
    }

    @AfterScenario
    public void tearDown(ExecutionContext context) throws InterruptedException {
        boolean scenarioFailed = context.getCurrentScenario().getIsFailing();
        if (scenarioFailed) {
            String scenarioName = context.getCurrentScenario().getName();
            String path = ScreenshotUtils.captureOnFailure(getDriver(), scenarioName);
            System.err.println("Senaryo basarisiz oldu, ekran goruntusu: " + path);
        }

        // Tarayici hemen kapanirsa son ekran (ör. sepet) goruntulenemiyor.
        // Headless degilse, kapatmadan once kisa bir sure bekleyip son durumu goruntulemeye firsat verilir.
        int pauseSeconds = ConfigReader.getPostScenarioPauseSeconds();
        if (!ConfigReader.isHeadless() && pauseSeconds > 0) {
            Thread.sleep(pauseSeconds * 1000L);
        }

        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
        }
    }
}
