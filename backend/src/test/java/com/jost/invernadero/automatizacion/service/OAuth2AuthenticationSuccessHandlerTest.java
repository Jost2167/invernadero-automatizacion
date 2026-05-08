package com.jost.invernadero.automatizacion.service;

import com.jost.invernadero.automatizacion.config.JwtProperties;
import com.jost.invernadero.automatizacion.config.OAuth2Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OAuth2AuthenticationSuccessHandlerTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret";

    private JwtTokenProvider tokenProvider;
    private UserDetailsService userDetailsService;
    private OAuth2AuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(new JwtProperties(SECRET, 900_000));
        userDetailsService = mock(UserDetailsService.class);
        OAuth2Properties properties = new OAuth2Properties(
                false, "http://localhost:5173/auth/callback");
        handler = new OAuth2AuthenticationSuccessHandler(
                tokenProvider, userDetailsService, properties);
    }

    @Test
    void successfulAuth_redirectsWithValidJwt() throws Exception {
        Map<String, Object> attributes = Map.of(
                "sub", "g-12345",
                "email", "alice@example.com",
                "name", "Alice");
        OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "email");
        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                oAuth2User, oAuth2User.getAuthorities(), "google");

        UserDetails details = User.withUsername("alice@example.com")
                .password("")
                .authorities("ROLE_USER")
                .build();
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(details);

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(req, res, auth);

        assertThat(res.getStatus()).isEqualTo(302);
        String location = res.getRedirectedUrl();
        assertThat(location).startsWith("http://localhost:5173/auth/callback?token=");

        String token = location.substring(location.indexOf("token=") + "token=".length());
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getEmailFromToken(token)).isEqualTo("alice@example.com");
    }
}
