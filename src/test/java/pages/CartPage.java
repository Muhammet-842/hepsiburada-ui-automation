package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ElementRepository;
import utils.WaitUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CartPage {

    private final WebDriver driver;
    private final WaitUtils waitUtils;

    // Sepetteki urun adi linklerinin class'i yok; href'teki urun sayfasi pattern'i
    // ("-p-HBC...") en guvenilir ayirt edici isaret.
    private final By cartItemNames = ElementRepository.get("cartItemNames");

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
    }

    public boolean containsProduct(String expectedProductNameFragment) {
        List<String> names = waitUtils.waitForAllVisible(cartItemNames).stream()
                .map(el -> el.getText().toLowerCase())
                .collect(Collectors.toList());
        return names.stream().anyMatch(name -> name.contains(expectedProductNameFragment.toLowerCase()));
    }
}
