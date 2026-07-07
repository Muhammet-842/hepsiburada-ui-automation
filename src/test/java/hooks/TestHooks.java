package hooks;

import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.ExecutionContext;
import utils.ConfigReader;
import utils.DriverManager;
import utils.ScreenshotUtils;

public class TestHooks {

    @BeforeScenario
    public void setUp() {
        DriverManager.initDriver();
    }

    @AfterScenario
    public void tearDown(ExecutionContext context) throws InterruptedException {
        boolean scenarioFailed = context.getCurrentScenario().getIsFailing();
        if (scenarioFailed) {
            String scenarioName = context.getCurrentScenario().getName();
            String path = ScreenshotUtils.captureOnFailure(DriverManager.getDriver(), scenarioName);
            System.err.println("Senaryo basarisiz oldu, ekran goruntusu: " + path);
        }

        // Tarayici hemen kapanirsa son ekran (ör. sepet) goruntulenemiyor.
        // Headless degilse, kapatmadan once kisa bir sure bekleyip son durumu goruntulemeye firsat verilir.
        int pauseSeconds = ConfigReader.getPostScenarioPauseSeconds();
        if (!ConfigReader.isHeadless() && pauseSeconds > 0) {
            Thread.sleep(pauseSeconds * 1000L);
        }

        DriverManager.quitDriver();
    }
}
