package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret";

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(new JwtProperties(SECRET, 60_000));
    }

    @Test
    void generateToken_includesEmailAsSubject() {
        UserDetails user = User.withUsername("alice@example.com")
                .password("")
                .authorities("ROLE_USER")
                .build();

        String token = provider.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getEmailFromToken(token)).isEqualTo("alice@example.com");
    }

    @Test
    void validateToken_returnsFalseForGarbage() {
        assertThat(provider.validateToken("not-a-token")).isFalse();
        assertThat(provider.validateToken("")).isFalse();
        assertThat(provider.validateToken(null)).isFalse();
    }

    @Test
    void validateToken_returnsFalseForExpired() throws InterruptedException {
        JwtTokenProvider shortLived = new JwtTokenProvider(new JwtProperties(SECRET, 1));
        UserDetails user = User.withUsername("a@b.com").password("").authorities("ROLE_USER").build();
        String token = shortLived.generateToken(user);

        Thread.sleep(50);

        assertThat(shortLived.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_returnsFalseWhenSignedWithDifferentSecret() {
        UserDetails user = User.withUsername("a@b.com").password("").authorities("ROLE_USER").build();
        String token = provider.generateToken(user);

        JwtTokenProvider otherProvider = new JwtTokenProvider(
                new JwtProperties("OTHER-SECRET-OTHER-SECRET-OTHER-SECRET-OTHER", 60_000));

        assertThat(otherProvider.validateToken(token)).isFalse();
    }

    @Test
    void constructor_failsForShortSecret() {
        assertThatThrownBy(() -> new JwtTokenProvider(new JwtProperties("short", 60_000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }
}
