# Hepsiburada UI Automation

UI test automation project for [hepsiburada.com](https://www.hepsiburada.com), built with **Java**, **Gauge**, and **Selenium WebDriver** following the **Page Object Model (POM)**.

## Scenario covered

**HB-TC01** — Login, search for "bilgisayar", add the first product in the second row to the cart, and verify it appears in the cart.

## Tech stack

- Java 17
- [Gauge](https://gauge.org/) — BDD-style test runner (specs written in plain language)
- Selenium WebDriver 4
- WebDriverManager (auto-downloads the matching ChromeDriver)
- Maven
- Gson (for reading locators from JSON)

## Project structure

```
specs/                                  Gauge .spec files (scenarios, plain language)
src/test/java/
  hooks/        TestHooks.java          BeforeScenario/AfterScenario (browser lifecycle, failure screenshots)
  pages/                                Page Object classes (one per page)
  stepimpl/     HBTestSteps.java        Maps spec steps to Page Object calls
  utils/                                ConfigReader, DriverManager, ElementRepository, WaitUtils, ScreenshotUtils
src/test/resources/
  config/       config.properties       Local settings + credentials (gitignored)
  element-infos/*.json                  Locators, kept outside Java code
env/default/                            Gauge environment configuration
```

Locators are **not hardcoded in Java** — each Page Object pulls its `By` locators from a matching JSON file under `element-infos/` via `ElementRepository`.

## Setup

### Prerequisites

- JDK 17+
- [Gauge CLI](https://docs.gauge.org/getting_started/installing-gauge.html) + `gauge install java` plugin
- Chrome browser installed

### Configure credentials

Copy the example config and fill in your own test account credentials:

```bash
cp src/test/resources/config/config.properties.example src/test/resources/config/config.properties
```

Credentials can also be supplied via environment variables instead of editing the file:

```bash
export HB_USERNAME="your-email@example.com"
export HB_PASSWORD="your-password"
```

`config.properties` is gitignored — never commit real credentials.

### Generate the Gauge classpath file (first run only)

`env/default/java.properties` (which tells Gauge's Java runner where Maven's downloaded
dependencies live) is machine-specific and gitignored. Generate it once:

```bash
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
```

Then turn `cp.txt` into `env/default/java.properties` (comma-separated, forward slashes):

```bash
# macOS/Linux
echo "gauge_additional_libs = $(cat cp.txt | tr ':' ','),src/test/resources" > env/default/java.properties

# Windows (Git Bash)
echo "gauge_additional_libs = $(cat cp.txt | tr ';' ',' | tr '\\\\' '/'),src/test/resources" > env/default/java.properties
```

Delete `cp.txt` afterwards — it's a temporary file.

### Persistent Chrome profile (2FA)

The site may challenge login with SMS/2FA on a new device. The project uses a **persistent Chrome profile** (`chrome.profile.dir` in config) so that once you complete the SMS verification manually one time, the site remembers the device and subsequent automated runs skip it. This folder is gitignored (contains session data).

## Running the tests

```bash
gauge run specs
```

or a single spec:

```bash
gauge run specs/HB-TC01.spec
```

On failure, a screenshot is saved under `reports/screenshots/`.

## Notes / known quirks handled in this project

- **Bot detection**: Chrome is launched with flags that hide Selenium's automation fingerprint (`--disable-blink-features=AutomationControlled`, etc.) to avoid the site's anti-bot check.
- **Cookie consent banner**: rendered inside a shadow DOM; dismissed via a small JS helper that pierces shadow roots.
- **Login menu**: only appears on hover; triggered via a real mouse move + a JS `mouseover` dispatch for reliability.
- **Search icon**: broken on the site, so search is submitted via `Enter` instead.
- **Product grid**: there is no per-row DOM container — products are a flat list; "row/column" is computed via `(row-1) * columnsPerRow + (column-1)`, skipping sponsored/ad list items.
- **Product links**: some open in a new tab (`target="_blank"`); the code detects and switches to the new window handle.
- **Cart verification**: verified against the actual product title captured on the product page, not the search term (not every product title contains the literal search keyword).

## Disclaimer

This project was built for an internal training/assignment exercise. It is not affiliated with or endorsed by Hepsiburada.
