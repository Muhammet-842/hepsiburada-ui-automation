package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ScreenshotUtils {

    private ScreenshotUtils() {
    }

    /**
     * Basarisiz senaryonun ekran goruntusunu alir, dosya yolunu doner.
     * Locator/timeout hatalarinin sebebini raporda hizli anlamak icin kullanilir.
     */
    public static String captureOnFailure(WebDriver driver, String scenarioName) {
        try {
            Path dir = Path.of(ConfigReader.getScreenshotDir());
            Files.createDirectories(dir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String safeName = scenarioName.replaceAll("[^a-zA-Z0-9_-]", "_");
            Path target = dir.resolve(safeName + "_" + timestamp + ".png");

            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), target);
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            return "Screenshot alinamadi: " + e.getMessage();
        }
    }
}
