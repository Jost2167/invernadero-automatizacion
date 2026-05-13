package com.jost.invernadero.tests.selenium;

import org.openqa.selenium.By;

public final class SelectorSupport {

    private SelectorSupport() {
    }

    public static By locator(String selector) {
        String trimmedSelector = selector.trim();

        if (trimmedSelector.startsWith("/") || trimmedSelector.startsWith("(")) {
            return By.xpath(trimmedSelector);
        }

        return By.cssSelector(trimmedSelector);
    }
}
