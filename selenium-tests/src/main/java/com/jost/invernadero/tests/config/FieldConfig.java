package com.jost.invernadero.tests.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldConfig {

    private String selector;
    private String value;

    public FieldConfig() {
    }

    public FieldConfig(String selector, String value) {
        this.selector = selector;
        this.value = value;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
