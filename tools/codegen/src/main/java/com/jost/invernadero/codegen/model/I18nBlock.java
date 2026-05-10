package com.jost.invernadero.codegen.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Map;

public record I18nBlock(Map<String, LocaleLabels> locales) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public I18nBlock(Map<String, LocaleLabels> locales) {
        this.locales = locales == null ? Map.of() : Map.copyOf(locales);
    }

    public LocaleLabels resolve(String locale) {
        if (locale == null || locale.isBlank()) {
            return LocaleLabels.empty();
        }
        return locales.getOrDefault(locale, LocaleLabels.empty());
    }

    public static I18nBlock empty() {
        return new I18nBlock(Map.of());
    }
}
