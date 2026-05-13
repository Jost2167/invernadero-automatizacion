package com.jost.invernadero.tests.runner;

import com.jost.invernadero.tests.config.FieldConfig;
import com.jost.invernadero.tests.config.ModelTestData;
import com.jost.invernadero.tests.report.TestResult;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormFillerTest {

    @Test
    void navigatesFillsFieldsClicksSubmitAndWaitsForSuccess() {
        RecordingElement nameInput = new RecordingElement("input", "text");
        RecordingElement submitButton = new RecordingElement("button", "submit");
        RecordingElement successIndicator = new RecordingElement("div", null);

        Map<String, WebElement> elements = Map.of(
                By.cssSelector("#name").toString(), nameInput.proxy(),
                By.cssSelector(FormFiller.DEFAULT_SUBMIT_SELECTOR).toString(), submitButton.proxy(),
                By.cssSelector(".alert-success").toString(), successIndicator.proxy()
        );
        RecordingDriver driver = new RecordingDriver(elements, true);
        FormFiller formFiller = new FormFiller(driver.proxy(), "http://localhost:3000", Duration.ofMillis(50), null);

        formFiller.create(modelWithField("#name", "Sensor QA"));

        assertAll(
                () -> assertEquals(List.of("http://localhost:3000/sensor/new"), driver.visitedUrls()),
                () -> assertTrue(nameInput.cleared()),
                () -> assertEquals("Sensor QA", nameInput.sentText()),
                () -> assertTrue(submitButton.clicked())
        );
    }

    @Test
    void submitsWithJavascriptWhenSubmitButtonIsNotFound() {
        RecordingElement nameInput = new RecordingElement("input", "text");
        RecordingElement successIndicator = new RecordingElement("div", null);

        Map<String, WebElement> elements = Map.of(
                By.cssSelector("#name").toString(), nameInput.proxy(),
                By.cssSelector(".alert-success").toString(), successIndicator.proxy()
        );
        RecordingDriver driver = new RecordingDriver(elements, true);
        FormFiller formFiller = new FormFiller(driver.proxy(), "http://localhost:3000", Duration.ofMillis(10), null);

        formFiller.create(modelWithField("#name", "Sensor QA"));

        assertTrue(driver.scriptExecuted());
    }

    @Test
    void throwsFailedResultWhenFieldSelectorIsMissing() {
        RecordingElement submitButton = new RecordingElement("button", "submit");

        Map<String, WebElement> elements = Map.of(
                By.cssSelector(FormFiller.DEFAULT_SUBMIT_SELECTOR).toString(), submitButton.proxy()
        );
        RecordingDriver driver = new RecordingDriver(elements, true);
        FormFiller formFiller = new FormFiller(driver.proxy(), "http://localhost:3000", Duration.ofMillis(10), null);

        FormExecutionException exception = assertThrows(
                FormExecutionException.class,
                () -> formFiller.create(modelWithField("#missing", "Sensor QA"))
        );

        assertTrue(exception.getMessage().contains("FAILED Sensor"));
        assertTrue(exception.getMessage().contains("#missing"));
    }

    @Test
    void caughtInvalidSelectorDoesNotPreventExecutingNextModel() {
        RecordingElement nameInput = new RecordingElement("input", "text");
        RecordingElement submitButton = new RecordingElement("button", "submit");
        RecordingElement successIndicator = new RecordingElement("div", null);

        Map<String, WebElement> elements = Map.of(
                By.cssSelector("#name").toString(), nameInput.proxy(),
                By.cssSelector(FormFiller.DEFAULT_SUBMIT_SELECTOR).toString(), submitButton.proxy(),
                By.cssSelector(".alert-success").toString(), successIndicator.proxy()
        );
        RecordingDriver driver = new RecordingDriver(elements, true);
        FormFiller formFiller = new FormFiller(driver.proxy(), "http://localhost:3000", Duration.ofMillis(10), null);
        List<ModelTestData> models = List.of(
                modelWithField("#missing", "Bad"),
                modelWithField("#name", "Good")
        );
        List<TestResult> results = new ArrayList<>();

        for (ModelTestData model : models) {
            try {
                formFiller.create(model);
                results.add(TestResult.passed(model.getName(), 1));
            } catch (FormExecutionException exception) {
                results.add(TestResult.failed(model.getName(), 1, exception.getMessage()));
            }
        }

        assertAll(
                () -> assertEquals(TestResult.Status.FAILED, results.get(0).getStatus()),
                () -> assertEquals(TestResult.Status.PASSED, results.get(1).getStatus()),
                () -> assertEquals("Good", nameInput.sentText())
        );
    }

    private ModelTestData modelWithField(String selector, String value) {
        return new ModelTestData(
                "Sensor",
                "/sensor/new",
                List.of(new FieldConfig(selector, value)),
                ".alert-success"
        );
    }

    private static final class RecordingDriver {

        private final Map<String, WebElement> elements;
        private final boolean javascriptSubmitResult;
        private final List<String> visitedUrls = new ArrayList<>();
        private boolean scriptExecuted;

        private RecordingDriver(Map<String, WebElement> elements, boolean javascriptSubmitResult) {
            this.elements = elements;
            this.javascriptSubmitResult = javascriptSubmitResult;
        }

        private WebDriver proxy() {
            InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
                case "get" -> {
                    visitedUrls.add((String) args[0]);
                    yield null;
                }
                case "findElement" -> {
                    String locator = args[0].toString();
                    WebElement element = elements.get(locator);
                    if (element == null) {
                        throw new NoSuchElementException(locator);
                    }
                    yield element;
                }
                case "executeScript" -> {
                    scriptExecuted = true;
                    yield javascriptSubmitResult;
                }
                case "findElements" -> {
                    WebElement element = elements.get(args[0].toString());
                    yield element == null ? List.of() : List.of(element);
                }
                case "getCurrentUrl" -> visitedUrls.isEmpty() ? "" : visitedUrls.get(visitedUrls.size() - 1);
                case "getTitle", "getPageSource" -> "";
                case "getWindowHandles" -> Set.of("window");
                case "getWindowHandle" -> "window";
                case "close", "quit" -> null;
                case "toString" -> "RecordingDriver";
                default -> throw new UnsupportedOperationException("Unsupported WebDriver method: " + method.getName());
            };

            return (WebDriver) Proxy.newProxyInstance(
                    WebDriver.class.getClassLoader(),
                    new Class<?>[]{WebDriver.class, JavascriptExecutor.class},
                    handler
            );
        }

        private List<String> visitedUrls() {
            return visitedUrls;
        }

        private boolean scriptExecuted() {
            return scriptExecuted;
        }
    }

    private static final class RecordingElement {

        private final String tagName;
        private final String type;
        private boolean cleared;
        private boolean clicked;
        private String sentText = "";

        private RecordingElement(String tagName, String type) {
            this.tagName = tagName;
            this.type = type;
        }

        private WebElement proxy() {
            InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
                case "clear" -> {
                    cleared = true;
                    sentText = "";
                    yield null;
                }
                case "sendKeys" -> {
                    for (CharSequence text : (CharSequence[]) args[0]) {
                        sentText += text;
                    }
                    yield null;
                }
                case "click" -> {
                    clicked = true;
                    yield null;
                }
                case "isDisplayed", "isEnabled" -> true;
                case "isSelected" -> false;
                case "getTagName" -> tagName;
                case "getDomAttribute" -> "type".equals(args[0]) ? type : null;
                case "findElements" -> List.of();
                case "getText", "getAttribute", "getDomProperty", "getCssValue", "getAriaRole",
                     "getAccessibleName" -> "";
                case "toString" -> "RecordingElement";
                default -> defaultValue(method.getReturnType());
            };

            return (WebElement) Proxy.newProxyInstance(
                    WebElement.class.getClassLoader(),
                    new Class<?>[]{WebElement.class},
                    handler
            );
        }

        private boolean cleared() {
            return cleared;
        }

        private boolean clicked() {
            return clicked;
        }

        private String sentText() {
            return sentText;
        }

        private Object defaultValue(Class<?> returnType) {
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == byte.class) {
                return (byte) 0;
            }
            if (returnType == short.class) {
                return (short) 0;
            }
            if (returnType == int.class) {
                return 0;
            }
            if (returnType == long.class) {
                return 0L;
            }
            if (returnType == float.class) {
                return 0F;
            }
            if (returnType == double.class) {
                return 0D;
            }
            if (returnType == char.class) {
                return '\0';
            }
            return null;
        }
    }
}
