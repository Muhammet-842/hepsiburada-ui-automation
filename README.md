# Hepsiburada UI Automation

UI test automation project for [hepsiburada.com](https://www.hepsiburada.com), built with **Java**, **Gauge**, and **Selenium WebDriver**, using a single keyword-driven step class instead of separate Page Object classes.

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
specs/
  HB-TC01.spec                          Scenario, plain language
  concepts/hepsiburada.cpt               Reusable step groups (login, search, add-to-cart, cart check)
src/test/java/
  driver/       Driver.java             BeforeScenario/AfterScenario (browser lifecycle)
  driver/       DriverFactory.java      ChromeOptions setup (anti-bot flags, persistent profile, headless)
  steps/        StepImplementation.java All @Step methods and Selenium logic in one class
  utils/                                ConfigReader, ElementRepository, WaitUtils
src/test/resources/
  config/       config.properties       Local settings + credentials (gitignored)
  element-infos/elements.json           All locators, kept outside Java code
env/default/                            Gauge environment configuration
```

Locators are **not hardcoded in Java** — `StepImplementation` pulls its `By` locators from
`element-infos/elements.json` via `ElementRepository`.

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

```powershell
# Windows (PowerShell)
$cp = (Get-Content cp.txt) -replace ';', ',' -replace '\\', '/'
"gauge_additional_libs = $cp,src/test/resources" | Set-Content -Encoding ascii env/default/java.properties
```

Delete `cp.txt` afterwards — it's a temporary file. Without this step, `gauge run` fails with a
wall of `package ... does not exist` compiler errors, because Gauge's Java runner has no
classpath at all on a fresh clone.

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
