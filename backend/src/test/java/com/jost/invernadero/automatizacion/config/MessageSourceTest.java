package com.jost.invernadero.automatizacion.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MessageSourceTest {

    private static final Locale ES = Locale.forLanguageTag("es");
    private static final Locale EN = Locale.forLanguageTag("en");

    @Autowired
    private MessageSource messageSource;

    @Test
    void resolvesUserInactiveInSpanish() {
        assertThat(messageSource.getMessage("auth.errors.USER_INACTIVE", null, ES))
                .isEqualTo("La cuenta no se encuentra habilitada");
    }

    @Test
    void resolvesUserInactiveInEnglish() {
        assertThat(messageSource.getMessage("auth.errors.USER_INACTIVE", null, EN))
                .isEqualTo("The account is not enabled");
    }

    @Test
    void resolvesUnauthorizedInSpanish() {
        assertThat(messageSource.getMessage("error.unauthorized", null, ES))
                .isEqualTo("No autorizado");
    }

    @Test
    void resolvesUnauthorizedInEnglish() {
        assertThat(messageSource.getMessage("error.unauthorized", null, EN))
                .isEqualTo("Unauthorized");
    }

    @Test
    void unknownKeyReturnsKeyItselfWhenUseCodeAsDefault() {
        assertThat(messageSource.getMessage("non.existent.key", null, ES))
                .isEqualTo("non.existent.key");
    }
}
