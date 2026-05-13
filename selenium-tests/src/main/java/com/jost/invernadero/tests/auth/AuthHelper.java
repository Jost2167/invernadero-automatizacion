package com.jost.invernadero.tests.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jost.invernadero.tests.selenium.SelectorSupport;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class AuthHelper {

    public static final String BASE_URL_ENV = "BASE_URL";
    public static final String TEST_USER_ENV = "TEST_USER";
    public static final String TEST_PASS_ENV = "TEST_PASS";
    public static final String LOGIN_PATH_ENV = "LOGIN_PATH";
    public static final String LOGIN_USER_SELECTOR_ENV = "LOGIN_USER_SELECTOR";
    public static final String LOGIN_PASS_SELECTOR_ENV = "LOGIN_PASS_SELECTOR";
    public static final String LOGIN_SUBMIT_SELECTOR_ENV = "LOGIN_SUBMIT_SELECTOR";
    public static final String SESSION_INDICATOR_SELECTOR_ENV = "SESSION_INDICATOR_SELECTOR";
    public static final String TEST_AUTH_TOKEN_ENV = "TEST_AUTH_TOKEN";
    public static final String TEST_AUTH_TOKEN_URL_ENV = "TEST_AUTH_TOKEN_URL";

    public static final String DEFAULT_LOGIN_PATH = "/login";
    public static final String TOKEN_STORAGE_KEY = "invernadero.jwt";
    public static final String DEFAULT_USER_SELECTOR =
            "input[name='username'], input[name='email'], input[type='email'], #username, #email";
    public static final String DEFAULT_PASS_SELECTOR = "input[name='password'], input[type='password'], #password";
    public static final String DEFAULT_SUBMIT_SELECTOR = "button[type='submit'], input[type='submit']";
    public static final String DEFAULT_SESSION_INDICATOR_SELECTOR =
            "[data-testid='session-active'], [data-testid='authenticated-session'], .authenticated-session, .MuiAvatar-root";

    private final WebDriver driver;
    private final Function<String, String> environment;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthHelper(WebDriver driver) {
        this(driver, System::getenv);
    }

    AuthHelper(WebDriver driver, Function<String, String> environment) {
        this.driver = Objects.requireNonNull(driver, "driver");
        this.environment = Objects.requireNonNull(environment, "environment");
    }

    public void login(int timeoutSeconds) {
        login(Duration.ofSeconds(timeoutSeconds));
    }

    public void login(Duration timeout) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("El timeout de autenticacion debe ser mayor que cero");
        }

        AuthConfig config = readConfig();
        WebDriverWait wait = new WebDriverWait(driver, timeout);

        String token = resolveToken();
        if (!isBlank(token)) {
            loginWithToken(config.baseUrl(), token, wait, config.sessionIndicatorSelector(), timeout);
            return;
        }

        driver.get(config.loginUrl());

        WebElement usernameInput = waitForVisible(wait, config.usernameSelector(), "campo de usuario");
        usernameInput.clear();
        usernameInput.sendKeys(config.username());

        WebElement passwordInput = waitForVisible(wait, config.passwordSelector(), "campo de contrasena");
        passwordInput.clear();
        passwordInput.sendKeys(config.password());

        WebElement submitButton = waitForClickable(wait, config.submitSelector(), "boton de login");
        submitButton.click();

        waitForActiveSession(wait, config.sessionIndicatorSelector(), timeout);
    }

    private AuthConfig readConfig() {
        List<String> missingVariables = new ArrayList<>();
        String baseUrl = requiredValue(BASE_URL_ENV, missingVariables);
        String username = requiredValue(TEST_USER_ENV, missingVariables);
        String password = requiredValue(TEST_PASS_ENV, missingVariables);

        if (!missingVariables.isEmpty()) {
            throw new IllegalStateException("Faltan variables de entorno requeridas para autenticacion: "
                    + String.join(", ", missingVariables));
        }

        String loginPath = optionalValue(LOGIN_PATH_ENV, DEFAULT_LOGIN_PATH);

        return new AuthConfig(
                baseUrl.trim(),
                buildLoginUrl(baseUrl, loginPath),
                username,
                password,
                optionalValue(LOGIN_USER_SELECTOR_ENV, DEFAULT_USER_SELECTOR),
                optionalValue(LOGIN_PASS_SELECTOR_ENV, DEFAULT_PASS_SELECTOR),
                optionalValue(LOGIN_SUBMIT_SELECTOR_ENV, DEFAULT_SUBMIT_SELECTOR),
                optionalValue(SESSION_INDICATOR_SELECTOR_ENV, DEFAULT_SESSION_INDICATOR_SELECTOR)
        );
    }

    private WebElement waitForVisible(WebDriverWait wait, String selector, String description) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(SelectorSupport.locator(selector)));
        } catch (TimeoutException exception) {
            throw new IllegalStateException("No se encontro " + description + " usando selector: " + selector, exception);
        }
    }

    private WebElement waitForClickable(WebDriverWait wait, String selector, String description) {
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(SelectorSupport.locator(selector)));
        } catch (TimeoutException exception) {
            throw new IllegalStateException("No se encontro " + description + " usando selector: " + selector, exception);
        }
    }

    private void waitForActiveSession(WebDriverWait wait, String selector, Duration timeout) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(SelectorSupport.locator(selector)));
        } catch (TimeoutException exception) {
            throw new IllegalStateException("Fallo de autenticacion: no aparecio el indicador de sesion activa "
                    + "dentro de " + timeout.toSeconds() + " segundos. Verifica credenciales y selector: "
                    + selector + ". URL actual: " + driver.getCurrentUrl()
                    + ". Texto visible: " + visibleTextPreview(), exception);
        }
    }

    private void loginWithToken(String baseUrl, String token, WebDriverWait wait, String sessionIndicatorSelector, Duration timeout) {
        if (!(driver instanceof JavascriptExecutor javascriptExecutor)) {
            throw new IllegalStateException("El driver no permite configurar localStorage para autenticacion por token");
        }

        driver.get(baseUrl);
        javascriptExecutor.executeScript(
                "window.localStorage.setItem(arguments[0], arguments[1]);",
                TOKEN_STORAGE_KEY,
                token
        );
        driver.get(baseUrl);
        waitForActiveSession(wait, sessionIndicatorSelector, timeout);
    }

    private String resolveToken() {
        String token = environment.apply(TEST_AUTH_TOKEN_ENV);

        if (!isBlank(token)) {
            return token.trim();
        }

        String tokenUrl = environment.apply(TEST_AUTH_TOKEN_URL_ENV);
        if (isBlank(tokenUrl)) {
            return null;
        }

        return fetchToken(tokenUrl.trim());
    }

    private String fetchToken(String tokenUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUrl))
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("No se pudo obtener token E2E desde " + tokenUrl
                        + ". HTTP " + response.statusCode());
            }

            return parseToken(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo obtener token E2E desde " + tokenUrl + ": "
                    + exception.getMessage(), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpio la obtencion del token E2E", exception);
        }
    }

    private String parseToken(String responseBody) throws IOException {
        JsonNode json = objectMapper.readTree(responseBody);
        JsonNode tokenNode = json.get("token");

        if (tokenNode == null || isBlank(tokenNode.asText())) {
            throw new IllegalStateException("La respuesta de token E2E no contiene el campo 'token'");
        }

        return tokenNode.asText();
    }

    private String visibleTextPreview() {
        String text = driver.getPageSource();

        if (text == null) {
            return "";
        }

        String normalizedText = text.replaceAll("\\s+", " ").trim();
        int maxLength = Math.min(normalizedText.length(), 300);
        return normalizedText.substring(0, maxLength);
    }

    private String requiredValue(String name, List<String> missingVariables) {
        String value = environment.apply(name);

        if (isBlank(value)) {
            missingVariables.add(name);
        }

        return value;
    }

    private String optionalValue(String name, String defaultValue) {
        String value = environment.apply(name);
        return isBlank(value) ? defaultValue : value.trim();
    }

    private String buildLoginUrl(String baseUrl, String loginPath) {
        if (isAbsoluteUrl(loginPath)) {
            return loginPath;
        }

        String normalizedBaseUrl = baseUrl.trim().replaceAll("/+$", "");
        String normalizedLoginPath = loginPath.startsWith("/") ? loginPath : "/" + loginPath;
        return normalizedBaseUrl + normalizedLoginPath;
    }

    private boolean isAbsoluteUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record AuthConfig(
            String baseUrl,
            String loginUrl,
            String username,
            String password,
            String usernameSelector,
            String passwordSelector,
            String submitSelector,
            String sessionIndicatorSelector
    ) {
    }
}
