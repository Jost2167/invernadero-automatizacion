package com.jost.invernadero.tests.auth;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthHelperTest {

    @Test
    void abortsBeforeOpeningBrowserWhenRequiredEnvironmentVariablesAreMissing() {
        List<String> visitedUrls = new ArrayList<>();
        WebDriver driver = fakeDriver(Map.of(), visitedUrls, new ArrayList<>());
        AuthHelper authHelper = new AuthHelper(driver, name -> AuthHelper.BASE_URL_ENV.equals(name)
                ? "http://localhost:3000"
                : null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authHelper.login(Duration.ofSeconds(1))
        );

        assertAll(
                () -> assertTrue(exception.getMessage().contains("Faltan variables de entorno requeridas")),
                () -> assertTrue(exception.getMessage().contains(AuthHelper.TEST_USER_ENV)),
                () -> assertTrue(exception.getMessage().contains(AuthHelper.TEST_PASS_ENV)),
                () -> assertTrue(visitedUrls.isEmpty())
        );
    }

    @Test
    void navigatesToLoginFormEntersCredentialsAndWaitsForActiveSession() {
        RecordingElement usernameInput = new RecordingElement();
        RecordingElement passwordInput = new RecordingElement();
        RecordingElement submitButton = new RecordingElement();
        RecordingElement sessionIndicator = new RecordingElement();

        Map<String, WebElement> elements = new HashMap<>();
        elements.put(By.cssSelector(AuthHelper.DEFAULT_USER_SELECTOR).toString(), usernameInput.proxy());
        elements.put(By.cssSelector(AuthHelper.DEFAULT_PASS_SELECTOR).toString(), passwordInput.proxy());
        elements.put(By.cssSelector(AuthHelper.DEFAULT_SUBMIT_SELECTOR).toString(), submitButton.proxy());
        elements.put(By.cssSelector(AuthHelper.DEFAULT_SESSION_INDICATOR_SELECTOR).toString(), sessionIndicator.proxy());

        List<String> visitedUrls = new ArrayList<>();
        List<String> requestedLocators = new ArrayList<>();
        WebDriver driver = fakeDriver(elements, visitedUrls, requestedLocators);
        Map<String, String> environment = Map.of(
                AuthHelper.BASE_URL_ENV, "http://localhost:3000/",
                AuthHelper.TEST_USER_ENV, "qa@example.com",
                AuthHelper.TEST_PASS_ENV, "secret"
        );

        new AuthHelper(driver, environment::get).login(Duration.ofSeconds(1));

        assertAll(
                () -> assertEquals(List.of("http://localhost:3000/login"), visitedUrls),
                () -> assertTrue(usernameInput.cleared()),
                () -> assertEquals("qa@example.com", usernameInput.sentText()),
                () -> assertTrue(passwordInput.cleared()),
                () -> assertEquals("secret", passwordInput.sentText()),
                () -> assertTrue(submitButton.clicked()),
                () -> assertTrue(requestedLocators.contains(By.cssSelector(AuthHelper.DEFAULT_SESSION_INDICATOR_SELECTOR).toString()))
        );
    }

    @Test
    void usesOptionalSelectorsAndLoginPathFromEnvironment() {
        String userSelector = "#user";
        String passSelector = "#pass";
        String submitSelector = "#sign-in";
        String sessionSelector = "//main[@data-authenticated='true']";

        Map<String, WebElement> elements = new HashMap<>();
        elements.put(By.cssSelector(userSelector).toString(), new RecordingElement().proxy());
        elements.put(By.cssSelector(passSelector).toString(), new RecordingElement().proxy());
        elements.put(By.cssSelector(submitSelector).toString(), new RecordingElement().proxy());
        elements.put(By.xpath(sessionSelector).toString(), new RecordingElement().proxy());

        List<String> visitedUrls = new ArrayList<>();
        WebDriver driver = fakeDriver(elements, visitedUrls, new ArrayList<>());
        Map<String, String> environment = Map.of(
                AuthHelper.BASE_URL_ENV, "http://localhost:3000",
                AuthHelper.TEST_USER_ENV, "qa@example.com",
                AuthHelper.TEST_PASS_ENV, "secret",
                AuthHelper.LOGIN_PATH_ENV, "sign-in",
                AuthHelper.LOGIN_USER_SELECTOR_ENV, userSelector,
                AuthHelper.LOGIN_PASS_SELECTOR_ENV, passSelector,
                AuthHelper.LOGIN_SUBMIT_SELECTOR_ENV, submitSelector,
                AuthHelper.SESSION_INDICATOR_SELECTOR_ENV, sessionSelector
        );

        new AuthHelper(driver, environment::get).login(Duration.ofSeconds(1));

        assertEquals(List.of("http://localhost:3000/sign-in"), visitedUrls);
    }

    private WebDriver fakeDriver(Map<String, WebElement> elements, List<String> visitedUrls, List<String> requestedLocators) {
        InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
            case "get" -> {
                visitedUrls.add((String) args[0]);
                yield null;
            }
            case "findElement" -> {
                String locator = args[0].toString();
                requestedLocators.add(locator);
                WebElement element = elements.get(locator);
                if (element == null) {
                    throw new NoSuchElementException(locator);
                }
                yield element;
            }
            case "findElements" -> {
                String locator = args[0].toString();
                requestedLocators.add(locator);
                WebElement element = elements.get(locator);
                yield element == null ? List.of() : List.of(element);
            }
            case "getCurrentUrl" -> visitedUrls.isEmpty() ? "" : visitedUrls.get(visitedUrls.size() - 1);
            case "getTitle", "getPageSource" -> "";
            case "getWindowHandles" -> Set.of("window");
            case "getWindowHandle" -> "window";
            case "close", "quit" -> null;
            case "toString" -> "FakeWebDriver";
            default -> throw new UnsupportedOperationException("Unsupported WebDriver method: " + method.getName());
        };

        return (WebDriver) Proxy.newProxyInstance(
                WebDriver.class.getClassLoader(),
                new Class<?>[]{WebDriver.class},
                handler
        );
    }

    private static final class RecordingElement {

        private boolean cleared;
        private boolean clicked;
        private String sentText = "";

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
                case "getText", "getTagName", "getAttribute", "getDomAttribute", "getDomProperty",
                     "getCssValue", "getAriaRole", "getAccessibleName" -> "";
                case "isSelected" -> false;
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
