package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.openqa.selenium.By;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Locator'lari Java kodunun disinda, src/test/resources/element-infos altindaki
 * JSON dosyalarinda tutar. Page Object siniflari locator'i By.cssSelector(...) yazmak
 * yerine ElementRepository.get("key") ile cagirir.
 */
public class ElementRepository {

    private static final Map<String, By> elements = new HashMap<>();

    static {
        try {
            URL folderUrl = ElementRepository.class.getClassLoader().getResource("element-infos");
            if (folderUrl == null) {
                throw new IllegalStateException("element-infos klasoru classpath'te bulunamadi");
            }
            File folder = new File(folderUrl.toURI());
            File[] jsonFiles = folder.listFiles((dir, name) -> name.endsWith(".json"));
            if (jsonFiles == null) {
                throw new IllegalStateException("element-infos klasorunde json dosyasi bulunamadi");
            }

            Gson gson = new Gson();
            Type listType = new TypeToken<List<ElementInfo>>() {}.getType();

            for (File file : jsonFiles) {
                try (FileReader reader = new FileReader(file)) {
                    List<ElementInfo> infos = gson.fromJson(reader, listType);
                    for (ElementInfo info : infos) {
                        elements.put(info.key, toBy(info));
                    }
                }
            }
        } catch (IOException | java.net.URISyntaxException e) {
            throw new RuntimeException("element-infos JSON dosyalari okunamadi", e);
        }
    }

    private ElementRepository() {
    }

    public static By get(String key) {
        By locator = elements.get(key);
        if (locator == null) {
            throw new IllegalArgumentException(
                    "'" + key + "' anahtarli locator element-infos/*.json icinde bulunamadi");
        }
        return locator;
    }

    private static By toBy(ElementInfo info) {
        return switch (info.type) {
            case "id" -> By.id(info.value);
            case "css" -> By.cssSelector(info.value);
            case "xpath" -> By.xpath(info.value);
            default -> throw new IllegalArgumentException(
                    "Bilinmeyen locator tipi: " + info.type + " (key: " + info.key + ")");
        };
    }

    private static class ElementInfo {
        String key;
        String value;
        String type;
    }
}
