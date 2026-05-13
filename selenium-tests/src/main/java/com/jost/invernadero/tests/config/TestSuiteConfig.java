package com.jost.invernadero.tests.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestSuiteConfig {

    private List<ModelTestData> models = new ArrayList<>();
    private Integer timeout;

    public TestSuiteConfig() {
    }

    public TestSuiteConfig(List<ModelTestData> models, Integer timeout) {
        setModels(models);
        this.timeout = timeout;
    }

    public List<ModelTestData> getModels() {
        return models;
    }

    public void setModels(List<ModelTestData> models) {
        this.models = models == null ? new ArrayList<>() : models;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
