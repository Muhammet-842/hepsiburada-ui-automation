package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ElementRepository;
import utils.WaitUtils;

public class ProductDetailPage {

    private final WebDriver driver;
    private final WaitUtils waitUtils;

    private final By addToCartButton = ElementRepository.get("addToCartButton");
    private final By addToCartConfirmationToast = ElementRepository.get("addToCartConfirmationToast");
    private final By productTitle = ElementRepository.get("productTitle");

    public ProductDetailPage(WebDriver driver) {
        this.driver = driver;
        this.waitUtils = new WaitUtils(driver);
    }

    public void addToCart() {
        waitUtils.waitForClickable(addToCartButton).click();
        waitUtils.waitForVisible(addToCartConfirmationToast);
    }

    public String getProductTitle() {
        return waitUtils.waitForVisible(productTitle).getText();
    }
}
