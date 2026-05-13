package com.jost.invernadero.tests.runner;

import com.jost.invernadero.tests.auth.AuthHelper;
import com.jost.invernadero.tests.config.ModelTestData;
import com.jost.invernadero.tests.config.TestDataLoader;
import com.jost.invernadero.tests.config.TestSuiteConfig;
import com.jost.invernadero.tests.report.ReportGenerator;
import com.jost.invernadero.tests.report.TestResult;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

class CrudFormTest {

    private static TestSuiteConfig suiteConfig;
    private static WebDriver driver;
    private static FormFiller formFiller;
    private static final List<TestResult> RESULTS = Collections.synchronizedList(new ArrayList<>());

    @BeforeAll
    static void setUp() {
        Assumptions.assumeTrue(shouldRunCrudSuite(),
                "CrudFormTest se ejecuta explicitamente con -Dtest=CrudFormTest");

        RESULTS.clear();
        suiteConfig = loadSuiteConfig();
        if (suiteConfig.getModels().isEmpty()) {
            throw new IllegalStateException("test-data.json no contiene modelos validos para ejecutar");
        }

        validateAuthEnvironment();
        driver = createDriver();

        Duration timeout = Duration.ofSeconds(suiteConfig.getTimeout());
        new AuthHelper(driver).login(timeout);
        formFiller = new FormFiller(driver, requiredEnvironment(AuthHelper.BASE_URL_ENV), timeout);
        System.out.println("Ejecutando pruebas CRUD para " + suiteConfig.getModels().size() + " modelos");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }

        if (!RESULTS.isEmpty()) {
            new ReportGenerator().generate(List.copyOf(RESULTS));
        }
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("models")
    void createsConfiguredModel(ModelTestData model) {
        Assumptions.assumeTrue(shouldRunCrudSuite(),
                "CrudFormTest se ejecuta explicitamente con -Dtest=CrudFormTest");

        long startedAt = System.nanoTime();

        try {
            formFiller.create(model);
            TestResult result = TestResult.passed(model.getName(), elapsedMillis(startedAt));
            record(result);
        } catch (FormExecutionException exception) {
            TestResult result = TestResult.failed(model.getName(), elapsedMillis(startedAt), exception.getMessage());
            record(result);
            Assertions.fail(exception.getMessage(), exception);
        } catch (RuntimeException exception) {
            TestResult result = TestResult.error(model.getName(), elapsedMillis(startedAt), exception.getMessage());
            record(result);
            Assertions.fail("ERROR " + model.getName() + ": " + exception.getMessage(), exception);
        }
    }

    static Stream<ModelTestData> models() {
        if (!shouldRunCrudSuite()) {
            return Stream.of(new ModelTestData());
        }

        return loadSuiteConfig().getModels().stream();
    }

    private static TestSuiteConfig loadSuiteConfig() {
        if (suiteConfig == null) {
            suiteConfig = new TestDataLoader().load();
        }

        return suiteConfig;
    }

    private static WebDriver createDriver() {
        String browser = System.getProperty("browser", "chrome").trim().toLowerCase(Locale.ROOT);

        return switch (browser) {
            case "edge", "msedge" -> createEdgeDriver();
            case "chrome" -> createChromeDriver();
            default -> throw new IllegalArgumentException("Browser no soportado: " + browser);
        };
    }

    private static WebDriver createChromeDriver() {
        if (isBlank(System.getProperty("webdriver.chrome.driver"))) {
            WebDriverManager.chromedriver().setup();
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1366,768");

        if (isHeadless()) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
        }

        return new ChromeDriver(options);
    }

    private static WebDriver createEdgeDriver() {
        if (isBlank(System.getProperty("webdriver.edge.driver"))) {
            WebDriverManager.edgedriver().setup();
        }

        EdgeOptions options = new EdgeOptions();
        options.addArguments("--window-size=1366,768");

        if (isHeadless()) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
        }

        return new EdgeDriver(options);
    }

    private static boolean isHeadless() {
        return !"false".equalsIgnoreCase(System.getProperty("headless", "true"));
    }

    private static boolean shouldRunCrudSuite() {
        String selectedTests = System.getProperty("test", "");
        return selectedTests.contains("CrudFormTest") || Boolean.getBoolean("runCrudFormTest");
    }

    private static void validateAuthEnvironment() {
        List<String> missingVariables = new ArrayList<>();

        for (String variable : List.of(AuthHelper.BASE_URL_ENV, AuthHelper.TEST_USER_ENV, AuthHelper.TEST_PASS_ENV)) {
            if (isBlank(System.getenv(variable))) {
                missingVariables.add(variable);
            }
        }

        if (!missingVariables.isEmpty()) {
            throw new IllegalStateException("Faltan variables de entorno requeridas para autenticacion: "
                    + String.join(", ", missingVariables));
        }
    }

    private static String requiredEnvironment(String variable) {
        String value = System.getenv(variable);

        if (isBlank(value)) {
            throw new IllegalStateException("Falta variable de entorno requerida: " + variable);
        }

        return value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void record(TestResult result) {
        RESULTS.add(result);
        System.out.println("[%s] %s (%ss)".formatted(
                result.getStatus(),
                result.getName(),
                seconds(result.getDurationMillis())
        ));
    }

    private static long elapsedMillis(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
    }

    private static String seconds(long durationMillis) {
        return String.format(Locale.US, "%.3f", durationMillis / 1000.0);
    }
}
