package com.jost.invernadero.tests.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelTestData {

    private String name;
    private String formUrl;
    private List<FieldConfig> fields = new ArrayList<>();
    private String successIndicator;

    public ModelTestData() {
    }

    public ModelTestData(String name, String formUrl, List<FieldConfig> fields, String successIndicator) {
        this.name = name;
        this.formUrl = formUrl;
        setFields(fields);
        this.successIndicator = successIndicator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormUrl() {
        return formUrl;
    }

    public void setFormUrl(String formUrl) {
        this.formUrl = formUrl;
    }

    public List<FieldConfig> getFields() {
        return fields;
    }

    public void setFields(List<FieldConfig> fields) {
        this.fields = fields == null ? new ArrayList<>() : fields;
    }

    public String getSuccessIndicator() {
        return successIndicator;
    }

    public void setSuccessIndicator(String successIndicator) {
        this.successIndicator = successIndicator;
    }
}
