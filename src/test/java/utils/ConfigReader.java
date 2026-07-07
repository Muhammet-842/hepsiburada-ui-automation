package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config/config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("config.properties okunamadi", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? defaultValue : Boolean.parseBoolean(value);
    }

    public static String getBaseUrl() {
        return get("base.url", "https://www.hepsiburada.com");
    }

    public static boolean isHeadless() {
        return getBoolean("headless", false);
    }

    public static int getExplicitTimeoutSeconds() {
        return getInt("timeout.explicit.seconds", 15);
    }

    public static int getPageLoadTimeoutSeconds() {
        return getInt("timeout.pageload.seconds", 30);
    }

    public static String getScreenshotDir() {
        return get("screenshot.dir", "reports/screenshots");
    }

    /**
     * Senaryo bitince tarayici kapanmadan once, son ekrani (ör. sepet) gozlemleyebilmek
     * icin beklenecek sure. Headless modda kullanilmaz.
     */
    public static int getPostScenarioPauseSeconds() {
        return getInt("post.scenario.pause.seconds", 5);
    }

    /**
     * SMS/2FA dogrulamasinin tekrar tekrar istenmemesi icin kalici Chrome profil dizini.
     * Bu klasor gitignore'dadir (kisisel oturum verisi icerir).
     */
    public static String getChromeProfileDir() {
        return get("chrome.profile.dir", "chrome-profile");
    }

    /**
     * Once environment variable, yoksa config.properties fallback.
     * Kullanici adi/sifre gibi kimlik bilgileri kod icine gomulmez.
     */
    public static String getUsername() {
        String env = System.getenv("HB_USERNAME");
        return (env != null && !env.isBlank()) ? env : get("hb.username");
    }

    public static String getPassword() {
        String env = System.getenv("HB_PASSWORD");
        return (env != null && !env.isBlank()) ? env : get("hb.password");
    }
}
