package com.jost.invernadero.automatizacion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth2")
public record OAuth2Properties(boolean autoRegister, String redirectUri) {
}
