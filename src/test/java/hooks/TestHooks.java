package hooks;

import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.ExecutionContext;
import utils.DriverManager;
import utils.ScreenshotUtils;

public class TestHooks {

    @BeforeScenario
    public void setUp() {
        DriverManager.initDriver();
    }

    @AfterScenario
    public void tearDown(ExecutionContext context) {
        boolean scenarioFailed = context.getCurrentScenario().getIsFailing();
        if (scenarioFailed) {
            String scenarioName = context.getCurrentScenario().getName();
            String path = ScreenshotUtils.captureOnFailure(DriverManager.getDriver(), scenarioName);
            System.err.println("Senaryo basarisiz oldu, ekran goruntusu: " + path);
        }
        DriverManager.quitDriver();
    }
}
