package com.jost.invernadero.tests.runner;

import com.jost.invernadero.tests.config.FieldConfig;
import com.jost.invernadero.tests.config.ModelTestData;
import com.jost.invernadero.tests.selenium.SelectorSupport;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class FormFiller {

    public static final String FORM_SUBMIT_SELECTOR_ENV = "FORM_SUBMIT_SELECTOR";
    public static final String TEST_RUN_ID_PROPERTY = "test.run.id";
    public static final String STEP_DELAY_MS_PROPERTY = "stepDelayMs";
    public static final String RUN_ID_PLACEHOLDER = "${RUN_ID}";
    public static final String DEFAULT_SUBMIT_SELECTOR =
            "button[type='submit'], input[type='submit'], [data-testid='submit'], [data-testid='save']";
    private static final DateTimeFormatter RUN_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final WebDriver driver;
    private final String baseUrl;
    private final WebDriverWait wait;
    private final String submitSelector;
    private final String runId;
    private final long stepDelayMillis;

    public FormFiller(WebDriver driver, String baseUrl, Duration timeout) {
        this(driver, baseUrl, timeout, System.getenv(FORM_SUBMIT_SELECTOR_ENV));
    }

    FormFiller(WebDriver driver, String baseUrl, Duration timeout, String submitSelector) {
        this.driver = Objects.requireNonNull(driver, "driver");
        this.baseUrl = requireBaseUrl(baseUrl);
        this.wait = new WebDriverWait(driver, requireTimeout(timeout));
        this.submitSelector = isBlank(submitSelector) ? DEFAULT_SUBMIT_SELECTOR : submitSelector.trim();
        this.runId = resolveRunId();
        this.stepDelayMillis = resolveStepDelayMillis();
    }

    public void create(ModelTestData model) {
        Objects.requireNonNull(model, "model");

        driver.get(resolveUrl(replacePlaceholders(model.getFormUrl())));
        pause();
        fillFields(model);
        submit(model);
        waitForSuccess(model);
    }

    private void fillFields(ModelTestData model) {
        for (FieldConfig field : model.getFields()) {
            fillField(model, field);
        }
    }

    private void fillField(ModelTestData model, FieldConfig field) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    SelectorSupport.locator(replacePlaceholders(field.getSelector()))));
            pause();
            setValue(element, replacePlaceholders(field.getValue()));
            pause();
        } catch (TimeoutException exception) {
            throw new FormExecutionException("FAILED " + model.getName()
                    + ": campo no encontrado con selector " + replacePlaceholders(field.getSelector()), exception);
        } catch (WebDriverException exception) {
            throw new FormExecutionException("FAILED " + model.getName()
                    + ": no se pudo llenar el campo " + replacePlaceholders(field.getSelector())
                    + " con valor '" + replacePlaceholders(safeValue(field.getValue())) + "'", exception);
        }
    }

    private void setValue(WebElement element, String value) {
        String safeValue = safeValue(value);
        String tagName = lower(element.getTagName());
        String type = lower(element.getDomAttribute("type"));
        String role = lower(element.getDomAttribute("role"));

        if ("select".equals(tagName)) {
            selectValue(element, safeValue);
            return;
        }

        if ("combobox".equals(role)) {
            selectMaterialOption(element, safeValue);
            return;
        }

        if ("checkbox".equals(type) || "radio".equals(type) || hasNestedChoiceInput(element)) {
            setChecked(element, safeValue);
            return;
        }

        element.clear();
        element.sendKeys(safeValue);
    }

    private void selectValue(WebElement element, String value) {
        Select select = new Select(element);

        try {
            select.selectByValue(value);
        } catch (NoSuchElementException exception) {
            select.selectByVisibleText(value);
        }
    }

    private void setChecked(WebElement element, String value) {
        boolean expectedSelected = switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "true", "1", "yes", "on", "checked", "si" -> true;
            default -> false;
        };
        WebElement choiceInput = choiceInput(element);

        if (choiceInput.isSelected() != expectedSelected) {
            element.click();
        }
    }

    private void selectMaterialOption(WebElement element, String value) {
        element.click();
        String escapedValue = cssEscape(value);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                    "[role='option'][data-value='" + escapedValue + "'], li[data-value='" + escapedValue + "']"
            ))).click();
        } catch (TimeoutException exception) {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//*[@role='option' and normalize-space()=" + xpathLiteral(value) + "]"
                            + " | //li[normalize-space()=" + xpathLiteral(value) + "]"
            ))).click();
        }
    }

    private boolean hasNestedChoiceInput(WebElement element) {
        return !element.findElements(By.cssSelector("input[type='checkbox'], input[type='radio']")).isEmpty();
    }

    private WebElement choiceInput(WebElement element) {
        String type = lower(element.getDomAttribute("type"));

        if ("checkbox".equals(type) || "radio".equals(type)) {
            return element;
        }

        return element.findElement(By.cssSelector("input[type='checkbox'], input[type='radio']"));
    }

    private void submit(ModelTestData model) {
        try {
            WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(SelectorSupport.locator(submitSelector)));
            pause();
            submitButton.click();
            pause();
        } catch (TimeoutException exception) {
            submitWithJavascript(model, exception);
        }
    }

    private void submitWithJavascript(ModelTestData model, TimeoutException cause) {
        if (!(driver instanceof JavascriptExecutor javascriptExecutor)) {
            throw new FormExecutionException("FAILED " + model.getName()
                    + ": no se encontro boton de envio con selector " + submitSelector
                    + " y el driver no permite submit por JavaScript", cause);
        }

        Object submitted = javascriptExecutor.executeScript("""
                const form = document.querySelector('form');
                if (!form) return false;
                if (typeof form.requestSubmit === 'function') {
                  form.requestSubmit();
                } else {
                  form.submit();
                }
                return true;
                """);

        if (!Boolean.TRUE.equals(submitted)) {
            throw new FormExecutionException("FAILED " + model.getName()
                    + ": no se encontro boton de envio ni formulario para enviar", cause);
        }

        pause();
    }

    private void waitForSuccess(ModelTestData model) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    SelectorSupport.locator(replacePlaceholders(model.getSuccessIndicator()))));
            pause();
        } catch (TimeoutException exception) {
            throw new FormExecutionException("FAILED " + model.getName()
                    + ": no aparecio el indicador de exito "
                    + replacePlaceholders(model.getSuccessIndicator()), exception);
        }
    }

    private String resolveUrl(String formUrl) {
        if (isAbsoluteUrl(formUrl)) {
            return formUrl;
        }

        String normalizedBaseUrl = baseUrl.replaceAll("/+$", "");
        String normalizedFormUrl = formUrl.startsWith("/") ? formUrl : "/" + formUrl;
        return normalizedBaseUrl + normalizedFormUrl;
    }

    private String requireBaseUrl(String baseUrl) {
        if (isBlank(baseUrl)) {
            throw new IllegalArgumentException("BASE_URL es requerido para navegar a formularios");
        }

        return baseUrl.trim();
    }

    private Duration requireTimeout(Duration timeout) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("El timeout de formularios debe ser mayor que cero");
        }

        return timeout;
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private String replacePlaceholders(String value) {
        return value == null ? null : value.replace(RUN_ID_PLACEHOLDER, runId);
    }

    private String resolveRunId() {
        String configuredRunId = System.getProperty(TEST_RUN_ID_PROPERTY);

        if (!isBlank(configuredRunId)) {
            return configuredRunId.trim();
        }

        return LocalDateTime.now().format(RUN_ID_FORMATTER);
    }

    private long resolveStepDelayMillis() {
        String configuredDelay = System.getProperty(STEP_DELAY_MS_PROPERTY, "0");

        try {
            long delay = Long.parseLong(configuredDelay);
            if (delay < 0) {
                throw new IllegalArgumentException(STEP_DELAY_MS_PROPERTY + " no puede ser negativo: " + configuredDelay);
            }
            return delay;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(STEP_DELAY_MS_PROPERTY + " debe ser un entero en milisegundos: "
                    + configuredDelay, exception);
        }
    }

    private void pause() {
        if (stepDelayMillis == 0) {
            return;
        }

        try {
            Thread.sleep(stepDelayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FormExecutionException("Ejecucion interrumpida durante pausa visual", exception);
        }
    }

    private String cssEscape(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        String[] parts = value.split("'");
        StringBuilder literal = new StringBuilder("concat(");
        for (int index = 0; index < parts.length; index++) {
            if (index > 0) {
                literal.append(", \"'\", ");
            }
            literal.append("'").append(parts[index]).append("'");
        }
        literal.append(")");
        return literal.toString();
    }

    private boolean isAbsoluteUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
